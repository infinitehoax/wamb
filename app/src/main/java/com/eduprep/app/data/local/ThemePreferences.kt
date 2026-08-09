package com.eduprep.app.data.local

import android.content.Context

object ThemePreferences {
    private const val PREFS_NAME = "eduprep_settings"
    private const val KEY_THEME = "selected_theme"

    fun getTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, "System") ?: "System"
    }

    fun setTheme(context: Context, theme: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme).apply()
    }
}
