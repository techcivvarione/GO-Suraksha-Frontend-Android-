package com.gosuraksha.app.network

import retrofit2.HttpException
import java.io.IOException

object ApiErrorHandler {

    fun getErrorMessage(throwable: Throwable): String {

        return when (throwable) {

            is HttpException -> {
                when (throwable.code()) {
                    400 -> "Invalid request"
                    401 -> "Invalid email or password"
                    403 -> "Upgrade required to access this feature"
                    404 -> "Resource not found"
                    422 -> "Validation error"
                    500 -> "Server error. Please try again later"
                    else -> throwable.response()?.errorBody()?.string()
                        ?: "Something went wrong"
                }
            }

            is IOException -> {
                "Network error. Check your internet connection"
            }

            else -> {
                "Unexpected error occurred"
            }
        }
    }
}
