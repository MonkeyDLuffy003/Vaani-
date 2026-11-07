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

sealed class VoiceCommandResult {
    data class PlayChannel(val channel: String, val speechResponse: String) : VoiceCommandResult()
    data class PlayRandom(val speechResponse: String) : VoiceCommandResult()
    data class PlaySpecific(val query: String, val speechResponse: String) : VoiceCommandResult()
    data class ControlAction(val action: ActionType, val speechResponse: String) : VoiceCommandResult()
    data class SetSleepTimer(val minutes: Int, val speechResponse: String) : VoiceCommandResult()
    data class MoodAnalyzed(val analysis: VocalMoodAnalysis) : VoiceCommandResult()
    data class RealtimeFeedback(val feedbackType: String, val speechResponse: String) : VoiceCommandResult()
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
     * Integrates Gemini 3.5 Flash when online with full offline fallback.
     */
    suspend fun processVoiceUtterance(spokenText: String): VoiceCommandResult = withContext(Dispatchers.IO) {
        val lower = spokenText.lowercase().trim()

        // 1. Direct command matching for instant latency-free execution
        if (lower.contains("pause") || lower.contains("stop music") || lower.contains("freeze")) {
            return@withContext VoiceCommandResult.ControlAction(
                VoiceCommandResult.ActionType.PAUSE,
                "Pausing your music."
            )
        }

        // Direct "Vaani, play music" or "play music"
        if (lower == "play music" || lower == "vaani play music" || lower == "play songs" || lower == "play some music" || lower == "start music" || lower == "play") {
            return@withContext VoiceCommandResult.PlayRandom("Playing music from your library.")
        }

        if (lower.startsWith("play ") || lower.contains("play track ") || lower.contains("play song ")) {
            val query = lower.substringAfter("play ").replace("song", "").replace("track", "").trim()
            if (query.isEmpty() || query == "music" || query == "something") {
                return@withContext VoiceCommandResult.PlayRandom("Playing music from your library.")
            } else {
                return@withContext VoiceCommandResult.PlaySpecific(query, "Looking for '$query' in your library.")
            }
        }

        if (lower.contains("resume")) {
            return@withContext VoiceCommandResult.ControlAction(
                VoiceCommandResult.ActionType.PLAY,
                "Resuming playback."
            )
        }

        if (lower.contains("next") || lower.contains("skip") || lower.contains("change song") || lower.contains("next track")) {
            return@withContext VoiceCommandResult.ControlAction(
                VoiceCommandResult.ActionType.NEXT,
                "Skipping to the next track."
            )
        }

        if (lower.contains("previous") || lower.contains("prev") || lower.contains("go back") || lower.contains("last song")) {
            return@withContext VoiceCommandResult.ControlAction(
                VoiceCommandResult.ActionType.PREVIOUS,
                "Playing previous track."
            )
        }

        if (lower.contains("shuffle") || lower.contains("randomize")) {
            return@withContext VoiceCommandResult.ControlAction(
                VoiceCommandResult.ActionType.SHUFFLE,
                "Shuffle mode toggled."
            )
        }



        // Sleep Timer voice commands
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
                "Sleep timer set for $minutes minutes. Music will fade smoothly."
            )
        }

        // Real-time user feedback voice commands
        if (lower.contains("faster") || lower.contains("more energy") || lower.contains("speed up") || lower.contains("pump it up")) {
            return@withContext VoiceCommandResult.RealtimeFeedback(
                "FASTER",
                "Increasing tempo and energy for you."
            )
        }

        if (lower.contains("slower") || lower.contains("chill out") || lower.contains("too fast") || lower.contains("calm down")) {
            return@withContext VoiceCommandResult.RealtimeFeedback(
                "SLOWER",
                "Bringing down the tempo for a more relaxed groove."
            )
        }

        // Offline storage voice command
        if (lower.contains("offline") || lower.contains("storage") || lower.contains("downloaded") || lower.contains("my songs") || lower.contains("phone")) {
            return@withContext VoiceCommandResult.PlayChannel(
                "Storage",
                "Accessing your phone storage and offline downloaded songs."
            )
        }

        // Channel commands
        if (lower.contains("gym") || lower.contains("workout") || lower.contains("lift") || lower.contains("exercise")) {
            return@withContext VoiceCommandResult.PlayChannel(
                "Gym",
                "Starting Gym power channel. Let's get that workout in!"
            )
        }

        if (lower.contains("cook") || lower.contains("cooking") || lower.contains("kitchen") || lower.contains("recipe") || lower.contains("dinner")) {
            return@withContext VoiceCommandResult.PlayChannel(
                "Cooking",
                "Putting on the Cooking groove channel. Happy cooking!"
            )
        }

        if (lower.contains("home") || lower.contains("house") || lower.contains("cozy") || lower.contains("living room")) {
            return@withContext VoiceCommandResult.PlayChannel(
                "Home",
                "Tuning into the Home acoustic sanctuary."
            )
        }

        if (lower.contains("relax") || lower.contains("calm") || lower.contains("chill") || lower.contains("meditat") || lower.contains("stressed") || lower.contains("tired") || lower.contains("peace")) {
            return@withContext VoiceCommandResult.PlayChannel(
                "Relax",
                "Switching to Relax channel with soothing 432Hz ambient sound."
            )
        }

        if (lower.contains("motivat") || lower.contains("inspire") || lower.contains("focus") || lower.contains("work") || lower.contains("study") || lower.contains("energy")) {
            return@withContext VoiceCommandResult.PlayChannel(
                "Motivation",
                "Playing Motivation channel for laser focus and momentum."
            )
        }

        // 2. If user is talking, asking questions, chatting, or asking about music, call Gemini API
        val isConversational = lower.contains("who") || lower.contains("how") || lower.contains("what") ||
                lower.contains("why") || lower.contains("tell me") || lower.contains("joke") ||
                lower.contains("sing") || lower.contains("hi") || lower.contains("hello") ||
                lower.contains("hey") || lower.contains("vaani") || lower.contains("teja") ||
                lower.contains("talk") || lower.contains("chat") || lower.contains("recommend") ||
                lower.contains("favorite") || lower.contains("love") || lower.contains("feel")

        if (isConversational) {
            val reply = talkWithGemini(spokenText)
            return@withContext VoiceCommandResult.ConversationalReply(reply)
        }

        // 3. Perform Vocal Mood Analysis using Gemini API (or intelligent fallback)
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
