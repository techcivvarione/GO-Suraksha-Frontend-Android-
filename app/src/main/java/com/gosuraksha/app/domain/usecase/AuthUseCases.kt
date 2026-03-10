package com.gosuraksha.app.domain.usecase

data class AuthUseCases(
    val login: LoginUseCase,
    val googleLogin: GoogleLoginUseCase,
    val signup: SignupUseCase,
    val restoreSession: RestoreSessionUseCase
)

interface AuthUseCaseProvider {
    fun authUseCases(): AuthUseCases
}
