package com.gosuraksha.app.data.remote

import android.util.Log
import com.google.gson.Gson
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.data.remote.dto.auth.DeleteAccountRequest
import com.gosuraksha.app.data.remote.dto.auth.GoogleAuthRequest
import com.gosuraksha.app.data.remote.dto.auth.LoginRequest
import com.gosuraksha.app.data.remote.dto.auth.SignupRequest
import com.gosuraksha.app.data.remote.dto.auth.UserResponse
import com.gosuraksha.app.network.AuthApi

class AuthRemoteDataSource(
    private val api: AuthApi
) {
    suspend fun login(request: LoginRequest) = api.login(request)
    suspend fun googleLogin(request: GoogleAuthRequest) = api.googleLoginV2(request)
    suspend fun signup(request: SignupRequest) = api.signup(request)
    suspend fun deleteAccount(request: DeleteAccountRequest) = api.deleteAccount(request)

    suspend fun getMe(): UserResponse {
        val response = api.getMe()
        if (BuildConfig.DEBUG) {
            Log.d("AUTH_FLOW", "API response received")
            Log.d("AUTH_FLOW", "Full /auth/me response = ${Gson().toJson(response)}")
            Log.d("AUTH_FLOW", "Envelope status = ${response.status}")
        }
        return response.data
    }
}
