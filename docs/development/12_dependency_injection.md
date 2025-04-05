# 💉 Dependency Injection Guide

## Overview

Our app uses Hilt for dependency injection, providing:
- Simplified DI setup
- Standardized components and scopes
- Android class integration
- Testing utilities

## Setup

### 1. Application Configuration

```kotlin
@HiltAndroidApp
class MeditationApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeTimber()
    }
}
```

### 2. Base Activity

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeditationTheme {
                MeditationNavigation()
            }
        }
    }
}
```

## Module Definitions

### 1. App Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("settings")
        }
    }

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
}
```

### 2. Database Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MeditationDatabase {
        return Room.databaseBuilder(
            context,
            MeditationDatabase::class.java,
            "meditation.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideMeditationDao(
        database: MeditationDatabase
    ): MeditationDao = database.meditationDao()

    @Provides
    @Singleton
    fun provideStatisticsDao(
        database: MeditationDatabase
    ): StatisticsDao = database.statisticsDao()
}
```

### 3. Repository Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMeditationRepository(
        meditationDao: MeditationDao,
        statisticsDao: StatisticsDao,
        dataStore: DataStore<Preferences>,
        @IoDispatcher dispatcher: CoroutineDispatcher
    ): MeditationRepository {
        return MeditationRepositoryImpl(
            meditationDao,
            statisticsDao,
            dataStore,
            dispatcher
        )
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        dataStore: DataStore<Preferences>,
        @IoDispatcher dispatcher: CoroutineDispatcher
    ): UserRepository {
        return UserRepositoryImpl(dataStore, dispatcher)
    }
}
```

### 4. Dispatcher Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainDispatcher
```

## Use Case Injection

### 1. Use Case Module

```kotlin
@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideStartMeditationUseCase(
        repository: MeditationRepository,
        @IoDispatcher dispatcher: CoroutineDispatcher
    ): StartMeditationUseCase {
        return StartMeditationUseCase(repository, dispatcher)
    }

    @Provides
    fun provideGetStatisticsUseCase(
        repository: MeditationRepository,
        @IoDispatcher dispatcher: CoroutineDispatcher
    ): GetStatisticsUseCase {
        return GetStatisticsUseCase(repository, dispatcher)
    }
}
```

### 2. ViewModel Injection

```kotlin
@HiltViewModel
class MeditationViewModel @Inject constructor(
    private val startMeditationUseCase: StartMeditationUseCase,
    private val getStatisticsUseCase: GetStatisticsUseCase,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Implementation
}
```

## Service Injection

### 1. Worker Injection

```kotlin
@HiltWorker
class MeditationReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationManager: NotificationManager,
    private val repository: MeditationRepository
) : CoroutineWorker(context, workerParams) {
    // Implementation
}
```

### 2. Service Module

```kotlin
@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
    }

    @Provides
    fun provideMeditationTimer(
        @ApplicationContext context: Context
    ): MeditationTimer {
        return MeditationTimerImpl(context)
    }
}
```

## Testing Support

### 1. Test Modules

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {
    
    @Provides
    @Singleton
    fun provideTestDatabase(
        @ApplicationContext context: Context
    ): MeditationDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            MeditationDatabase::class.java
        ).build()
    }

    @Provides
    @Singleton
    fun provideTestDispatcher(): CoroutineDispatcher {
        return StandardTestDispatcher()
    }
}
```

### 2. Test Rules

```kotlin
class HiltTestRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                launchInHiltContainer {
                    base.evaluate()
                }
            }
        }
    }
}
```

## Best Practices

### 1. Scope Usage

```kotlin
// Singleton scope for app-wide dependencies
@Singleton
class AnalyticsManager @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
)

// ViewModel scope for screen-specific dependencies
@ViewModelScoped
class MeditationStateHolder @Inject constructor()
```

### 2. Qualifier Usage

```kotlin
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthInterceptorOkHttpClient

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class RegularOkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @AuthInterceptorOkHttpClient
    @Provides
    fun provideAuthClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @RegularOkHttpClient
    @Provides
    fun provideRegularClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }
}
```

### 3. Interface Bindings

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {
    
    @Binds
    @Singleton
    abstract fun bindMeditationRepository(
        impl: MeditationRepositoryImpl
    ): MeditationRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}
```
```

Soll ich mit dem nächsten Dokument (ui_components.md) fortfahren?