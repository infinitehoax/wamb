package com.eduprep.app.data.remote

import com.eduprep.app.BuildConfig
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface EduPrepBackendService {

    @POST("grade_essay")
    @Headers("x-eduprep-app-token: " + BuildConfig.APP_SECRET_TOKEN)
    suspend fun gradeEssay(
        @Body request: GradeEssayRequest
    ): GradeEssayResponse

    @POST("tutor_chat")
    @Headers("x-eduprep-app-token: " + BuildConfig.APP_SECRET_TOKEN)
    suspend fun tutorChat(
        @Body request: TutorChatRequest
    ): TutorChatResponse
}
