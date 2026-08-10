package com.eduprep.app.di

import android.content.Context
import androidx.room.Room
import com.eduprep.app.data.NetworkTracker
import com.eduprep.app.data.local.AppDatabase
import com.eduprep.app.data.local.BookmarkDao
import com.eduprep.app.data.local.QuestionDao
import com.eduprep.app.data.local.TheoryFeedbackDao
import com.eduprep.app.data.local.ChatDao
import com.eduprep.app.data.remote.EduPrepBackendService
import com.eduprep.app.data.repository.BackendRepositoryImpl
import com.eduprep.app.data.repository.QuizRepositoryImpl
import com.eduprep.app.domain.repository.BackendRepository
import com.eduprep.app.domain.repository.QuizRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    fun provideTheoryFeedbackDao(database: AppDatabase): TheoryFeedbackDao {
        return database.theoryFeedbackDao()
    }

    @Provides
    @Singleton
    fun provideChatDao(database: AppDatabase): ChatDao {
        return database.chatDao()
    }

    @Provides
    @Singleton
    fun provideQuizRepository(
        questionDao: QuestionDao,
        bookmarkDao: BookmarkDao
    ): QuizRepository {
        return QuizRepositoryImpl(questionDao, bookmarkDao)
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(com.eduprep.app.BuildConfig.BACKEND_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideEduPrepBackendService(retrofit: Retrofit): EduPrepBackendService {
        return retrofit.create(EduPrepBackendService::class.java)
    }

    @Provides
    @Singleton
    fun provideBackendRepository(
        apiService: EduPrepBackendService,
        networkTracker: NetworkTracker
    ): BackendRepository {
        return BackendRepositoryImpl(apiService, networkTracker)
    }
}
