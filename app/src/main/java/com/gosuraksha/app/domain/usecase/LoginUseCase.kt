package com.gosuraksha.app.domain.usecase

import com.gosuraksha.app.core.dispatchers.DispatcherProvider
import com.gosuraksha.app.core.result.AppResult
import com.gosuraksha.app.domain.model.AuthSession
import com.gosuraksha.app.domain.repository.AuthRepository

data class LoginParams(
    val identifier: String,
    val password: String
)

class LoginUseCase(
    private val repository: AuthRepository,
    dispatchers: DispatcherProvider
) : UseCase<LoginParams, AppResult<AuthSession>>(dispatchers) {

    override suspend fun execute(params: LoginParams): AppResult<AuthSession> {
        return repository.login(params.identifier, params.password)
    }
}
