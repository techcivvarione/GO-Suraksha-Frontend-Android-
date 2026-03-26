package com.gosuraksha.app.core.network

import android.util.Log
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.core.result.AppError
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

object NetworkErrorMapper {

    fun map(throwable: Throwable): AppError {
        return when (throwable) {
            is SocketTimeoutException -> AppError.Timeout
            is IOException -> AppError.Network
            is HttpException -> mapHttpException(throwable)
            else -> AppError.Unknown(throwable.message)
        }
    }

    private fun mapHttpException(throwable: HttpException): AppError {
        val errorBody = runCatching {
            throwable.response()?.errorBody()?.string()
        }.getOrNull()

        if (BuildConfig.DEBUG && !errorBody.isNullOrBlank()) {
            Log.e("DELETE_ACCOUNT", errorBody)
        }

        val detail = errorBody
            ?.takeIf { it.isNotBlank() }
            ?.let {
                runCatching {
                    JSONObject(it).optString("detail").takeIf(String::isNotBlank)
                }.getOrNull()
            }

        if (!detail.isNullOrBlank()) {
            return AppError.Validation(detail)
        }

        return when (throwable.code()) {
            401 -> AppError.Validation("Session expired. Please login again")
            409 -> AppError.Validation("This phone number is already linked to another account")
            403 -> AppError.Forbidden
            404 -> AppError.NotFound
            in 500..599 -> AppError.Server
            else -> AppError.Validation("Something went wrong. Please try again")
        }
    }
}
