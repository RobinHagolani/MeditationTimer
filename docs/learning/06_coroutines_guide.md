# 🧵 Kotlin Coroutines Guide

## Overview
This guide explains Coroutines in the context of our meditation app, with practical examples from our codebase.

## Basic Concepts

### 1. Coroutine Scope
```kotlin
class TimerViewModel : ViewModel() {
    // Automatically cancelled when ViewModel is cleared
    private val viewModelScope = viewModelScope
    
    // Custom scope for timer operations
    private val timerScope = CoroutineScope(
        Dispatchers.Default + SupervisorJob()
    )
    
    fun startTimer(durationSeconds: Int) {
        viewModelScope.launch {
            try {
                runTimer(durationSeconds)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
}
```

### 2. Coroutine Context
```kotlin
// Different dispatchers for different tasks
launch(Dispatchers.Main) {
    // UI updates
    updateTimerDisplay()
}

launch(Dispatchers.IO) {
    // Database operations
    saveSession()
}

launch(Dispatchers.Default) {
    // CPU-intensive calculations
    calculateStatistics()
}
```

## Practical Examples

### 1. Timer Implementation
```kotlin
class MeditationTimer {
    private val timerScope = CoroutineScope(
        Dispatchers.Default + SupervisorJob()
    )
    
    suspend fun startCountdown(
        durationSeconds: Int,
        onTick: (Int) -> Unit,
        onComplete: () -> Unit
    ) = coroutineScope {
        for (remaining in durationSeconds downTo 0) {
            onTick(remaining)
            delay(1000)
        }
        onComplete()
    }
    
    fun start(durationSeconds: Int) {
        timerScope.launch {
            try {
                startCountdown(
                    durationSeconds = durationSeconds,
                    onTick = { updateDisplay(it) },
                    onComplete = { handleCompletion() }
                )
            } catch (e: CancellationException) {
                handleCancellation()
            }
        }
    }
}
```

### 2. Database Operations
```kotlin
class MeditationRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun saveSession(session: MeditationSession) {
        withContext(dispatcher) {
            sessionDao.insert(session)
        }
    }
    
    fun getSessions(): Flow<List<MeditationSession>> =
        sessionDao.getAllSessions()
            .flowOn(dispatcher)
            .catch { emit(emptyList()) }
}
```

### 3. Parallel Operations
```kotlin
suspend fun initializeSession() = coroutineScope {
    // Launch multiple operations in parallel
    val soundJob = async { initializeSound() }
    val databaseJob = async { prepareDatabase() }
    val analyticsJob = async { setupAnalytics() }
    
    // Wait for all to complete
    awaitAll(soundJob, databaseJob, analyticsJob)
}
```

## Error Handling

### 1. Exception Handling
```kotlin
class SafeTimer {
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("Timer", "Error in timer coroutine", exception)
        handleError(exception)
    }
    
    fun startWithSafety() {
        viewModelScope.launch(exceptionHandler) {
            runTimer()
        }
    }
}
```

### 2. Structured Concurrency
```kotlin
suspend fun runMeditationSession() = coroutineScope {
    try {
        val timerJob = launch { runTimer() }
        val soundJob = launch { playSound() }
        
        // If any job fails, all are cancelled
        joinAll(timerJob, soundJob)
    } catch (e: Exception) {
        handleSessionError(e)
    }
}
```

## StateFlow and SharedFlow

### 1. StateFlow for UI State
```kotlin
class TimerViewModel : ViewModel() {
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    fun updateTime(remaining: Int) {
        _timerState.update { current ->
            current.copy(
                timeRemaining = remaining,
                progress = remaining.toFloat() / current.duration
            )
        }
    }
}
```

### 2. SharedFlow for Events
```kotlin
class SessionManager {
    private val _events = MutableSharedFlow<SessionEvent>()
    val events: SharedFlow<SessionEvent> = _events.asSharedFlow()
    
    suspend fun emitEvent(event: SessionEvent) {
        _events.emit(event)
    }
}

// Usage in Compose
LaunchedEffect(Unit) {
    sessionManager.events.collect { event ->
        when (event) {
            is SessionEvent.Complete -> showCompletionDialog()
            is SessionEvent.Milestone -> showMilestoneMessage()
        }
    }
}
```

## Testing Coroutines

### 1. Unit Tests
```kotlin
@Test
fun `timer counts down correctly`() = runTest {
    val timer = MeditationTimer()
    var currentTime = 0
    
    timer.startCountdown(
        durationSeconds = 5,
        onTick = { currentTime = it },
        onComplete = { /* verify completion */ }
    )
    
    advanceTimeBy(2000) // Advance 2 seconds
    assertEquals(3, currentTime)
    
    advanceTimeBy(3000) // Advance to end
    assertEquals(0, currentTime)
}
```

### 2. Flow Testing
```kotlin
@Test
fun `state updates properly`() = runTest {
    val viewModel = TimerViewModel()
    val states = mutableListOf<TimerState>()
    
    val job = launch {
        viewModel.timerState.collect { states.add(it) }
    }
    
    viewModel.startTimer(5)
    advanceTimeBy(5000)
    
    assertEquals(6, states.size) // Initial + 5 updates
    assertTrue(states.last().isComplete)
    
    job.cancel()
}
```

## Best Practices

### 1. Scope Management
- Use appropriate scopes (viewModelScope, lifecycleScope)
- Cancel scopes when no longer needed
- Handle scope cancellation properly

### 2. Error Handling
- Use exception handlers
- Implement proper fallbacks
- Log errors appropriately

### 3. Testing
- Use TestCoroutineDispatcher
- Test cancellation scenarios
- Verify state updates