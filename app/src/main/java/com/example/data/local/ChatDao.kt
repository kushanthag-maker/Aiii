package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinnedConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE isPinned = 0 ORDER BY updatedAt DESC")
    fun getRecentConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversation(id: String): Conversation?

    @Query("SELECT * FROM conversations WHERE title LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchConversations(query: String): Flow<List<Conversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)

    @Update
    suspend fun updateConversation(conversation: Conversation)

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: String, isPinned: Boolean)

    @Query("UPDATE conversations SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTitleAndTimestamp(id: String, title: String, updatedAt: Long)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: String)

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)
}
