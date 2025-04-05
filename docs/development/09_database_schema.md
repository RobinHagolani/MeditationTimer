# 💾 Database Schema Documentation

## Overview

Our app uses Room as the primary database, implementing a robust schema for meditation sessions, user preferences, and statistics tracking.

## Tables

### 1. Meditation Sessions

```kotlin
@Entity(tableName = "meditation_sessions")
data class MeditationSessionEntity(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    
    @ColumnInfo(name = "start_time")
    val startTime: Instant,
    
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int,
    
    @ColumnInfo(name = "completion_percentage")
    val completionPercentage: Float,
    
    @ColumnInfo(name = "session_type")
    val sessionType: SessionType,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "mood_rating")
    val moodRating: Int? = null
)

enum class SessionType {
    FOCUS,
    RELAXATION,
    BREATHING,
    CUSTOM
}
```

#### Indices
```kotlin
@Entity(
    tableName = "meditation_sessions",
    indices = [
        Index(value = ["start_time"]),
        Index(value = ["session_type"])
    ]
)
```

### 2. User Preferences

```kotlin
@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val id: Int = 1, // Single row for preferences
    
    @ColumnInfo(name = "default_duration")
    val defaultDuration: Int = 600, // 10 minutes
    
    @ColumnInfo(name = "sound_enabled")
    val soundEnabled: Boolean = true,
    
    @ColumnInfo(name = "vibration_enabled")
    val vibrationEnabled: Boolean = true,
    
    @ColumnInfo(name = "dark_mode")
    val darkMode: Boolean = false,
    
    @ColumnInfo(name = "notification_time")
    val notificationTime: LocalTime? = null
)
```

### 3. Statistics

```kotlin
@Entity(tableName = "meditation_statistics")
data class StatisticsEntity(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    
    @ColumnInfo(name = "date")
    val date: LocalDate,
    
    @ColumnInfo(name = "total_minutes")
    val totalMinutes: Int,
    
    @ColumnInfo(name = "sessions_completed")
    val sessionsCompleted: Int,
    
    @ColumnInfo(name = "average_completion")
    val averageCompletion: Float,
    
    @ColumnInfo(name = "streak_days")
    val streakDays: Int
)
```

## Database Access Objects (DAOs)

### 1. Session DAO

```kotlin
@Dao
interface MeditationSessionDao {
    @Query("SELECT * FROM meditation_sessions ORDER BY start_time DESC")
    fun getAllSessions(): Flow<List<MeditationSessionEntity>>
    
    @Query("""
        SELECT * FROM meditation_sessions 
        WHERE start_time >= :startDate AND start_time < :endDate
        ORDER BY start_time DESC
    """)
    suspend fun getSessionsInRange(
        startDate: Instant,
        endDate: Instant
    ): List<MeditationSessionEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MeditationSessionEntity)
    
    @Delete
    suspend fun deleteSession(session: MeditationSessionEntity)
    
    @Query("""
        SELECT COUNT(*) FROM meditation_sessions
        WHERE start_time >= :startOfDay AND start_time < :endOfDay
    """)
    suspend fun getSessionCountForDay(
        startOfDay: Instant,
        endOfDay: Instant
    ): Int
}
```

### 2. Preferences DAO

```kotlin
@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences LIMIT 1")
    fun getUserPreferences(): Flow<UserPreferencesEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePreferences(preferences: UserPreferencesEntity)
    
    @Query("UPDATE user_preferences SET sound_enabled = :enabled")
    suspend fun updateSoundSetting(enabled: Boolean)
    
    @Query("UPDATE user_preferences SET default_duration = :duration")
    suspend fun updateDefaultDuration(duration: Int)
}
```

### 3. Statistics DAO

```kotlin
@Dao
interface StatisticsDao {
    @Query("""
        SELECT * FROM meditation_statistics
        WHERE date >= :startDate AND date <= :endDate
        ORDER BY date DESC
    """)
    fun getStatisticsInRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<StatisticsEntity>>
    
    @Query("""
        SELECT SUM(total_minutes) 
        FROM meditation_statistics
        WHERE date >= :startDate AND date <= :endDate
    """)
    suspend fun getTotalMinutesInRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Int
    
    @Query("SELECT MAX(streak_days) FROM meditation_statistics")
    suspend fun getLongestStreak(): Int
}
```

## Database Migrations

### Version 1 to 2
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            ALTER TABLE meditation_sessions 
            ADD COLUMN mood_rating INTEGER DEFAULT NULL
        """)
    }
}
```

### Version 2 to 3
```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create statistics table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS meditation_statistics (
                id TEXT PRIMARY KEY NOT NULL,
                date TEXT NOT NULL,
                total_minutes INTEGER NOT NULL,
                sessions_completed INTEGER NOT NULL,
                average_completion REAL NOT NULL,
                streak_days INTEGER NOT NULL
            )
        """)
    }
}
```

## Type Converters

```kotlin
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMillis(it) }
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }

    @TypeConverter
    fun fromSessionType(value: SessionType): String {
        return value.name
    }

    @TypeConverter
    fun toSessionType(value: String): SessionType {
        return SessionType.valueOf(value)
    }
}
```

## Database Setup

```kotlin
@Database(
    entities = [
        MeditationSessionEntity::class,
        UserPreferencesEntity::class,
        StatisticsEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MeditationDatabase : RoomDatabase() {
    abstract fun sessionDao(): MeditationSessionDao
    abstract fun preferencesDao(): UserPreferencesDao
    abstract fun statisticsDao(): StatisticsDao
    
    companion object {
        const val DATABASE_NAME = "meditation-db"
    }
}
```

## Dependency Injection

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
            MeditationDatabase.DATABASE_NAME
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .build()
    }
    
    @Provides
    fun provideSessionDao(db: MeditationDatabase): MeditationSessionDao {
        return db.sessionDao()
    }
    
    @Provides
    fun providePreferencesDao(db: MeditationDatabase): UserPreferencesDao {
        return db.preferencesDao()
    }
    
    @Provides
    fun provideStatisticsDao(db: MeditationDatabase): StatisticsDao {
        return db.statisticsDao()
    }
}
```
```

Soll ich mit dem nächsten Dokument fortfahren, nachdem Sie dieses erstellt haben?