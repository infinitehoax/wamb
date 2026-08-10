package com.eduprep.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "eduprep_settings")

object ThemePreferences {
    private const val PREFS_NAME = "eduprep_settings"
    private const val KEY_THEME = "selected_theme"

    val KEY_MAX_CHAT_HISTORY = intPreferencesKey("max_chat_history")

    fun getTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, "System") ?: "System"
    }

    fun setTheme(context: Context, theme: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme).apply()
    }

    fun maxChatHistoryFlow(context: Context): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_MAX_CHAT_HISTORY] ?: 10
        }
    }

    suspend fun setMaxChatHistory(context: Context, limit: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_MAX_CHAT_HISTORY] = limit
        }
    }
}
