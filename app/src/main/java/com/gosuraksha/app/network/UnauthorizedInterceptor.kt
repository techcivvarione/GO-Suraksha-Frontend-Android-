package com.gosuraksha.app.network

import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.security.EncryptedTokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class UnauthorizedInterceptor(
    private val context: android.content.Context
) : Interceptor {

    private val tokenStorage by lazy { EncryptedTokenStorage(context) }

    override fun intercept(chain: Interceptor.Chain): Response {

        val response = chain.proceed(chain.request())
        val authError = StructuredApiErrorParser.parseAuthError(
            response.peekBody(Long.MAX_VALUE).string()
        )

        if (authError?.code == AuthErrorCode.TOKEN_EXPIRED) {
            tokenStorage.clearTokenSync()
            SessionManager.clear()
            SessionManager.notifySessionExpired()
        }

        return response
    }
}
