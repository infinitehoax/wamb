package com.eduprep.app.data.local

import androidx.room.*

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Long): QuestionEntity?

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestions(): List<QuestionEntity>

    @Query("SELECT DISTINCT subject FROM questions")
    suspend fun getDistinctSubjects(): List<String>

    @Query("SELECT DISTINCT topic FROM questions WHERE (:subject = 'All' OR subject = :subject)")
    suspend fun getDistinctTopics(subject: String): List<String>

    @Query("SELECT DISTINCT year FROM questions WHERE (:subject = 'All' OR subject = :subject)")
    suspend fun getDistinctYears(subject: String): List<String>

    @Query("""
        SELECT * FROM questions
        WHERE (:subject = 'All' OR subject = :subject)
          AND (:year = 'All' OR year = :year)
          AND (:topic = 'All' OR topic = :topic)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getRandomQuestions(
        subject: String,
        year: String,
        topic: String,
        limit: Int
    ): List<QuestionEntity>
}
