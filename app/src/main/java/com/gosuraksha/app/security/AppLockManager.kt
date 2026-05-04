package com.gosuraksha.app.security

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AppLockController {
    fun requestEnable()
    fun disable()
}

val LocalAppLockController = staticCompositionLocalOf<AppLockController?> { null }
val LocalAppLockEnabled = staticCompositionLocalOf { false }

class AppLockManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val enabledFlow = MutableStateFlow(prefs.getBoolean(KEY_APP_LOCK_ENABLED, false))
    private val lastUnlockFlow = MutableStateFlow(prefs.getLong(KEY_LAST_UNLOCK_TIME, 0L))

    val isAppLockEnabled: StateFlow<Boolean> = enabledFlow.asStateFlow()
    val lastUnlockTime: StateFlow<Long> = lastUnlockFlow.asStateFlow()

    fun isEnabled(): Boolean = enabledFlow.value

    fun getLastUnlockTime(): Long = lastUnlockFlow.value

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
        enabledFlow.value = enabled
        if (!enabled) {
            clearLastUnlockTime()
        }
    }

    fun markUnlocked(now: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_LAST_UNLOCK_TIME, now).apply()
        lastUnlockFlow.value = now
    }

    fun clearLastUnlockTime() {
        prefs.edit().putLong(KEY_LAST_UNLOCK_TIME, 0L).apply()
        lastUnlockFlow.value = 0L
    }

    fun isSessionExpired(
        now: Long = System.currentTimeMillis(),
        timeoutMillis: Long = APP_LOCK_TIMEOUT_MILLIS,
    ): Boolean {
        val lastUnlock = lastUnlockFlow.value
        if (lastUnlock <= 0L) return true
        return now - lastUnlock > timeoutMillis
    }

    companion object {
        private const val PREFS_NAME = "app_lock_prefs"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_LAST_UNLOCK_TIME = "last_unlock_time"
        const val APP_LOCK_TIMEOUT_MILLIS = 30_000L

        @Volatile
        private var instance: AppLockManager? = null

        fun getInstance(context: Context): AppLockManager {
            return instance ?: synchronized(this) {
                instance ?: AppLockManager(context).also { instance = it }
            }
        }
    }
}
