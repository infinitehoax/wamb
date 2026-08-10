package com.eduprep.app.data.local

import androidx.room.*

@Dao
interface TheoryFeedbackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: TheoryFeedbackEntity)

    @Query("SELECT * FROM theory_feedback WHERE question_id = :questionId")
    suspend fun getFeedbackForQuestion(questionId: Long): TheoryFeedbackEntity?

    @Query("SELECT * FROM theory_feedback")
    suspend fun getAllFeedback(): List<TheoryFeedbackEntity>

    @Query("DELETE FROM theory_feedback WHERE question_id = :questionId")
    suspend fun deleteFeedbackForQuestion(questionId: Long)

    @Query("DELETE FROM theory_feedback")
    suspend fun clearAllFeedback()
}
