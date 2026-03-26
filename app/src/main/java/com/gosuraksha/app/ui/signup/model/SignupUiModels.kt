package com.gosuraksha.app.ui.signup.model

import androidx.compose.ui.graphics.Color

data class SignupFormState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirm: String = "",
    val otp: String = "",
    val showPassword: Boolean = false,
    val showConfirm: Boolean = false,
    val acceptedTerms: Boolean = false,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
)

val SignupGreen400 = Color(0xFF2EC472)
val SignupHeroBg = Color(0xFF0D1F14)
val SignupLightBg = Color(0xFFF0F6F2)
val SignupLightSurface = Color(0xFFFFFFFF)
val SignupLightBorder = Color(0xFFCCE3D4)
val SignupLightTextPri = Color(0xFF0D1F14)
val SignupLightTextSec = Color(0xFF000000)
val SignupLightTextTert = Color(0xFF000000)
val SignupDarkBg = Color(0xFF000000)
val SignupDarkSurface = Color(0xFF040705)
val SignupDarkBorder = Color(0xFF254D30)
val SignupDarkTextPri = Color(0xFFD4EDD9)
val SignupDarkTextSec = Color(0xFFFFFFFF)
val SignupDarkTextTert = Color(0xFFF2FAF3)
val SignupDarkIconTint = Color(0xFF3A7A50)
val SignupDarkBadgeBg = Color(0xFF132318)
val SignupDarkBadgeBorder = Color(0xFF1E3D28)
