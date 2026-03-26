package com.gosuraksha.app.ui.login

import androidx.compose.ui.graphics.Color

data class LoginFormState(
    val loginMode: Int = 0,
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val showContent: Boolean = false
)

const val LOGIN_TAG = "GoogleSignInFlow"

val Green400 = Color(0xFF2EC472)
val Green600 = Color(0xFF17753D)
val Green700 = Color(0xFF0F5C2E)
val HeroBg = Color(0xFF0D1F14)
val LightBg = Color(0xFFF0F6F2)
val LightSurface = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFCCE3D4)
val LightTabTray = Color(0xFFDCEDE4)
val LightTextPri = Color(0xFF0D1F14)
val LightTextSec = Color(0xFF05230F)
val LightTextTert = Color(0xFF8BB89A)
val LightBadgeBg = Color(0xFFFFFFFF)
val DarkBg = Color(0xFF000000)
val DarkSurface = Color(0xFF070908)
val DarkSurfaceAlt = Color(0xFF1A3322)
val DarkBorder = Color(0xFF254D30)
val DarkBorderFocus = Color(0xFF2EC472)
val DarkTabTray = Color(0xFF000000)
val DarkTabActive = Color(0xFF1E3D28)
val DarkTextPri = Color(0xFFEAECEA)
val DarkTextSec = Color(0xFFFFFFFF)
val DarkTextTert = Color(0xFFE5E5E5)
val DarkBadgeBg = Color(0xFF030503)
val DarkBadgeBorder = Color(0xFF1E3D28)
val DarkIconTint = Color(0xFFFFFFFF)
