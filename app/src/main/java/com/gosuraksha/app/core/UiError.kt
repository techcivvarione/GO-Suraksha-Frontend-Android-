package com.gosuraksha.app.core

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.gosuraksha.app.R

sealed class UiError {
    object Network : UiError()
    object Timeout : UiError()
    object Unauthorized : UiError()
    object Server : UiError()
    object Unknown : UiError()
    data class Custom(val message: String) : UiError()

    // ✅ Context-based message (for ViewModels)
    fun message(context: Context): String {
        return when (this) {
            Network      -> context.getString(R.string.error_network)
            Timeout      -> context.getString(R.string.error_timeout)
            Unauthorized -> context.getString(R.string.error_unauthorized)
            Server       -> context.getString(R.string.error_server)
            Unknown      -> context.getString(R.string.error_generic)
            is Custom    -> message
        }
    }

    // ✅ Composable message (for UI directly)
    @Composable
    fun message(): String {
        val context = LocalContext.current
        return message(context)
    }

    // ✅ Alternative: use stringResource directly in Compose
    @Composable
    fun messageResource(): String {
        return when (this) {
            Network      -> stringResource(R.string.error_network)
            Timeout      -> stringResource(R.string.error_timeout)
            Unauthorized -> stringResource(R.string.error_unauthorized)
            Server       -> stringResource(R.string.error_server)
            Unknown      -> stringResource(R.string.error_generic)
            is Custom    -> message
        }
    }
}