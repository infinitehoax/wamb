package com.eduprep.app.domain.repository

import com.eduprep.app.data.remote.GradeEssayResponse
import com.eduprep.app.data.remote.TutorRequest
import com.eduprep.app.data.remote.TutorResponse

interface BackendRepository {
    suspend fun submitEssayForGrading(
        question: String,
        markingGuide: String,
        studentAnswer: String
    ): Result<GradeEssayResponse>

    suspend fun sendTutorMessage(
        request: TutorRequest
    ): Result<TutorResponse>
}
