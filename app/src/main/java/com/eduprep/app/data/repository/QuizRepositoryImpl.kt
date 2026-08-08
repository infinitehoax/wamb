package com.eduprep.app.data.repository

import com.eduprep.app.data.local.BookmarkDao
import com.eduprep.app.data.local.BookmarkEntity
import com.eduprep.app.data.local.QuestionDao
import com.eduprep.app.data.mapper.toDomain
import com.eduprep.app.data.mapper.toEntity
import com.eduprep.app.domain.model.Question
import com.eduprep.app.domain.repository.QuizRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val questionDao: QuestionDao,
    private val bookmarkDao: BookmarkDao
) : QuizRepository {

    override suspend fun getDistinctSubjects(): List<String> {
        return questionDao.getDistinctSubjects()
    }

    override suspend fun getDistinctTopics(subject: String): List<String> {
        return questionDao.getDistinctTopics(subject)
    }

    override suspend fun getDistinctYears(subject: String): List<String> {
        return questionDao.getDistinctYears(subject)
    }

    override suspend fun getRandomQuestions(
        subject: String,
        year: String,
        topic: String,
        limit: Int
    ): List<Question> {
        return questionDao.getRandomQuestions(subject, year, topic, limit).map { it.toDomain() }
    }

    override suspend fun insertQuestions(questions: List<Question>) {
        questionDao.insertQuestions(questions.map { it.toEntity() })
    }

    override suspend fun insertBookmark(questionId: Long) {
        bookmarkDao.insertBookmark(BookmarkEntity(questionId = questionId))
    }

    override suspend fun deleteBookmark(questionId: Long) {
        bookmarkDao.deleteBookmark(questionId)
    }

    override suspend fun isBookmarked(questionId: Long): Boolean {
        return bookmarkDao.isBookmarked(questionId)
    }

    override suspend fun getBookmarkedQuestions(): List<Question> {
        return bookmarkDao.getBookmarkedQuestions().map { it.toDomain() }
    }
}
