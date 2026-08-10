package com.eduprep.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatEntity)

    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    suspend fun getAllMessagesSync(): List<ChatEntity>

    @Query("DELETE FROM chat_history")
    suspend fun clearHistory()
}
