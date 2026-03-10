package com.gosuraksha.app.core.result

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Failure(val error: AppError) : AppResult<Nothing>()
}

sealed class AppError {
    data object Network : AppError()
    data object Timeout : AppError()
    data object Unauthorized : AppError()
    data object Forbidden : AppError()
    data object NotFound : AppError()
    data object Server : AppError()
    data class Validation(val message: String) : AppError()
    data class Unknown(val message: String? = null) : AppError()
}
