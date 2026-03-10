package com.gosuraksha.app.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Theme Preferences Manager
 * Handles dark/light mode toggle persistence
 */

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

object ThemePrefs {

    private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")

    /**
     * Get current theme mode
     * Returns: Flow<Boolean> - true for dark mode, false for light mode
     */
    fun isDarkMode(context: Context): Flow<Boolean> {
        return context.themeDataStore.data.map { preferences ->
            preferences[IS_DARK_MODE] ?: false // Default to light mode
        }
    }

    /**
     * Save theme mode
     */
    suspend fun setDarkMode(context: Context, isDark: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }

    /**
     * Toggle between dark and light mode
     */
    suspend fun toggleTheme(context: Context) {
        context.themeDataStore.edit { preferences ->
            val currentMode = preferences[IS_DARK_MODE] ?: false
            preferences[IS_DARK_MODE] = !currentMode
        }
    }
}