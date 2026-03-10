package com.gosuraksha.app.security

interface SecureTokenStorage {
    suspend fun setToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
}
