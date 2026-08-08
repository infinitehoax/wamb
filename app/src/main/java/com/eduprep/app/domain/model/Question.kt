package com.eduprep.app.domain.model

data class Question(
    val id: Long,
    val subject: String,
    val topic: String,
    val year: String,
    val text: String,
    val optA: String,
    val optB: String,
    val optC: String,
    val optD: String,
    val optE: String? = null,
    val answer: String,
    val explanation: String,
    val isTheory: Boolean,
    val exactMathAnswer: String? = null
)
