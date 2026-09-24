package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "voice_logs")
data class VoiceLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val command: String,
    val detectedMood: String,
    val response: String,
    val actionTaken: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface VoiceLogDao {
    @Query("SELECT * FROM voice_logs ORDER BY timestamp DESC LIMIT 20")
    fun getRecentLogs(): Flow<List<VoiceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: VoiceLogEntity)
}
