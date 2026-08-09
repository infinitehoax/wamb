package com.eduprep.app.data.repository

import com.eduprep.app.data.NetworkTracker
import com.eduprep.app.data.remote.EduPrepBackendService
import com.eduprep.app.data.remote.GradeEssayRequest
import com.eduprep.app.data.remote.GradeEssayResponse
import com.eduprep.app.data.remote.TutorChatRequest
import com.eduprep.app.data.remote.TutorChatResponse
import com.eduprep.app.data.remote.TutorHistoryItem
import com.eduprep.app.domain.repository.BackendRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendRepositoryImpl @Inject constructor(
    private val apiService: EduPrepBackendService,
    private val networkTracker: NetworkTracker
) : BackendRepository {

    override suspend fun submitEssayForGrading(
        question: String,
        markingGuide: String,
        studentAnswer: String
    ): Result<GradeEssayResponse> {
        if (!networkTracker.isCurrentlyConnected()) {
            return Result.failure(IOException("No internet connection available. Please check your network settings."))
        }

        return try {
            val response = apiService.gradeEssay(
                GradeEssayRequest(
                    question = question,
                    markingGuide = markingGuide,
                    studentAnswer = studentAnswer
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendTutorChat(
        history: List<TutorHistoryItem>
    ): Result<TutorChatResponse> {
        if (!networkTracker.isCurrentlyConnected()) {
            return Result.failure(IOException("No internet connection available. Please check your network settings."))
        }

        return try {
            val response = apiService.tutorChat(
                TutorChatRequest(
                    history = history
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
