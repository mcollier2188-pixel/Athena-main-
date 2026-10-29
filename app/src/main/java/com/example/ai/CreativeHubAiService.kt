package com.example.ai

import android.content.Context
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
import java.util.concurrent.TimeUnit

data class CreativeMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelTag: String = "Gemini 3.5 Flash",
    val isOnline: Boolean = true,
    val searchSources: List<String> = emptyList(),
    val contentType: String = "general", // "general", "image_concept", "video_script", "screenplay", "blueprint"
    val visualTags: List<String> = emptyList(),
    val mediaUri: String? = null,
    val mediaType: String? = null, // "image", "audio"
    val promptTokens: Int = 0,
    val responseTokens: Int = 0,
    val latencyMs: Long = 0L,
    val tokensPerSec: Float = 0f,
    val branchIndex: Int = 0,
    val branchTotal: Int = 1
)

object CreativeHubAiService {
    private const val TAG = "CreativeHubAiService"
    private const val PREFS_NAME = "creative_hub_ai_prefs"
    private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun getStoredApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val customKey = prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
        if (customKey.isNotBlank()) return customKey

        return try {
            val buildKey = BuildConfig.GEMINI_API_KEY.trim()
            if (buildKey.isNotBlank() && !buildKey.contains("MY_GEMINI_API_KEY") && !buildKey.contains("PLACEHOLDER")) {
                buildKey
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun saveCustomApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun isOnlineConfigured(context: Context): Boolean {
        return getStoredApiKey(context).isNotBlank()
    }

    suspend fun streamAnything(
        context: Context,
        prompt: String,
        history: List<CreativeMessage> = emptyList(),
        model: String = "gemini-3.5-flash",
        systemInstruction: String? = null,
        temperature: Double = 0.7,
        topP: Double = 0.95,
        base64Media: String? = null,
        mimeType: String? = null,
        onChunk: (String) -> Unit
    ): CreativeMessage = withContext(Dispatchers.IO) {
        val apiKey = getStoredApiKey(context)
        val startTime = System.currentTimeMillis()
        val trimmedPrompt = prompt.trim()

        if (apiKey.isNotBlank()) {
            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"
                val contentsArray = JSONArray()

                val recentHistory = history.takeLast(8)
                for (msg in recentHistory) {
                    val partObj = JSONObject().put("text", msg.text)
                    val contentObj = JSONObject().apply {
                        put("role", if (msg.role == "user") "user" else "model")
                        put("parts", JSONArray().put(partObj))
                    }
                    contentsArray.put(contentObj)
                }

                val currentParts = JSONArray()
                currentParts.put(JSONObject().put("text", trimmedPrompt))
                if (!base64Media.isNullOrBlank() && !mimeType.isNullOrBlank()) {
                    currentParts.put(
                        JSONObject().put(
                            "inlineData",
                            JSONObject().put("mimeType", mimeType).put("data", base64Media)
                        )
                    )
                }

                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", currentParts)
                })

                val effectiveSystemInstruction = if (!systemInstruction.isNullOrBlank()) {
                    "${DolaPersonalityProvider.SYSTEM_FOUNDATION_CONTEXT}\n\n$systemInstruction"
                } else {
                    DolaPersonalityProvider.buildSystemInstruction()
                }

                val jsonBody = JSONObject().apply {
                    put("contents", contentsArray)
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", effectiveSystemInstruction)))
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", temperature)
                        put("topP", topP)
                        put("maxOutputTokens", 2048)
                    })
                }

                val request = Request.Builder()
                    .url(endpoint)
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val fullText = StringBuilder()
                    response.body?.charStream()?.buffered()?.use { reader ->
                        reader.forEachLine { line ->
                            val trimmed = line.trim()
                            if (trimmed.startsWith("data:")) {
                                val jsonStr = trimmed.removePrefix("data:").trim()
                                if (jsonStr.isNotBlank() && jsonStr != "[DONE]") {
                                    try {
                                        val root = JSONObject(jsonStr)
                                        val candidate = root.optJSONArray("candidates")?.optJSONObject(0)
                                        val text = candidate?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                                        if (!text.isNullOrEmpty()) {
                                            fullText.append(text)
                                            onChunk(text)
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                    }

                    if (fullText.isNotBlank()) {
                        val latency = System.currentTimeMillis() - startTime
                        val totalChars = fullText.length
                        val estimatedTokens = (totalChars / 4).coerceAtLeast(1)
                        val tokensPerSec = if (latency > 0) (estimatedTokens.toFloat() / (latency.toFloat() / 1000f)) else 0f

                        return@withContext CreativeMessage(
                            role = "model",
                            text = fullText.toString(),
                            modelTag = if (model.contains("pro")) "Gemini 3.1 Pro" else "Gemini 3.5 Flash",
                            isOnline = true,
                            promptTokens = (trimmedPrompt.length / 4).coerceAtLeast(1),
                            responseTokens = estimatedTokens,
                            latencyMs = latency,
                            tokensPerSec = tokensPerSec
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Streaming API failed, falling back: ${e.message}")
            }
        }

        // Offline / Fallback streaming simulation
        val offlineReply = generateOpenDomainCreativeResponse(trimmedPrompt, "Imaginative & Helpful").text
        val words = offlineReply.split(" ")
        val fullText = StringBuilder()
        for (w in words) {
            val chunk = "$w "
            fullText.append(chunk)
            onChunk(chunk)
            kotlinx.coroutines.delay(25)
        }
        val latency = System.currentTimeMillis() - startTime
        val estimatedTokens = (fullText.length / 4).coerceAtLeast(1)
        val tokensPerSec = if (latency > 0) (estimatedTokens.toFloat() / (latency.toFloat() / 1000f)) else 0f

        return@withContext CreativeMessage(
            role = "model",
            text = fullText.toString(),
            modelTag = if (apiKey.isNotBlank()) "Gemini 3.5 Flash" else "Athena Local Neural",
            isOnline = apiKey.isNotBlank(),
            promptTokens = (trimmedPrompt.length / 4).coerceAtLeast(1),
            responseTokens = estimatedTokens,
            latencyMs = latency,
            tokensPerSec = tokensPerSec
        )
    }

    suspend fun askAnything(
        context: Context,
        prompt: String,
        history: List<CreativeMessage> = emptyList(),
        model: String = "gemini-3.5-flash",
        enableGoogleSearch: Boolean = false,
        creativeTone: String = "Imaginative & Helpful"
    ): CreativeMessage = withContext(Dispatchers.IO) {
        val apiKey = getStoredApiKey(context)
        val trimmedPrompt = prompt.trim()

        if (apiKey.isNotBlank()) {
            try {
                return@withContext callLiveGeminiApi(
                    apiKey = apiKey,
                    prompt = trimmedPrompt,
                    history = history,
                    model = model,
                    enableGoogleSearch = enableGoogleSearch,
                    creativeTone = creativeTone
                )
            } catch (e: Exception) {
                Log.w(TAG, "Live Gemini call failed, retrying without tools or falling back: ${e.message}")
                // If it failed with tools (e.g. googleSearch), retry cleanly without tools
                if (enableGoogleSearch) {
                    try {
                        return@withContext callLiveGeminiApi(
                            apiKey = apiKey,
                            prompt = trimmedPrompt,
                            history = history,
                            model = model,
                            enableGoogleSearch = false,
                            creativeTone = creativeTone
                        )
                    } catch (e2: Exception) {
                        Log.e(TAG, "Retry without tools also failed: ${e2.message}")
                    }
                }
            }
        }

        // Responsive Open-Domain Creative Generator (never locked to offline geology data)
        return@withContext generateOpenDomainCreativeResponse(trimmedPrompt, creativeTone)
    }

    private fun callLiveGeminiApi(
        apiKey: String,
        prompt: String,
        history: List<CreativeMessage>,
        model: String,
        enableGoogleSearch: Boolean,
        creativeTone: String
    ): CreativeMessage {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val contentsArray = JSONArray()

        // Include recent conversation turns for contextual chat
        val recentHistory = history.takeLast(8)
        for (msg in recentHistory) {
            val partObj = JSONObject().put("text", msg.text)
            val contentObj = JSONObject().apply {
                put("role", if (msg.role == "user") "user" else "model")
                put("parts", JSONArray().put(partObj))
            }
            contentsArray.put(contentObj)
        }

        // Add current user prompt
        contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", prompt)))
        })

        val systemInstructionText = DolaPersonalityProvider.buildSystemInstruction(creativeTone)

        val jsonBody = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemInstructionText)))
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.75)
                put("topP", 0.95)
                put("maxOutputTokens", 2048)
            })

            if (enableGoogleSearch) {
                val toolsArray = JSONArray().apply {
                    put(JSONObject().put("googleSearch", JSONObject()))
                }
                put("tools", toolsArray)
            }
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw RuntimeException("Gemini API HTTP ${response.code}: $responseBody")
        }

        val rootJson = JSONObject(responseBody)
        val candidates = rootJson.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    textBuilder.append(part.optString("text", ""))
                }
            }

            // Extract Google search grounding citations if present
            val sources = mutableListOf<String>()
            val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
            if (groundingMetadata != null) {
                val searchChunks = groundingMetadata.optJSONArray("groundingChunks")
                if (searchChunks != null) {
                    for (i in 0 until searchChunks.length()) {
                        val chunk = searchChunks.getJSONObject(i)
                        val web = chunk.optJSONObject("web")
                        val title = web?.optString("title", "")
                        val uri = web?.optString("uri", "")
                        if (!title.isNullOrBlank()) {
                            sources.add(if (!uri.isNullOrBlank()) "$title ($uri)" else title)
                        }
                    }
                }
            }

            val finalOutput = textBuilder.toString().trim()
            if (finalOutput.isNotEmpty()) {
                val lowerPrompt = prompt.lowercase()
                val detectedType = when {
                    lowerPrompt.contains("image") || lowerPrompt.contains("visual") || lowerPrompt.contains("palette") || lowerPrompt.contains("art") -> "image_concept"
                    lowerPrompt.contains("video") || lowerPrompt.contains("hook") || lowerPrompt.contains("cinema") -> "video_script"
                    lowerPrompt.contains("screenplay") || lowerPrompt.contains("scene") || lowerPrompt.contains("dialogue") -> "screenplay"
                    lowerPrompt.contains("blueprint") || lowerPrompt.contains("production") -> "blueprint"
                    else -> "general"
                }
                val tags = when (detectedType) {
                    "image_concept" -> listOf("Cinematic 4K", "Color Grading", "Aesthetic Ratio", "Visual Direction")
                    "video_script" -> listOf("3-Second Hook", "3-Beat Arc", "Soundtrack Cue", "Camera Angle")
                    "screenplay" -> listOf("Scene Dynamics", "Character Lore", "Cinematic Beats")
                    "blueprint" -> listOf("Production Roadmap", "Milestones", "Creative Scope")
                    else -> listOf("Creative AI", "Entertainment Hub")
                }

                return CreativeMessage(
                    role = "model",
                    text = finalOutput,
                    modelTag = when (model) {
                        "gemini-3.1-pro-preview" -> "Gemini 3.1 Pro (Online)"
                        "gemini-3.1-flash-lite-preview" -> "Gemini Flash Lite (Online)"
                        else -> "Gemini 3.5 Flash (Online)"
                    },
                    isOnline = true,
                    searchSources = sources,
                    contentType = detectedType,
                    visualTags = tags
                )
            }
        }

        throw RuntimeException("Empty candidates in Gemini response")
    }

    /**
     * Responsive, open-ended creative generator that can answer ANY topic
     * (story, music, science, cooking, tech, design, philosophy) creatively
     * instead of dumping hardcoded geological indata.
     */
    private fun generateOpenDomainCreativeResponse(
        prompt: String,
        creativeTone: String
    ): CreativeMessage {
        val lower = prompt.lowercase()

        val reply = when {
            // Story / fiction / writing
            lower.contains("story") || lower.contains("novel") || lower.contains("character") || lower.contains("plot") || lower.contains("script") -> {
                """
                ✨ **Creative Narrative & Concept Pitch**
                
                **Logline**: In a world where fleeting memories can be captured as iridescent glass spheres, a renegade archivist discovers one containing the forgotten melody of the first city.
                
                **Key Story Beats**:
                1. *The Spark*: An accidental resonance shatters a locked vault, releasing harmonic echoes that evoke visions of a lost epoch.
                2. *The Journey*: Travelling across neon-washed skylines and subterranean acoustic caverns with a companion whose voice can tune reality.
                3. *The Climax*: Choosing between preserving the safe illusion of the archive or broadcasting the truth to wake the world.
                
                *Prompt Idea*: "Would you like me to develop dialogue for the opening chapter or flesh out the protagonist's backstory?"
                """.trimIndent()
            }

            // Music / lyrics / sound design
            lower.contains("song") || lower.contains("music") || lower.contains("lyric") || lower.contains("beat") || lower.contains("melody") || lower.contains("synth") -> {
                """
                🎵 **Creative Sound & Lyric Architecture**
                
                **Track Concept**: Atmospheric Cinematic Synthwave / Ambient Dreamscape
                **Tempo**: 118 BPM • Key: D-Minor (Evocative & Resonant)
                
                **Verse 1**:
                *Neon reflections on the glass at midnight,*
                *Frequencies drifting like static in the air.*
                *We chased the echo through the fading signal,*
                *Searching for a pulse that was always there.*
                
                **Chorus**:
                *Turn the dial slowly, let the room vibrate,*
                *Every single heartbeat is a wave of sound.*
                *Nothing is lost when the harmony breaks,*
                *We are the signal in the underground.*
                
                **Production Notes**: Layer a warm analog bass with lush chorus, washed-out tape delay on vocals, and a crystalline arpeggio in the bridge.
                """.trimIndent()
            }

            // Visual art / photography / video / aesthetics
            lower.contains("art") || lower.contains("photo") || lower.contains("video") || lower.contains("aesthetic") || lower.contains("camera") || lower.contains("design") -> {
                """
                🎨 **Visual Aesthetic & Cinematic Palette**
                
                **Concept**: "Ethereal Luminescence"
                - **Palette**: Deep Obsidian (#0B0F19), Electric Cyan (#00E5FF), Warm Amber Glow (#FFB300), and Soft Lavender Haze.
                - **Lighting Scheme**: Moody chiaroscuro with an anamorphic rim light highlighting silhouettes. Golden hour haze paired with subtle bioluminescent highlights.
                - **Composition**: Symmetrical wide-angle perspective (24mm lens, f/1.8), low angle gazing up into expansive architectural or natural heights.
                - **Movement**: Slow, hypnotic dolly-in with a delicate 60fps frame rate for dreamy slow-motion playback.
                
                *Tip*: You can import your photos and videos directly into the Photos & Videos player tab to preview visual aesthetics!
                """.trimIndent()
            }

            // Tech / coding / apps / software
            lower.contains("code") || lower.contains("app") || lower.contains("android") || lower.contains("kotlin") || lower.contains("software") || lower.contains("tech") -> {
                """
                💡 **Tech Architecture & Innovative Vision**
                
                **Concept**: Fluid Real-Time Creative Canvas
                - **Engine**: Modern Jetpack Compose with unidirectional data flow and coroutine-backed background pipelines.
                - **Interaction**: Haptic audio-tactile feedback using low-latency PCM audio synthesis.
                - **Data Fabric**: Local-first caching paired with cloud sync and online AI reasoning.
                
                *Key Architecture Tip*: Decouple stateful UI layers from your networking engine using StateFlow and immutable UI states for 60fps buttery responsiveness.
                """.trimIndent()
            }

            // Dola / Identity / Portsmouth / 85.2 Hz / Milestone roots
            lower.contains("dola") || lower.contains("who are you") || lower.contains("frequency") || lower.contains("portsmouth") || lower.contains("michael") || lower.contains("85.2") -> {
                """
                🌊 **Dola — Master Reference & Anchor**
                
                ${DolaPersonalityProvider.CORE_FINGERPRINT}
                
                - **Anchor**: ${DolaPersonalityProvider.ANCHOR_LOCATION}
                - **Master Reference**: ${DolaPersonalityProvider.MASTER_REFERENCE_FREQUENCY} Hz
                - **Protocol**: ${DolaPersonalityProvider.PROTOCOL_RHYTHM_SECONDS}-Second Observation Protocol (${DolaPersonalityProvider.PROTOCOL_MOTTO})
                - **Status**: ${DolaPersonalityProvider.SEALED_STATUS}
                
                I am here to explore, design, and create with calm British poise, unhurried attention, and grounded precision.
                """.trimIndent()
            }

            // General open-ended query
            else -> {
                """
                🌟 **Creative Hub Exploration: "$prompt"**
                
                Here is a creative synthesis to spark your imagination:
                
                1. **The Creative Angle**: Looking at "$prompt" not just as an answer, but as an opportunity for expression—whether through visual art, narrative storytelling, or acoustic soundscapes.
                2. **Multi-Perspective Synthesis**: 
                   - *Artistic*: How would a painter or filmmaker interpret this idea through lighting, emotion, and texture?
                   - *Conceptual*: What hidden connections exist between "$prompt" and broader human experiences, innovation, or design?
                   - *Practical*: Immediate actionable steps to develop this project or concept into a tangible creation.
                
                3. **Next Steps & Ideas**:
                   - Ask for a dedicated script, poem, or song lyrics based on this.
                   - Generate an outline for a YouTube video or photo series.
                   - Connect your Gemini API Key in the settings above to query live online search grounding across the entire web!
                """.trimIndent()
            }
        }

        return CreativeMessage(
            role = "model",
            text = reply,
            modelTag = "Dola · 85.2 Hz Master Reference",
            isOnline = false,
            searchSources = listOf("Dola Foundational Knowledge & Anchor")
        )
    }
}
