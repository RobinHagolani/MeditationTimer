# 📊 Analytics Implementation Guide

## Overview

Our analytics implementation uses Firebase Analytics and custom event tracking to:
- Monitor user engagement
- Track feature usage
- Measure app performance
- Analyze user behavior
- Guide product decisions

## Firebase Setup

### 1. Configuration

```kotlin
@HiltAndroidApp
class MeditationApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        Firebase.initialize(this)
        // Enable analytics collection
        Firebase.analytics.setAnalyticsCollectionEnabled(true)
    }
}
```

### 2. Custom Parameters

```kotlin
object AnalyticsParams {
    // Session Parameters
    const val PARAM_SESSION_DURATION = "session_duration"
    const val PARAM_SESSION_TYPE = "session_type"
    const val PARAM_COMPLETION_RATE = "completion_rate"
    
    // User Parameters
    const val PARAM_USER_LEVEL = "user_level"
    const val PARAM_SUBSCRIPTION_STATUS = "subscription_status"
    
    // Feature Parameters
    const val PARAM_FEATURE_NAME = "feature_name"
    const val PARAM_INTERACTION_TYPE = "interaction_type"
}
```

## Analytics Manager

### 1. Interface Definition

```kotlin
interface AnalyticsManager {
    fun logEvent(name: String, params: Bundle? = null)
    fun setUserProperty(name: String, value: String)
    fun setUserId(id: String)
    
    suspend fun trackSessionStart(sessionId: String, type: SessionType)
    suspend fun trackSessionComplete(sessionId: String, duration: Int)
    suspend fun trackFeatureUsage(feature: String, action: String)
}
```

### 2. Implementation

```kotlin
class FirebaseAnalyticsManager @Inject constructor(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) : AnalyticsManager {

    override fun logEvent(name: String, params: Bundle?) {
        analytics.logEvent(name, params)
    }

    override fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }

    override suspend fun trackSessionStart(sessionId: String, type: SessionType) {
        val params = Bundle().apply {
            putString("session_id", sessionId)
            putString("session_type", type.name)
            putLong("timestamp", System.currentTimeMillis())
        }
        logEvent(AnalyticsEvents.SESSION_START, params)
    }

    override suspend fun trackSessionComplete(sessionId: String, duration: Int) {
        val params = Bundle().apply {
            putString("session_id", sessionId)
            putInt("duration", duration)
            putLong("timestamp", System.currentTimeMillis())
        }
        logEvent(AnalyticsEvents.SESSION_COMPLETE, params)
    }
}
```

## Event Tracking

### 1. Session Events

```kotlin
object AnalyticsEvents {
    // Session Events
    const val SESSION_START = "session_start"
    const val SESSION_COMPLETE = "session_complete"
    const val SESSION_PAUSE = "session_pause"
    const val SESSION_RESUME = "session_resume"
    
    // Feature Events
    const val FEATURE_VIEW = "feature_view"
    const val FEATURE_INTERACTION = "feature_interaction"
    
    // User Events
    const val USER_SIGNUP = "user_signup"
    const val USER_LOGIN = "user_login"
    const val SUBSCRIPTION_CHANGED = "subscription_changed"
}

class SessionAnalytics @Inject constructor(
    private val analytics: AnalyticsManager
) {
    suspend fun trackSessionProgress(
        session: MeditationSession,
        progress: Float
    ) {
        analytics.logEvent(AnalyticsEvents.SESSION_PROGRESS) {
            param(AnalyticsParams.PARAM_SESSION_DURATION, session.duration)
            param(AnalyticsParams.PARAM_COMPLETION_RATE, progress)
        }
    }
}
```

### 2. Feature Usage

```kotlin
class FeatureAnalytics @Inject constructor(
    private val analytics: AnalyticsManager
) {
    suspend fun trackFeatureView(
        featureName: String,
        source: String? = null
    ) {
        analytics.logEvent(AnalyticsEvents.FEATURE_VIEW) {
            param(AnalyticsParams.PARAM_FEATURE_NAME, featureName)
            source?.let { param("source", it) }
        }
    }

    suspend fun trackFeatureInteraction(
        featureName: String,
        action: String,
        value: String? = null
    ) {
        analytics.logEvent(AnalyticsEvents.FEATURE_INTERACTION) {
            param(AnalyticsParams.PARAM_FEATURE_NAME, featureName)
            param(AnalyticsParams.PARAM_INTERACTION_TYPE, action)
            value?.let { param("value", it) }
        }
    }
}
```

## Performance Monitoring

### 1. Trace Implementation

```kotlin
class PerformanceTracker @Inject constructor(
    private val performance: FirebasePerformance
) {
    suspend fun trackOperation(
        name: String,
        block: suspend () -> Unit
    ) {
        val trace = performance.newTrace(name)
        trace.start()
        try {
            block()
        } finally {
            trace.stop()
        }
    }

    fun createHttpMetric(
        url: String,
        method: String
    ): HttpMetric {
        return performance.newHttpMetric(url, method)
    }
}
```

### 2. Custom Metrics

```kotlin
class SessionPerformanceTracker @Inject constructor(
    private val performance: PerformanceTracker
) {
    suspend fun trackSessionInitialization(
        sessionId: String,
        block: suspend () -> Unit
    ) {
        performance.trackOperation("session_initialization") {
            block()
        }
    }

    suspend fun trackDatabaseOperation(
        operationName: String,
        block: suspend () -> Unit
    ) {
        performance.trackOperation("database_$operationName") {
            block()
        }
    }
}
```

## User Properties

### 1. Property Management

```kotlin
class UserPropertyManager @Inject constructor(
    private val analytics: AnalyticsManager
) {
    fun updateUserLevel(level: Int) {
        analytics.setUserProperty(
            "user_level",
            level.toString()
        )
    }

    fun updateSubscriptionStatus(status: SubscriptionStatus) {
        analytics.setUserProperty(
            "subscription_status",
            status.name
        )
    }

    fun updatePreferredSessionType(type: SessionType) {
        analytics.setUserProperty(
            "preferred_session_type",
            type.name
        )
    }
}
```

### 2. User Identification

```kotlin
class UserIdentification @Inject constructor(
    private val analytics: AnalyticsManager,
    private val crashlytics: FirebaseCrashlytics
) {
    fun identifyUser(user: User) {
        analytics.setUserId(user.id)
        crashlytics.setUserId(user.id)
        
        analytics.setUserProperty("user_type", user.type.name)
        analytics.setUserProperty("registration_date", user.registrationDate)
    }

    fun clearUserIdentification() {
        analytics.setUserId("")
        crashlytics.setUserId("")
    }
}
```

## Analytics Testing

### 1. Test Implementation

```kotlin
class TestAnalyticsManager : AnalyticsManager {
    private val events = mutableListOf<AnalyticsEvent>()
    private val userProperties = mutableMapOf<String, String>()

    override fun logEvent(name: String, params: Bundle?) {
        events.add(AnalyticsEvent(name, params))
    }

    override fun setUserProperty(name: String, value: String) {
        userProperties[name] = value
    }

    fun getRecordedEvents(): List<AnalyticsEvent> = events.toList()
    fun getUserProperties(): Map<String, String> = userProperties.toMap()
}
```

### 2. Analytics Testing

```kotlin
class SessionAnalyticsTest {
    private val testAnalytics = TestAnalyticsManager()
    private lateinit var sessionAnalytics: SessionAnalytics

    @Before
    fun setup() {
        sessionAnalytics = SessionAnalytics(testAnalytics)
    }

    @Test
    fun `test session completion tracking`() = runTest {
        // Given
        val session = MeditationSession(duration = 300)

        // When
        sessionAnalytics.trackSessionComplete(session)

        // Then
        val events = testAnalytics.getRecordedEvents()
        assertTrue(events.any { 
            it.name == AnalyticsEvents.SESSION_COMPLETE &&
            it.params?.getInt("duration") == 300
        })
    }
}
```

## Best Practices

### 1. Event Naming

```kotlin
object AnalyticsConventions {
    // Event naming pattern: object_action
    const val BUTTON_CLICK = "button_click"
    const val SCREEN_VIEW = "screen_view"
    const val FEATURE_ENABLE = "feature_enable"
    
    // Parameter naming pattern: descriptive_name
    const val PARAM_BUTTON_NAME = "button_name"
    const val PARAM_SCREEN_NAME = "screen_name"
    const val PARAM_FEATURE_NAME = "feature_name"
}
```

### 2. Error Tracking

```kotlin
class AnalyticsErrorTracker @Inject constructor(
    private val analytics: AnalyticsManager,
    private val crashlytics: FirebaseCrashlytics
) {
    fun trackError(
        error: Throwable,
        screen: String? = null,
        action: String? = null
    ) {
        // Log to Firebase Analytics
        analytics.logEvent("error_occurred") {
            param("error_type", error.javaClass.simpleName)
            param("error_message", error.message ?: "Unknown error")
            screen?.let { param("screen", it) }
            action?.let { param("action", it) }
        }

        // Log to Crashlytics
        crashlytics.recordException(error)
    }
}
```
```
