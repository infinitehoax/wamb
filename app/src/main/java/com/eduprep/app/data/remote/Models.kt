package com.eduprep.app.data.remote

import com.google.gson.annotations.SerializedName

data class GradeEssayRequest(
    @SerializedName("question") val question: String,
    @SerializedName("marking_guide") val markingGuide: String,
    @SerializedName("student_answer") val studentAnswer: String
)

data class GradeEssayResponse(
    @SerializedName("score") val score: Int,
    @SerializedName("feedback") val feedback: String,
    @SerializedName("missing_keywords") val missingKeywords: List<String> = emptyList()
)

data class TutorHistoryItem(
    @SerializedName("type") val type: String, // "user_input" or "ai_reply" or "model"
    @SerializedName("content") val content: String
)

data class TutorChatRequest(
    @SerializedName("history") val history: List<TutorHistoryItem>
)

data class TutorChatResponse(
    @SerializedName("reply") val reply: String
)
