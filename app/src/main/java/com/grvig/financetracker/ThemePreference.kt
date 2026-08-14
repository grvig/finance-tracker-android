package com.grvig.financetracker

import android.content.Context

enum class ThemeMode(val label: String) {
    SYSTEM("Follow system"),
    LIGHT("Light"),
    DARK("Dark")
}

/**
 * Survives process death, unlike [SessionManager], so the app doesn't flash the
 * wrong theme on cold start.
 */
object ThemePreference {

    private const val PREFS_NAME = "finance_tracker_settings"
    private const val KEY_THEME_MODE = "theme_mode"

    fun load(context: Context): ThemeMode {

        val stored = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, null)
            ?: return ThemeMode.SYSTEM

        return try {
            ThemeMode.valueOf(stored)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    fun save(context: Context, mode: ThemeMode) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
    }
}
