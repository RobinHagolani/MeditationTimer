# ⏱ Meditation Timer Feature Specification

## Overview
The core feature of our app - a customizable meditation timer with visual and audio feedback.

## User Stories
1. As a user, I want to set a meditation duration so that I can meditate for a specific time
2. As a user, I want to see the remaining time so that I can track my progress
3. As a user, I want to pause/resume my session so that I can handle interruptions
4. As a user, I want to hear gentle sounds at start/end so that I don't need to watch the screen

## Technical Implementation

### 1. UI Components
```kotlin
data class TimerState(
    val duration: Long,
    val remaining: Long,
    val isRunning: Boolean,
    val progress: Float
)

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimerCircle(
            progress = timerState.progress,
            remaining = timerState.remaining
        )
        TimerControls(
            isRunning = timerState.isRunning,
            onStart = viewModel::startTimer,
            onPause = viewModel::pauseTimer,
            onReset = viewModel::resetTimer
        )
        DurationPicker(
            duration = timerState.duration,
            onDurationChange = viewModel::setDuration
        )
    }
}
```

### 2. ViewModel
```kotlin
class TimerViewModel @Inject constructor(
    private val soundManager: SoundManager
) : ViewModel() {
    private var timer: CountDownTimer? = null
    
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    fun startTimer() {
        soundManager.playStartSound()
        timer = object : CountDownTimer(remaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateState(millisUntilFinished)
            }
            override fun onFinish() {
                soundManager.playEndSound()
                completeSession()
            }
        }.start()
    }
    
    private fun updateState(remaining: Long) {
        _timerState.update { current ->
            current.copy(
                remaining = remaining,
                progress = remaining.toFloat() / current.duration
            )
        }
    }
}
```

### 3. Sound Management
```kotlin
class SoundManager @Inject constructor(
    private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    
    fun playStartSound() = playSound(R.raw.meditation_start)
    fun playEndSound() = playSound(R.raw.meditation_end)
    
    private fun playSound(@RawRes soundRes: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, soundRes)
        mediaPlayer?.start()
    }
}
```

## Visual Design

### Timer Circle
- Large circular progress indicator
- Current time in center
- Smooth progress animation
- Calming color scheme

### Controls
- Play/Pause button
- Reset button
- Duration picker
- Settings button (future)

## Sound Design
- Start sound: Gentle bell
- End sound: Three gentle bells
- Optional interval bells
- Background ambient sounds (future)

## Future Enhancements
- [ ] Custom sound selection
- [ ] Interval bells
- [ ] Background sounds
- [ ] Haptic feedback
- [ ] Widget support
- [ ] Wear OS support

## Testing Strategy

### Unit Tests
```kotlin
@Test
fun `timer counts down correctly`() {
    viewModel.setDuration(60_000) // 1 minute
    viewModel.startTimer()
    
    advanceTimeBy(30_000) // 30 seconds
    
    assertEquals(30_000, viewModel.timerState.value.remaining)
    assertEquals(0.5f, viewModel.timerState.value.progress)
}
```

### UI Tests
```kotlin
@Test
fun `timer displays correct format`() {
    composeTestRule.setContent {
        TimerDisplay(duration = 65_000) // 1:05
    }
    
    composeTestRule
        .onNodeWithText("1:05")
        .assertIsDisplayed()
}
```
```

