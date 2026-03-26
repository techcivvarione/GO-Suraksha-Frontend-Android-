package com.gosuraksha.app.ui.auth

// =============================================================================
// AuthDesignTokens.kt — PhonePe-style security design system
//
// Visual: Deep navy security hero (top) + white card slides up (bottom)
// Dark:  hero #04081A→#091428, card #111827
// Light: hero #04081A→#0A1F3C (always dark hero), card #FFFFFF
// Accent #1AB87A — CTA button, active states, filled inputs
// =============================================================================

import androidx.compose.ui.graphics.Color

internal object AuthColors {
    // ── Hero gradients (always dark — security aesthetic) ──────────────────
    val HeroTopDark    = Color(0xFF04081A)
    val HeroMidDark    = Color(0xFF061225)
    val HeroBottomDark = Color(0xFF091C38)

    val HeroTopLight    = Color(0xFF04081A)
    val HeroMidLight    = Color(0xFF071428)
    val HeroBottomLight = Color(0xFF0A1F3C)

    // Legacy aliases (used by AuthHero)
    val HeroStartDark  = HeroTopDark
    val HeroMidDark2   = HeroMidDark
    val HeroEndDark    = HeroBottomDark
    val HeroStartLight = HeroTopLight
    val HeroMidLight2  = HeroMidLight
    val HeroEndLight   = HeroBottomLight

    // ── Radar / glow colors ────────────────────────────────────────────────
    val RadarGlow      = Color(0xFF1AB87A)        // pulsing ring color
    val RadarGlowAlt   = Color(0xFF00E5FF)        // secondary glow (cyan)
    val HexDot         = Color(0xFF1A3A5C)        // grid dots
    val ShieldGlow     = Color(0xFF22D68E)

    // ── Page backgrounds ───────────────────────────────────────────────────
    val BgDark         = Color(0xFF0D1117)
    val BgLight        = Color(0xFFF6F8FA)

    // ── Card surfaces ──────────────────────────────────────────────────────
    val CardDark       = Color(0xFF111827)
    val CardLight      = Color(0xFFFFFFFF)

    // ── Input field backgrounds ────────────────────────────────────────────
    val FieldDark      = Color(0xFF1C2433)
    val FieldLight     = Color(0xFFF4F6FA)

    // ── Borders ────────────────────────────────────────────────────────────
    val BorderDark     = Color(0xFF1E2D40)
    val BorderLight    = Color(0xFFD8DDE6)

    // ── Text ───────────────────────────────────────────────────────────────
    val TextPriDark    = Color(0xFFF0F6FC)
    val TextPriLight   = Color(0xFF0D1117)
    val TextSecDark    = Color(0xFF7D8FA3)
    val TextSecLight   = Color(0xFF57606A)
    val TextTertDark   = Color(0xFF2A3A4A)
    val TextTertLight  = Color(0xFFB0BAC4)

    // ── Brand ──────────────────────────────────────────────────────────────
    val Accent         = Color(0xFF1AB87A)
    val AccentBright   = Color(0xFF22D68E)
    val AccentDim      = Color(0xFF0E9060)
    val AccentGradStart = Color(0xFF1AB87A)
    val AccentGradEnd   = Color(0xFF0EA56A)
    val AccentDisabledDark  = Color(0xFF1E5C35)
    val AccentDisabledLight = Color(0xFFB8D8C4)

    // ── Semantic ───────────────────────────────────────────────────────────
    val ErrorRed       = Color(0xFFE5484D)
    val ErrorBgDark    = Color(0xFF1A0808)
    val ErrorBgLight   = Color(0xFFFFF0F0)

    val SuccessGreen   = Color(0xFF1AB87A)
    val SuccessBgDark  = Color(0xFF071A0A)
    val SuccessBgLight = Color(0xFFF0FDF4)

    // ── Google button ──────────────────────────────────────────────────────
    val GoogleBgDark   = Color(0xFF1C2330)
    val GoogleBgLight  = Color(0xFFFFFFFF)

    // helpers
    fun bg(isDark: Boolean)       = if (isDark) BgDark         else BgLight
    fun card(isDark: Boolean)     = if (isDark) CardDark        else CardLight
    fun field(isDark: Boolean)    = if (isDark) FieldDark       else FieldLight
    fun border(isDark: Boolean)   = if (isDark) BorderDark      else BorderLight
    fun textPri(isDark: Boolean)  = if (isDark) TextPriDark     else TextPriLight
    fun textSec(isDark: Boolean)  = if (isDark) TextSecDark     else TextSecLight
    fun textTert(isDark: Boolean) = if (isDark) TextTertDark    else TextTertLight
    fun accentDisabled(isDark: Boolean) = if (isDark) AccentDisabledDark else AccentDisabledLight
    fun heroTop(isDark: Boolean)  = if (isDark) HeroTopDark     else HeroTopLight
    fun heroMid(isDark: Boolean)  = if (isDark) HeroMidDark     else HeroMidLight
    fun heroBot(isDark: Boolean)  = if (isDark) HeroBottomDark  else HeroBottomLight
}
