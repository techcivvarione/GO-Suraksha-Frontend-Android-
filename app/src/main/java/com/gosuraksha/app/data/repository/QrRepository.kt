package com.gosuraksha.app.data.repository

import com.google.gson.Gson
import com.gosuraksha.app.network.ApiError
import com.gosuraksha.app.network.QrAnalyzeRequest
import com.gosuraksha.app.network.QrAnalyzeResponse
import com.gosuraksha.app.network.QrApi
import java.io.IOException
import java.net.SocketTimeoutException

enum class QrErrorType {
    BAD_REQUEST,
    RATE_LIMIT,
    SERVER,
    NETWORK,
    UNKNOWN
}

sealed class QrRepoResult<out T> {
    data class Success<T>(val data: T) : QrRepoResult<T>()
    data class Error(val type: QrErrorType, val message: String) : QrRepoResult<Nothing>()
}

class QrRepository(
    private val api: QrApi
) {
    suspend fun analyzeQr(rawPayload: String): QrRepoResult<QrAnalyzeResponse> {
        return try {
            val response = api.analyzeQr(QrAnalyzeRequest(raw_payload = rawPayload))

            if (!response.isSuccessful) {
                val apiError = parseApiError(response.errorBody()?.string())
                val message = apiError?.detail?.ifBlank { null } ?: "error_server"

                return when (response.code()) {
                    400 -> QrRepoResult.Error(QrErrorType.BAD_REQUEST, message)
                    429 -> QrRepoResult.Error(QrErrorType.RATE_LIMIT, message)
                    500 -> QrRepoResult.Error(QrErrorType.SERVER, message)
                    else -> QrRepoResult.Error(QrErrorType.SERVER, message)
                }
            }

            val body = response.body()
            if (body == null) {
                QrRepoResult.Error(QrErrorType.SERVER, "error_server")
            } else {
                QrRepoResult.Success(body)
            }
        } catch (_: SocketTimeoutException) {
            QrRepoResult.Error(QrErrorType.NETWORK, "error_timeout")
        } catch (_: IOException) {
            QrRepoResult.Error(QrErrorType.NETWORK, "error_network")
        } catch (_: Exception) {
            QrRepoResult.Error(QrErrorType.UNKNOWN, "error_generic")
        }
    }

    private fun parseApiError(raw: String?): ApiError? {
        if (raw.isNullOrBlank()) return null
        return try {
            Gson().fromJson(raw, ApiError::class.java)
        } catch (_: Exception) {
            null
        }
    }
}

