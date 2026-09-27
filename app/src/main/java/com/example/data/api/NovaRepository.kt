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
import okhttp3.OkHttpClient
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
        const val DEFAULT_API_KEY = "supun-emdf5nu7h8b3svh1cah0axti"
        private const val BASE_URL = "https://supunofc.site/"
        private const val TAG = "NovaRepository"
    }

    private val apiService: NovaApiService

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

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        apiService = retrofit.create(NovaApiService::class.java)
    }

    // Conversations Flow
    val allConversations: Flow<List<Conversation>> = chatDao.getAllConversations()
    val pinnedConversations: Flow<List<Conversation>> = chatDao.getPinnedConversations()
    val recentConversations: Flow<List<Conversation>> = chatDao.getRecentConversations()

    fun getMessages(conversationId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForConversation(conversationId)
    }

    fun searchConversations(query: String): Flow<List<Conversation>> {
        return chatDao.searchConversations(query)
    }

    suspend fun createConversation(personaId: String = "coder", title: String = "New Chat"): Conversation {
        val conv = Conversation(
            id = UUID.randomUUID().toString(),
            title = title,
            personaId = personaId,
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
     * Send user message and get response from Nova AI
     */
    suspend fun sendMessage(
        conversationId: String,
        userText: String,
        apiKey: String = DEFAULT_API_KEY,
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
                hasCode = userText.contains("```") || userText.contains("fun ") || userText.contains("class ")
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

            // Build query context with instructions and recent conversational continuity
            val recentContext = conversationHistory.takeLast(4)
                .joinToString("\n") { "${if (it.role == "user") "User" else "Assistant"}: ${it.content.take(150)}" }

            val fullQuery = buildString {
                append("Instructions: ").append(effectivePrompt)
                if (recentContext.isNotBlank()) {
                    append("\nRecent chat context:\n").append(recentContext)
                }
                append("\nUser query: ").append(userText)
            }

            // 3. Call Nova AI API
            val keyToUse = if (apiKey.isNotBlank()) apiKey else DEFAULT_API_KEY
            val response = apiService.queryNova(query = fullQuery, apiKey = keyToUse)

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "HTTP error: ${response.code()}"
                val errorMsg = "Error: Server returned code ${response.code()}"
                saveErrorMessage(conversationId, errorMsg)
                return@withContext Result.failure(Exception(errorMsg))
            }

            val bodyString = response.body()?.string() ?: ""
            Log.d(TAG, "Raw API response: $bodyString")

            val replyText = parseNovaResponse(bodyString)

            if (replyText.isBlank()) {
                val errMsg = "Error: Empty response received from Nova AI."
                saveErrorMessage(conversationId, errMsg)
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
                isError = false
            )
            chatDao.insertMessage(assistantMsg)

            Result.success(replyText)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to call Nova AI", e)
            val errorMsg = "Failed to connect to Nova AI: ${e.localizedMessage ?: "Unknown error"}. Please check your connection."
            saveErrorMessage(conversationId, errorMsg)
            Result.failure(e)
        }
    }

    private suspend fun saveErrorMessage(conversationId: String, errorText: String) {
        val errorMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = "assistant",
            content = errorText,
            timestamp = System.currentTimeMillis(),
            isError = true
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
            // Not JSON, return as plain text
            rawJson
        }
    }
}
