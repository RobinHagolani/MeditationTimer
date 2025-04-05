# 📝 Coding Standards

## General Principles

### 1. Code Organization
- One class/interface per file
- Related extensions in same file
- Maximum file length: 400 lines
- Maximum function length: 30 lines

### 2. Naming Conventions
```kotlin
// Classes: PascalCase
class MeditationTimer
class BreathingAnimation

// Functions & Variables: camelCase
fun startTimer()
val sessionDuration: Int

// Constants: UPPER_SNAKE_CASE
const val DEFAULT_DURATION = 300L
const val MAX_SESSION_LENGTH = 3600L

// Files: PascalCase matching class name
// MeditationTimer.kt
// BreathingAnimation.kt
```

## Kotlin-Specific Standards

### 1. Properties
```kotlin
// Prefer val over var
class MeditationSession {
    val id: UUID = UUID.randomUUID()  // Immutable
    var duration: Int = 0  // Mutable only if necessary
    
    // Custom getter
    val formattedDuration: String
        get() = "${duration / 60}:${duration % 60}"
    
    // Late-initialized properties
    lateinit var soundManager: SoundManager
}
```

### 2. Functions
```kotlin
// Single expression functions
fun formatTime(seconds: Int) = "${seconds / 60}:${seconds % 60}"

// Function with default parameters
fun startTimer(
    duration: Int = 300,
    withSound: Boolean = true
) {
    // Implementation
}

// Extension functions in separate files
// TimeFormatting.kt
fun Int.toFormattedTime(): String {
    return "${this / 60}:${this % 60}"
}
```

## Compose UI Standards

### 1. Composable Functions
```kotlin
// Naming: PascalCase, descriptive
@Composable
fun TimerDisplay(
    // Required parameters first
    duration: Int,
    // Optional parameters with defaults
    modifier: Modifier = Modifier,
    // State hoisting callbacks last
    onTimerComplete: () -> Unit
) {
    // Implementation
}

// Preview functions
@Preview(showBackground = true)
@Composable
fun TimerDisplayPreview() {
    TimerDisplay(
        duration = 300,
        onTimerComplete = {}
    )
}
```

### 2. State Management
```kotlin
// State hoisting
@Composable
fun StatefulTimer() {
    var isRunning by remember { mutableStateOf(false) }
    StatelessTimer(
        isRunning = isRunning,
        onStateChange = { isRunning = it }
    )
}

// State holder pattern
class TimerState(
    initialDuration: Int = 300
) {
    var duration by mutableStateOf(initialDuration)
    var isRunning by mutableStateOf(false)
    var progress by mutableStateOf(1f)
}
```

## Architecture Standards

### 1. MVVM Pattern
```kotlin
// ViewModel
class MeditationViewModel @Inject constructor(
    private val repository: MeditationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MeditationState())
    val state: StateFlow<MeditationState> = _state.asStateFlow()
    
    fun handleAction(action: MeditationAction) {
        when (action) {
            is Start -> startSession()
            is Pause -> pauseSession()
            is Reset -> resetSession()
        }
    }
}

// View
@Composable
fun MeditationScreen(
    viewModel: MeditationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    // UI implementation
}
```

### 2. Repository Pattern
```kotlin
// Repository interface
interface MeditationRepository {
    suspend fun saveSession(session: MeditationSession)
    fun getSessions(): Flow<List<MeditationSession>>
}

// Implementation
class MeditationRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val analytics: AnalyticsTracker
) : MeditationRepository {
    override suspend fun saveSession(session: MeditationSession) {
        sessionDao.insert(session)
        analytics.trackSession(session)
    }
}
```

## Testing Standards

### 1. Unit Tests
```kotlin
class TimerViewModelTest {
    @Test
    fun `timer counts down correctly`() = runTest {
        // Given
        val viewModel = TimerViewModel()
        
        // When
        viewModel.startTimer(60)
        advanceTimeBy(30_000)
        
        // Then
        assertEquals(30, viewModel.state.value.remaining)
    }
}
```

### 2. UI Tests
```kotlin
class TimerScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `displays correct time format`() {
        // Given
        composeTestRule.setContent {
            TimerDisplay(duration = 65)
        }
        
        // Then
        composeTestRule
            .onNodeWithText("1:05")
            .assertIsDisplayed()
    }
}
```

## Documentation Standards

### 1. KDoc Format
```kotlin
/**
 * Manages the meditation timer functionality.
 *
 * @property duration Initial duration in seconds
 * @property soundEnabled Whether to play sounds
 */
class MeditationTimer(
    private val duration: Int,
    private val soundEnabled: Boolean = true
) {
    /**
     * Starts the meditation timer.
     *
     * @param withVibration Whether to include vibration feedback
     * @throws IllegalStateException if timer is already running
     */
    fun start(withVibration: Boolean = false) {
        // Implementation
    }
}
```

### 2. Code Comments
```kotlin
// TODO: Implement haptic feedback
// FIXME: Timer drifts over long sessions
// NOTE: Must be called on main thread
```

## Git Standards

### 1. Commit Messages

feat: Add breathing animation
fix: Correct timer drift
docs: Update README
refactor: Simplify timer logic
test: Add unit tests for timer

### 2. Branch Naming
feature/breathing-animation
bugfix/timer-drift
docs/readme-update

## Resource Standards

### 1. Resource Naming

ic_timer_start.xml // Icons
bg_meditation.xml // Backgrounds
sound_bell_start.mp3 // Audio files


### 2. String Resources

```xml
<resources>
    <string name="timer_start">Start</string>
    <string name="timer_pause">Pause</string>
    <string name="timer_format">%1$d:%2$02d</string>
</resources>
```
```

