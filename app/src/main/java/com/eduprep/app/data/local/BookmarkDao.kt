package com.eduprep.app.data.local

import androidx.room.*

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE question_id = :questionId")
    suspend fun deleteBookmark(questionId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE question_id = :questionId)")
    suspend fun isBookmarked(questionId: Long): Boolean

    @Query("""
        SELECT q.* FROM questions q
        INNER JOIN bookmarks b ON q.id = b.question_id
        ORDER BY b.created_at DESC
    """)
    suspend fun getBookmarkedQuestions(): List<QuestionEntity>
}
