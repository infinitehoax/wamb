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

data class TutorRequest(
    @SerializedName("history") val history: List<TutorStep>
)

data class TutorStep(
    @SerializedName("type") val type: String, // "user_input" or "model_response"
    @SerializedName("content") val content: List<TutorContent>
)

data class TutorContent(
    @SerializedName("type") val type: String = "text",
    @SerializedName("text") val text: String
)

data class TutorChatResponse(
    @SerializedName("reply") val reply: String
)
