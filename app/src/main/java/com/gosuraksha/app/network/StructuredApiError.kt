package com.gosuraksha.app.network

import com.google.gson.Gson

data class StructuredApiError(
    val success: Boolean? = null,
    val error: String? = null,
    val message: String? = null,
    val detail: String? = null
)

enum class AuthErrorCode {
    TOKEN_EXPIRED,
    INVALID_TOKEN
}

data class ParsedAuthError(
    val code: AuthErrorCode,
    val messageKey: String
)

object StructuredApiErrorParser {
    private val gson = Gson()

    fun parse(raw: String?): StructuredApiError? {
        if (raw.isNullOrBlank()) return null
        return try {
            gson.fromJson(raw, StructuredApiError::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun parseAuthError(raw: String?): ParsedAuthError? {
        val parsed = parse(raw) ?: return null
        return when (parsed.error?.uppercase()) {
            "TOKEN_EXPIRED" -> ParsedAuthError(AuthErrorCode.TOKEN_EXPIRED, "error_unauthorized")
            "INVALID_TOKEN" -> ParsedAuthError(AuthErrorCode.INVALID_TOKEN, "error_session_invalid")
            else -> null
        }
    }

    fun parseErrorCode(raw: String?): String? {
        return parse(raw)?.error?.uppercase()
    }
}
