package com.gosuraksha.app.domain.result

sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Failure(val error: DomainError) : DomainResult<Nothing>()
}

sealed class DomainError {
    data object Network : DomainError()
    data object Timeout : DomainError()
    data object Unauthorized : DomainError()
    data object Forbidden : DomainError()
    data object NotFound : DomainError()
    data object Server : DomainError()
    data class Unknown(val message: String? = null) : DomainError()
}
