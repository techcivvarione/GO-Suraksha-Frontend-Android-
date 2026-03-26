package com.gosuraksha.app.network

import android.content.Context
import android.util.Log
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.security.EncryptedTokenStorage
import okhttp3.Authenticator
import okhttp3.CertificatePinner
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TokenAuthenticator(
    context: Context
) : Authenticator {

    private val tokenStorage = EncryptedTokenStorage(context.applicationContext)

    // Minimal client without auth interceptor or authenticator — avoids infinite loops
    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .certificatePinner(
            CertificatePinner.Builder()
                .add(BACKEND_HOST, "sha256/8OxN2qOE2YnXCf040tnA++ZD+3cDj+Ly/xPyUUpckyo=")
                .add(BACKEND_HOST, "sha256/iFvwVyJSxnQdyaUvUERIf+8qk7gRze3612JMwoO3zdU=")
                .build()
        )
        .build()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null

        val retryCount = response.responseCount()
        if (retryCount >= MAX_AUTH_RETRIES) {
            clearSession("Authentication failed after $retryCount attempts")
            return null
        }

        val currentToken = tokenStorage.getAccessTokenSync()
        if (currentToken.isNullOrBlank()) {
            clearSession("No access token available for refresh")
            return null
        }

        Log.w(TAG, "401 received — attempting token refresh")

        val newToken = tryRefreshToken(currentToken)
        if (newToken == null) {
            clearSession("Token refresh failed — clearing session")
            return null
        }

        tokenStorage.saveTokenSync(newToken)
        SessionManager.updateAccessToken(newToken)
        Log.i(TAG, "Token refreshed successfully")

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    private fun tryRefreshToken(currentToken: String): String? {
        return try {
            val body = "{}".toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("${BASE_URL}auth/refresh")
                .post(body)
                .header("Authorization", "Bearer $currentToken")
                .header("Accept", "application/json")
                .build()

            val response = refreshClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Refresh returned HTTP ${response.code}")
                return null
            }

            val responseBody = response.body?.string() ?: return null
            val json = JSONObject(responseBody)

            // Handle both raw response and { status, data: { access_token } } envelope
            val token = if (json.has("data")) {
                json.getJSONObject("data").optString("access_token", null.toString()).takeIf { it != "null" }
            } else {
                json.optString("access_token", null.toString()).takeIf { it != "null" }
            }
            token
        } catch (e: Exception) {
            Log.e(TAG, "Token refresh threw exception", e)
            null
        }
    }

    private fun clearSession(reason: String) {
        Log.w(TAG, reason)
        SessionManager.clear(reason)
        SessionManager.notifySessionExpired()
    }

    private fun Response.responseCount(): Int {
        var current: Response? = this
        var count = 1
        while (current?.priorResponse != null) {
            count++
            current = current.priorResponse
        }
        return count
    }

    private companion object {
        private const val TAG = "TokenAuthenticator"
        private const val MAX_AUTH_RETRIES = 2
        private const val BASE_URL = "https://api.gosuraksha.in/"
        private const val BACKEND_HOST = "api.gosuraksha.in"
    }
}
