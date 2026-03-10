package com.gosuraksha.app.data.local

import com.gosuraksha.app.security.SecureTokenStorage

class TokenLocalDataSource(
    private val storage: SecureTokenStorage
) {
    suspend fun saveToken(token: String) {
        storage.setToken(token)
    }

    suspend fun getToken(): String? {
        return storage.getToken()
    }

    suspend fun clearToken() {
        storage.clearToken()
    }
}
