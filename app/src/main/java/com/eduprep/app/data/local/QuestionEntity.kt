package com.eduprep.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subject: String,
    val topic: String,
    val year: String,
    val text: String,
    val optA: String,
    val optB: String,
    val optC: String,
    val optD: String,
    val answer: String,
    val explanation: String,
    @ColumnInfo(name = "is_theory")
    val isTheory: Boolean,
    @ColumnInfo(name = "exact_math_answer")
    val exactMathAnswer: String? = null
)
