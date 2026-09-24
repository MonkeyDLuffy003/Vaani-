package com.example.data

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SongRepository(
    private val songDao: SongDao,
    private val voiceLogDao: VoiceLogDao,
    private val context: Context
) {
    val allSongs: Flow<List<SongEntity>> = songDao.getAllSongs()
    val offlineSongs: Flow<List<SongEntity>> = songDao.getOfflineSongs()
    val recentVoiceLogs: Flow<List<VoiceLogEntity>> = voiceLogDao.getRecentLogs()

    suspend fun getSongsByGenre(genre: String): Flow<List<SongEntity>> = songDao.getSongsByGenre(genre)
    suspend fun getSongsByMood(mood: String): Flow<List<SongEntity>> = songDao.getSongsByMood(mood)

    suspend fun toggleFavorite(id: Long, currentVal: Boolean) = withContext(Dispatchers.IO) {
        songDao.setFavorite(id, !currentVal)
    }

    suspend fun toggleOffline(id: Long, currentVal: Boolean) = withContext(Dispatchers.IO) {
        songDao.setOffline(id, !currentVal)
    }

    suspend fun logVoiceAction(command: String, detectedMood: String, response: String, actionTaken: String) = withContext(Dispatchers.IO) {
        voiceLogDao.insertLog(
            VoiceLogEntity(
                command = command,
                detectedMood = detectedMood,
                response = response,
                actionTaken = actionTaken
            )
        )
    }

    suspend fun addCustomTrack(title: String, artist: String, uri: String, genre: String = "Storage") = withContext(Dispatchers.IO) {
        val song = SongEntity(
            title = title,
            artist = artist,
            genre = genre,
            durationMs = 210000L,
            uri = uri,
            isOffline = true,
            isFavorite = false,
            moodTag = "Focus",
            bpm = 120,
            waveformSeed = title.hashCode()
        )
        songDao.insertSong(song)
    }

    suspend fun initializeDefaultSongsIfEmpty() = withContext(Dispatchers.IO) {
        val count = songDao.getSongCount()
        if (count == 0) {
            val defaults = listOf(
                // Cooking Channel
                SongEntity(
                    title = "Sizzle & Chill Beats",
                    artist = "Chef's Cut",
                    genre = "Cooking",
                    durationMs = 184000L,
                    uri = "synth://cooking_1",
                    isOffline = true,
                    moodTag = "Cozy",
                    bpm = 92,
                    waveformSeed = 101
                ),
                SongEntity(
                    title = "Acoustic Kitchen Groove",
                    artist = "Olive & Thyme",
                    genre = "Cooking",
                    durationMs = 210000L,
                    uri = "synth://cooking_2",
                    isOffline = true,
                    moodTag = "Calm",
                    bpm = 104,
                    waveformSeed = 102
                ),
                SongEntity(
                    title = "Lo-Fi Recipe Rhythm",
                    artist = "Slow Simmer",
                    genre = "Cooking",
                    durationMs = 195000L,
                    uri = "synth://cooking_3",
                    isOffline = true,
                    moodTag = "Cozy",
                    bpm = 88,
                    waveformSeed = 103
                ),

                // Gym Channel
                SongEntity(
                    title = "High Voltage Beast Mode",
                    artist = "Iron Pulse",
                    genre = "Gym",
                    durationMs = 205000L,
                    uri = "synth://gym_1",
                    isOffline = true,
                    moodTag = "Energetic",
                    bpm = 145,
                    waveformSeed = 201
                ),
                SongEntity(
                    title = "Cardio Sprint 150",
                    artist = "Hyperdrive",
                    genre = "Gym",
                    durationMs = 192000L,
                    uri = "synth://gym_2",
                    isOffline = true,
                    moodTag = "Energetic",
                    bpm = 150,
                    waveformSeed = 202
                ),
                SongEntity(
                    title = "Rep Till Failure",
                    artist = "Bass Forge",
                    genre = "Gym",
                    durationMs = 218000L,
                    uri = "synth://gym_3",
                    isOffline = true,
                    moodTag = "Energetic",
                    bpm = 140,
                    waveformSeed = 203
                ),

                // Home Channel
                SongEntity(
                    title = "Sunday Balcony Breeze",
                    artist = "Linen Shades",
                    genre = "Home",
                    durationMs = 230000L,
                    uri = "synth://home_1",
                    isOffline = true,
                    moodTag = "Calm",
                    bpm = 85,
                    waveformSeed = 301
                ),
                SongEntity(
                    title = "Warm Hearth Acoustic",
                    artist = "Cedar Wood",
                    genre = "Home",
                    durationMs = 215000L,
                    uri = "synth://home_2",
                    isOffline = true,
                    moodTag = "Cozy",
                    bpm = 90,
                    waveformSeed = 302
                ),
                SongEntity(
                    title = "Living Room Vinyl",
                    artist = "Dusty Needle",
                    genre = "Home",
                    durationMs = 198000L,
                    uri = "synth://home_3",
                    isOffline = true,
                    moodTag = "Calm",
                    bpm = 78,
                    waveformSeed = 303
                ),

                // Relax Channel
                SongEntity(
                    title = "Deep Sleep 432Hz Soundscape",
                    artist = "Ether Wave",
                    genre = "Relax",
                    durationMs = 360000L,
                    uri = "synth://relax_1",
                    isOffline = true,
                    moodTag = "Calm",
                    bpm = 60,
                    waveformSeed = 401
                ),
                SongEntity(
                    title = "Midnight Rain Piano",
                    artist = "Solfeggio Dreams",
                    genre = "Relax",
                    durationMs = 280000L,
                    uri = "synth://relax_2",
                    isOffline = true,
                    moodTag = "Calm",
                    bpm = 64,
                    waveformSeed = 402
                ),
                SongEntity(
                    title = "Cosmic Breath Meditation",
                    artist = "Astral Drift",
                    genre = "Relax",
                    durationMs = 320000L,
                    uri = "synth://relax_3",
                    isOffline = true,
                    moodTag = "Calm",
                    bpm = 55,
                    waveformSeed = 403
                ),

                // Motivation Channel
                SongEntity(
                    title = "Limitless Horizon",
                    artist = "Apex Velocity",
                    genre = "Motivation",
                    durationMs = 212000L,
                    uri = "synth://motivation_1",
                    isOffline = true,
                    moodTag = "Uplifting",
                    bpm = 128,
                    waveformSeed = 501
                ),
                SongEntity(
                    title = "Unstoppable Momentum",
                    artist = "Valiant Sound",
                    genre = "Motivation",
                    durationMs = 224000L,
                    uri = "synth://motivation_2",
                    isOffline = true,
                    moodTag = "Uplifting",
                    bpm = 132,
                    waveformSeed = 502
                ),
                SongEntity(
                    title = "Morning Victory Anthem",
                    artist = "Ascent",
                    genre = "Motivation",
                    durationMs = 208000L,
                    uri = "synth://motivation_3",
                    isOffline = true,
                    moodTag = "Uplifting",
                    bpm = 126,
                    waveformSeed = 503
                )
            )
            songDao.insertSongs(defaults)
        }
    }

    /**
     * Scans device phone storage using MediaStore so songs already on the user's phone
     * are automatically found and added to the offline library.
     */
    suspend fun scanDeviceStorageSongs(): Int = withContext(Dispatchers.IO) {
        var addedCount = 0
        try {
            val contentResolver: ContentResolver = context.contentResolver
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            val cursor: Cursor? = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Storage Song"
                    val artist = it.getString(artistColumn) ?: "Phone Storage"
                    val duration = it.getLong(durationColumn)
                    val contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString()).toString()

                    val existing = songDao.getSongByUri(contentUri)
                    if (existing == null) {
                        val newSong = SongEntity(
                            title = title,
                            artist = artist,
                            genre = "Storage",
                            durationMs = if (duration > 0) duration else 180000L,
                            uri = contentUri,
                            isOffline = true,
                            isFavorite = false,
                            moodTag = "Focus",
                            bpm = 120,
                            waveformSeed = title.hashCode()
                        )
                        songDao.insertSong(newSong)
                        addedCount++
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Device storage scan note: ${e.message}")
        }
        addedCount
    }
}
