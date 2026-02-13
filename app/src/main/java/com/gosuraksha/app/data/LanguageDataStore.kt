package com.gosuraksha.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

object LanguageDataStore {

    private val LANGUAGE_KEY = stringPreferencesKey("selected_language")

    fun getSelectedLanguage(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY]
        }
    }

    suspend fun saveLanguage(context: Context, languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }
}
