package com.gosuraksha.app.core

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.languageDataStore by preferencesDataStore(name = "language_prefs")

object LanguagePrefs {
    private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
    private val HAS_SELECTED_LANGUAGE_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("has_selected_language")
    private const val DEFAULT_LANGUAGE = "en"

    fun getLanguage(context: Context): Flow<String> {
        return context.languageDataStore.data.map { prefs ->
            prefs[LANGUAGE_KEY] ?: DEFAULT_LANGUAGE
        }
    }

    fun hasSelectedLanguage(context: Context): Flow<Boolean> {
        return context.languageDataStore.data.map { prefs ->
            prefs[HAS_SELECTED_LANGUAGE_KEY] ?: prefs.contains(LANGUAGE_KEY)
        }
    }

    suspend fun saveLanguage(context: Context, code: String) {
        context.languageDataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = code
            prefs[HAS_SELECTED_LANGUAGE_KEY] = true
        }
    }
}

data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String
)

val SUPPORTED_LANGUAGES = listOf(
    Language("en", "English",   "English",   "????"),
    Language("hi", "Hindi",     "?????",    "????"),
    Language("te", "Telugu",    "??????",    "????"),
    Language("kn", "Kannada",   "?????",     "????"),
    Language("ta", "Tamil",     "?????",     "????"),
    Language("bn", "Bengali",   "?????",     "????"),
    Language("mr", "Marathi",   "?????",     "????")
)

val LocalLanguage = androidx.compose.runtime.compositionLocalOf<String> { "en" }

@Composable
fun LanguageManager(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val currentLanguage by LanguagePrefs.getLanguage(context).collectAsState(initial = "en")

    androidx.compose.runtime.LaunchedEffect(currentLanguage) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(currentLanguage)
        )
    }

    CompositionLocalProvider(LocalLanguage provides currentLanguage) {
        content()
    }
}

suspend fun changeLanguage(context: Context, languageCode: String) {
    LanguagePrefs.saveLanguage(context, languageCode)
    AppCompatDelegate.setApplicationLocales(
        LocaleListCompat.forLanguageTags(languageCode)
    )
}

@Composable
fun getCurrentLanguage(): Language {
    val code = LocalLanguage.current
    return SUPPORTED_LANGUAGES.find { it.code == code } ?: SUPPORTED_LANGUAGES.first()
}
