# 🏗️ App Architecture

## Overview

Our meditation app follows Clean Architecture principles combined with MVVM pattern, ensuring:
- Separation of concerns
- Testability
- Scalability
- Maintainability

## Architecture Layers
App
├── Presentation Layer (UI)
│ ├── Composables
│ └── ViewModels
├── Domain Layer
│ ├── Use Cases
│ ├── Models
│ └── Repository Interfaces
└── Data Layer
├── Repository Implementations
├── Local Data Sources
└── Remote Data Sources


## 1. Presentation Layer

### ViewModels

```kotlin
class MeditationViewModel @Inject constructor(
    private val startMeditationUseCase: StartMeditationUseCase,
    private val getMeditationStateUseCase: GetMeditationStateUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeditationUiState())
    val uiState: StateFlow<MeditationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getMeditationStateUseCase()
                .collect { state ->
                    _uiState.update { it.copy(
                        duration = state.duration,
                        isRunning = state.isRunning,
                        progress = state.progress
                    )}
                }
        }
    }

    fun onAction(action: MeditationAction) {
        when (action) {
            is Start -> startMeditation()
            is Pause -> pauseMeditation()
            is Reset -> resetMeditation()
        }
    }
}
```

### UI State

```kotlin
data class MeditationUiState(
    val duration: Int = 600, // 10 minutes default
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val isLoading: Boolean = false
)

sealed interface MeditationAction {
    object Start : MeditationAction
    object Pause : MeditationAction
    object Reset : MeditationAction
    data class SetDuration(val seconds: Int) : MeditationAction
}
```

### Composables

```kotlin
@Composable
fun MeditationScreen(
    viewModel: MeditationViewModel = hiltViewModel(),
    navigator: Navigator
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimerDisplay(
            duration = uiState.duration,
            progress = uiState.progress
        )
        
        ActionButtons(
            isRunning = uiState.isRunning,
            onStart = { viewModel.onAction(Start) },
            onPause = { viewModel.onAction(Pause) },
            onReset = { viewModel.onAction(Reset) }
        )
    }
}
```

## 2. Domain Layer

### Use Cases

```kotlin
class StartMeditationUseCase @Inject constructor(
    private val meditationRepository: MeditationRepository,
    private val timerManager: TimerManager,
    private val soundManager: SoundManager
) {
    suspend operator fun invoke(duration: Int) {
        try {
            timerManager.startTimer(duration)
            soundManager.playStartSound()
            
            meditationRepository.createSession(
                MeditationSession(
                    duration = duration,
                    startTime = Clock.System.now()
                )
            )
        } catch (e: Exception) {
            throw MeditationException("Failed to start meditation", e)
        }
    }
}
```

### Domain Models

```kotlin
data class MeditationSession(
    val id: UUID = UUID.randomUUID(),
    val duration: Int,
    val startTime: Instant,
    val type: SessionType = SessionType.FOCUS,
    val completionPercentage: Float = 0f
)

sealed class MeditationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    class TimerError(message: String) : MeditationException(message)
    class DatabaseError(message: String) : MeditationException(message)
}
```

### Repository Interfaces

```kotlin
interface MeditationRepository {
    suspend fun createSession(session: MeditationSession)
    suspend fun updateSession(session: MeditationSession)
    fun getActiveSessions(): Flow<List<MeditationSession>>
    suspend fun getSessionById(id: UUID): MeditationSession?
}
```

## 3. Data Layer

### Repository Implementations

```kotlin
class MeditationRepositoryImpl @Inject constructor(
    private val sessionDao: MeditationSessionDao,
    private val sessionMapper: SessionMapper
) : MeditationRepository {

    override suspend fun createSession(session: MeditationSession) {
        sessionDao.insertSession(sessionMapper.toEntity(session))
    }

    override fun getActiveSessions(): Flow<List<MeditationSession>> {
        return sessionDao.getActiveSessions()
            .map { entities -> 
                entities.map(sessionMapper::toDomain)
            }
    }
}
```

### Data Sources

```kotlin
@Dao
interface MeditationSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MeditationSessionEntity)

    @Query("""
        SELECT * FROM meditation_sessions 
        WHERE completion_percentage < 100
        ORDER BY start_time DESC
    """)
    fun getActiveSessions(): Flow<List<MeditationSessionEntity>>
}
```

### Mappers

```kotlin
class SessionMapper @Inject constructor() {
    fun toEntity(domain: MeditationSession): MeditationSessionEntity {
        return MeditationSessionEntity(
            id = domain.id,
            duration = domain.duration,
            startTime = domain.startTime,
            type = domain.type,
            completionPercentage = domain.completionPercentage
        )
    }

    fun toDomain(entity: MeditationSessionEntity): MeditationSession {
        return MeditationSession(
            id = entity.id,
            duration = entity.duration,
            startTime = entity.startTime,
            type = entity.type,
            completionPercentage = entity.completionPercentage
        )
    }
}
```

## Dependency Injection

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideMeditationRepository(
        sessionDao: MeditationSessionDao,
        sessionMapper: SessionMapper
    ): MeditationRepository {
        return MeditationRepositoryImpl(sessionDao, sessionMapper)
    }

    @Provides
    @Singleton
    fun provideTimerManager(): TimerManager {
        return TimerManagerImpl()
    }

    @Provides
    @Singleton
    fun provideSoundManager(
        @ApplicationContext context: Context
    ): SoundManager {
        return SoundManagerImpl(context)
    }
}
```

## Navigation

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Meditation : Screen("meditation")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Meditation.route) {
            MeditationScreen(navController)
        }
        composable(Screen.Statistics.route) {
            StatisticsScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
    }
}
```

## Error Handling

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

fun <T> Flow<T>.asResult(): Flow<Result<T>> = flow {
    try {
        emit(Result.Loading)
        collect { value ->
            emit(Result.Success(value))
        }
    } catch (e: Exception) {
        emit(Result.Error(e))
    }
}
```

## State Management

```kotlin
class StateManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val isDarkMode: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] ?: false
        }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = enabled
        }
    }
}
```