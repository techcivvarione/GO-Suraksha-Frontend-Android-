package com.gosuraksha.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.gosuraksha.app.BuildConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.gosuraksha.app.core.LanguagePrefs
import com.gosuraksha.app.core.ThemePrefs
import com.gosuraksha.app.data.remote.dto.auth.RegisterDeviceRequest
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.scan.SharedScanIntentStore
import com.gosuraksha.app.scan.SharedScanPayload
import com.gosuraksha.app.scam.ScamAlertNavigationStore
import com.gosuraksha.app.ui.theme.GOSurakshaTheme
import com.gosuraksha.app.ui.theme.LocalThemeState
import com.gosuraksha.app.ui.theme.ThemeState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (BuildConfig.DEBUG) {
            Log.d("GO_SURAKSHA_PUSH", "POST_NOTIFICATIONS granted=$granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleShareIntent(intent)
        handleScamNavigationIntent(intent)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        val savedLanguage = LanguagePrefs.getLanguageSync(this)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(savedLanguage)
        )

        requestNotificationPermissionIfNeeded()
        fetchAndRegisterDeviceToken()

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val isDark by ThemePrefs.isDarkMode(context)
                .collectAsStateWithLifecycle(initialValue = false)

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
                LocalThemeState provides themeState,
                ColorTokens.LocalAppDarkMode provides isDark
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
        handleScamNavigationIntent(intent)
    }

    override fun onResume() {
        super.onResume()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun fetchAndRegisterDeviceToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("GO_SURAKSHA_PUSH", "Failed to fetch device token", task.exception)
                    return@addOnCompleteListener
                }

                registerDeviceToken(task.result)
            }
    }

    private fun registerDeviceToken(token: String) {
        lifecycleScope.launch {
            runCatching {
                ApiClient.authApi.registerDevice(
                    RegisterDeviceRequest(
                        device_token = token,
                        device_type = "android"
                    )
                )
            }.onFailure { error ->
                Log.e("GO_SURAKSHA_PUSH", "Failed to register device token", error)
            }
        }
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

    private fun handleScamNavigationIntent(intent: Intent?) {
        val safeIntent = intent ?: return
        val route = safeIntent.getStringExtra(ScamAlertNavigationStore.EXTRA_SCAM_ROUTE) ?: return
        val alertId = safeIntent.getStringExtra(ScamAlertNavigationStore.EXTRA_SCAM_ALERT_ID)
        ScamAlertNavigationStore.publish(route = route, alertId = alertId)
    }

}

