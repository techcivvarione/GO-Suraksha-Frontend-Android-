package com.gosuraksha.app.network

import android.content.Context
import com.gosuraksha.app.core.LanguagePrefs
import com.gosuraksha.app.security.EncryptedTokenStorage
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context
) : Interceptor {

    private val tokenStorage = EncryptedTokenStorage(context)

    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()

        // ✅ Get token (your existing logic — preserved)
        val token = runBlocking { tokenStorage.getToken() }

        // ✅ Get current language code (NEW)
        val languageCode = runBlocking {
            LanguagePrefs.getLanguage(context).firstOrNull() ?: "en"
        }

        // ✅ Build request with both Authorization + Accept-Language headers
        val newRequest = originalRequest.newBuilder().apply {
            // Add Authorization header if token exists
            if (!token.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $token")
            }
            // Add Accept-Language header (always sent)
            addHeader("Accept-Language", languageCode)
        }.build()

        return chain.proceed(newRequest)
    }
}
