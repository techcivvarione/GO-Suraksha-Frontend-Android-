package com.gosuraksha.app.ui.auth

// =============================================================================
// LoginScreen.kt — Final redesign
//
// Changes from previous version:
//   • Hero: #060E08 pitch dark background
//   • Dot grid via Canvas drawBehind (no glow, no radar rings)
//   • Logo from res/drawable/logo — top center, no circle bg, no "GoSuraksha" label
//   • No scrolling ticker
//   • Bottom card spring slide-up preserved
//   • ViewModel wiring unchanged
// =============================================================================

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.R
import com.gosuraksha.app.auth.GoogleSignInManager
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.presentation.auth.AuthViewModel

private const val LOGIN_TAG = "UnifiedLogin"

// ── Hero colours (hardcoded — not theme-adaptive, hero is always dark) ────────
private val HeroBg       = Color(0xFF060E08)
private val DotColor     = Color(0xFF52B788).copy(alpha = 0.22f)
private val AccentGreen  = Color(0xFF52B788)
private val HeroTitle    = Color(0xFFFFFFFF)
private val HeroSubtitle = Color(0xFF52B788)

@Composable
fun LoginScreen(
    viewModel:          AuthViewModel,
    onOtpSent:          () -> Unit,
    onLoginResolved:    (Boolean) -> Unit,
    onNavigateToSignup: () -> Unit
) {
    val isDark   = ColorTokens.LocalAppDarkMode.current
    val context  = LocalContext.current
    val activity = context as? android.app.Activity

    // ── ViewModel state ───────────────────────────────────────────────────
    val isSendingOtp by viewModel.isSendingOtp.collectAsStateWithLifecycle()
    val vmError      by viewModel.otpError.collectAsStateWithLifecycle()

    // ── Local UI state ────────────────────────────────────────────────────
    var phone         by remember { mutableStateOf("") }
    var email         by remember { mutableStateOf("") }
    var password      by remember { mutableStateOf("") }
    var passVisible   by remember { mutableStateOf(false) }
    var showEmailForm by remember { mutableStateOf(false) }
    var localError    by remember { mutableStateOf<String?>(null) }
    var googleLoading by remember { mutableStateOf(false) }
    var emailLoading  by remember { mutableStateOf(false) }

    val error = vmError ?: localError

    BackHandler { activity?.finish() }

    LaunchedEffect(isSendingOtp) {
        if (BuildConfig.DEBUG) Log.d("AUTH_DEBUG", "Loader state: $isSendingOtp")
    }

    // ── Card slide-up ──────────────────────────────────────────────────────
    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { cardVisible = true }

    // ── Google launcher ───────────────────────────────────────────────────
    val googleSignInManager = remember(context) { GoogleSignInManager(context) }
    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val token   = account?.idToken
            if (token.isNullOrBlank()) {
                googleLoading = false
                localError    = "Unable to sign in with Google"
                return@rememberLauncherForActivityResult
            }
            viewModel.loginWithGoogleResult(
                idToken  = token,
                onResult = { googleLoading = false; onLoginResolved(it) },
                onError  = { googleLoading = false; localError = it }
            )
        } catch (e: ApiException) {
            if (BuildConfig.DEBUG) Log.e(LOGIN_TAG, "Google sign-in failed", e)
            googleLoading = false
            localError = if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED)
                "Google sign-in canceled"
            else "Unable to sign in with Google"
        }
    }

    // ── Screen ────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HeroBg)
    ) {

        // ── Dark hero ─────────────────────────────────────────────────────
        AuthHeroDark(
            title    = "Welcome\nback",
            subtitle = "Sign in to stay protected",
            height   = 300.dp
        )

        // ── Bottom card — spring slide-up ─────────────────────────────────
        AnimatedVisibility(
            visible  = cardVisible,
            modifier = Modifier.weight(1f),
            enter    = slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMediumLow
                ),
                initialOffsetY = { it }
            ) + fadeIn(tween(350))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AuthCardSurface(
                    isDark   = isDark,
                    modifier = Modifier.fillMaxHeight()
                ) {

                    // Header
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text          = "Sign in",
                            fontSize      = 22.sp,
                            fontWeight    = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                            color         = AuthColors.textPri(isDark)
                        )
                        Text(
                            text     = "Enter your mobile number to continue",
                            fontSize = 13.sp,
                            color    = AuthColors.textSec(isDark)
                        )
                    }

                    // Phone field
                    AuthPhoneField(
                        value         = phone,
                        onValueChange = { phone = it; localError = null },
                        isDark        = isDark
                    )

                    // Get OTP
                    AuthPrimaryButton(
                        text      = "Get OTP",
                        onClick   = {
                            localError = null
                            viewModel.sendOtp(
                                phone     = "+91$phone",
                                onSuccess = onOtpSent,
                                onError   = { localError = it }
                            )
                        },
                        enabled   = phone.length == 10,
                        isLoading = isSendingOtp,
                        isDark    = isDark
                    )

                    // OR
                    AuthOrDivider(isDark = isDark)

                    // Google
                    AuthGoogleButton(
                        onClick   = {
                            googleLoading = true
                            localError    = null
                            googleLauncher.launch(googleSignInManager.client.signInIntent)
                        },
                        enabled   = !googleLoading && !isSendingOtp,
                        isLoading = googleLoading,
                        isDark    = isDark
                    )

                    // Email toggle
                    AuthOutlinedButton(
                        text    = if (showEmailForm) "✕  Hide email login" else "✉  Use email instead",
                        onClick = {
                            showEmailForm = !showEmailForm
                            localError    = null
                        },
                        isDark  = isDark
                    )

                    // Email form
                    AnimatedVisibility(
                        visible = showEmailForm,
                        enter   = fadeIn(tween(280)) + expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness    = Spring.StiffnessMedium
                            )
                        ),
                        exit    = fadeOut(tween(180)) + shrinkVertically(tween(200))
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            AuthTextField(
                                value         = email,
                                onValueChange = { email = it; localError = null },
                                label         = "Email Address",
                                placeholder   = "you@email.com",
                                leadingIcon   = Icons.Outlined.Email,
                                keyboardType  = KeyboardType.Email,
                                isDark        = isDark
                            )
                            AuthPasswordField(
                                value              = password,
                                onValueChange      = { password = it; localError = null },
                                label              = "Password",
                                placeholder        = "Your password",
                                passwordVisible    = passVisible,
                                onVisibilityToggle = { passVisible = !passVisible },
                                isDark             = isDark
                            )
                            AuthPrimaryButton(
                                text      = "Login with Email",
                                onClick   = {
                                    localError   = null
                                    emailLoading = true
                                    viewModel.loginWithResult(
                                        identifier = email.trim(),
                                        password   = password.trim(),
                                        onResult   = { emailLoading = false; onLoginResolved(it) },
                                        onError    = { emailLoading = false; localError = it }
                                    )
                                },
                                enabled   = email.isNotBlank() && password.isNotBlank(),
                                isLoading = emailLoading,
                                isDark    = isDark
                            )
                        }
                    }

                    // Error
                    error?.let { AuthErrorRow(it, isDark) }

                    // Signup link
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "New here? ",
                            fontSize = 13.sp,
                            color    = AuthColors.textSec(isDark)
                        )
                        Text(
                            text       = "Create account",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color      = AuthColors.Accent,
                            modifier   = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = onNavigateToSignup
                            )
                        )
                    }

                    // Trust line
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("🔒 ", fontSize = 11.sp)
                        Text(
                            "256-bit encrypted  ·  RBI compliant  ·  Zero data sold",
                            fontSize = 10.sp,
                            color    = AuthColors.textTert(isDark)
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

// =============================================================================
// AuthHeroDark — reusable dark hero with dot grid + logo
// =============================================================================
@Composable
fun AuthHeroDark(
    title:    String,
    subtitle: String,
    height:   Dp
) {
    val dotSpacingPx = with(androidx.compose.ui.platform.LocalDensity.current) { 10.dp.toPx() }
    val dotRadiusPx  = with(androidx.compose.ui.platform.LocalDensity.current) { 1.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(HeroBg)
            .drawBehind {
                // Dot grid
                val cols = (size.width / dotSpacingPx).toInt() + 1
                val rows = (size.height / dotSpacingPx).toInt() + 1
                for (col in 0..cols) {
                    for (row in 0..rows) {
                        drawCircle(
                            color  = DotColor,
                            radius = dotRadiusPx,
                            center = Offset(col * dotSpacingPx, row * dotSpacingPx)
                        )
                    }
                }
            }
    ) {
        // Logo — top center
        Image(
            painter            = painterResource(id = R.drawable.logo),
            contentDescription = "GO Suraksha",
            modifier           = Modifier
                .size(96.dp)
                .align(Alignment.TopCenter)
                .padding(top = 28.dp)
        )

        // Title + subtitle — bottom left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text          = title,
                fontSize      = 28.sp,
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = (-0.6).sp,
                lineHeight    = 32.sp,
                color         = HeroTitle
            )
            Text(
                text       = subtitle,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = HeroSubtitle
            )
        }
    }
}