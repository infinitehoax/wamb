package com.eduprep.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduprep.app.data.local.BookmarkDao
import com.eduprep.app.data.local.ChatDao
import com.eduprep.app.data.local.TheoryFeedbackDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val chatDao: ChatDao,
    private val theoryFeedbackDao: TheoryFeedbackDao,
    private val bookmarkDao: BookmarkDao
) : ViewModel() {

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
