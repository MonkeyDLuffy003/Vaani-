package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * High-fidelity offline audio synthesizer for conceptual music channels.
 * Produces real, soothing, or high-energy audio through native AudioTrack PCM streaming
 * without needing any internet connection or external media files.
 */
class OfflineAudioSynthesizer {

    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    var isSynthesizing = false
        private set

    @Volatile
    var volumeMultiplier = 1.0f

    fun start(channelType: String, bpm: Int = 120) {
        stop()

        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(sampleRate / 4)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
        isSynthesizing = true

        synthJob = scope.launch {
            val buffer = ShortArray(1024)
            var sampleIndex = 0L

            // Musical chords / scale notes per channel
            val baseFreqs = when (channelType.lowercase()) {
                "gym" -> doubleArrayOf(55.0, 110.0, 164.81, 220.0, 329.63) // E minor heavy bass
                "cooking" -> doubleArrayOf(261.63, 329.63, 392.0, 493.88, 523.25) // Cmaj7 smooth lo-fi
                "home" -> doubleArrayOf(196.0, 246.94, 293.66, 392.0, 440.0) // G major cozy acoustic
                "relax" -> doubleArrayOf(108.0, 216.0, 432.0, 540.0) // 432Hz healing resonance
                "motivation" -> doubleArrayOf(293.66, 369.99, 440.0, 587.33) // D major triumphant
                else -> doubleArrayOf(220.0, 277.18, 329.63, 440.0)
            }

            val beatPeriodSamples = (sampleRate * 60) / bpm.coerceIn(50, 180)

            while (isActive && isSynthesizing) {
                for (i in buffer.indices) {
                    val t = (sampleIndex + i).toDouble() / sampleRate
                    val beatPhase = ((sampleIndex + i) % beatPeriodSamples).toDouble() / beatPeriodSamples

                    // Select chord note based on bar
                    val bar = ((sampleIndex + i) / (beatPeriodSamples * 2)) % baseFreqs.size
                    val freq = baseFreqs[bar.toInt()]

                    var sample = 0.0

                    when (channelType.lowercase()) {
                        "gym" -> {
                            // Punchy kick envelope + driving saw-like synth
                            val kickEnv = (1.0 - beatPhase).coerceAtLeast(0.0)
                            val kick = sin(2.0 * PI * (55.0 + 80.0 * kickEnv) * t) * kickEnv
                            val bass = sin(2.0 * PI * freq * t) * 0.4 + sin(2.0 * PI * (freq * 2) * t) * 0.2
                            sample = kick * 0.6 + bass * 0.4
                        }
                        "cooking" -> {
                            // Warm rhodes-like sine with gentle vibrato and sub-bass
                            val vibrato = 1.0 + 0.005 * sin(2.0 * PI * 4.5 * t)
                            val tone1 = sin(2.0 * PI * freq * vibrato * t) * 0.4
                            val tone2 = sin(2.0 * PI * (freq * 1.5) * vibrato * t) * 0.2
                            val sub = sin(2.0 * PI * (freq * 0.5) * t) * 0.25
                            sample = tone1 + tone2 + sub
                        }
                        "home" -> {
                            // Gentle plucking envelope with mellow warm harmonics
                            val pluckEnv = (1.0 - (beatPhase * 0.75)).coerceIn(0.0, 1.0)
                            val acoustic = sin(2.0 * PI * freq * t) * 0.5 + sin(2.0 * PI * (freq * 2.0) * t) * 0.2
                            sample = acoustic * pluckEnv
                        }
                        "relax" -> {
                            // Pure 432Hz harmonic drone with slow, meditative breathing envelope (0.1 Hz)
                            val breathe = 0.5 + 0.5 * sin(2.0 * PI * 0.12 * t)
                            val wave1 = sin(2.0 * PI * 432.0 * t) * 0.35
                            val wave2 = sin(2.0 * PI * 216.0 * t) * 0.25
                            val sub = sin(2.0 * PI * 108.0 * t) * 0.2
                            sample = (wave1 + wave2 + sub) * (0.6 + 0.4 * breathe)
                        }
                        "motivation" -> {
                            // Bright uplifting arpeggio & pulsing chords
                            val arpStep = (((sampleIndex + i) / (beatPeriodSamples / 4)) % 4).toInt()
                            val arpFreq = freq * when (arpStep) {
                                0 -> 1.0
                                1 -> 1.25
                                2 -> 1.5
                                else -> 2.0
                            }
                            val lead = sin(2.0 * PI * arpFreq * t) * 0.45
                            val pad = sin(2.0 * PI * freq * t) * 0.25
                            sample = lead + pad
                        }
                        else -> {
                            sample = sin(2.0 * PI * freq * t) * 0.4
                        }
                    }

                    // Apply master volume multiplier and clip safely to 16-bit PCM
                    val finalSample = (sample * volumeMultiplier.coerceIn(0f, 1f) * 16000.0).coerceIn(-32767.0, 32767.0)
                    buffer[i] = finalSample.toInt().toShort()
                }

                sampleIndex += buffer.size
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stop() {
        isSynthesizing = false
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioSynth", "Error stopping track: ${e.message}")
        }
        audioTrack = null
    }
}
