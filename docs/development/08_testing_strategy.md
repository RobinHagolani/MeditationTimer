# 🧪 Testing Strategy

## Overview

Our testing strategy follows the Testing Pyramid approach:
- Unit Tests (Base): 70% of our test suite
- Integration Tests (Middle): 20% of our test suite
- UI Tests (Top): 10% of our test suite

## Test Categories

### 1. Unit Tests

```kotlin
class MeditationTimerTest {
    @Test
    fun `timer calculation is accurate`() {
        val timer = MeditationTimer(duration = 300)
        timer.start()
        advanceTimeBy(150_000) // 2.5 minutes
        assertEquals(150, timer.remainingSeconds)
    }

    @Test
    fun `pause stops the timer`() {
        val timer = MeditationTimer(duration = 300)
        timer.start()
        advanceTimeBy(10_000)
        timer.pause()
        val remainingAtPause = timer.remainingSeconds
        advanceTimeBy(5_000)
        assertEquals(remainingAtPause, timer.remainingSeconds)
    }
}
```

#### Key Areas for Unit Testing
- ✓ ViewModels
- ✓ Use Cases
- ✓ Repositories
- ✓ Data Mappers
- ✓ Utility Functions
- ✓ Domain Logic

### 2. Integration Tests

```kotlin
@HiltAndroidTest
class MeditationRepositoryTest {
    @Inject
    lateinit var repository: MeditationRepository

    @Inject
    lateinit var database: MeditationDatabase

    @Test
    fun `completed session is saved and retrieved correctly`() = runTest {
        // Given
        val session = MeditationSession(
            duration = 300,
            completed = true,
            timestamp = Clock.System.now()
        )

        // When
        repository.saveSession(session)
        val savedSessions = repository.getSessions().first()

        // Then
        assertTrue(savedSessions.contains(session))
    }
}
```

#### Integration Test Focus
- Database Operations
- Repository Implementations
- API Services
- WorkManager Tasks
- Service Integration

### 3. UI Tests

```kotlin
@HiltAndroidTest
class MeditationScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `timer counts down and updates UI`() {
        // Given
        composeTestRule.setContent {
            MeditationScreen()
        }

        // When
        composeTestRule
            .onNodeWithText("Start")
            .performClick()

        // Then
        composeTestRule
            .onNodeWithText("5:00")
            .assertIsDisplayed()
    }

    @Test
    fun `pause button appears after starting timer`() {
        composeTestRule.setContent {
            MeditationScreen()
        }

        composeTestRule
            .onNodeWithText("Start")
            .performClick()

        composeTestRule
            .onNodeWithText("Pause")
            .assertIsDisplayed()
    }
}
```

#### UI Test Scenarios
- Navigation Flow
- Screen State Changes
- User Interactions
- Error States
- Animations
- Accessibility

## Test Coverage Goals

| Component          | Coverage Target |
|-------------------|----------------|
| Domain Logic      | 90%           |
| ViewModels        | 85%           |
| Repositories      | 80%           |
| UI Components     | 70%           |
| Utilities         | 95%           |

## Testing Tools

### Core Testing
- JUnit5: Unit testing framework
- Mockk: Mocking library
- Turbine: Flow testing
- Truth: Assertions library

### Android Testing
- Espresso: UI testing
- Compose Testing: Compose UI testing
- Hilt Testing: DI testing
- Coroutines Test: Async testing

## Best Practices

### 1. Test Structure
```kotlin
@Test
fun `test description in backticks`() {
    // Given (Setup)
    val viewModel = MeditationViewModel()

    // When (Action)
    viewModel.startTimer()

    // Then (Verification)
    assertEquals(EXPECTED_STATE, viewModel.state.value)
}
```

### 2. Naming Conventions
- Test classes: `*Test.kt`
- Test functions: `fun test description in backticks()`
- Test files: Same name as class under test

### 3. Test Data
```kotlin
object TestData {
    val DEFAULT_SESSION = MeditationSession(
        id = UUID.randomUUID(),
        duration = 300,
        type = SessionType.FOCUS
    )

    val TEST_USER = User(
        id = "test_user_1",
        preferences = UserPreferences(
            defaultDuration = 600,
            soundEnabled = true
        )
    )
}
```

## Continuous Integration

### GitHub Actions Workflow
```yaml
name: Android Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2

      - name: Unit Tests
        run: ./gradlew test

      - name: Android Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 29
          script: ./gradlew connectedCheck
```

## Performance Testing

### 1. Metrics to Monitor
- Timer Accuracy
- Animation Smoothness
- Database Operations
- Memory Usage
- Battery Impact

### 2. Benchmark Tests
```kotlin
@RunWith(AndroidJUnit4::class)
class MeditationBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun measureDatabaseAccess() {
        benchmarkRule.measureRepeated {
            runBlocking {
                repository.getSessions().first()
            }
        }
    }
}
```

## Test Maintenance

### Regular Tasks
- Review and update tests with new features
- Remove obsolete tests
- Update test data
- Monitor test coverage
- Review test performance
- Update testing dependencies

### Documentation
- Keep test documentation updated
- Document complex test scenarios
- Maintain testing guidelines
- Document known limitations