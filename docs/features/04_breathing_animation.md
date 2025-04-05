# 🫁 Breathing Animation Feature

## Overview
A visual guide for breathing exercises during meditation, featuring smooth animations and customizable patterns.

## Animation Patterns

### 1. Box Breathing (4-4-4-4)
```kotlin
data class BreathingPattern(
    val inhaleDuration: Int = 4000,  // 4 seconds
    val inhaleHoldDuration: Int = 4000,
    val exhaleDuration: Int = 4000,
    val exhaleHoldDuration: Int = 4000
)
```

### 2. Relaxing Breath (4-7-8)
```kotlin
val relaxingBreath = BreathingPattern(
    inhaleDuration = 4000,
    inhaleHoldDuration = 7000,
    exhaleDuration = 8000,
    exhaleHoldDuration = 0
)
```

## Technical Implementation

### 1. Core Animation Component
```kotlin
@Composable
fun BreathingAnimation(
    pattern: BreathingPattern,
    modifier: Modifier = Modifier
) {
    var currentPhase by remember { mutableStateOf(BreathingPhase.INHALE) }
    var size by remember { mutableStateOf(0.8f) }
    
    val animatedSize by animateFloatAsState(
        targetValue = size,
        animationSpec = tween(
            durationMillis = when(currentPhase) {
                BreathingPhase.INHALE -> pattern.inhaleDuration
                BreathingPhase.EXHALE -> pattern.exhaleDuration
                else -> pattern.inhaleHoldDuration
            },
            easing = LinearEasing
        )
    )
    
    LaunchedEffect(currentPhase) {
        when(currentPhase) {
            BreathingPhase.INHALE -> size = 1.2f
            BreathingPhase.EXHALE -> size = 0.8f
            else -> delay(pattern.inhaleHoldDuration.toLong())
        }
    }
    
    Box(
        modifier = modifier
            .size(200.dp)
            .scale(animatedSize)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = CircleShape
            )
    )
}
```

### 2. Phase Management
```kotlin
enum class BreathingPhase {
    INHALE,
    INHALE_HOLD,
    EXHALE,
    EXHALE_HOLD
}

@Composable
fun BreathingPhaseManager(
    pattern: BreathingPattern,
    onPhaseChange: (BreathingPhase) -> Unit
) {
    LaunchedEffect(Unit) {
        while(true) {
            onPhaseChange(BreathingPhase.INHALE)
            delay(pattern.inhaleDuration.toLong())
            
            onPhaseChange(BreathingPhase.INHALE_HOLD)
            delay(pattern.inhaleHoldDuration.toLong())
            
            onPhaseChange(BreathingPhase.EXHALE)
            delay(pattern.exhaleDuration.toLong())
            
            onPhaseChange(BreathingPhase.EXHALE_HOLD)
            delay(pattern.exhaleHoldDuration.toLong())
        }
    }
}
```

### 3. Visual Guidance
```kotlin
@Composable
fun BreathingGuidance(
    currentPhase: BreathingPhase,
    modifier: Modifier = Modifier
) {
    val text = when(currentPhase) {
        BreathingPhase.INHALE -> "Breathe In"
        BreathingPhase.EXHALE -> "Breathe Out"
        BreathingPhase.INHALE_HOLD -> "Hold"
        BreathingPhase.EXHALE_HOLD -> "Hold"
    }
    
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier.padding(16.dp)
    )
}
```

## User Interface

### Main Components
1. Breathing Circle
    - Expands and contracts
    - Smooth transitions
    - Semi-transparent background

2. Text Guidance
    - Clear instructions
    - Fade transitions
    - Optional visibility

3. Pattern Selection
    - Preset patterns
    - Custom duration input
    - Pattern preview

## Customization Options

### 1. Pattern Presets
```kotlin
object BreathingPatterns {
    val BOX_BREATHING = BreathingPattern(4000, 4000, 4000, 4000)
    val RELAXING_BREATH = BreathingPattern(4000, 7000, 8000, 0)
    val ENERGIZING_BREATH = BreathingPattern(2000, 0, 2000, 0)
    val CALMING_BREATH = BreathingPattern(6000, 0, 6000, 2000)
}
```

### 2. Visual Options
- Animation size
- Color scheme
- Opacity levels
- Text visibility

### 3. Sound Options
- Guidance sounds
- Background music
- Volume control
- Mute option

## Testing Strategy

### 1. Unit Tests
```kotlin
@Test
fun `pattern timing is correct`() {
    val pattern = BreathingPatterns.BOX_BREATHING
    assertEquals(4000, pattern.inhaleDuration)
    assertEquals(4000, pattern.inhaleHoldDuration)
    assertEquals(4000, pattern.exhaleDuration)
    assertEquals(4000, pattern.exhaleHoldDuration)
}
```

### 2. UI Tests
```kotlin
@Test
fun `animation scales correctly`() {
    composeTestRule.setContent {
        BreathingAnimation(BreathingPatterns.BOX_BREATHING)
    }
    
    // Test initial size
    // Test expansion
    // Test contraction
}
```

## Future Enhancements
- [ ] Custom pattern creation
- [ ] Pattern sharing
- [ ] Haptic feedback
- [ ] Voice guidance
- [ ] Animation variations