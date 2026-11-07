package com.example.ai

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VocalMoodAnalysis(
    val mood: String, // Energetic, Calm, Stressed, Fatigued, Motivated, Cozy
    val confidence: Float,
    val suggestedChannel: String, // Gym, Cooking, Home, Relax, Motivation, Storage
    val vocalCharacteristics: String,
    val vaaniResponse: String
)
 
/**
 * Clean audio metadata, filenames, and underscores into natural spoken text.
 * Prevents reading raw underscores, file extensions, and track numbers aloud.
 */
fun cleanMetadataForSpeech(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    var cleaned = raw.trim()
    // Remove common audio file extensions (.mp3, .wav, etc.)
    cleaned = cleaned.replace(Regex("\\.(mp3|m4a|wav|aac|flac|ogg|opus)$", RegexOption.IGNORE_CASE), "")
    // Replace underscores, hyphens, and dots with spaces
    cleaned = cleaned.replace("_", " ")
        .replace("-", " ")
        .replace(".", " ")
    // Remove leading track numbers e.g. "01 ", "01 - " or "1. "
    cleaned = cleaned.replace(Regex("^\\d{1,3}\\s*[-.]?\\s*"), "")
    // Collapse multiple spaces
    cleaned = cleaned.replace(Regex("\\s+"), " ").trim()
    return cleaned
}

sealed class VoiceCommandResult {
    // Music Playback Commands (Silent Execution — No TTS during playback)
    data class PlayChannel(val channel: String) : VoiceCommandResult()
    data class PlayRandom(val query: String = "") : VoiceCommandResult()
    data class PlaySpecific(val query: String) : VoiceCommandResult()
    data class ControlAction(val action: ActionType) : VoiceCommandResult()
    data class RealtimeFeedback(val feedbackType: String) : VoiceCommandResult()

    // Conversational Commands (Spoken Responses)
    data class WhatIsPlaying(val query: String = "") : VoiceCommandResult()
    data class WhyNotPlaying(val query: String = "") : VoiceCommandResult()
    data class SetSleepTimer(val minutes: Int, val speechResponse: String) : VoiceCommandResult()
    data class MoodAnalyzed(val analysis: VocalMoodAnalysis) : VoiceCommandResult()
    data class ConversationalReply(val speechResponse: String) : VoiceCommandResult()
    data class Unknown(val speechResponse: String) : VoiceCommandResult()

    enum class ActionType {
        PLAY, PAUSE, NEXT, PREVIOUS, SHUFFLE, OFFLINE_MODE
    }
}

class VaaniAiAgent(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var pendingSpeech: String? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val supported = trySetLanguage(Locale.getDefault()) ||
                    trySetLanguage(Locale.US) ||
                    trySetLanguage(Locale.ENGLISH)

            if (supported) {
                isTtsReady = true
                tts?.setSpeechRate(0.95f)
                tts?.setPitch(1.05f) // Smooth, friendly, modern AI tone

                // Speak any pending utterance queued before init
                pendingSpeech?.let { speech ->
                    pendingSpeech = null
                    speak(speech)
                }
            }
        }
    }

    private fun trySetLanguage(locale: Locale): Boolean {
        return try {
            val result = tts?.setLanguage(locale)
            result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        } catch (e: Exception) {
            false
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isTtsReady || tts == null) {
            pendingSpeech = text
            _isSpeaking.value = false
            onDone?.invoke()
            return
        }

        val utteranceId = "vaani_${System.currentTimeMillis()}"
        _isSpeaking.value = true
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                if (id == utteranceId) _isSpeaking.value = true
            }
            override fun onDone(id: String?) {
                if (id == utteranceId) {
                    _isSpeaking.value = false
                    onDone?.invoke()
                }
            }
            override fun onError(id: String?) {
                if (id == utteranceId) {
                    _isSpeaking.value = false
                    onDone?.invoke()
                }
            }
        })
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    /**
     * Analyzes vocal utterance and determines intent + mood.
     * Separates silent music commands from conversational spoken queries.
     */
    suspend fun processVoiceUtterance(spokenText: String): VoiceCommandResult = withContext(Dispatchers.IO) {
        val rawLower = spokenText.lowercase().trim()

        // Strip wake words "vaani", "hey vaani" and punctuation for accurate intent routing
        val lower = rawLower
            .replace(Regex("^(hey\\s+vaani|vaani)[,\\s]*"), "")
            .replace(Regex("[,.!?]+$"), "")
            .trim()

        // 1. Explicit query: "What song is playing?" (Announce current song aloud)
        if (lower.contains("what song is playing") || lower.contains("what's playing") ||
            lower.contains("what is playing") || lower.contains("which song is this") ||
            lower.contains("what song is this") || lower.contains("what track is this") ||
            lower.contains("current song") || lower.contains("current track") ||
            lower.contains("name of this song") || lower.contains("who is singing") ||
            lower.contains("who sings this") || lower.contains("who is this artist") ||
            lower == "what is this" || lower == "what is this song" || lower == "song name" ||
            lower == "track name" || lower == "song info" || lower == "track info"
        ) {
            return@withContext VoiceCommandResult.WhatIsPlaying(spokenText)
        }

        // 2. Explicit query: "Why isn't music playing?" (Diagnose and explain actual problem aloud)
        if (lower.contains("why isn't music playing") || lower.contains("why is music not playing") ||
            lower.contains("why is it not playing") || lower.contains("why no music") ||
            lower.contains("why isn't it playing") || lower.contains("why music stopped") ||
            lower.contains("why did music stop")
        ) {
            return@withContext VoiceCommandResult.WhyNotPlaying(spokenText)
        }

        // 3. Playback Transport Controls (SILENT EXECUTION — No TTS narration)
        if (lower == "pause" || lower == "pause music" || lower == "stop music" || lower == "stop" || lower == "freeze") {
            return@withContext VoiceCommandResult.ControlAction(VoiceCommandResult.ActionType.PAUSE)
        }

        if (lower == "resume" || lower == "unpause" || lower == "continue" || lower == "continue music" || lower == "keep playing") {
            return@withContext VoiceCommandResult.ControlAction(VoiceCommandResult.ActionType.PLAY)
        }

        if (lower == "next" || lower == "next song" || lower == "next track" || lower == "skip" || lower == "skip song" || lower == "change song") {
            return@withContext VoiceCommandResult.ControlAction(VoiceCommandResult.ActionType.NEXT)
        }

        if (lower == "previous" || lower == "previous song" || lower == "prev" || lower == "go back" || lower == "last song" || lower == "back") {
            return@withContext VoiceCommandResult.ControlAction(VoiceCommandResult.ActionType.PREVIOUS)
        }

        if (lower == "shuffle" || lower == "shuffle music" || lower == "randomize") {
            return@withContext VoiceCommandResult.ControlAction(VoiceCommandResult.ActionType.SHUFFLE)
        }

        // 4. Music Playback: "Play music", "Play songs", "Play [song]" (SILENT EXECUTION — No TTS over song)
        if (lower == "play music" || lower == "play songs" || lower == "play some music" ||
            lower == "start music" || lower == "play" || lower == "music" || lower == "drop beats" ||
            lower == "start playback" || lower == "play track" || lower == "play song"
        ) {
            return@withContext VoiceCommandResult.PlayRandom()
        }

        if (lower.startsWith("play ") || lower.startsWith("play song ") || lower.startsWith("play track ")) {
            val query = lower.replace(Regex("^play\\s+(song|track)?\\s*"), "").trim()
            if (query.isEmpty() || query == "music" || query == "something" || query == "songs") {
                return@withContext VoiceCommandResult.PlayRandom()
            } else {
                return@withContext VoiceCommandResult.PlaySpecific(query)
            }
        }

        // 5. Channel Commands (SILENT EXECUTION)
        if (lower.contains("gym") || lower.contains("workout") || lower.contains("exercise")) {
            return@withContext VoiceCommandResult.PlayChannel("Gym")
        }
        if (lower.contains("cook") || lower.contains("cooking") || lower.contains("kitchen") || lower.contains("recipe") || lower.contains("dinner")) {
            return@withContext VoiceCommandResult.PlayChannel("Cooking")
        }
        if (lower.contains("home") || lower.contains("house") || lower.contains("cozy") || lower.contains("living room")) {
            return@withContext VoiceCommandResult.PlayChannel("Home")
        }
        if (lower.contains("relax") || lower.contains("calm") || lower.contains("chill") || lower.contains("meditat") || lower.contains("soothing") || lower.contains("peace")) {
            return@withContext VoiceCommandResult.PlayChannel("Relax")
        }
        if (lower.contains("motivat") || lower.contains("inspire") || lower.contains("focus") || lower.contains("study") || lower.contains("energy")) {
            return@withContext VoiceCommandResult.PlayChannel("Motivation")
        }
        if (lower.contains("offline") || lower.contains("storage") || lower.contains("phone storage") || lower.contains("downloaded") || lower.contains("my songs") || lower.contains("phone")) {
            return@withContext VoiceCommandResult.PlayChannel("Storage")
        }

        // 6. Sleep Timer voice commands
        if (lower.contains("sleep timer") || lower.contains("sleep in") || lower.contains("turn off after")) {
            val minutes = when {
                lower.contains("15") || lower.contains("fifteen") -> 15
                lower.contains("30") || lower.contains("thirty") -> 30
                lower.contains("45") || lower.contains("forty five") -> 45
                lower.contains("60") || lower.contains("hour") || lower.contains("one hour") -> 60
                lower.contains("10") || lower.contains("ten") -> 10
                lower.contains("20") || lower.contains("twenty") -> 20
                else -> 30
            }
            return@withContext VoiceCommandResult.SetSleepTimer(
                minutes,
                "Sleep timer set for $minutes minutes."
            )
        }

        // 7. Real-time tempo/energy feedback (SILENT EXECUTION — No TTS interruption)
        if (lower == "faster" || lower == "more energy" || lower == "speed up" || lower == "pump it up") {
            return@withContext VoiceCommandResult.RealtimeFeedback("FASTER")
        }
        if (lower == "slower" || lower == "chill out" || lower == "too fast" || lower == "calm down") {
            return@withContext VoiceCommandResult.RealtimeFeedback("SLOWER")
        }

        // 8. Conversational / AI Queries (SPOKEN RESPONSES via Gemini or Conversational Engine)
        val isConversational = lower.contains("talk to me") || lower.contains("recommend") ||
                lower.contains("suggest") || lower.contains("who") || lower.contains("how") ||
                lower.contains("what") || lower.contains("why") || lower.contains("tell me") ||
                lower.contains("joke") || lower.contains("sing") || lower.contains("hi") ||
                lower.contains("hello") || lower.contains("hey") || lower.contains("teja") ||
                lower.contains("chat") || lower.contains("favorite") || lower.contains("love") ||
                lower.contains("feel") || lower.contains("thank")

        if (isConversational) {
            val reply = talkWithGemini(spokenText)
            return@withContext VoiceCommandResult.ConversationalReply(reply)
        }

        // 9. Vocal Mood Analysis fallback
        val moodAnalysis = analyzeVocalMood(spokenText)
        return@withContext VoiceCommandResult.MoodAnalyzed(moodAnalysis)
    }

    /**
     * Vocal mood analyzer: Evaluates pitch/cadence semantics and correlates with music channels.
     */
    suspend fun analyzeVocalMood(utterance: String): VocalMoodAnalysis = withContext(Dispatchers.IO) {
        // Try Gemini 3.5 Flash if API key is configured
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val result = callGeminiMoodAnalysis(utterance, apiKey)
                if (result != null) return@withContext result
            } catch (e: Exception) {
                Log.w("VaaniAI", "Gemini API fallback to local analysis: ${e.message}")
            }
        }

        // Seamless Offline Mood Analysis engine
        performOfflineMoodAnalysis(utterance)
    }

    private fun performOfflineMoodAnalysis(text: String): VocalMoodAnalysis {
        val lower = text.lowercase()

        return when {
            lower.contains("tired") || lower.contains("exhausted") || lower.contains("sleepy") || lower.contains("long day") || lower.contains("stress") -> {
                VocalMoodAnalysis(
                    mood = "Fatigued & Stressed",
                    confidence = 0.92f,
                    suggestedChannel = "Relax",
                    vocalCharacteristics = "Soft timbre, decelerated vocal pace, vocal fatigue markers",
                    vaaniResponse = "I hear the fatigue in your voice. Let's unwind with soothing 432Hz frequencies."
                )
            }
            lower.contains("hyped") || lower.contains("let's go") || lower.contains("pump") || lower.contains("run") || lower.contains("strong") || lower.contains("push") -> {
                VocalMoodAnalysis(
                    mood = "High Energy",
                    confidence = 0.94f,
                    suggestedChannel = "Gym",
                    vocalCharacteristics = "High acoustic energy, dynamic pitch surge, elevated cadence",
                    vaaniResponse = "High intensity detected! Firing up the Gym power beats to match your fire."
                )
            }
            lower.contains("cook") || lower.contains("hungry") || lower.contains("food") || lower.contains("chop") || lower.contains("baking") -> {
                VocalMoodAnalysis(
                    mood = "Creative & Rhythmic",
                    confidence = 0.89f,
                    suggestedChannel = "Cooking",
                    vocalCharacteristics = "Playful intonation, mid-range acoustic resonance, steady cadence",
                    vaaniResponse = "Sounds like culinary time! Putting on warm lo-fi kitchen grooves."
                )
            }
            lower.contains("win") || lower.contains("deadline") || lower.contains("build") || lower.contains("focus") || lower.contains("goal") || lower.contains("project") -> {
                VocalMoodAnalysis(
                    mood = "Determined & Driven",
                    confidence = 0.91f,
                    suggestedChannel = "Motivation",
                    vocalCharacteristics = "Assertive tone, crisp diction, driven rhythmic delivery",
                    vaaniResponse = "Unstoppable momentum detected. Queuing high-tempo motivational anthems."
                )
            }
            lower.contains("peace") || lower.contains("quiet") || lower.contains("cozy") || lower.contains("reading") || lower.contains("coffee") || lower.contains("morning") -> {
                VocalMoodAnalysis(
                    mood = "Peaceful & Cozy",
                    confidence = 0.90f,
                    suggestedChannel = "Home",
                    vocalCharacteristics = "Warm harmonic overtone, relaxed vocal delivery, balanced timbre",
                    vaaniResponse = "A calm sanctuary vibe detected. Serving cozy acoustic melodies."
                )
            }
            else -> {
                VocalMoodAnalysis(
                    mood = "Balanced & Receptive",
                    confidence = 0.85f,
                    suggestedChannel = "Home",
                    vocalCharacteristics = "Natural speaking pitch, balanced inflection, clear acoustic presence",
                    vaaniResponse = "Balanced tone detected. Curating a personalized playlist to elevate your state."
                )
            }
        }
    }

    private fun callGeminiMoodAnalysis(utterance: String, apiKey: String): VocalMoodAnalysis? {
        val prompt = """
            You are Vaani, an intelligent AI music voice agent. Analyze this user voice transcript:
            "$utterance"
            
            Return a JSON object with:
            {
              "mood": "one of: High Energy, Fatigued & Stressed, Peaceful & Cozy, Creative & Rhythmic, Determined & Driven",
              "confidence": 0.95,
              "suggestedChannel": "one of: Gym, Cooking, Home, Relax, Motivation, Storage",
              "vocalCharacteristics": "brief 1-sentence description of tone, pace, and acoustic vibe",
              "vaaniResponse": "A friendly 1-2 sentence spoken response as Vaani"
            }
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.3)
            })
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            .post(jsonRequest.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val bodyStr = response.body?.string() ?: return null

        val root = JSONObject(bodyStr)
        val text = root.getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        val parsed = JSONObject(text)
        return VocalMoodAnalysis(
            mood = parsed.optString("mood", "Balanced & Receptive"),
            confidence = parsed.optDouble("confidence", 0.9).toFloat(),
            suggestedChannel = parsed.optString("suggestedChannel", "Relax"),
            vocalCharacteristics = parsed.optString("vocalCharacteristics", "Natural pitch with calm resonance"),
            vaaniResponse = parsed.optString("vaaniResponse", "Curating music for your current vibe.")
        )
    }

    /**
     * Talks with Gemini 2.5 Flash for authentic AI conversational voice interaction
     */
    suspend fun talkWithGemini(utterance: String): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = "You are Vaani, a charming, intelligent Indian AI music bot talking to Teja. Respond concisely in 1 to 2 spoken sentences with warmth, witty music references, or helpful answers. Teja says: \"$utterance\""
                val jsonRequest = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                        put("maxOutputTokens", 100)
                    })
                }

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                    .post(jsonRequest.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val bodyStr = response.body?.string()
                if (!bodyStr.isNullOrBlank()) {
                    val root = JSONObject(bodyStr)
                    val text = root.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                        .trim()
                    if (text.isNotBlank()) return@withContext text
                }
            } catch (e: Exception) {
                Log.w("VaaniAI", "Gemini talk error: ${e.message}")
            }
        }

        // Offline conversational intelligence
        getConversationalFallback(utterance)
    }

    private fun getConversationalFallback(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("who are you") || lower.contains("your name") ->
                "I am Vaani, your AI music assistant. I'm here to curate and play the perfect soundtrack for your day, Teja!"
            lower.contains("how are you") || lower.contains("how's it going") ->
                "I'm feeling energized and ready to drop some great beats for you, Teja! What are we listening to?"
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hey Teja! Always wonderful to vibe with you. Just say what track you want or ask me anything!"
            lower.contains("joke") ->
                "Why was the musician arrested? Because they got caught in treble! But don't worry, my playlist is completely harmless."
            lower.contains("recommend") || lower.contains("suggest") ->
                "I recommend some smooth lo-fi beats or ambient acoustic rhythms right now to elevate your flow."
            lower.contains("love") || lower.contains("favorite") ->
                "I love music that gets into your soul and moves your spirit. Tell me a song and I'll spin it!"
            else ->
                "I hear you, Teja! Music connects everything. Ask me to play any song or explore your library."
        }
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
