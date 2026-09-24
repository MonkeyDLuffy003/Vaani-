package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String,
    val genre: String, // Cooking, Gym, Home, Relax, Motivation, Storage
    val durationMs: Long,
    val uri: String,
    val isOffline: Boolean = true,
    val isFavorite: Boolean = false,
    val moodTag: String, // Energetic, Calm, Uplifting, Focus, Cozy, Stressed
    val bpm: Int = 120,
    val waveformSeed: Int = 42,
    val dateAdded: Long = System.currentTimeMillis()
)
