package com.gosuraksha.app.ui.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object GS {
    val DarkBg = Color(0xFF0B0F14)
    val LightBg = Color(0xFFF6F8FA)
    val DarkSurface = Color(0xFF121821)
    val LightSurface = Color(0xFFFFFFFF)
    val DarkGap = Color(0xFF080C11)
    val LightGap = Color(0xFFF2F6F3)
    val DarkBorder = Color(0xFF1F2A37)
    val LightBorder = Color(0xFFE8F0EA)
    val Green500 = Color(0xFF22C55E)
    val Green600 = Color(0xFF16A34A)
    val Green700 = Color(0xFF15803D)
    val GreenDim = Color(0xFFFFFFFF)
    val DarkIconGreen = Color(0xFF071A0A)
    val DarkIconRed = Color(0xFF1A0808)
    val DarkIconAmber = Color(0xFF1A1000)
    val DarkIconPurple = Color(0xFF14102A)
    val DarkIconBlue = Color(0xFF0A1430)
    val LightIconGreen = Color(0xFFE8F5EC)
    val LightIconRed = Color(0xFFFEF2F2)
    val LightIconAmber = Color(0xFFFFFBEB)
    val LightIconPurple = Color(0xFFF5F3FF)
    val LightIconBlue = Color(0xFFEFF6FF)
    val DarkStrokeGreen = Color(0xFF22C55E)
    val DarkStrokeRed = Color(0xFFEF4444)
    val DarkStrokeAmber = Color(0xFFF59E0B)
    val DarkStrokePurp = Color(0xFF86EFAC)
    val LightStrokeGreen = Color(0xFF16A34A)
    val LightStrokeRed = Color(0xFFDC2626)
    val LightStrokeAmber = Color(0xFFD97706)
    val LightStrokePurple = Color(0xFF7C3AED)
    val Red = Color(0xFFEF4444)
    val RedLight = Color(0xFFDC2626)
    val Amber = Color(0xFFF59E0B)
    val AmberLight = Color(0xFFD97706)

    fun onSurf(isDark: Boolean) = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0D1117)
    fun subText(isDark: Boolean) = if (isDark) GreenDim else Color(0xFF6B9E78)
    fun mutedText(isDark: Boolean) = if (isDark) Color(0xFF4B5563) else Color(0xFF94A3B8)
}

data class QuickTool(
    val label: String,
    val icon: ImageVector,
    val iconBgDark: Color,
    val iconBgLight: Color,
    val iconTintDark: Color,
    val iconTintLight: Color,
    val onClick: () -> Unit
)

data class ThreatItem(
    val title: String,
    val subtitle: String,
    val severity: String,
    val icon: ImageVector,
    val onClick: () -> Unit = {}
)

data class SectionTool(
    val label: String,
    val icon: ImageVector,
    val iconBgDark: Color,
    val iconBgLight: Color,
    val iconTintDark: Color,
    val iconTintLight: Color,
    val onClick: () -> Unit
)

fun getGreetingText(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}
