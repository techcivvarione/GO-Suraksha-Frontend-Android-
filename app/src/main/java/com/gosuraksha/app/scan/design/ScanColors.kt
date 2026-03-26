package com.gosuraksha.app.scan.design

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─── Brand ───────────────────────────────────────────────────────────────────
val BrandBlue           = Color(0xFF0062FF)
val BrandBlueDark       = Color(0xFF3B9EFF)
val BrandGreen          = Color(0xFF00C896)

// ─── Per-tool accent colors (same in both modes — band color) ─────────────────
val AccentThreat        = Color(0xFF3B82F6)
val AccentEmail         = Color(0xFFA855F7)
val AccentPassword      = Color(0xFFF59E0B)
val AccentQR            = Color(0xFF10B981)
val AccentDeepfake      = Color(0xFFEC4899)

// ─── Status colors ────────────────────────────────────────────────────────────
val SafeGreen           = Color(0xFF00C896)
val SafeGreenSoft       = Color(0xFFE6FAF5)
val SafeGreenSoftDark   = Color(0x1A00C896)
val WarningOrange       = Color(0xFFF59E0B)
val WarningOrangeSoft   = Color(0xFFFFF3E0)
val WarningOrangeSoftDk = Color(0x1AF59E0B)
val DangerRed           = Color(0xFFEF4444)
val DangerRedSoft       = Color(0xFFFFF0EF)
val DangerRedSoftDark   = Color(0x1AEF4444)
val PurpleBreach        = Color(0xFFA855F7)
val PurpleBreachSoft    = Color(0xFFF5EEFA)
val PurpleBreachSoftDk  = Color(0x1AA855F7)

// ─── Light tokens ────────────────────────────────────────────────────────────
val BgLight             = Color(0xFFF2F4F8)
val SurfaceLight        = Color(0xFFFFFFFF)
val Surface2Light       = Color(0xFFF2F4F8)
val BorderLight         = Color(0xFFE8ECF2)
val TextPrimLight       = Color(0xFF0F172A)
val TextSecLight        = Color(0xFF64748B)
val TextTertLight       = Color(0xFF94A3B8)

// ─── Dark tokens ─────────────────────────────────────────────────────────────
val BgDark              = Color(0xFF0C0E14)
val SurfaceDark         = Color(0xFF131720)
val Surface2Dark        = Color(0xFF1A2030)
val BorderDark          = Color(0x0EFFFFFF)
val TextPrimDark        = Color(0xFFE2E8F0)
val TextSecDark         = Color(0xFF475569)
val TextTertDark        = Color(0xFF334155)

// ─── ScanColors ───────────────────────────────────────────────────────────────
@Immutable
data class ScanColors(
    // Brand
    val primaryBlue: Color,
    val brandGreen: Color,
    // Tool accent bands (same both modes)
    val accentThreat: Color,
    val accentEmail: Color,
    val accentPassword: Color,
    val accentQR: Color,
    val accentDeepfake: Color,
    // Icon box backgrounds
    val accentThreatBg: Color,
    val accentEmailBg: Color,
    val accentPasswordBg: Color,
    val accentQRBg: Color,
    val accentDeepfakeBg: Color,
    // Tag text colors
    val accentThreatText: Color,
    val accentEmailText: Color,
    val accentPasswordText: Color,
    val accentQRText: Color,
    val accentDeepfakeText: Color,
    // Tag backgrounds
    val accentThreatTagBg: Color,
    val accentEmailTagBg: Color,
    val accentPasswordTagBg: Color,
    val accentQRTagBg: Color,
    val accentDeepfakeTagBg: Color,
    // Status
    val safeGreen: Color,
    val safeGreenSoft: Color,
    val warningOrange: Color,
    val warningOrangeSoft: Color,
    val dangerRed: Color,
    val dangerRedSoft: Color,
    val purpleBreach: Color,
    val purpleBreachSoft: Color,
    // Surfaces
    val background: Color,
    val surface: Color,
    val surface2: Color,
    val border: Color,
    // Status banner
    val statusBannerBg: Color,
    val statusBannerBg2: Color,
    val statusBannerBorder: Color,
    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    // Legacy aliases (keeps other screens compiling)
    val blueTint: Color,
    val blueMid: Color,
)

// ─── Light palette ────────────────────────────────────────────────────────────
val LightPalette = ScanColors(
    primaryBlue         = BrandBlue,
    brandGreen          = BrandGreen,
    accentThreat        = AccentThreat,
    accentEmail         = AccentEmail,
    accentPassword      = AccentPassword,
    accentQR            = AccentQR,
    accentDeepfake      = AccentDeepfake,
    accentThreatBg      = Color(0xFFEFF6FF),
    accentEmailBg       = Color(0xFFFAF5FF),
    accentPasswordBg    = Color(0xFFFFFBEB),
    accentQRBg          = Color(0xFFECFDF5),
    accentDeepfakeBg    = Color(0xFFFDF2F8),
    accentThreatText    = Color(0xFF1D4ED8),
    accentEmailText     = Color(0xFF7E22CE),
    accentPasswordText  = Color(0xFF92400E),
    accentQRText        = Color(0xFF065F46),
    accentDeepfakeText  = Color(0xFF9D174D),
    accentThreatTagBg   = Color(0xFFDBEAFE),
    accentEmailTagBg    = Color(0xFFF3E8FF),
    accentPasswordTagBg = Color(0xFFFEF3C7),
    accentQRTagBg       = Color(0xFFD1FAE5),
    accentDeepfakeTagBg = Color(0xFFFCE7F3),
    safeGreen           = SafeGreen,
    safeGreenSoft       = SafeGreenSoft,
    warningOrange       = WarningOrange,
    warningOrangeSoft   = WarningOrangeSoft,
    dangerRed           = DangerRed,
    dangerRedSoft       = DangerRedSoft,
    purpleBreach        = PurpleBreach,
    purpleBreachSoft    = PurpleBreachSoft,
    background          = BgLight,
    surface             = SurfaceLight,
    surface2            = Surface2Light,
    border              = BorderLight,
    statusBannerBg      = Color(0xFF0F172A),
    statusBannerBg2     = Color(0xFF1E3A5F),
    statusBannerBorder  = Color.Transparent,
    textPrimary         = TextPrimLight,
    textSecondary       = TextSecLight,
    textTertiary        = TextTertLight,
    blueTint            = Color(0xFFEFF6FF),
    blueMid             = Color(0xFFDBEAFE),
)

// ─── Dark palette ─────────────────────────────────────────────────────────────
val DarkPalette = ScanColors(
    primaryBlue         = BrandBlueDark,
    brandGreen          = BrandGreen,
    accentThreat        = AccentThreat,
    accentEmail         = AccentEmail,
    accentPassword      = AccentPassword,
    accentQR            = AccentQR,
    accentDeepfake      = AccentDeepfake,
    accentThreatBg      = Color(0x193B82F6),
    accentEmailBg       = Color(0x19A855F7),
    accentPasswordBg    = Color(0x19F59E0B),
    accentQRBg          = Color(0x1910B981),
    accentDeepfakeBg    = Color(0x19EC4899),
    accentThreatText    = Color(0xFF93C5FD),
    accentEmailText     = Color(0xFFC4B5FD),
    accentPasswordText  = Color(0xFFFCD34D),
    accentQRText        = Color(0xFF6EE7B7),
    accentDeepfakeText  = Color(0xFFF9A8D4),
    accentThreatTagBg   = Color(0x1E3B82F6),
    accentEmailTagBg    = Color(0x1EA855F7),
    accentPasswordTagBg = Color(0x1EF59E0B),
    accentQRTagBg       = Color(0x1E10B981),
    accentDeepfakeTagBg = Color(0x1EEC4899),
    safeGreen           = SafeGreen,
    safeGreenSoft       = SafeGreenSoftDark,
    warningOrange       = WarningOrange,
    warningOrangeSoft   = WarningOrangeSoftDk,
    dangerRed           = DangerRed,
    dangerRedSoft       = DangerRedSoftDark,
    purpleBreach        = PurpleBreach,
    purpleBreachSoft    = PurpleBreachSoftDk,
    background          = BgDark,
    surface             = SurfaceDark,
    surface2            = Surface2Dark,
    border              = BorderDark,
    statusBannerBg      = Color(0xFF151B2A),
    statusBannerBg2     = Color(0xFF151B2A),
    statusBannerBorder  = Color(0x0FFFFFFF),
    textPrimary         = TextPrimDark,
    textSecondary       = TextSecDark,
    textTertiary        = TextTertDark,
    blueTint            = Color(0x193B82F6),
    blueMid             = Color(0x193B82F6),
)

// ─── Material3 schemes ────────────────────────────────────────────────────────
internal val ScanLightColorScheme = lightColorScheme(
    primary             = BrandBlue,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFFEFF6FF),
    onPrimaryContainer  = BrandBlue,
    secondary           = SafeGreen,
    onSecondary         = Color.White,
    tertiary            = WarningOrange,
    onTertiary          = Color.Black,
    background          = BgLight,
    onBackground        = TextPrimLight,
    surface             = SurfaceLight,
    onSurface           = TextPrimLight,
    surfaceVariant      = Surface2Light,
    onSurfaceVariant    = TextSecLight,
    outline             = BorderLight,
    error               = DangerRed,
    onError             = Color.White,
)

internal val ScanDarkColorScheme = darkColorScheme(
    primary             = BrandBlueDark,
    onPrimary           = Color.White,
    primaryContainer    = Color(0x193B82F6),
    onPrimaryContainer  = Color.White,
    secondary           = SafeGreen,
    onSecondary         = Color.Black,
    tertiary            = WarningOrange,
    onTertiary          = Color.Black,
    background          = BgDark,
    onBackground        = TextPrimDark,
    surface             = SurfaceDark,
    onSurface           = TextPrimDark,
    surfaceVariant      = Surface2Dark,
    onSurfaceVariant    = TextSecDark,
    outline             = BorderDark,
    error               = DangerRed,
    onError             = Color.White,
)

val LocalScanColors = staticCompositionLocalOf { LightPalette }