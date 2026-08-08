package com.eduprep.app.domain.repository

import com.eduprep.app.domain.model.Question

interface QuizRepository {
    suspend fun getDistinctSubjects(): List<String>
    suspend fun getDistinctTopics(subject: String): List<String>
    suspend fun getDistinctYears(subject: String): List<String>
    suspend fun getRandomQuestions(
        subject: String,
        year: String,
        topic: String,
        limit: Int
    ): List<Question>
    suspend fun insertQuestions(questions: List<Question>)
    suspend fun insertBookmark(questionId: Long)
    suspend fun deleteBookmark(questionId: Long)
    suspend fun isBookmarked(questionId: Long): Boolean
    suspend fun getBookmarkedQuestions(): List<Question>
}
