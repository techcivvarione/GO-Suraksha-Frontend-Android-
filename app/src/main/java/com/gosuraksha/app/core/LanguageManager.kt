package com.gosuraksha.app.core

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.languageDataStore by preferencesDataStore(name = "language_prefs")

object LanguagePrefs {
    private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
    private val HAS_SELECTED_LANGUAGE_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("has_selected_language")
    private const val DEFAULT_LANGUAGE = "en"
    private const val PREFS_NAME = "language_prefs_sync"
    private const val PREF_LANGUAGE = "selected_language"
    private const val PREF_HAS_SELECTED = "has_selected_language"

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
        syncPrefs(context).edit()
            .putString(PREF_LANGUAGE, code)
            .putBoolean(PREF_HAS_SELECTED, true)
            .apply()
    }

    fun getLanguageSync(context: Context): String =
        syncPrefs(context).getString(PREF_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE

    fun hasSelectedLanguageSync(context: Context): Boolean =
        syncPrefs(context).getBoolean(PREF_HAS_SELECTED, false) ||
            syncPrefs(context).contains(PREF_LANGUAGE)

    private fun syncPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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
    val currentLanguage by LanguagePrefs.getLanguage(context)
        .collectAsStateWithLifecycle(initialValue = "en")

    LaunchedEffect(currentLanguage) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(currentLanguage)
        )
        // On Android 12 and below, setApplicationLocales does not automatically
        // recreate the activity because AppCompatDelegate per-app language API
        // relies on the OS recreation hook only available on API 33+.
        // We recreate manually to ensure every string resource refreshes.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            (context as? Activity)?.recreate()
        }
    }

    CompositionLocalProvider(LocalLanguage provides currentLanguage) {
        content()
    }
}

/**
 * Save [languageCode] to DataStore and immediately apply the new locale.
 * On API < 33 the calling activity is recreated so all string resources refresh.
 */
suspend fun changeLanguage(context: Context, languageCode: String) {
    LanguagePrefs.saveLanguage(context, languageCode)
    AppCompatDelegate.setApplicationLocales(
        LocaleListCompat.forLanguageTags(languageCode)
    )
    // Recreate on pre-API-33 so resources load in the new locale.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        (context as? Activity)?.recreate()
    }
}

@Composable
fun getCurrentLanguage(): Language {
    val code = LocalLanguage.current
    return SUPPORTED_LANGUAGES.find { it.code == code } ?: SUPPORTED_LANGUAGES.first()
}

