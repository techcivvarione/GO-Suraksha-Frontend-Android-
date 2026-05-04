package com.gosuraksha.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.gosuraksha.app.security.AppLockController
import com.gosuraksha.app.security.AppLockManager
import com.gosuraksha.app.security.LocalAppLockController
import com.gosuraksha.app.security.LocalAppLockEnabled
import com.gosuraksha.app.ui.theme.GOSurakshaTheme
import com.gosuraksha.app.ui.theme.LocalThemeState
import com.gosuraksha.app.ui.theme.ThemeState
import com.gosuraksha.app.ui.security.AppLockBlockingOverlay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var appLockManager: AppLockManager
    private var isAppLocked by mutableStateOf(false)
    private var isAuthPromptShowing by mutableStateOf(false)
    private var lockMessage by mutableStateOf<String?>(null)
    private var pendingEnableRequest = false
    private var hasRenderedContent = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (BuildConfig.DEBUG) {
            Log.d("GO_SURAKSHA_PUSH", "POST_NOTIFICATIONS granted=$granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appLockManager = AppLockManager.getInstance(this)
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
        Log.d("APP_VERSION", "MainActivity launched version=${BuildConfig.VERSION_NAME} code=${BuildConfig.VERSION_CODE}")
        isAppLocked = shouldRequireAppLock()

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val isDark by ThemePrefs.isDarkMode(context)
                .collectAsStateWithLifecycle(initialValue = false)
            val appLockEnabled by appLockManager.isAppLockEnabled
                .collectAsStateWithLifecycle(initialValue = appLockManager.isEnabled())

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

            val appLockController = remember {
                object : AppLockController {
                    override fun requestEnable() {
                        enableAppLock()
                    }

                    override fun disable() {
                        disableAppLock()
                    }
                }
            }

            androidx.compose.runtime.CompositionLocalProvider(
                LocalThemeState provides themeState,
                ColorTokens.LocalAppDarkMode provides isDark,
                LocalAppLockController provides appLockController,
                LocalAppLockEnabled provides appLockEnabled
            ) {
                GOSurakshaTheme(darkTheme = isDark) {
                    AppRoot()
                    if (isAppLocked) {
                        AppLockBlockingOverlay(
                            message = lockMessage,
                            isPromptShowing = isAuthPromptShowing,
                            onUnlockClick = { showAppLockPrompt() },
                        )
                    }
                }
            }
        }
        hasRenderedContent = true
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
        handleScamNavigationIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (shouldRequireAppLock()) {
            isAppLocked = true
            if (hasRenderedContent) {
                showAppLockPrompt()
            }
        }
    }

    private fun shouldRequireAppLock(): Boolean {
        return appLockManager.isEnabled() && appLockManager.isSessionExpired()
    }

    private fun enableAppLock() {
        when (canAuthenticateForAppLock()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                pendingEnableRequest = true
                isAppLocked = true
                lockMessage = null
                showAppLockPrompt()
            }
            else -> {
                pendingEnableRequest = false
                lockMessage = "Please set up fingerprint or device lock in settings"
                showToast(lockMessage ?: return)
            }
        }
    }

    private fun disableAppLock() {
        pendingEnableRequest = false
        isAuthPromptShowing = false
        isAppLocked = false
        lockMessage = null
        appLockManager.setAppLockEnabled(false)
        showToast("App Lock disabled")
    }

    private fun canAuthenticateForAppLock(): Int {
        return BiometricManager.from(this)
            .canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
    }

    private fun showAppLockPrompt() {
        if (isAuthPromptShowing) return
        when (canAuthenticateForAppLock()) {
            BiometricManager.BIOMETRIC_SUCCESS -> Unit
            else -> {
                isAppLocked = true
                isAuthPromptShowing = false
                pendingEnableRequest = false
                lockMessage = "Please set up fingerprint or device lock in settings"
                showToast(lockMessage ?: return)
                return
            }
        }

        isAuthPromptShowing = true
        lockMessage = null
        val executor = ContextCompat.getMainExecutor(this)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                isAuthPromptShowing = false
                pendingEnableRequest = false
                appLockManager.setAppLockEnabled(true)
                appLockManager.markUnlocked()
                isAppLocked = false
                lockMessage = null
            }

            override fun onAuthenticationFailed() {
                isAppLocked = true
                lockMessage = "Authentication failed. Please try again."
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                isAuthPromptShowing = false
                if (pendingEnableRequest) {
                    pendingEnableRequest = false
                    appLockManager.setAppLockEnabled(false)
                    appLockManager.clearLastUnlockTime()
                    lockMessage = "App Lock was not enabled"
                    isAppLocked = false
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_CANCELED
                    ) {
                        showToast(lockMessage ?: return)
                    }
                    return
                }

                isAppLocked = true
                lockMessage = when (errorCode) {
                    BiometricPrompt.ERROR_NO_BIOMETRICS,
                    BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> "Please set up fingerprint or device lock in settings"
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> "Unlock required to continue using the app"
                    else -> errString.toString()
                }
            }
        }

        BiometricPrompt(this, executor, callback).authenticate(buildPromptInfo())
    }

    private fun buildPromptInfo(): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock GO Suraksha")
            .setSubtitle("Use your fingerprint, face, or device lock to continue")

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL).build()
        } else {
            builder.setDeviceCredentialAllowed(true).build()
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
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
