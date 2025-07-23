package com.finesi.neuronav.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ThemeManager {
    private const val PREFS_NAME = "neuronav_theme"
    private const val THEME_KEY = "current_theme"

    var currentTheme by mutableStateOf(ThemeType.DARK)
        private set

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedTheme = prefs.getString(THEME_KEY, ThemeType.DARK.name)
        currentTheme = ThemeType.valueOf(savedTheme ?: ThemeType.DARK.name)
    }

    fun setTheme(context: Context, theme: ThemeType) {
        currentTheme = theme
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(THEME_KEY, theme.name).apply()
    }

    fun toggleTheme(context: Context) {
        val newTheme = if (currentTheme == ThemeType.DARK) ThemeType.LIGHT else ThemeType.DARK
        setTheme(context, newTheme)
    }
}

enum class ThemeType {
    DARK, LIGHT
}