package com.gosuraksha.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R

// ─────────────────────────────────────────────────────────────────────────────
// Manrope font family
// Files required in res/font/:
//   manrope_light.ttf        (300)
//   manrope_regular.ttf      (400)
//   manrope_medium.ttf       (500)
//   manrope_semibold.ttf     (600)
//   manrope_bold.ttf         (700)
//   manrope_extrabold.ttf    (800)
// ─────────────────────────────────────────────────────────────────────────────
val Manrope = FontFamily(
    Font(R.font.manrope_light,     FontWeight.Light),
    Font(R.font.manrope_regular,   FontWeight.Normal),
    Font(R.font.manrope_medium,    FontWeight.Medium),
    Font(R.font.manrope_semibold,  FontWeight.SemiBold),
    Font(R.font.manrope_bold,      FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
)

// ─────────────────────────────────────────────────────────────────────────────
// Material 3 type scale — all 15 roles mapped to Manrope
//
// Design decisions:
//   Display*   → ExtraBold   — hero numbers, screen titles
//   Headline*  → Bold        — section headers, card titles
//   Title*     → SemiBold    — list item titles, tab labels
//   Body*      → Regular     — content text, descriptions
//   Label*     → Medium/Semi — chips, badges, captions
//
// Line heights: 1.25× for display, 1.35× for body
// Letter spacing: slight negative for display, neutral for body
// ─────────────────────────────────────────────────────────────────────────────
val GoSurakshaTypography = Typography(

    // ── Display ──────────────────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 57.sp,
        lineHeight    = 64.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 45.sp,
        lineHeight    = 52.sp,
        letterSpacing = (-0.3).sp
    ),
    displaySmall = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
        letterSpacing = (-0.2).sp
    ),

    // ── Headline ─────────────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.Bold,
        fontSize      = 32.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineMedium = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.Bold,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineSmall = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.Bold,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
        letterSpacing = (-0.1).sp
    ),

    // ── Title ─────────────────────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = (-0.1).sp
    ),
    titleMedium = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.sp
    ),

    // ── Body ──────────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 26.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 22.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.1.sp
    ),

    // ── Label ─────────────────────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily    = Manrope,
        fontWeight    = FontWeight.Medium,
        fontSize      = 10.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.5.sp
    ),
)