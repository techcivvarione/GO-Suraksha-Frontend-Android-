package com.gosuraksha.app.network

import android.content.Context
import com.gosuraksha.app.core.LanguagePrefs
import com.gosuraksha.app.security.EncryptedTokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context
) : Interceptor {

    private val tokenStorage = EncryptedTokenStorage(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenStorage.getTokenSync()
        val languageCode = LanguagePrefs.getLanguageSync(context)

        val newRequest = originalRequest.newBuilder().apply {
            if (!token.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $token")
            }
            addHeader("Accept-Language", languageCode)
        }.build()

        return chain.proceed(newRequest)
    }
}
