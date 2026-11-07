package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import com.example.data.SongEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VaaniAudioPlayer(private val context: Context) {

    private val synth = OfflineAudioSynthesizer()
    private var mediaPlayer: MediaPlayer? = null

    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong: StateFlow<SongEntity?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(180000L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    // Sleep Mode Timer
    private val _sleepTimerRemainingSec = MutableStateFlow(0)
    val sleepTimerRemainingSec: StateFlow<Int> = _sleepTimerRemainingSec.asStateFlow()

    private val _isSleepTimerActive = MutableStateFlow(false)
    val isSleepTimerActive: StateFlow<Boolean> = _isSleepTimerActive.asStateFlow()

    private var sleepCountDownTimer: CountDownTimer? = null
    private var progressJob: Job? = null
    private val playerScope = CoroutineScope(Dispatchers.Main)

    var onTrackFinished: (() -> Unit)? = null

    init {
        startProgressTracker()
    }

    fun playSong(song: SongEntity) {
        stopCurrent()
        _currentSong.value = song
        _durationMs.value = song.durationMs
        _currentPositionMs.value = 0L

        if (song.uri.startsWith("synth://") || song.uri.isEmpty()) {
            // Use native offline synthesizer
            synth.start(song.genre, song.bpm)
            _isPlaying.value = true
        } else {
            // Play from device storage URI or network stream via MediaPlayer
            val isNetworkStream = song.uri.startsWith("http://") || song.uri.startsWith("https://")
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, Uri.parse(song.uri))
                    setOnCompletionListener {
                        _isPlaying.value = false
                        onTrackFinished?.invoke()
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("VaaniPlayer", "MediaPlayer playback error: what=$what, extra=$extra. Falling back to synth.")
                        synth.start(song.genre, song.bpm)
                        _isPlaying.value = true
                        true
                    }
                    if (isNetworkStream) {
                        setOnPreparedListener { mp ->
                            if (mp.duration > 0) {
                                _durationMs.value = mp.duration.toLong()
                            }
                            mp.start()
                            _isPlaying.value = true
                        }
                        prepareAsync()
                    } else {
                        prepare()
                        if (duration > 0) {
                            _durationMs.value = duration.toLong()
                        }
                        start()
                        _isPlaying.value = true
                    }
                }
            } catch (e: Exception) {
                Log.e("VaaniPlayer", "Fallback to synth for error: ${e.message}")
                // Graceful fallback to offline synth if file is missing/unreadable
                synth.start(song.genre, song.bpm)
                _isPlaying.value = true
            }
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        _isPlaying.value = false
        synth.stop()
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            Log.e("VaaniPlayer", "Error pausing: ${e.message}")
        }
    }

    fun resume() {
        val song = _currentSong.value ?: return
        _isPlaying.value = true
        if (song.uri.startsWith("synth://") || song.uri.isEmpty()) {
            synth.start(song.genre, song.bpm)
        } else {
            try {
                mediaPlayer?.start() ?: playSong(song)
            } catch (e: Exception) {
                playSong(song)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        _currentPositionMs.value = positionMs.coerceIn(0L, _durationMs.value)
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
        } catch (e: Exception) {
            Log.e("VaaniPlayer", "Seek error: ${e.message}")
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _isRepeat.value = !_isRepeat.value
    }

    /**
     * Real-time user feedback: Faster tempo / Energy boost
     */
    fun boostEnergy(moreEnergy: Boolean) {
        val song = _currentSong.value ?: return
        val newBpm = if (moreEnergy) (song.bpm + 10).coerceAtMost(180) else (song.bpm - 10).coerceAtLeast(60)
        _currentSong.value = song.copy(bpm = newBpm)
        if (_isPlaying.value && (song.uri.startsWith("synth://") || song.uri.isEmpty())) {
            synth.start(song.genre, newBpm)
        }
    }

    // Sleep Mode Timer Implementation
    fun setSleepTimer(minutes: Int) {
        cancelSleepTimer()
        val totalSec = minutes * 60
        _sleepTimerRemainingSec.value = totalSec
        _isSleepTimerActive.value = true

        sleepCountDownTimer = object : CountDownTimer((totalSec * 1000).toLong(), 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingSec = (millisUntilFinished / 1000).toInt()
                _sleepTimerRemainingSec.value = remainingSec

                // Gentle fade-out in final 20 seconds
                if (remainingSec <= 20) {
                    val fadeVol = (remainingSec / 20.0f).coerceIn(0.05f, 1.0f)
                    synth.volumeMultiplier = fadeVol
                    try {
                        mediaPlayer?.setVolume(fadeVol, fadeVol)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }

            override fun onFinish() {
                _sleepTimerRemainingSec.value = 0
                _isSleepTimerActive.value = false
                synth.volumeMultiplier = 1.0f
                pause()
            }
        }.start()
    }

    fun cancelSleepTimer() {
        sleepCountDownTimer?.cancel()
        sleepCountDownTimer = null
        _sleepTimerRemainingSec.value = 0
        _isSleepTimerActive.value = false
        synth.volumeMultiplier = 1.0f
        try {
            mediaPlayer?.setVolume(1.0f, 1.0f)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun clearSession() {
        cancelSleepTimer()
        stopCurrent()
        _currentSong.value = null
        _isPlaying.value = false
        _currentPositionMs.value = 0L
    }

    private fun stopCurrent() {
        synth.stop()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = playerScope.launch {
            while (isActive) {
                if (_isPlaying.value) {
                    if (mediaPlayer != null) {
                        try {
                            _currentPositionMs.value = mediaPlayer?.currentPosition?.toLong() ?: 0L
                        } catch (e: Exception) {
                            // ignore
                        }
                    } else {
                        // Synthesizer progress increment
                        val nextPos = _currentPositionMs.value + 1000L
                        if (nextPos >= _durationMs.value) {
                            if (_isRepeat.value) {
                                _currentPositionMs.value = 0L
                            } else {
                                onTrackFinished?.invoke()
                            }
                        } else {
                            _currentPositionMs.value = nextPos
                        }
                    }
                }
                delay(1000L)
            }
        }
    }

    fun release() {
        cancelSleepTimer()
        progressJob?.cancel()
        stopCurrent()
    }
}
