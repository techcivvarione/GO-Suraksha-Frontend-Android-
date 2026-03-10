package com.gosuraksha.app.domain.usecase

import com.gosuraksha.app.core.dispatchers.DispatcherProvider
import kotlinx.coroutines.withContext

abstract class UseCase<in P, R>(
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(params: P): R = withContext(dispatchers.io) {
        execute(params)
    }

    protected abstract suspend fun execute(params: P): R
}
