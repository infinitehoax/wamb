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
    val prompt: String
)

data class TutorResponse(
    val reply: String
)
