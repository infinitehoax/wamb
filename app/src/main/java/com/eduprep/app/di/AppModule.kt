package com.eduprep.app.di

import android.content.Context
import androidx.room.Room
import com.eduprep.app.data.local.AppDatabase
import com.eduprep.app.data.local.BookmarkDao
import com.eduprep.app.data.local.QuestionDao
import com.eduprep.app.data.repository.QuizRepositoryImpl
import com.eduprep.app.domain.repository.QuizRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "eduprep_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideQuestionDao(database: AppDatabase): QuestionDao {
        return database.questionDao()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(database: AppDatabase): BookmarkDao {
        return database.bookmarkDao()
    }

    @Provides
    @Singleton
    fun provideQuizRepository(
        questionDao: QuestionDao,
        bookmarkDao: BookmarkDao
    ): QuizRepository {
        return QuizRepositoryImpl(questionDao, bookmarkDao)
    }
}
