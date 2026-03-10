package com.gosuraksha.app.domain.usecase

import com.gosuraksha.app.core.dispatchers.DispatcherProvider
import com.gosuraksha.app.core.result.AppResult
import com.gosuraksha.app.domain.model.AuthSession
import com.gosuraksha.app.domain.repository.AuthRepository

class GoogleLoginUseCase(
    private val repository: AuthRepository,
    dispatchers: DispatcherProvider
) : UseCase<String, AppResult<AuthSession>>(dispatchers) {

    override suspend fun execute(params: String): AppResult<AuthSession> {
        return repository.googleLogin(params)
    }
}
