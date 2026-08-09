package com.eduprep.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [QuestionEntity::class, BookmarkEntity::class, TheoryFeedbackEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun theoryFeedbackDao(): TheoryFeedbackDao
}
