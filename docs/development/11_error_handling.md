# 🐛 Error Handling & Logging Strategy

## Overview

Our error handling strategy follows these principles:
- Graceful degradation
- User-friendly error messages
- Comprehensive logging
- Error recovery where possible
- Crash reporting integration

## Error Types

### 1. Domain Exceptions

```kotlin
sealed class MeditationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    class TimerError(
        message: String = "Timer operation failed",
        cause: Throwable? = null
    ) : MeditationException(message, cause)
    
    class DatabaseError(
        message: String = "Database operation failed",
        cause: Throwable? = null
    ) : MeditationException(message, cause)
    
    class NetworkError(
        message: String = "Network operation failed",
        cause: Throwable? = null
    ) : MeditationException(message, cause)
    
    class ValidationError(
        message: String = "Validation failed",
        cause: Throwable? = null
    ) : MeditationException(message, cause)
    
    class StateError(
        message: String = "Invalid state transition",
        cause: Throwable? = null
    ) : MeditationException(message, cause)
}
```

### 2. UI State Error Handling

```kotlin
data class UiState<T>(
    val data: T? = null,
    val isLoading: Boolean = false,
    val error: ErrorState? = null
)

sealed class ErrorState {
    data class Message(val message: String) : ErrorState()
    data class Resource(@StringRes val resourceId: Int) : ErrorState()
    data class NetworkError(val code: Int, val message: String) : ErrorState()
}

sealed class UserMessage {
    data class Error(val message: String) : UserMessage()
    data class Success(val message: String) : UserMessage()
    data class Info(val message: String) : UserMessage()
}
```

## Error Handling Implementation

### 1. Repository Layer

```kotlin
class MeditationRepositoryImpl @Inject constructor(
    private val sessionDao: MeditationSessionDao,
    private val remoteDataSource: RemoteDataSource,
    private val logger: Logger
) : MeditationRepository {

    override suspend fun saveSession(session: MeditationSession): Result<Unit> {
        return try {
            sessionDao.insertSession(session.toEntity())
            remoteDataSource.syncSession(session)
            Result.success(Unit)
        } catch (e: SQLiteException) {
            logger.error("Database error while saving session", e)
            Result.failure(MeditationException.DatabaseError(cause = e))
        } catch (e: IOException) {
            logger.error("Network error while syncing session", e)
            Result.failure(MeditationException.NetworkError(cause = e))
        }
    }

    override fun getSessions(): Flow<Result<List<MeditationSession>>> = flow {
        try {
            sessionDao.getAllSessions()
                .map { sessions -> sessions.map { it.toDomain() } }
                .collect { emit(Result.success(it)) }
        } catch (e: Exception) {
            logger.error("Error fetching sessions", e)
            emit(Result.failure(MeditationException.DatabaseError(cause = e)))
        }
    }
}
```

### 2. ViewModel Layer

```kotlin
class MeditationViewModel @Inject constructor(
    private val meditationUseCase: MeditationUseCase,
    private val logger: Logger,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState<MeditationState>())
    val uiState: StateFlow<UiState<MeditationState>> = _uiState.asStateFlow()

    private val _userMessage = Channel<UserMessage>()
    val userMessage = _userMessage.receiveAsFlow()

    fun handleMeditation(action: MeditationAction) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val result = meditationUseCase.execute(action)
                _uiState.update { 
                    it.copy(
                        data = result,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: MeditationException) {
                logger.error("Meditation action failed", e)
                val errorState = errorHandler.handleError(e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = errorState
                    )
                }
                _userMessage.send(UserMessage.Error(errorState.message))
            }
        }
    }
}
```

### 3. UI Layer

```kotlin
@Composable
fun MeditationScreen(
    viewModel: MeditationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            when (message) {
                is UserMessage.Error -> {
                    snackbarHostState.showSnackbar(
                        message = message.message,
                        duration = SnackbarDuration.Long
                    )
                }
                is UserMessage.Success -> {
                    snackbarHostState.showSnackbar(
                        message = message.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null -> ErrorView(
                error = uiState.error,
                onRetry = { viewModel.retry() }
            )
            uiState.data != null -> MeditationContent(
                state = uiState.data,
                onAction = viewModel::handleMeditation
            )
        }
    }
}
```

## Logging Implementation

### 1. Logger Interface

```kotlin
interface Logger {
    fun debug(message: String, vararg args: Any?)
    fun info(message: String, vararg args: Any?)
    fun warning(message: String, vararg args: Any?)
    fun error(message: String, throwable: Throwable? = null, vararg args: Any?)
    
    fun startSession(sessionId: String)
    fun endSession()
    fun setUser(userId: String)
    fun addTag(key: String, value: String)
}
```

### 2. Timber Implementation

```kotlin
class TimberLogger @Inject constructor(
    private val crashlytics: FirebaseCrashlytics
) : Logger {

    override fun debug(message: String, vararg args: Any?) {
        Timber.d(message, *args)
    }

    override fun error(message: String, throwable: Throwable?, vararg args: Any?) {
        Timber.e(throwable, message, *args)
        throwable?.let { crashlytics.recordException(it) }
    }

    override fun startSession(sessionId: String) {
        crashlytics.setCustomKey("session_id", sessionId)
        info("Session started: $sessionId")
    }
}
```

### 3. Error Handler

```kotlin
class ErrorHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun handleError(error: Throwable): ErrorState {
        return when (error) {
            is MeditationException.NetworkError -> {
                ErrorState.Message(
                    context.getString(R.string.network_error)
                )
            }
            is MeditationException.DatabaseError -> {
                ErrorState.Message(
                    context.getString(R.string.database_error)
                )
            }
            is MeditationException.ValidationError -> {
                ErrorState.Message(error.message ?: "Validation failed")
            }
            else -> ErrorState.Message(
                context.getString(R.string.unknown_error)
            )
        }
    }
}
```

## Crash Reporting

### 1. Crashlytics Setup

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object LoggingModule {
    
    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
        return FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(true)
        }
    }

    @Provides
    @Singleton
    fun provideLogger(
        crashlytics: FirebaseCrashlytics
    ): Logger {
        return TimberLogger(crashlytics)
    }
}
```

### 2. Custom Keys

```kotlin
object CrashKeys {
    const val USER_ID = "user_id"
    const val SESSION_ID = "session_id"
    const val APP_VERSION = "app_version"
    const val DEVICE_TYPE = "device_type"
}

class CrashReporting @Inject constructor(
    private val crashlytics: FirebaseCrashlytics
) {
    fun setCustomKeys(
        userId: String,
        sessionId: String,
        appVersion: String
    ) {
        crashlytics.apply {
            setCustomKey(CrashKeys.USER_ID, userId)
            setCustomKey(CrashKeys.SESSION_ID, sessionId)
            setCustomKey(CrashKeys.APP_VERSION, appVersion)
            setCustomKey(CrashKeys.DEVICE_TYPE, Build.MODEL)
        }
    }
}
```

## Best Practices

### 1. Error Recovery

```kotlin
class RetryableOperation<T>(
    private val operation: suspend () -> T,
    private val maxAttempts: Int = 3,
    private val delayMillis: Long = 1000
) {
    suspend fun execute(): T {
        var lastException: Exception? = null
        repeat(maxAttempts) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxAttempts - 1) {
                    delay(delayMillis * (attempt + 1))
                }
            }
        }
        throw lastException ?: IllegalStateException("Operation failed")
    }
}
```

### 2. Error Prevention

```kotlin
class InputValidator {
    fun validateDuration(minutes: Int): ValidationResult {
        return when {
            minutes <= 0 -> ValidationResult.Error("Duration must be positive")
            minutes > 120 -> ValidationResult.Error("Duration cannot exceed 2 hours")
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : Validation