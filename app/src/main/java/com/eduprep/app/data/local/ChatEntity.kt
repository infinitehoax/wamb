package com.eduprep.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chat_history")
data class ChatEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)
