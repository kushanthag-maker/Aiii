package com.example.data.api

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.ChatDao
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.persona.Personas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.SocketTimeoutException
import java.util.UUID
import java.util.concurrent.TimeUnit

class NovaRepository(
    context: Context,
    private val chatDao: ChatDao = AppDatabase.getDatabase(context).chatDao()
) {
    companion object {
        val DEFAULT_NOVA_KEY: String get() = "supun-emdf5nu7h8b3svh1cah0axti"
        val DEFAULT_THENUX_KEY: String get() = String(
            android.util.Base64.decode("dG54X2xpdmVfOWEzYjNkMzMzN2U1MDgwMWU5NjY0YzRhMGU3M2FmMjQ5NWZjODM0YzA1MmI0OWVk", android.util.Base64.NO_WRAP)
        )
        val DEFAULT_KIZER_KEY: String get() = try {
            if (!BuildConfig.GEMINI_API_KEY.isNullOrBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") {
                BuildConfig.GEMINI_API_KEY
            } else {
                String(
                    android.util.Base64.decode("QVEuQWI4Uk42S1pEbFFnSHEwOTdNaG9WUlh1NEw4ZE9ES1RlMHB0em9SeGtkR3p2bzJPemc=", android.util.Base64.NO_WRAP)
                )
            }
        } catch (e: Throwable) {
            ""
        }

        private const val NOVA_BASE_URL = "https://supunofc.site/"
        private const val THENUX_BASE_URL = "https://edge.thenuxofc.store/"
        private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
        private const val TAG = "NovaRepository"
    }

    private val novaApiService: NovaApiService
    private val thenuxApiService: ThenuxApiService
    private val kizerApiService: KizerApiService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        // Generous timeouts (120s) and connection pool for mobile networks and AI models
        val client = OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) NovaAI/1.2")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()

        val novaRetrofit = Retrofit.Builder()
            .baseUrl(NOVA_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        novaApiService = novaRetrofit.create(NovaApiService::class.java)

        val thenuxRetrofit = Retrofit.Builder()
            .baseUrl(THENUX_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        thenuxApiService = thenuxRetrofit.create(ThenuxApiService::class.java)

        val kizerRetrofit = Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        kizerApiService = kizerRetrofit.create(KizerApiService::class.java)
    }

    val allConversations: Flow<List<Conversation>> = chatDao.getAllConversations()
    val pinnedConversations: Flow<List<Conversation>> = chatDao.getPinnedConversations()
    val recentConversations: Flow<List<Conversation>> = chatDao.getRecentConversations()

    fun getMessages(conversationId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForConversation(conversationId)
    }

    fun searchConversations(query: String): Flow<List<Conversation>> {
        return chatDao.searchConversations(query)
    }

    suspend fun createConversation(
        personaId: String = "coder",
        selectedModelId: String = "kizer",
        title: String = "New Chat"
    ): Conversation {
        val conv = Conversation(
            id = UUID.randomUUID().toString(),
            title = title,
            personaId = personaId,
            selectedModelId = selectedModelId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        chatDao.insertConversation(conv)
        return conv
    }

    suspend fun getConversation(id: String): Conversation? {
        return chatDao.getConversation(id)
    }

    suspend fun togglePin(id: String, isPinned: Boolean) {
        chatDao.setPinned(id, isPinned)
    }

    suspend fun deleteConversation(id: String) {
        chatDao.deleteConversationById(id)
    }

    suspend fun clearMessages(conversationId: String) {
        chatDao.deleteMessagesForConversation(conversationId)
    }

    /**
     * Send user message to selected AI model (Kizer AI, Nova AI, or Thenux AI)
     */
    suspend fun sendMessage(
        conversationId: String,
        userText: String,
        modelId: String = "kizer",
        novaApiKey: String = DEFAULT_NOVA_KEY,
        thenuxApiKey: String = DEFAULT_THENUX_KEY,
        kizerApiKey: String = DEFAULT_KIZER_KEY,
        customSystemPrompt: String? = null,
        personaId: String = "coder",
        conversationHistory: List<ChatMessage> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. Insert user message into DB
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                role = "user",
                content = userText,
                timestamp = System.currentTimeMillis(),
                hasCode = userText.contains("```") || userText.contains("fun ") || userText.contains("class "),
                modelUsed = modelId
            )
            chatDao.insertMessage(userMsg)

            // Update conversation title if it's the first message
            val conversation = chatDao.getConversation(conversationId)
            if (conversation != null && (conversation.title == "New Chat" || conversation.title.isBlank())) {
                val newTitle = if (userText.length > 32) userText.take(30) + "…" else userText
                chatDao.updateTitleAndTimestamp(conversationId, newTitle, System.currentTimeMillis())
            } else {
                chatDao.updateTitleAndTimestamp(conversationId, conversation?.title ?: "Chat", System.currentTimeMillis())
            }

            // 2. Dispatch to selected AI model
            val replyText = when (modelId) {
                "kizer" -> {
                    callKizerApi(
                        userText = userText,
                        apiKey = kizerApiKey,
                        customSystemPrompt = customSystemPrompt,
                        personaId = personaId,
                        conversationHistory = conversationHistory
                    )
                }
                "thenux" -> {
                    // Thenux AI is an edge neural endpoint that works best with concise, direct prompts
                    val promptPrefix = when {
                        !customSystemPrompt.isNullOrBlank() -> "[Instruction: ${customSystemPrompt.take(120)}] "
                        personaId == "coder" -> "[Act as Senior Software Architect] "
                        personaId == "debugger" -> "[Act as Code Debugger & Optimizer] "
                        personaId == "sinhala" -> "[Answer in Sinhala] "
                        personaId == "ui_ux" -> "[Act as Jetpack Compose UI Expert] "
                        else -> ""
                    }
                    val formattedThenuxQuery = promptPrefix + userText

                    try {
                        callThenuxApi(formattedThenuxQuery, thenuxApiKey)
                    } catch (e: SocketTimeoutException) {
                        Log.w(TAG, "Thenux timeout on initial request, retrying with raw query...", e)
                        delay(1000)
                        callThenuxApi(userText, thenuxApiKey)
                    }
                }
                else -> {
                    // Nova AI
                    val persona = Personas.getById(personaId)
                    val effectivePrompt = customSystemPrompt?.takeIf { it.isNotBlank() } ?: persona.systemPrompt

                    val recentContext = conversationHistory.takeLast(4)
                        .joinToString("\n") { "${if (it.role == "user") "User" else "Assistant"}: ${it.content.take(150)}" }

                    val fullQuery = buildString {
                        append("Instructions: ").append(effectivePrompt)
                        if (recentContext.isNotBlank()) {
                            append("\nRecent chat context:\n").append(recentContext)
                        }
                        append("\nUser query: ").append(userText)
                    }

                    callNovaApi(fullQuery, novaApiKey)
                }
            }

            if (replyText.isBlank()) {
                val errMsg = "Empty response received from $modelId. Please try again."
                saveErrorMessage(conversationId, errMsg, modelId)
                return@withContext Result.failure(Exception(errMsg))
            }

            // 3. Insert Assistant reply into DB
            val hasCodeBlock = replyText.contains("```")
            val assistantMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                role = "assistant",
                content = replyText,
                timestamp = System.currentTimeMillis(),
                hasCode = hasCodeBlock,
                isError = false,
                modelUsed = modelId
            )
            chatDao.insertMessage(assistantMsg)

            Result.success(replyText)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to call AI model $modelId", e)
            val isTimeout = e is SocketTimeoutException || e.message?.contains("timeout", ignoreCase = true) == true
            val errorMsg = if (isTimeout) {
                if (modelId == "thenux") {
                    "Thenux AI edge server took too long to respond. You can tap Retry or switch to Kizer AI or Nova AI at the top."
                } else {
                    "Request timed out while connecting to $modelId. Please check your internet connection and tap Retry."
                }
            } else {
                "Failed to connect to AI: ${e.localizedMessage ?: "Unknown error"}. Please check your connection."
            }
            saveErrorMessage(conversationId, errorMsg, modelId)
            Result.failure(e)
        }
    }

    private suspend fun callKizerApi(
        userText: String,
        apiKey: String,
        customSystemPrompt: String?,
        personaId: String,
        conversationHistory: List<ChatMessage>
    ): String {
        val keyToUse = if (apiKey.isNotBlank()) {
            apiKey
        } else if (!BuildConfig.GEMINI_API_KEY.isNullOrBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") {
            BuildConfig.GEMINI_API_KEY
        } else {
            DEFAULT_KIZER_KEY
        }

        val persona = Personas.getById(personaId)
        val systemInstructionText = buildString {
            append("You are Kizer AI, an elite, world-class software engineer, coding mastermind, and software architect.")
            append(" You specialize in writing clean, robust, production-grade, bug-free code with complete implementations (no placeholders, no omissions).")
            append(" You excel at software architecture, algorithms, deep debugging, security, Kotlin, Jetpack Compose, Python, Web, and backend systems.")
            if (!customSystemPrompt.isNullOrBlank()) {
                append("\nAdditional User Instructions: ").append(customSystemPrompt)
            } else if (persona.systemPrompt.isNotBlank()) {
                append("\nFocus Mode: ").append(persona.systemPrompt)
            }
        }

        // Build contents array with conversation history
        val contentsArray = JSONArray()
        val recentTurns = conversationHistory.takeLast(6)
        for (turn in recentTurns) {
            val role = if (turn.role == "user") "user" else "model"
            val partsArr = JSONArray().apply {
                put(JSONObject().apply { put("text", turn.content) })
            }
            contentsArray.put(JSONObject().apply {
                put("role", role)
                put("parts", partsArr)
            })
        }
        // Current user message
        contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", userText) })
            })
        })

        val jsonBody = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstructionText) })
                })
            })
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

        // Try primary model (gemini-flash-lite-latest for ultra-fast, robust coding)
        val response = try {
            kizerApiService.generateContent(
                model = "gemini-flash-lite-latest",
                apiKey = keyToUse,
                body = requestBody
            )
        } catch (e: Exception) {
            Log.w(TAG, "Kizer flash-lite failed, falling back to gemini-flash-latest", e)
            kizerApiService.generateContent(
                model = "gemini-flash-latest",
                apiKey = keyToUse,
                body = requestBody
            )
        }

        if (!response.isSuccessful) {
            // If primary had an error code (like 503), try gemini-flash-latest
            val secondTry = kizerApiService.generateContent(
                model = "gemini-flash-latest",
                apiKey = keyToUse,
                body = requestBody
            )
            if (!secondTry.isSuccessful) {
                val errBody = secondTry.errorBody()?.string() ?: ""
                throw Exception("Kizer Engine error (${secondTry.code()}): $errBody")
            }
            val bodyString = secondTry.body()?.string() ?: ""
            return parseKizerResponse(bodyString)
        }

        val bodyString = response.body()?.string() ?: ""
        return parseKizerResponse(bodyString)
    }

    private suspend fun callNovaApi(fullQuery: String, apiKey: String): String {
        val keyToUse = if (apiKey.isNotBlank()) apiKey else DEFAULT_NOVA_KEY
        val response = novaApiService.queryNova(query = fullQuery, apiKey = keyToUse)

        if (!response.isSuccessful) {
            throw Exception("Nova Server returned code ${response.code()}")
        }

        val bodyString = response.body()?.string() ?: ""
        return parseNovaResponse(bodyString)
    }

    private suspend fun callThenuxApi(queryText: String, apiKey: String): String {
        val keyToUse = if (apiKey.isNotBlank()) apiKey else DEFAULT_THENUX_KEY
        val bearer = if (keyToUse.startsWith("Bearer ")) keyToUse else "Bearer $keyToUse"

        val jsonBody = JSONObject().apply {
            put("message", queryText)
            put("model", "t-nex-1.0")
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val response = thenuxApiService.chat(authorization = bearer, body = requestBody)

        if (!response.isSuccessful) {
            val errBody = response.errorBody()?.string() ?: ""
            throw Exception("Thenux Server returned code ${response.code()}: $errBody")
        }

        val bodyString = response.body()?.string() ?: ""
        return parseThenuxResponse(bodyString)
    }

    private suspend fun saveErrorMessage(conversationId: String, errorText: String, modelId: String) {
        val errorMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = "assistant",
            content = errorText,
            timestamp = System.currentTimeMillis(),
            isError = true,
            modelUsed = modelId
        )
        chatDao.insertMessage(errorMsg)
    }

    private fun parseKizerResponse(rawJson: String): String {
        return try {
            val json = JSONObject(rawJson)
            if (json.has("candidates")) {
                val candidates = json.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    if (parts.length() > 0) {
                        return parts.getJSONObject(0).getString("text")
                    }
                }
            }
            if (json.has("error")) {
                val err = json.getJSONObject("error")
                "Error from Kizer AI: " + err.optString("message", "Unknown error")
            } else {
                rawJson
            }
        } catch (e: Exception) {
            rawJson
        }
    }

    private fun parseNovaResponse(rawJson: String): String {
        return try {
            val json = JSONObject(rawJson)
            if (json.has("results")) {
                val resultsObj = json.optJSONObject("results")
                if (resultsObj != null && resultsObj.has("text")) {
                    resultsObj.getString("text")
                } else {
                    json.optString("results", "")
                }
            } else if (json.has("text")) {
                json.getString("text")
            } else if (json.has("error")) {
                "Error from Nova AI: " + json.getString("error")
            } else {
                rawJson
            }
        } catch (e: Exception) {
            rawJson
        }
    }

    private fun parseThenuxResponse(rawJson: String): String {
        return try {
            val json = JSONObject(rawJson)
            if (json.has("response")) {
                json.getString("response")
            } else if (json.has("message")) {
                json.getString("message")
            } else if (json.has("error")) {
                "Error from Thenux AI: " + json.getString("error")
            } else {
                rawJson
            }
        } catch (e: Exception) {
            rawJson
        }
    }
}
