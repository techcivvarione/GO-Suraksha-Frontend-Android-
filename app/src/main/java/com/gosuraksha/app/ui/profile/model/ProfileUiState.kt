package com.gosuraksha.app.ui.main

import androidx.compose.ui.graphics.Color

// ── Profile colour tokens (dark mode: NO green tint — uses proper dark palette) ───
object PC {
    // Hero gradient — dark mode uses neutral charcoal, light mode uses subtle teal
    val HeroStart = Color(0xFF0B0F14)
    val HeroMid   = Color(0xFF111820)
    val HeroEnd   = Color(0xFF0B0F14)

    // Backgrounds — clean, no green tint
    val DarkBg    = Color(0xFF0B0F14)
    val LightBg   = Color(0xFFF4F6F8)

    // Cards & borders
    val DarkCard    = Color(0xFF121821)
    val LightCard   = Color(0xFFFFFFFF)
    val DarkBorder  = Color(0xFF1F2A37)
    val LightBorder = Color(0xFFE5E7EB)

    // Text helpers
    fun onSurf(isDark: Boolean)  = if (isDark) Color(0xFFE5E7EB) else Color(0xFF0D1117)
    fun subText(isDark: Boolean) = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    fun muted(isDark: Boolean)   = if (isDark) Color(0xFF374151) else Color(0xFFD1D5DB)
    fun secLbl(isDark: Boolean)  = if (isDark) Color(0xFF4B5563) else Color(0xFF9CA3AF)
    fun divider(isDark: Boolean) = if (isDark) Color(0xFF1F2A37) else Color(0xFFF3F4F6)

    // Accent palette
    val Green    = Color(0xFF22C55E)
    val GreenDeep = Color(0xFF101311)
    val GreenDim = Color(0xFF15803D)
    val Red   = Color(0xFFEF4444)
    val Amber = Color(0xFFF59E0B)
    val Blue  = Color(0xFF3B82F6)

    // Icon background helpers
    fun iconBgRed(isDark: Boolean)   = if (isDark) Color(0xFF1A0808) else Color(0xFFFEF2F2)
    fun iconBgGreen(isDark: Boolean) = if (isDark) Color(0xFF0A1A0A) else Color(0xFFF0FDF4)
    fun iconBgBlue(isDark: Boolean)  = if (isDark) Color(0xFF0A1430) else Color(0xFFEFF6FF)
    fun iconBgAmber(isDark: Boolean) = if (isDark) Color(0xFF1A1000) else Color(0xFFFFFBEB)
    fun iconBgSlate(isDark: Boolean) = if (isDark) Color(0xFF111820) else Color(0xFFF9FAFB)
}

data class ProfileFormUiState(
    val name: String = "",
    val phone: String = "",
    val currentPass: String = "",
    val newPass: String = "",
    val confirmPass: String = "",
    val deleteConfirmationInput: String = "",
    val showCurrentPass: Boolean = false,
    val showNewPass: Boolean = false,
    val showConfirmPass: Boolean = false
)

data class ProfileDialogUiState(
    val showLogoutDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val securityExpanded: Boolean = false
)

fun formatCardNumber(id: String): String {
    val clean = id.replace("-", "").uppercase()
    return if (clean.length >= 12) {
        "${clean.substring(0, 4)} ${clean.substring(4, 7)} ${clean.substring(7, 10)}"
    } else {
        "CC08 K38 56B"
    }
}
