package com.gosuraksha.app.core

import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException

object ErrorMapper {

    fun map(throwable: Throwable): UiError {

        return when (throwable) {

            is SocketTimeoutException -> UiError.Timeout

            is IOException -> UiError.Network

            is HttpException -> {
                when (throwable.code()) {
                    401 -> UiError.Unauthorized
                    in 500..599 -> UiError.Server
                    else -> UiError.Unknown
                }
            }

            else -> UiError.Unknown
        }
    }
}
