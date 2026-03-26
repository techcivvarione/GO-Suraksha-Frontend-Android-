package com.gosuraksha.app.network

import android.content.Context
import android.util.Log
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.core.LanguagePrefs
import com.gosuraksha.app.security.EncryptedTokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context
) : Interceptor {

    private val tokenStorage = EncryptedTokenStorage(context.applicationContext)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val skipAuthPaths = listOf(
            "/auth/login",
            "/auth/google",
            "/auth/signup",
            "/auth/send-email-otp",
            "/auth/verify-email-otp",
            "/auth/refresh"
        )

        if (skipAuthPaths.any { path.startsWith(it) }) {
            if (BuildConfig.DEBUG) {
                Log.d("AuthInterceptor", "Skipping auth header")
            }
            return chain.proceed(request)
        }

        val languageCode = LanguagePrefs.getLanguageSync(context)
        val token = tokenStorage.getAccessTokenSync()
        val isPhoneOtpPath = path.startsWith("/auth/send-phone-otp") || path.startsWith("/auth/verify-phone-otp")

        val newRequest = request.newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
                if (BuildConfig.DEBUG) {
                    Log.d("AuthInterceptor", "Injecting bearer token")
                }
            }
            header("Accept-Language", languageCode)
        }.build()

        if (BuildConfig.DEBUG && isPhoneOtpPath) {
            Log.d(
                "AUTH_FLOW",
                "Interceptor phone OTP tokenPresent=${!token.isNullOrBlank()} authHeaderPresent=${!newRequest.header("Authorization").isNullOrBlank()}"
            )
        }

        return chain.proceed(newRequest)
    }
}
