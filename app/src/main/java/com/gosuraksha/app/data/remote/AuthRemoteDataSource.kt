package com.gosuraksha.app.data.remote

import com.gosuraksha.app.data.remote.dto.auth.LoginRequest
import com.gosuraksha.app.data.remote.dto.auth.GoogleAuthRequest
import com.gosuraksha.app.data.remote.dto.auth.SignupRequest
import com.gosuraksha.app.network.AuthApi

class AuthRemoteDataSource(
    private val api: AuthApi
) {
    suspend fun login(request: LoginRequest) = api.login(request)
    suspend fun googleLogin(request: GoogleAuthRequest) = api.googleLogin(request)
    suspend fun signup(request: SignupRequest) = api.signup(request)
    suspend fun getMe() = api.getMe()
}
