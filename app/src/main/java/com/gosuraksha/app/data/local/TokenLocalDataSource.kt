package com.gosuraksha.app.data.local

import android.util.Log
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.security.SecureTokenStorage

class TokenLocalDataSource(
    private val storage: SecureTokenStorage
) {
    suspend fun saveToken(token: String) {
        if (token.isBlank()) {
            SessionManager.updateAccessToken(null)
            return
        }
        storage.setToken(token)
        SessionManager.updateAccessToken(token)
        if (BuildConfig.DEBUG) {
            Log.d("AUTH_FLOW", "Token persisted")
        }
    }

    suspend fun getToken(): String? {
        return storage.getToken()
    }

    suspend fun clearToken() {
        storage.clearToken()
        SessionManager.updateAccessToken(null)
    }
}
