package com.gosuraksha.app.core.network

import com.gosuraksha.app.core.result.AppError
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

object NetworkErrorMapper {

    fun map(throwable: Throwable): AppError {
        return when (throwable) {
            is SocketTimeoutException -> AppError.Timeout
            is IOException -> AppError.Network
            is HttpException -> {
                when (throwable.code()) {
                    401 -> AppError.Unauthorized
                    403 -> AppError.Forbidden
                    404 -> AppError.NotFound
                    in 500..599 -> AppError.Server
                    else -> AppError.Unknown("HTTP ${throwable.code()}")
                }
            }
            else -> AppError.Unknown(throwable.message)
        }
    }
}
