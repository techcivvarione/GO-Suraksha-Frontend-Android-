package com.gosuraksha.app.ui.onboarding.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.gosuraksha.app.R

data class OnboardingSlide(
    val tag: String,
    val titleLine1: String,
    val titleLine2: String,
    val description: String,
)

val SyneFamily = FontFamily(
    Font(R.font.syne_semibold, FontWeight.Normal),
    Font(R.font.syne_semibold, FontWeight.SemiBold),
    Font(R.font.syne_bold, FontWeight.Bold),
    Font(R.font.syne_extrabold, FontWeight.ExtraBold),
)

val DmSansFamily = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
)

val BgColor = Color(0xFF0B0D0B)
val GreenAccent = Color(0xFF3DDC84)
val GreenMid = Color(0xFF2EC472)
val IlloWhite = Color(0xFFFFFFFF)
val IlloFaint = Color(0x14FFFFFF)
val TextPri = Color(0xFFFFFFFF)
val TextSec = Color(0x55FFFFFF)
val DotOff = Color(0x16FFFFFF)
val WarnAmber = Color(0xFFFFB020)

val onboardingSlides = listOf(
    OnboardingSlide(
        tag = "Shield · 01",
        titleLine1 = "Stay Safe from",
        titleLine2 = "Digital Scams.",
        description = "Detect suspicious messages, fake media, and unsafe QR payments before they harm you."
    ),
    OnboardingSlide(
        tag = "Scan · 02",
        titleLine1 = "Scan Before",
        titleLine2 = "You Trust.",
        description = "Analyze messages, QR codes, images, videos, and audio to detect threats and AI deepfakes."
    ),
    OnboardingSlide(
        tag = "Family · 03",
        titleLine1 = "Protect Your",
        titleLine2 = "Family.",
        description = "Add trusted contacts. Auto-alert them if a cyber threat or scam is detected near you."
    ),
    OnboardingSlide(
        tag = "Score · 04",
        titleLine1 = "Know Your",
        titleLine2 = "Cyber Score.",
        description = "Track your digital safety with India's first personal Cyber Card and improve your score."
    ),
)
