package com.eduprep.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "theory_feedback")
data class TheoryFeedbackEntity(
    @PrimaryKey
    @ColumnInfo(name = "question_id")
    val questionId: Long,
    val score: Int,
    val feedback: String,
    @ColumnInfo(name = "missing_keywords")
    val missingKeywords: String // Comma-separated list of keywords
)
