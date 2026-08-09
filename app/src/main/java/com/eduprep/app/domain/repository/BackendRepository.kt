package com.eduprep.app.domain.repository

import com.eduprep.app.data.remote.GradeEssayResponse
import com.eduprep.app.data.remote.TutorChatResponse
import com.eduprep.app.data.remote.TutorHistoryItem

interface BackendRepository {
    suspend fun submitEssayForGrading(
        question: String,
        markingGuide: String,
        studentAnswer: String
    ): Result<GradeEssayResponse>

    suspend fun sendTutorChat(
        history: List<TutorHistoryItem>
    ): Result<TutorChatResponse>
}
