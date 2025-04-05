# ⏱ Meditation Timer Feature Specification

## Overview
The core feature of our app - a customizable meditation timer with visual feedback and future audio capabilities.

## Current Features (v0.1.0)
1. ✅ Basic Timer Functionality
    - Customizable duration (5, 10, 15, 20 minutes)
    - Custom duration input
    - Start/Pause/Resume/Reset controls
    - Visual countdown display

2. ✅ Visual Feedback
    - Circular progress indicator
    - Digital time display
    - Smooth animations
    - Clean, minimalist design

3. ✅ State Management
    - Persistent timer state
    - Automatic UI updates
    - State preservation across app restarts

## User Stories
1. ✅ As a user, I want to set a meditation duration so that I can meditate for a specific time
2. ✅ As a user, I want to see the remaining time so that I can track my progress
3. ✅ As a user, I want to pause/resume my session so that I can handle interruptions
4. 🚧 As a user, I want to hear gentle sounds at start/end so that I don't need to watch the screen

## Technical Implementation

### 1. UI Components
```kotlin
data class TimerState(
    val totalSeconds: Int = 900,    // 15 minutes default
    val remainingSeconds: Int = 900,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false
)

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Duration Selector (visible when timer not running)
        AnimatedVisibility(
            visible = !timerState.isRunning && !timerState.isPaused
        ) {
            DurationSelector(
                onDurationSelected = viewModel::setDuration
            )
        }
        
        // Centered Timer
        CircularTimer(
            progress = 1f - (timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()),
            time = formatTime(timerState.remainingSeconds)
        )
        
        // Control Buttons
        TimerControls(
            isRunning = timerState.isRunning,
            isPaused = timerState.isPaused,
            onStart = viewModel::startTimer,
            onPause = viewModel::pauseTimer,
            onReset = viewModel::resetTimer
        )
    }
}
```

### 2. ViewModel
```kotlin
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _totalSeconds = savedStateHandle.getStateFlow("total_seconds", 900)
    private val _remainingSeconds = savedStateHandle.getStateFlow("remaining_seconds", 900)
    private val _isRunning = savedStateHandle.getStateFlow("is_running", false)
    private val _isPaused = savedStateHandle.getStateFlow("is_paused", false)

    val timerState = combine(
        _totalSeconds,
        _remainingSeconds,
        _isRunning,
        _isPaused
    ) { total, remaining, running, paused ->
        TimerState(
            totalSeconds = total,
            remainingSeconds = remaining,
            isRunning = running,
            isPaused = paused
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimerState()
    )
}
```

## Visual Design

### Current Implementation
- Large circular progress indicator
- Digital time display in center
- Smooth progress animation
- Material 3 color scheme
- Responsive layout with fixed timer position
- Animated duration selector visibility

### Planned Improvements
- Custom color themes
- Dark mode support
- Breathing animation option
- Visual session completion indicator

## Future Enhancements (v0.2.0)
- [ ] Sound notifications
    - Start/end bells
    - Interval markers
    - Background ambient sounds
- [ ] Statistics tracking
    - Session history
    - Meditation streaks
    - Total meditation time
- [ ] Settings
    - Sound customization
    - Haptic feedback
    - Keep screen on option
- [ ] Advanced features
    - Interval timer
    - Guided meditation support
    - Widget support
    - Wear OS support

## Testing Strategy

### Unit Tests
- ViewModel state management
- Timer calculations
- State persistence
- Sound management (future)

### UI Tests
- Timer display format
- Control button functionality
- Duration selector
- Animation behavior