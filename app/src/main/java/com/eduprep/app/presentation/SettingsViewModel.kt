package com.eduprep.app.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduprep.app.data.local.BookmarkDao
import com.eduprep.app.data.local.ChatDao
import com.eduprep.app.data.local.ThemePreferences
import com.eduprep.app.data.local.TheoryFeedbackDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val chatDao: ChatDao,
    private val theoryFeedbackDao: TheoryFeedbackDao,
    private val bookmarkDao: BookmarkDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val maxChatHistoryFlow: Flow<Int> = ThemePreferences.maxChatHistoryFlow(context)

    fun setMaxChatHistory(limit: Int) {
        viewModelScope.launch {
            ThemePreferences.setMaxChatHistory(context, limit)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatDao.clearHistory()
        }
    }

    fun clearSavedData() {
        viewModelScope.launch {
            theoryFeedbackDao.clearAllFeedback()
            bookmarkDao.clearAllBookmarks()
        }
    }
}
