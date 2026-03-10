package com.gosuraksha.app.domain.usecase

import com.gosuraksha.app.core.dispatchers.DispatcherProvider
import com.gosuraksha.app.core.result.AppResult
import com.gosuraksha.app.domain.repository.AuthRepository

data class SignupParams(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val confirmPassword: String
)

class SignupUseCase(
    private val repository: AuthRepository,
    dispatchers: DispatcherProvider
) : UseCase<SignupParams, AppResult<Unit>>(dispatchers) {

    override suspend fun execute(params: SignupParams): AppResult<Unit> {
        return repository.signup(
            name = params.name,
            email = params.email,
            phone = params.phone,
            password = params.password,
            confirmPassword = params.confirmPassword
        )
    }
}
