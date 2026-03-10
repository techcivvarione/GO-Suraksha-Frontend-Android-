package com.gosuraksha.app.data.mapper

import com.gosuraksha.app.core.result.AppError
import com.gosuraksha.app.domain.result.DomainError

fun AppError.toDomain(): DomainError {
    return when (this) {
        AppError.Network -> DomainError.Network
        AppError.Timeout -> DomainError.Timeout
        AppError.Unauthorized -> DomainError.Unauthorized
        AppError.Forbidden -> DomainError.Forbidden
        AppError.NotFound -> DomainError.NotFound
        AppError.Server -> DomainError.Server
        is AppError.Validation -> DomainError.Unknown(message)
        is AppError.Unknown -> DomainError.Unknown(message)
    }
}
