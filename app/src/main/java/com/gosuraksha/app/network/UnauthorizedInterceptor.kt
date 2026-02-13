package com.gosuraksha.app.network

import okhttp3.Interceptor
import okhttp3.Response

class UnauthorizedInterceptor(
    private val context: android.content.Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val response = chain.proceed(chain.request())

        // 🚫 DO NOTHING on 401 here.
        // Let ViewModels handle logout logic explicitly.
        // Interceptor should NEVER control navigation or session.

        return response
    }
}
