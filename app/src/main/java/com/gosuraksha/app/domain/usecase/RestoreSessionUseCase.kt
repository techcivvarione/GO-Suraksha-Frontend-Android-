package com.gosuraksha.app.domain.usecase

import com.gosuraksha.app.core.dispatchers.DispatcherProvider
import com.gosuraksha.app.core.result.AppResult
import com.gosuraksha.app.domain.model.AuthSession
import com.gosuraksha.app.domain.repository.AuthRepository
import com.gosuraksha.app.domain.repository.SessionRepository

class RestoreSessionUseCase(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    dispatchers: DispatcherProvider
) : UseCase<Unit, AppResult<AuthSession>>(dispatchers) {

    override suspend fun execute(params: Unit): AppResult<AuthSession> {
        val tokenResult = sessionRepository.getToken()
        val token = when (tokenResult) {
            is AppResult.Success -> tokenResult.data
            is AppResult.Failure -> return tokenResult
        }

        if (token.isNullOrBlank()) {
            return AppResult.Failure(com.gosuraksha.app.core.result.AppError.Unauthorized)
        }

        return authRepository.getMe()
    }
}
