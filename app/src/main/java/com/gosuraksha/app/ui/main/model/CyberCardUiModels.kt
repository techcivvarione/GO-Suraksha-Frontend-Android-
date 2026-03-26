package com.gosuraksha.app.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// ── Signals (legacy — kept for backward-compat with V1 clients) ───────────────

data class CyberSignals(
    val emailScanCount: Int,
    val passwordScanCount: Int,
    val scanRewardPoints: Int,
    val ocrBonus: Int,
    val scamReports: Int,
    val lockReason: String?
)

fun Map<String, Any?>?.toCyberSignals() = CyberSignals(
    emailScanCount    = this.intValue("email_scan_count"),
    passwordScanCount = this.intValue("password_scan_count"),
    scanRewardPoints  = this.intValue("scan_reward_points"),
    ocrBonus          = this.intValue("ocr_bonus"),
    scamReports       = this.intValue("scam_reports"),
    lockReason        = this.stringValue("lock_reason")
)

// ── V2 Action ────────────────────────────────────────────────────────────────

data class CyberAction(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val priority: Int = 9
)

fun List<Map<String, Any?>>?.toCyberActions(): List<CyberAction> =
    this?.mapNotNull { m ->
        val id    = m["id"]?.toString()    ?: return@mapNotNull null
        val title = m["title"]?.toString() ?: return@mapNotNull null
        CyberAction(
            id       = id,
            title    = title,
            subtitle = m["subtitle"]?.toString() ?: "",
            icon     = m["icon"]?.toString()     ?: "",
            priority = (m["priority"] as? Number)?.toInt() ?: 9
        )
    } ?: emptyList()

fun actionIcon(iconName: String): ImageVector = when (iconName) {
    "lock"           -> Icons.Outlined.Lock
    "phone"          -> Icons.Outlined.Phone
    "people"         -> Icons.Outlined.People
    "qr_code_scanner"-> Icons.Outlined.QrCodeScanner
    "email"          -> Icons.Outlined.Email
    "refresh"        -> Icons.Outlined.Refresh
    else             -> Icons.Outlined.ArrowForward
}

// ── Risk color — handles both V1 human labels and V2 machine keys ─────────────

fun riskColor(riskLevel: String): Color {
    val n = riskLevel.lowercase()
    return when {
        "excellent" in n || "elite" in n                   -> Color(0xFFFFD166)
        "mostly_safe" in n || "mostly safe" in n
                || ("safe" in n && "mostly" !in n)         -> Color(0xFF3DDB92)
        "moderate" in n || "medium" in n                   -> Color(0xFFFFA726)
        "high_risk" in n || "high risk" in n || "high" in n -> Color(0xFFFF5C5C)
        "critical" in n                                     -> Color(0xFFEF4444)
        "locked" in n                                       -> Color(0xFF9CA3AF)
        else                                               -> Color(0xFF60A5FA)
    }
}

// ── Level label — converts machine key → human string ─────────────────────────

fun levelLabel(level: String?): String = when (level?.uppercase()) {
    "EXCELLENT"    -> "Excellent"
    "MOSTLY_SAFE"  -> "Mostly Safe"
    "MODERATE_RISK"-> "Moderate Risk"
    "HIGH_RISK"    -> "High Risk"
    "CRITICAL"     -> "Critical"
    else           -> level?.replace("_", " ")?.lowercase()
        ?.replaceFirstChar { it.uppercase() } ?: "Unknown"
}

// ── Private map helpers ───────────────────────────────────────────────────────

private fun Map<String, Any?>?.intValue(key: String): Int {
    val value = this?.get(key) ?: return 0
    return when (value) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull() ?: 0
        else      -> 0
    }
}

private fun Map<String, Any?>?.stringValue(key: String): String? =
    this?.get(key)?.toString()?.takeIf { it.isNotBlank() }
