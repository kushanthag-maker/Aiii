package com.example.data.api

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.ChatDao
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.persona.Personas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

class NovaRepository(
    context: Context,
    private val chatDao: ChatDao = AppDatabase.getDatabase(context).chatDao()
) {
    companion object {
        const val DEFAULT_NOVA_KEY = "supun-emdf5nu7h8b3svh1cah0axti"
        const val DEFAULT_THENUX_KEY = "tnx_live_9a3b3d3337e50801e9664c4a0e73af2495fc834c052b49ed"
        private const val NOVA_BASE_URL = "https://supunofc.site/"
        private const val THENUX_BASE_URL = "https://edge.thenuxofc.store/"
        private const val TAG = "NovaRepository"
    }

    private val novaApiService: NovaApiService
    private val thenuxApiService: ThenuxApiService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
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
        selectedModelId: String = "nova",
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
     * Send user message to selected AI model (Nova AI or Thenux AI)
     */
    suspend fun sendMessage(
        conversationId: String,
        userText: String,
        modelId: String = "nova",
        novaApiKey: String = DEFAULT_NOVA_KEY,
        thenuxApiKey: String = DEFAULT_THENUX_KEY,
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

            // 2. Build full prompt with agent persona instruction
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

            // 3. Dispatch to selected AI model
            val replyText = if (modelId == "thenux") {
                callThenuxApi(fullQuery, thenuxApiKey)
            } else {
                callNovaApi(fullQuery, novaApiKey)
            }

            if (replyText.isBlank()) {
                val errMsg = "Error: Empty response received from AI model."
                saveErrorMessage(conversationId, errMsg, modelId)
                return@withContext Result.failure(Exception(errMsg))
            }

            // 4. Insert Assistant reply into DB
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
            val errorMsg = "Failed to connect to AI: ${e.localizedMessage ?: "Unknown error"}. Please check your connection."
            saveErrorMessage(conversationId, errorMsg, modelId)
            Result.failure(e)
        }
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

    private suspend fun callThenuxApi(fullQuery: String, apiKey: String): String {
        val keyToUse = if (apiKey.isNotBlank()) apiKey else DEFAULT_THENUX_KEY
        val bearer = if (keyToUse.startsWith("Bearer ")) keyToUse else "Bearer $keyToUse"

        val jsonBody = JSONObject().apply {
            put("message", fullQuery)
            put("model", "t-nex-1.0")
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val response = thenuxApiService.chat(authorization = bearer, body = requestBody)

        if (!response.isSuccessful) {
            throw Exception("Thenux Server returned code ${response.code()}")
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
