# 🎨 UI Components Guide

## Overview

Our app uses a component-based UI architecture with Jetpack Compose, featuring:
- Reusable components
- Consistent theming
- Accessibility support
- Animation patterns

## Theme Configuration

### 1. Color System

```kotlin
object MeditationColors {
    val Primary = Color(0xFF6200EE)
    val PrimaryVariant = Color(0xFF3700B3)
    val Secondary = Color(0xFF03DAC6)
    val Background = Color(0xFFF5F5F5)
    val Surface = Color.White
    val Error = Color(0xFFB00020)

    val TextPrimary = Color(0xFF000000)
    val TextSecondary = Color(0xFF666666)
    val TextHint = Color(0xFF999999)
}

@Composable
fun MeditationTheme(
    darkTheme: Boolean = isSystemInDarkMode(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColorPalette()
    } else {
        lightColorPalette()
    }

    MaterialTheme(
        colors = colors,
        typography = MeditationTypography,
        shapes = MeditationShapes,
        content = content
    )
}
```

### 2. Typography

```kotlin
val MeditationTypography = Typography(
    h1 = TextStyle(
        fontFamily = Montserrat,
        fontSize = 96.sp,
        letterSpacing = (-1.5).sp
    ),
    h2 = TextStyle(
        fontFamily = Montserrat,
        fontSize = 60.sp,
        letterSpacing = (-0.5).sp
    ),
    body1 = TextStyle(
        fontFamily = Roboto,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    ),
    button = TextStyle(
        fontFamily = Roboto,
        fontSize = 14.sp,
        letterSpacing = 1.25.sp,
        fontWeight = FontWeight.Medium
    )
)
```

## Core Components

### 1. Timer Display

```kotlin
@Composable
fun TimerDisplay(
    duration: Int,
    progress: Float,
    modifier: Modifier = Modifier,
    style: TimerStyle = TimerStyle.Circular
) {
    Box(
        modifier = modifier
            .size(200.dp)
            .padding(16.dp)
    ) {
        when (style) {
            TimerStyle.Circular -> CircularProgress(
                progress = progress,
                strokeWidth = 8.dp
            )
            TimerStyle.Linear -> LinearProgress(
                progress = progress
            )
        }

        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.h2,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

enum class TimerStyle {
    Circular,
    Linear
}
```

### 2. Action Button

```kotlin
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth(),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = MaterialTheme.colors.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            icon?.invoke()
            Spacer(Modifier.width(8.dp))
            Text(text)
        }
    }
}
```

### 3. Session Card

```kotlin
@Composable
fun SessionCard(
    session: MeditationSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.h6
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SessionMetric(
                    icon = Icons.Default.Timer,
                    value = formatDuration(session.duration)
                )
                SessionMetric(
                    icon = Icons.Default.DateRange,
                    value = formatDate(session.date)
                )
            }
        }
    }
}
```

## Animation Components

### 1. Breathing Animation

```kotlin
@Composable
fun BreathingCircle(
    phase: BreathingPhase,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = when (phase) {
            BreathingPhase.INHALE -> 1.5f
            BreathingPhase.HOLD -> 1.5f
            BreathingPhase.EXHALE -> 1.0f
        },
        animationSpec = tween(
            durationMillis = phase.durationMillis,
            easing = FastOutSlowInEasing
        )
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(100.dp)
            .background(
                color = MaterialTheme.colors.primary,
                shape = CircleShape
            )
    )
}
```

### 2. Progress Animation

```kotlin
@Composable
fun AnimatedCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        )
    )

    CircularProgressIndicator(
        progress = animatedProgress,
        modifier = modifier,
        strokeWidth = 8.dp,
        color = MaterialTheme.colors.primary
    )
}
```

## Layout Components

### 1. Screen Container

```kotlin
@Composable
fun ScreenContainer(
    title: String,
    onBackPressed: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = onBackPressed?.let {
                    {
                        IconButton(onClick = it) {
                            Icon(Icons.Default.ArrowBack, null)
                        }
                    }
                },
                actions = actions
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
}
```

### 2. Loading States

```kotlin
@Composable
fun LoadingScreen(
    message: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            message?.let {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}
```

## Custom Modifiers

### 1. Shimmer Effect

```kotlin
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition()
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        )
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = 0.6f),
                Color.LightGray.copy(alpha = 0.2f),
                Color.LightGray.copy(alpha = 0.6f),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    )
    .onGloballyPositioned {
        size = it.size
    }
}
```

### 2. Ripple Click

```kotlin
fun Modifier.rippleClick(
    onClick: () -> Unit,
    bounded: Boolean = true
) = composed {
    clickable(
        indication = rememberRipple(bounded = bounded),
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
}
```

## Accessibility

### 1. Semantic Properties

```kotlin
@Composable
fun AccessibleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = text
            role = Role.Button
        }
    ) {
        Text(text)
    }
}
```

### 2. Content Descriptions

```kotlin
@Composable
fun StatisticsCard(
    stats: Statistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.semantics {
            contentDescription = buildString {
                append("Statistics for ${stats.date}. ")
                append("Total sessions: ${stats.sessionCount}. ")
                append("Duration: ${stats.totalDuration} minutes.")
            }
        }
    ) {
        // Card content
    }
}
```

## Preview Support

### 1. Preview Providers

```kotlin
@Preview(
    name = "Light Theme",
    showBackground = true
)
@Preview(
    name = "Dark Theme",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
fun TimerDisplayPreview() {
    MeditationTheme {
        TimerDisplay(
            duration = 300,
            progress = 0.7f
        )
    }
}
```

### 2. Sample Data

```kotlin
object PreviewData {
    val sampleSession = MeditationSession(
        id = UUID.randomUUID(),
        duration = 300,
        type = SessionType.FOCUS,
        date = Clock.System.now()
    )

    val sampleStatistics = Statistics(
        sessionCount = 5,
        totalDuration = 1500,
        averageSessionLength = 300
    )
}
```
```

Soll ich mit dem nächsten Dokument (analytics_implementation.md) fortfahren?