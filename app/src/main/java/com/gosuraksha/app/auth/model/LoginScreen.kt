package com.gosuraksha.app.ui.auth

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R
import com.gosuraksha.app.auth.GoogleSignInManager
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.presentation.auth.AuthViewModel
import com.gosuraksha.app.ui.components.localizedUiMessage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay

private const val TAG = "GoogleSignInFlow"

// ─────────────────────────────────────────────────────────────────────────────
// Design tokens — ALL solid colours, zero opacity hacks
// Dark mode tokens are readable on actual device screens
// ─────────────────────────────────────────────────────────────────────────────

// Brand green
private val Green400        = Color(0xFF2EC472)   // primary CTA, accents
private val Green600        = Color(0xFF17753D)   // gradient end, pressed
private val Green700        = Color(0xFF0F5C2E)   // darkest green used

// Hero section — deep forest green, same feel both modes
private val HeroBg          = Color(0xFF0D1F14)   // same for both — hero is always dark

// ── LIGHT MODE ──────────────────────────────────────────────────────────────
private val LightBg         = Color(0xFFF0F6F2)   // warm off-white with green tint
private val LightSurface    = Color(0xFFFFFFFF)   // cards, fields
private val LightBorder     = Color(0xFFCCE3D4)   // field borders, dividers
private val LightTabTray    = Color(0xFFDCEDE4)   // mode switcher background
private val LightTextPri    = Color(0xFF0D1F14)   // headings
private val LightTextSec    = Color(0xFF05230F)   // subtext, labels
private val LightTextTert   = Color(0xFF8BB89A)   // placeholders, hints
private val LightBadgeBg    = Color(0xFFFFFFFF)

// ── DARK MODE — solid visible colours on near-black ─────────────────────────
private val DarkBg          = Color(0xFF000000)   // deep dark green — NOT black
private val DarkSurface     = Color(0xFF070908)   // field / card surface
private val DarkSurfaceAlt  = Color(0xFF1A3322)   // slightly lighter surface (google btn etc)
private val DarkBorder      = Color(0xFF254D30)   // field borders — clearly visible
private val DarkBorderFocus = Color(0xFF2EC472)   // focused field border
private val DarkTabTray     = Color(0xFF000000)   // mode switcher background
private val DarkTabActive   = Color(0xFF1E3D28)   // active tab highlight
private val DarkTextPri     = Color(0xFFEAECEA)   // headings — near white green
private val DarkTextSec     = Color(0xFFFFFFFF)   // subtext — readable green-grey
private val DarkTextTert    = Color(0xFFE5E5E5)   // placeholders — visible but muted
private val DarkBadgeBg     = Color(0xFF030503)
private val DarkBadgeBorder = Color(0xFF1E3D28)
private val DarkIconTint    = Color(0xFFFFFFFF)   // icons in idle state

// ─────────────────────────────────────────────────────────────────────────────
// Root composable
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    val isDark = ColorTokens.LocalAppDarkMode.current

    var loginMode       by remember { mutableStateOf(0) }
    var email           by remember { mutableStateOf("") }
    var phone           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }
    var isLoading       by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var showContent     by remember { mutableStateOf(false) }

    val context             = LocalContext.current
    val googleSignInManager = remember(context) { GoogleSignInManager(context) }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val token   = account?.idToken
            if (account == null || token.isNullOrBlank()) {
                isGoogleLoading = false
                errorMessage    = "Unable to sign in with Google"
                return@rememberLauncherForActivityResult
            }
            viewModel.loginWithGoogle(
                idToken   = token,
                onSuccess = { isGoogleLoading = false; onLoginSuccess() },
                onError   = { isGoogleLoading = false; errorMessage = it }
            )
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign-in failed: ${e.statusCode}", e)
            isGoogleLoading = false
            errorMessage = if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED)
                "Google sign-in canceled"
            else
                "Unable to sign in with Google"
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    // Root — fills screen, NO scroll
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBg else LightBg)
    ) {
        // Hero — always the same dark forest green
        SecurityHero(isDark = isDark)

        // Form body — slides in on first load
        AnimatedVisibility(
            visible = showContent,
            enter   = fadeIn(tween(350)) + slideInVertically(tween(350)) { 20 }
        ) {
            LoginBody(
                isDark          = isDark,
                loginMode       = loginMode,
                email           = email,
                phone           = phone,
                password        = password,
                passwordVisible = passwordVisible,
                errorMessage    = errorMessage,
                isLoading       = isLoading,
                isGoogleLoading = isGoogleLoading,
                onModeChange    = { loginMode = it; errorMessage = null },
                onEmailChange   = { email     = it; errorMessage = null },
                onPhoneChange   = { phone     = it; errorMessage = null },
                onPasswordChange= { password  = it; errorMessage = null },
                onPasswordToggle= { passwordVisible = !passwordVisible },
                onLogin         = {
                    if (loginMode == 0 && email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        viewModel.login(
                            identifier = email,
                            password   = password,
                            onSuccess  = { isLoading = false; onLoginSuccess() },
                            onError    = { isLoading = false; errorMessage = it }
                        )
                    }
                },
                onGoogleLogin        = {
                    isGoogleLoading = true
                    errorMessage    = null
                    googleLauncher.launch(googleSignInManager.client.signInIntent)
                },
                onNavigateToSignup   = onNavigateToSignup
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero — 200dp, dark forest green bg, security dot-grid canvas pattern
// No glow. Single linear fade at bottom into body bg.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SecurityHero(isDark: Boolean) {
    // Fade target = body background below hero
    val fadeTo = if (isDark) DarkBg else LightBg

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(HeroBg)   // always the dark forest green
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSecurityPattern()
        }

        // Text — pinned bottom-left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 20.dp, bottom = 18.dp)
        ) {
            Spacer(Modifier.height(5.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Go Suraksha logo",
                    modifier = Modifier
                        .size(136.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }

    }
}

// Canvas security pattern — dot grid + circuit traces + nodes + icons
// Colours are absolute, not opacity-based, so they render on any bg
private fun DrawScope.drawSecurityPattern() {
    val w = size.width
    val h = size.height
    val sp = 22.dp.toPx()   // grid spacing

    // ── Dot grid ────────────────────────────────────────────────────────────
    val dotColor = Color(0xFF2A6640)   // solid dark-green dot — visible on HeroBg
    val dotR     = 1.3.dp.toPx()
    var gy = sp / 2f
    while (gy < h) {
        var gx = sp / 2f
        while (gx < w) {
            drawCircle(dotColor, radius = dotR, center = Offset(gx, gy))
            gx += sp
        }
        gy += sp
    }

    // ── Circuit traces ───────────────────────────────────────────────────────
    val traceColor = Color(0xFF2A6640)
    val traceW     = 0.7.dp.toPx()

    fun seg(x1: Float, y1: Float, x2: Float, y2: Float) =
        drawLine(traceColor, Offset(x1, y1), Offset(x2, y2), traceW)

    val g = sp
    seg(2*g, .5f*g, 2*g, 2.5f*g);   seg(2*g, 2.5f*g, 4.5f*g, 2.5f*g)
    seg(4.5f*g, 2.5f*g, 6.5f*g, 2.5f*g); seg(6.5f*g, 2.5f*g, 6.5f*g, 1.5f*g)
    seg(9.5f*g, .5f*g, 9.5f*g, 1.5f*g); seg(9.5f*g, 1.5f*g, 11.5f*g, 1.5f*g); seg(11.5f*g, 1.5f*g, 11.5f*g, .5f*g)
    seg(12.5f*g, .5f*g, 12.5f*g, 2.5f*g); seg(12.5f*g, 2.5f*g, 14.5f*g, 2.5f*g)
    seg(.5f*g, 3.5f*g, 2.5f*g, 3.5f*g); seg(2.5f*g, 3.5f*g, 2.5f*g, 4.5f*g); seg(2.5f*g, 4.5f*g, 4.5f*g, 4.5f*g); seg(4.5f*g, 4.5f*g, 4.5f*g, 3.5f*g); seg(4.5f*g, 3.5f*g, 6.5f*g, 3.5f*g)
    seg(9.5f*g, 3.5f*g, 11.5f*g, 3.5f*g); seg(11.5f*g, 3.5f*g, 11.5f*g, 4.5f*g); seg(11.5f*g, 4.5f*g, 13.5f*g, 4.5f*g)
    seg(.5f*g, 5.5f*g, 3.5f*g, 5.5f*g); seg(3.5f*g, 5.5f*g, 3.5f*g, 6.5f*g); seg(3.5f*g, 6.5f*g, 5.5f*g, 6.5f*g); seg(5.5f*g, 6.5f*g, 5.5f*g, 5.5f*g); seg(5.5f*g, 5.5f*g, 7.5f*g, 5.5f*g)
    seg(6.5f*g, 4.5f*g, 6.5f*g, 5.5f*g); seg(6.5f*g, 5.5f*g, 8.5f*g, 5.5f*g); seg(8.5f*g, 5.5f*g, 8.5f*g, 4.5f*g)
    seg(10.5f*g, 5.5f*g, 12.5f*g, 5.5f*g); seg(12.5f*g, 5.5f*g, 12.5f*g, 6.5f*g); seg(12.5f*g, 6.5f*g, 14.5f*g, 6.5f*g)

    // ── Junction nodes ───────────────────────────────────────────────────────
    val nodeColor = Color(0xFF3A7A50)
    fun node(x: Float, y: Float, r: Float = 2.2.dp.toPx()) =
        drawCircle(nodeColor, radius = r, center = Offset(x, y))

    node(2*g, 2.5f*g); node(4.5f*g, 2.5f*g, 1.8.dp.toPx()); node(6.5f*g, 2.5f*g, 1.8.dp.toPx())
    node(11.5f*g, 1.5f*g); node(2.5f*g, 4.5f*g, 1.8.dp.toPx()); node(4.5f*g, 4.5f*g)
    node(11.5f*g, 4.5f*g, 1.8.dp.toPx()); node(3.5f*g, 6.5f*g, 1.8.dp.toPx()); node(5.5f*g, 6.5f*g)
    node(6.5f*g, 4.5f*g, 1.8.dp.toPx()); node(8.5f*g, 4.5f*g, 1.8.dp.toPx()); node(12.5f*g, 6.5f*g, 1.8.dp.toPx())

    // ── Shield icon — centre ─────────────────────────────────────────────────
    val iconColor = Color(0xFF3A7A50)
    val iconStroke = Stroke(width = 1.1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    val cx = w * 0.50f; val cy = h * 0.42f
    val sw = 24.dp.toPx(); val sh = 27.dp.toPx()
    val shield = Path().apply {
        moveTo(cx, cy - sh / 2f)
        lineTo(cx + sw / 2f, cy - sh / 2f + sh * 0.15f)
        lineTo(cx + sw / 2f, cy - sh / 2f + sh * 0.65f)
        quadraticBezierTo(cx + sw / 2f, cy + sh / 2f, cx, cy + sh / 2f)
        quadraticBezierTo(cx - sw / 2f, cy + sh / 2f, cx - sw / 2f, cy - sh / 2f + sh * 0.65f)
        lineTo(cx - sw / 2f, cy - sh / 2f + sh * 0.15f)
        close()
    }
    drawPath(shield, iconColor, style = iconStroke)
    val ck = 4.dp.toPx()
    drawPath(Path().apply {
        moveTo(cx - ck * 1.5f, cy + 1.dp.toPx())
        lineTo(cx - ck * 0.3f, cy + ck)
        lineTo(cx + ck * 1.5f, cy - ck * 0.8f)
    }, iconColor, style = iconStroke)

    // ── Padlock — top-right ──────────────────────────────────────────────────
    val lx = w - 44.dp.toPx(); val ly = h * 0.30f
    val lw = 16.dp.toPx();     val lh = 12.dp.toPx()
    val lTop = ly + 8.dp.toPx()
    drawArc(iconColor, 180f, 180f, false,
        Offset(lx - lw / 2f + 3.dp.toPx(), ly - 5.dp.toPx()),
        Size(lw - 6.dp.toPx(), 10.dp.toPx()),
        style = Stroke(1.dp.toPx(), cap = StrokeCap.Round))
    drawRoundRect(iconColor, Offset(lx - lw / 2f, lTop), Size(lw, lh),
        CornerRadius(2.dp.toPx()), style = Stroke(1.dp.toPx()))
    drawCircle(iconColor, 1.6.dp.toPx(), Offset(lx, lTop + lh / 2f))

    // ── Fingerprint — bottom-left ────────────────────────────────────────────
    val fx = 26.dp.toPx(); val fy = h * 0.62f; val fr = 10.dp.toPx()
    listOf(fr * 0.28f, fr * 0.52f, fr * 0.74f, fr).forEach { r ->
        drawArc(iconColor, 180f, 180f, false,
            Offset(fx - r, fy - r), Size(r * 2f, r * 2f),
            style = Stroke(0.9.dp.toPx(), cap = StrokeCap.Round))
    }
    drawLine(iconColor, Offset(fx, fy), Offset(fx, fy + fr * 0.6f),
        0.9.dp.toPx(), StrokeCap.Round)
}

// ─────────────────────────────────────────────────────────────────────────────
// Login body — all form elements, no scroll
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LoginBody(
    isDark: Boolean,
    loginMode: Int,
    email: String,
    phone: String,
    password: String,
    passwordVisible: Boolean,
    errorMessage: String?,
    isLoading: Boolean,
    isGoogleLoading: Boolean,
    onModeChange: (Int) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordToggle: () -> Unit,
    onLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBg else LightBg)
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // ── Logo row ─────────────────────────────────────────────────────────
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

        }

        // ── Heading ──────────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text          = stringResource(R.string.ui_loginscreen_3),
                fontSize      = 24.sp,
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
                color         = if (isDark) DarkTextPri else LightTextPri
            )
            Text(
                text       = stringResource(R.string.ui_loginscreen_4),
                fontSize   = 14.sp,
                fontWeight = FontWeight.Normal,
                color      = if (isDark) DarkTextSec else LightTextSec
            )
        }

        // ── Mode switcher ─────────────────────────────────────────────────
        ModeSwitcher(loginMode = loginMode, isDark = isDark, onChange = onModeChange)

        // ── Fields ───────────────────────────────────────────────────────────
        if (loginMode == 0) {
            LoginTextField(
                value         = email,
                onValueChange = onEmailChange,
                label         = "Email Address",
                placeholder   = stringResource(R.string.ui_loginscreen_12),
                leadingIcon   = Icons.Outlined.Email,
                keyboardType  = KeyboardType.Email,
                isDark        = isDark
            )
            PasswordField(
                value              = password,
                onValueChange      = onPasswordChange,
                placeholder        = stringResource(R.string.ui_loginscreen_13),
                passwordVisible    = passwordVisible,
                onVisibilityToggle = onPasswordToggle,
                isDark             = isDark
            )
        } else {
            LoginTextField(
                value         = phone,
                onValueChange = onPhoneChange,
                label         = "Phone Number",
                placeholder   = stringResource(R.string.ui_loginscreen_14),
                leadingIcon   = Icons.Outlined.Phone,
                keyboardType  = KeyboardType.Phone,
                prefix        = "+91",
                isDark        = isDark
            )
        }

        // ── Error ─────────────────────────────────────────────────────────────
        if (errorMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.10f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(Icons.Outlined.Error, null,
                    tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                Text(localizedUiMessage(errorMessage),
                    fontSize = 11.sp, color = Color(0xFFEF4444))
            }
        }

        // ── Sign In button ────────────────────────────────────────────────────
        val ctaEnabled = if (loginMode == 0)
            email.isNotBlank() && password.isNotBlank() && !isLoading
        else
            phone.isNotBlank() && !isLoading

        Button(
            onClick   = onLogin,
            enabled   = ctaEnabled,
            modifier  = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape     = RoundedCornerShape(12.dp),
            colors    = ButtonDefaults.buttonColors(
                containerColor         = Green400,
                disabledContainerColor = Color(0xFF1E5C35)   // visible disabled — dark green
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color       = if (isDark) Color(0xFF0A1A0D) else Color.White,
                    modifier    = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.ui_loginscreen_6),
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (isDark) Color(0xFF0A1A0D) else Color.White)
            } else {
                Text(
                    text       = if (loginMode == 0) stringResource(R.string.ui_loginscreen_1)
                    else stringResource(R.string.login_send_otp),
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    // On Green400 bg: dark text in dark mode, white in light
                    color      = if (isDark) Color(0xFF051209) else Color.White
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) Color(0xFF0A2010) else Color.White.copy(alpha = 0.25f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("→", fontSize = 11.sp,
                        color = if (isDark) Green400 else Color.White)
                }
            }
        }

        // ── Divider ───────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HorizontalDivider(Modifier.weight(1f),
                color = if (isDark) DarkBorder else LightBorder)
            Text(
                stringResource(R.string.login_or_continue_with),
                fontSize   = 9.5.sp,
                fontWeight = FontWeight.Medium,
                color      = if (isDark) DarkTextSec else LightTextSec
            )
            HorizontalDivider(Modifier.weight(1f),
                color = if (isDark) DarkBorder else LightBorder)
        }

        // ── Google button ─────────────────────────────────────────────────────
        OutlinedButton(
            onClick   = onGoogleLogin,
            enabled   = !isLoading && !isGoogleLoading,
            modifier  = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape     = RoundedCornerShape(11.dp),
            border    = BorderStroke(1.5.dp, if (isDark) DarkBorder else LightBorder),
            colors    = ButtonDefaults.outlinedButtonColors(
                containerColor         = if (isDark) DarkSurfaceAlt else LightSurface,
                contentColor           = if (isDark) DarkTextPri else LightTextPri,
                disabledContentColor   = if (isDark) DarkTextTert else LightTextTert
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
        ) {
            if (isGoogleLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(15.dp),
                    strokeWidth = 2.dp,
                    color       = Green400
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.login_google_loading),
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            } else {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) Color(0xFF1E3D28) else Color(0xFFF0F0F0)
                        )
                        .border(
                            1.dp,
                            if (isDark) DarkBorder else Color(0xFFDDDDDD),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Green400 else Color(0xFF555555))
                }
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.login_continue_with_google),
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // ── Sign up link ──────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.ui_loginscreen_7),
                fontSize = 11.sp,
                color    = if (isDark) DarkTextSec else LightTextSec)
            Spacer(Modifier.width(4.dp))
            Text(
                stringResource(R.string.ui_loginscreen_8),
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                color      = Green400,
                modifier   = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onNavigateToSignup
                )
            )
        }

        // ── Spacer pushes badges to bottom ───────────────────────────────────
        Spacer(Modifier.weight(1f))

        // ── Trust badges ──────────────────────────────────────────────────────
        TrustBadges(isDark = isDark)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Mode switcher
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ModeSwitcher(
    loginMode: Int,
    isDark: Boolean,
    onChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDark) DarkTabTray else LightTabTray)
            .padding(3.dp)
    ) {
        listOf(
            stringResource(R.string.login_mode_email) to Icons.Outlined.Email,
            stringResource(R.string.login_mode_phone) to Icons.Outlined.Phone
        ).forEachIndexed { index, (label, icon) ->
            val active = loginMode == index

            val bgColor by animateColorAsState(
                when {
                    active && isDark  -> DarkTabActive
                    active && !isDark -> LightSurface
                    else              -> Color.Transparent
                },
                tween(180), "tabBg$index"
            )
            val contentColor by animateColorAsState(
                when {
                    active && isDark  -> Green400
                    active && !isDark -> Color(0xFF177A3C)
                    isDark            -> DarkTextTert
                    else              -> LightTextSec
                },
                tween(180), "tabText$index"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = { onChange(index) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(icon, null, tint = contentColor, modifier = Modifier.size(13.dp))
                    Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold,
                        color = contentColor)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Text field
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: String? = null,
    isDark: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text          = label.uppercase(),
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.9.sp,
            color         = if (isDark) DarkTextSec else LightTextSec
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = {
                Text(placeholder, fontSize = 12.sp,
                    color = if (isDark) DarkTextTert else LightTextTert)
            },
            leadingIcon   = {
                Icon(
                    leadingIcon, null,
                    tint     = if (value.isNotEmpty()) Green400
                    else if (isDark) DarkIconTint else LightTextTert,
                    modifier = Modifier.size(17.dp)
                )
            },
            prefix = prefix?.let { {
                Text("$it ", fontSize = 12.sp,
                    color = if (isDark) DarkTextSec else LightTextSec)
            } },
            modifier      = Modifier
                .fillMaxWidth()
                .height(50.dp),
            textStyle     = LocalTextStyle.current.copy(
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = if (isDark) DarkTextPri else LightTextPri
            ),
            colors = OutlinedTextFieldDefaults.colors(
                // Border
                focusedBorderColor      = Green400,
                unfocusedBorderColor    = if (isDark) DarkBorder else LightBorder,
                // Container (background of the field box)
                focusedContainerColor   = if (isDark) DarkSurface else LightSurface,
                unfocusedContainerColor = if (isDark) DarkSurface else LightSurface,
                // Cursor
                cursorColor             = Green400,
                // Text
                focusedTextColor        = if (isDark) DarkTextPri else LightTextPri,
                unfocusedTextColor      = if (isDark) DarkTextPri else LightTextPri,
            ),
            shape           = RoundedCornerShape(11.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = keyboardType
            ),
            singleLine      = true
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Password field
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    passwordVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    isDark: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text          = "PASSWORD",
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.9.sp,
            color         = if (isDark) DarkTextSec else LightTextSec
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = {
                Text(placeholder, fontSize = 12.sp,
                    color = if (isDark) DarkTextTert else LightTextTert)
            },
            leadingIcon   = {
                Icon(
                    Icons.Outlined.Lock, null,
                    tint     = if (value.isNotEmpty()) Green400
                    else if (isDark) DarkIconTint else LightTextTert,
                    modifier = Modifier.size(17.dp)
                )
            },
            trailingIcon  = {
                IconButton(
                    onClick  = onVisibilityToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (passwordVisible) Icons.Outlined.Visibility
                        else Icons.Outlined.VisibilityOff,
                        null,
                        tint     = if (isDark) DarkIconTint else LightTextTert,
                        modifier = Modifier.size(17.dp)
                    )
                }
            },
            modifier      = Modifier
                .fillMaxWidth()
                .height(50.dp),
            textStyle     = LocalTextStyle.current.copy(
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = if (isDark) DarkTextPri else LightTextPri
            ),
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Green400,
                unfocusedBorderColor    = if (isDark) DarkBorder else LightBorder,
                focusedContainerColor   = if (isDark) DarkSurface else LightSurface,
                unfocusedContainerColor = if (isDark) DarkSurface else LightSurface,
                cursorColor             = Green400,
                focusedTextColor        = if (isDark) DarkTextPri else LightTextPri,
                unfocusedTextColor      = if (isDark) DarkTextPri else LightTextPri,
            ),
            shape           = RoundedCornerShape(11.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            singleLine      = true
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Trust badges
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TrustBadges(isDark: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        data class Badge(
            val icon: androidx.compose.ui.graphics.vector.ImageVector,
            val line1: String,
            val line2: String
        )
        listOf(
            Badge(Icons.Outlined.Shield,       "E2E",       "Encrypted"),
            Badge(Icons.Outlined.VisibilityOff,"Zero Data", "Stored"),
            Badge(Icons.Outlined.Security,     "ISO",       "27001")
        ).forEach { badge ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isDark) DarkBadgeBg else LightBadgeBg)
                    .border(
                        1.5.dp,
                        if (isDark) DarkBadgeBorder else LightBorder,
                        RoundedCornerShape(9.dp)
                    )
                    .padding(vertical = 7.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    badge.icon, null,
                    tint     = if (isDark) Green400.copy(alpha = 0.7f) else Green400.copy(alpha = 0.65f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "${badge.line1}\n${badge.line2}",
                    fontSize   = 7.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isDark) DarkTextSec else LightTextSec,
                    textAlign  = TextAlign.Center,
                    lineHeight = 10.sp
                )
            }
        }
    }
}