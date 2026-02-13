package com.gosuraksha.app.core

sealed class UiError {

    object Network : UiError()
    object Timeout : UiError()
    object Unauthorized : UiError()
    object Server : UiError()
    object Unknown : UiError()

    fun message(): String {
        return when (this) {
            Network -> "No internet connection"
            Timeout -> "Request timed out"
            Unauthorized -> "Session expired. Please login again."
            Server -> "Server issue. Please try again."
            Unknown -> "Something went wrong"
        }
    }
}
