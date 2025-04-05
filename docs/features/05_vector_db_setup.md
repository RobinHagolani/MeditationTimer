# 🔍 Vector Database Setup and RAG Integration

## Overview
This document describes the setup and usage of our vector database for:
- Storing meditation session embeddings
- Context-aware feature recommendations
- Pattern recognition in meditation habits

## Vector Database Selection

### Primary Choice: Milvus
```yaml
version: '3.5'
services:
  milvus:
    image: milvusdb/milvus:latest
    container_name: milvus_standalone
    environment:
      ETCD_ENDPOINTS: etcd:2379
      MINIO_ADDRESS: minio:9000
    volumes:
      - ${DOCKER_VOLUME_DIRECTORY:-.}/volumes/milvus:/var/lib/milvus
    ports:
      - "19530:19530"
    networks:
      - milvus_net
```

## Schema Design

### 1. Meditation Sessions
```python
from pymilvus import Collection, DataType

session_schema = {
    "fields": [
        {
            "name": "session_id",
            "dtype": DataType.VARCHAR,
            "is_primary": True
        },
        {
            "name": "embedding",
            "dtype": DataType.FLOAT_VECTOR,
            "dim": 384  # BERT embedding dimension
        },
        {
            "name": "duration",
            "dtype": DataType.INT64
        },
        {
            "name": "completion_rate",
            "dtype": DataType.FLOAT
        },
        {
            "name": "timestamp",
            "dtype": DataType.INT64
        }
    ]
}
```

## Kotlin Implementation

### 1. Data Models
```kotlin
data class VectorizedSession(
    val sessionId: String,
    val embedding: FloatArray,
    val metadata: SessionMetadata
) {
    data class SessionMetadata(
        val duration: Long,
        val completionRate: Float,
        val timestamp: Long,
        val tags: List<String>
    )
}
```

### 2. Vector Database Service
```kotlin
class VectorDBService @Inject constructor(
    private val milvusClient: MilvusClient,
    private val embedder: TextEmbedder
) {
    suspend fun storeSession(session: MeditationSession) {
        val embedding = embedder.generateEmbedding(
            session.toEmbeddingText()
        )
        
        milvusClient.insert(
            collectionName = "meditation_sessions",
            vectors = embedding,
            metadata = session.toMetadata()
        )
    }
    
    suspend fun findSimilarSessions(
        currentSession: MeditationSession,
        limit: Int = 5
    ): List<MeditationSession> {
        val queryEmbedding = embedder.generateEmbedding(
            currentSession.toEmbeddingText()
        )
        
        return milvusClient.search(
            collectionName = "meditation_sessions",
            queryVector = queryEmbedding,
            limit = limit
        ).map { it.toMeditationSession() }
    }
}
```

### 3. Embedding Generation
```kotlin
class TextEmbedder @Inject constructor(
    private val bertModel: BertModel
) {
    suspend fun generateEmbedding(text: String): FloatArray {
        return withContext(Dispatchers.Default) {
            bertModel.encode(text)
        }
    }
}

fun MeditationSession.toEmbeddingText(): String {
    return buildString {
        append("Meditation session for ${duration} minutes ")
        append("with ${breathingPattern.name} breathing pattern ")
        append("at ${timestamp.toLocalDateTime()} ")
        if (notes.isNotEmpty()) {
            append("Notes: $notes")
        }
    }
}
```

## RAG Integration

### 1. Context-Aware Recommendations
```kotlin
class MeditationRecommender @Inject constructor(
    private val vectorDB: VectorDBService
) {
    suspend fun recommendNextSession(
        userHistory: List<MeditationSession>
    ): MeditationRecommendation {
        val recentSessions = userHistory.takeLast(5)
        val patterns = findPatterns(recentSessions)
        
        return when {
            patterns.hasProgressiveIncrease() -> 
                recommendLongerSession(recentSessions)
            patterns.hasConsistentTiming() ->
                recommendSimilarSession(recentSessions)
            else -> recommendDefaultSession()
        }
    }
}
```

### 2. Pattern Analysis
```kotlin
class PatternAnalyzer {
    fun analyzePatterns(
        sessions: List<VectorizedSession>
    ): List<MeditationPattern> {
        return sessions
            .groupBy { it.metadata.timestamp.toHourOfDay() }
            .map { (hour, sessions) ->
                MeditationPattern(
                    timeOfDay = hour,
                    averageDuration = sessions.averageDuration(),
                    successRate = sessions.calculateSuccessRate()
                )
            }
    }
}
```

## Maintenance and Monitoring

### 1. Index Management
```kotlin
class IndexManager @Inject constructor(
    private val milvusClient: MilvusClient
) {
    suspend fun createIndexes() {
        milvusClient.createIndex(
            collectionName = "meditation_sessions",
            fieldName = "embedding",
            indexType = "IVF_FLAT",
            metricType = "L2"
        )
    }
    
    suspend fun rebuildIndexes() {
        milvusClient.dropIndex("meditation_sessions")
        createIndexes()
    }
}
```

### 2. Performance Monitoring
```kotlin
class VectorDBMonitor @Inject constructor(
    private val milvusClient: MilvusClient
) {
    suspend fun checkHealth(): HealthStatus {
        return try {
            val stats = milvusClient.getCollectionStats(
                "meditation_sessions"
            )
            HealthStatus(
                isHealthy = true,
                vectorCount = stats.vectorCount,
                lastUpdated = stats.lastUpdated
            )
        } catch (e: Exception) {
            HealthStatus(isHealthy = false, error = e)
        }
    }
}
```

## Backup and Recovery

### 1. Backup Strategy
```kotlin
class VectorDBBackup @Inject constructor(
    private val milvusClient: MilvusClient,
    private val backupStorage: BackupStorage
) {
    suspend fun createBackup() {
        val backup = milvusClient.createBackup(
            collections = listOf("meditation_sessions")
        )
        backupStorage.store(backup)
    }
    
    suspend fun restore(backupId: String) {
        val backup = backupStorage.retrieve(backupId)
        milvusClient.restore(backup)
    }
}
```

## Security Considerations
- Data encryption at rest
- Secure communication channels
- Access control and authentication
- Regular security audits