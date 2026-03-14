package com.gosuraksha.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gosuraksha.app.R

@Composable
fun localizedUiMessage(raw: String): String {
    return when {
        raw == "error_network" -> stringResource(R.string.error_network)
        raw == "error_timeout" -> stringResource(R.string.error_timeout)
        raw == "error_unauthorized" -> stringResource(R.string.error_unauthorized)
        raw == "error_session_invalid" -> stringResource(R.string.error_session_invalid)
        raw == "error_scan_limit_reached_free" -> stringResource(R.string.error_scan_limit_reached_free)
        raw == "error_scan_limit_reached_pro" -> stringResource(R.string.error_scan_limit_reached_pro)
        raw == "error_forbidden" -> stringResource(R.string.error_forbidden)
        raw == "error_not_found" -> stringResource(R.string.error_not_found)
        raw == "error_server" -> stringResource(R.string.error_server)
        raw == "error_generic" -> stringResource(R.string.error_generic)
        raw == "error_auth_invalid_credentials" -> stringResource(R.string.error_auth_invalid_credentials)
        raw == "error_news_load_failed" -> stringResource(R.string.error_news_load_failed)
        raw == "error_trusted_alert_not_found" -> stringResource(R.string.error_trusted_alert_not_found)
        raw == "error_trusted_contacts_load_failed" -> stringResource(R.string.error_trusted_contacts_load_failed)
        raw == "error_trusted_alerts_load_failed" -> stringResource(R.string.error_trusted_alerts_load_failed)
        raw == "error_qr_invalid_upi" -> stringResource(R.string.qr_error_invalid_upi)
        raw == "error_qr_unable_scan_now" -> stringResource(R.string.qr_error_unable_scan_now)
        raw == "error_qr_weekly_report_limit_reached" -> stringResource(R.string.qr_error_weekly_report_limit_reached)
        raw == "error_qr_weekly_scan_limit_reached" -> stringResource(R.string.qr_error_weekly_scan_limit_reached)
        raw == "error_scan_image_read" -> stringResource(R.string.reality_error_read_image)
        raw == "INVALID_TOKEN" -> stringResource(R.string.error_session_invalid)
        raw == "TOKEN_EXPIRED" -> stringResource(R.string.error_unauthorized)
        raw.equals("Invalid token", ignoreCase = true) -> stringResource(R.string.error_unauthorized)
        raw.equals("Token expired", ignoreCase = true) -> stringResource(R.string.error_unauthorized)
        raw == "profile_updated" -> stringResource(R.string.profile_message_updated)
        raw == "profile_update_failed" -> stringResource(R.string.profile_message_update_failed)
        raw == "security_sessions_logged_out" -> stringResource(R.string.security_message_sessions_logged_out)
        raw == "security_password_changed" -> stringResource(R.string.security_message_password_changed)
        raw.startsWith("security_report_submitted:") -> stringResource(
            R.string.security_message_report_submitted,
            raw.substringAfter(":")
        )
        raw.startsWith("error_cybersos_failed:") -> stringResource(
            R.string.error_cybersos_failed_code,
            raw.substringAfter(":")
        )
        else -> raw
    }
}
