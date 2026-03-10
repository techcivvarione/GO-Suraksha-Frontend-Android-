package com.gosuraksha.app

import android.graphics.Color
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.os.LocaleListCompat
import androidx.activity.ComponentActivity
import com.gosuraksha.app.core.LanguagePrefs
import com.gosuraksha.app.core.ThemePrefs
import com.gosuraksha.app.ui.theme.GOSurakshaTheme
import com.gosuraksha.app.ui.theme.LocalThemeState
import com.gosuraksha.app.ui.theme.ThemeState
import com.gosuraksha.app.scan.SharedScanIntentStore
import com.gosuraksha.app.scan.SharedScanPayload
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleShareIntent(intent)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        val savedLanguage = runBlocking { LanguagePrefs.getLanguage(this@MainActivity).first() }
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(savedLanguage)
        )

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val isDark by ThemePrefs.isDarkMode(context)
                .collectAsState(initial = false)

            val themeState = remember(isDark) {
                ThemeState(
                    isDark = isDark,
                    setDark = { value ->
                        scope.launch { ThemePrefs.setDarkMode(context, value) }
                    },
                    toggle = {
                        scope.launch { ThemePrefs.toggleTheme(context) }
                    }
                )
            }

            androidx.compose.runtime.CompositionLocalProvider(
                LocalThemeState provides themeState
            ) {
                GOSurakshaTheme(darkTheme = isDark) {
                    AppRoot()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        val safeIntent = intent ?: return
        if (safeIntent.action != Intent.ACTION_SEND) return
        val stream = safeIntent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java) ?: return
        val mime = safeIntent.type?.lowercase() ?: return
        if (mime.startsWith("image/") || mime.startsWith("video/") || mime.startsWith("audio/")) {
            SharedScanIntentStore.publish(
                SharedScanPayload(uri = stream, mimeType = mime)
            )
        }
    }
}
