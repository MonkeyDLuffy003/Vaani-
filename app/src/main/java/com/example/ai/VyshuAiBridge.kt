package com.example.ai

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Vyshu AI Integration Bridge:
 * Connects the Vaani Music Voice Agent with the Vyshu AI Cognitive Companion.
 *
 * Architecture:
 * - Vaani = The Sound, Music & Voice Controller
 * - Vyshu AI = The Conversational Mind, Empathy & Companion Agent
 */
data class VyshuMessage(
    val sender: String, // "User", "Vaani", "Vyshu"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class VyshuAiBridge(private val context: Context) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // Dual-Agent Status
    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _vyshuMood = MutableStateFlow("Supportive & Cheerful")
    val vyshuMood: StateFlow<String> = _vyshuMood.asStateFlow()

    private val _conversationHistory = MutableStateFlow<List<VyshuMessage>>(
        listOf(
            VyshuMessage("Vyshu", "Hey! I'm Vyshu AI. Together with Vaani, we have both your music and your conversations covered! How can I help you today?"),
            VyshuMessage("Vaani", "Vaani here for all beats, vocal mood sensing, and music playback!")
        )
    )
    val conversationHistory: StateFlow<List<VyshuMessage>> = _conversationHistory.asStateFlow()

    // Optional custom webhook/server URL for user's own Vyshu AI instance
    var customWebhookUrl: String? = null

    /**
     * Determines whether an utterance is music-oriented (for Vaani)
     * or conversational/personal (for Vyshu AI).
     */
    fun shouldDelegateToVyshu(query: String): Boolean {
        val lower = query.lowercase().trim()
        val musicKeywords = listOf("play", "pause", "skip", "next", "previous", "volume", "bpm", "channel", "song", "track", "music", "sleep timer", "stop")
        val isMusicCommand = musicKeywords.any { lower.contains(it) }

        val vyshuKeywords = listOf("vyshu", "hello", "hi", "how are you", "who are you", "advice", "story", "joke", "talk", "feel", "sad", "happy", "love", "friend", "help")
        val isVyshuCommand = vyshuKeywords.any { lower.contains(it) }

        return isVyshuCommand || (!isMusicCommand && lower.split(" ").size >= 2)
    }

    /**
     * Sends a query to Vyshu AI.
     * Uses configured webhook or Gemini with the Vyshu AI companion persona.
     */
    suspend fun askVyshuAi(query: String): String = withContext(Dispatchers.IO) {
        addUserMessage(query)

        // 1. If user configured a custom Vyshu endpoint, call it first
        val webhook = customWebhookUrl
        if (!webhook.isNullOrBlank()) {
            try {
                val json = JSONObject().apply {
                    put("query", query)
                    put("agent", "vyshu_ai")
                    put("partner", "vaani_bot")
                }
                val req = Request.Builder()
                    .url(webhook)
                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                    .build()
                val res = okHttpClient.newCall(req).execute()
                val body = res.body?.string()
                if (!body.isNullOrBlank()) {
                    val reply = try {
                        JSONObject(body).optString("response", body)
                    } catch (e: Exception) {
                        body
                    }
                    addVyshuMessage(reply)
                    return@withContext reply
                }
            } catch (e: Exception) {
                Log.w("VyshuBridge", "Custom webhook call failed, falling back to built-in Vyshu engine: ${e.message}")
            }
        }

        // 2. Intelligent Built-in Vyshu AI Persona Engine (via Gemini or high-EQ fallback)
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val reply = callVyshuGeminiPersona(query, apiKey)
                if (reply != null) {
                    addVyshuMessage(reply)
                    return@withContext reply
                }
            } catch (e: Exception) {
                Log.w("VyshuBridge", "Gemini Vyshu response fallback: ${e.message}")
            }
        }

        // 3. Fallback Vyshu Personality responses
        val fallbackReply = generateOfflineVyshuReply(query)
        addVyshuMessage(fallbackReply)
        fallbackReply
    }

    private fun callVyshuGeminiPersona(query: String, apiKey: String): String? {
        val systemPrompt = """
            You are Vyshu AI, a warm, intelligent, empathetic, and witty AI companion.
            You work in synergy with Vaani (the music and audio voice agent).
            Vaani takes care of the songs, beats, and tempo, while you, Vyshu AI, take care of conversations, ideas, motivation, and companionship.
            Respond warmly, concisely (2-3 sentences), with high EQ and genuine care.
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "$systemPrompt\n\nUser: $query\nVyshu AI:")
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("maxOutputTokens", 160)
                put("temperature", 0.7)
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

        return text.trim()
    }

    private fun generateOfflineVyshuReply(query: String): String {
        val lower = query.lowercase()
        return when {
            lower.contains("who are you") || lower.contains("what is vyshu") -> {
                "I am Vyshu AI, your companion mind! While Vaani curates your soundtrack, I'm here to chat, brainstorm, and keep you company."
            }
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") -> {
                "Hello there! Vyshu here. I'm connected with Vaani to make your day vibrant and peaceful. What's on your mind?"
            }
            lower.contains("sad") || lower.contains("stressed") || lower.contains("tired") -> {
                "I'm here for you. Take a gentle breath. Let Vaani play some 432Hz calming soundscapes while we relax together."
            }
            lower.contains("joke") -> {
                "Why did the music teacher need a ladder? To reach the high notes!"
            }
            lower.contains("motivat") || lower.contains("goal") || lower.contains("work") -> {
                "You've got this! Focus on one small step at a time, let the rhythm carry you forward, and keep that champion mindset."
            }
            else -> {
                "Vyshu AI is right here with you! Whatever you're going through or creating today, Vaani and I have your back."
            }
        }
    }

    private fun addUserMessage(text: String) {
        val current = _conversationHistory.value.toMutableList()
        current.add(VyshuMessage("User", text))
        _conversationHistory.value = current
    }

    private fun addVyshuMessage(text: String) {
        val current = _conversationHistory.value.toMutableList()
        current.add(VyshuMessage("Vyshu", text))
        _conversationHistory.value = current
    }
}
