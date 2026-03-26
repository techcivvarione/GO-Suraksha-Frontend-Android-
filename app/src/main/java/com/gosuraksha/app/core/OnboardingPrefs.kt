package com.gosuraksha.app.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "onboarding_preferences"
)

object OnboardingPrefs {

    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    private const val PREFS_NAME = "onboarding_preferences_sync"
    private const val PREF_ONBOARDING_COMPLETED = "onboarding_completed"

    fun isCompleted(context: Context): Flow<Boolean> {
        return context.onboardingDataStore.data.map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }
    }

    suspend fun setCompleted(context: Context, completed: Boolean) {
        context.onboardingDataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_ONBOARDING_COMPLETED, completed)
            .apply()
    }

    fun isCompletedSync(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(PREF_ONBOARDING_COMPLETED, false)
    }
}
