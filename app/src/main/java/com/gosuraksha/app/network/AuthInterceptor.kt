package com.gosuraksha.app.network

import android.content.Context
import com.gosuraksha.app.data.TokenDataStore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()

        val token = runBlocking {
            TokenDataStore.getToken(context).firstOrNull()
        }

        val newRequest = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
