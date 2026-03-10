package com.gosuraksha.app.ui.auth

import android.app.Activity
import androidx.compose.ui.res.stringResource
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.gosuraksha.app.R
import com.gosuraksha.app.auth.GoogleSignInManager
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.presentation.auth.AuthViewModel
import com.gosuraksha.app.ui.components.localizedUiMessage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    var loginMode by remember { mutableStateOf(0) }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val googleSignInManager = remember(context) { GoogleSignInManager(context) }

    val googleLoginLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode != Activity.RESULT_OK) {
            isGoogleLoading = false
            errorMessage = "Google sign-in canceled"
            return@rememberLauncherForActivityResult
        }

        val signInTask = GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
        try {
            val account = signInTask.getResult(ApiException::class.java)
            val token = account.idToken
            if (token.isNullOrBlank()) {
                isGoogleLoading = false
                errorMessage = "Invalid Google token"
                return@rememberLauncherForActivityResult
            }

            viewModel.loginWithGoogle(
                idToken = token,
                onSuccess = {
                    isGoogleLoading = false
                    onLoginSuccess()
                },
                onError = {
                    isGoogleLoading = false
                    errorMessage = it
                }
            )
        } catch (_: ApiException) {
            isGoogleLoading = false
            errorMessage = "Unable to sign in with Google"
        }
    }

    LaunchedEffect(Unit) {
        delay(150)
        showContent = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorTokens.background())
    ) {
        // Premium grid background
        PremiumGridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // Clean logo (no glow)
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -30 }
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(70.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Title
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, 100)) + slideInVertically(tween(500, 100)) { -20 }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        stringResource(R.string.ui_loginscreen_3),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTokens.textPrimary(),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        stringResource(R.string.ui_loginscreen_4),
                        fontSize = 13.sp,
                        color = ColorTokens.textSecondary(),
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // Login Card
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { 30 }
            ) {
                CleanLoginCard(
                    loginMode = loginMode,
                    email = email,
                    phone = phone,
                    password = password,
                    passwordVisible = passwordVisible,
                    errorMessage = errorMessage,
                    isLoading = isLoading,
                    isGoogleLoading = isGoogleLoading,
                    onModeChange = { loginMode = it; errorMessage = null },
                    onEmailChange = { email = it; errorMessage = null },
                    onPhoneChange = { phone = it; errorMessage = null },
                    onPasswordChange = { password = it; errorMessage = null },
                    onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                    onLogin = {
                        if (loginMode == 0 && email.isNotBlank() && password.isNotBlank()) {
                            isLoading = true
                            viewModel.login(
                                identifier = email,
                                password = password,
                                onSuccess = { isLoading = false; onLoginSuccess() },
                                onError = { isLoading = false; errorMessage = it }
                            )
                        }
                    },
                    onGoogleLogin = {
                        isGoogleLoading = true
                        errorMessage = null
                        googleLoginLauncher.launch(googleSignInManager.client.signInIntent)
                    },
                    onNavigateToSignup = onNavigateToSignup
                )
            }

            Spacer(Modifier.height(24.dp))

            // Security badges
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, 400))
            ) {
                TrustBadges()
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
private fun PremiumGridBackground() {
    val infinite = rememberInfiniteTransition(label = "grid")
    val offsetY by infinite.animateFloat(
        0f, 40f,
        infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "scroll"
    )

    Canvas(modifier = Modifier.fillMaxSize().alpha(0.03f)) {
        val spacing = 40f
        val strokeWidth = 1f

        // Vertical lines
        var x = 0f
        while (x <= size.width) {
            drawLine(
                color = Color.White,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = strokeWidth
            )
            x += spacing
        }

        // Horizontal lines with animation
        var y = offsetY % spacing
        while (y <= size.height) {
            drawLine(
                color = Color.White,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth
            )
            y += spacing
        }
    }
}

@Composable
private fun CleanLoginCard(
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
    onPasswordVisibilityToggle: () -> Unit,
    onLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorTokens.surface())
            .border(0.5.dp, ColorTokens.border().copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode tabs
        MinimalModeSwitcher(loginMode, onModeChange)

        // Fields
        if (loginMode == 0) {
            CleanTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.ui_loginscreen_12),
                icon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email
            )
            CleanPasswordField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.ui_loginscreen_13),
                passwordVisible = passwordVisible,
                onVisibilityToggle = onPasswordVisibilityToggle
            )
        } else {
            CleanTextField(
                value = phone,
                onValueChange = onPhoneChange,
                placeholder = stringResource(R.string.ui_loginscreen_14),
                icon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone,
                prefix = "+91"
            )
        }

        // Error
        errorMessage?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ColorTokens.error().copy(alpha = 0.08f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Error, null, tint = ColorTokens.error(), modifier = Modifier.size(16.dp))
                Text(localizedUiMessage(it), fontSize = 12.sp, color = ColorTokens.error())
            }
        }

        // Button
        val enabled = if (loginMode == 0) {
            email.isNotBlank() && password.isNotBlank() && !isLoading
        } else {
            phone.isNotBlank() && !isLoading
        }

        Button(
            onClick = onLogin,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorTokens.accent(),
                disabledContainerColor = ColorTokens.accent().copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(10.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
        ) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        stringResource(R.string.ui_loginscreen_6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    if (loginMode == 0) stringResource(R.string.ui_loginscreen_1) else stringResource(R.string.login_send_otp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = ColorTokens.border().copy(alpha = 0.35f))
            Text(
                text = stringResource(R.string.login_or_continue_with),
                fontSize = 11.sp,
                color = ColorTokens.textSecondary()
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = ColorTokens.border().copy(alpha = 0.35f))
        }

        OutlinedButton(
            onClick = onGoogleLogin,
            enabled = !isLoading && !isGoogleLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, ColorTokens.border().copy(alpha = 0.4f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ColorTokens.textPrimary(),
                disabledContentColor = ColorTokens.textSecondary()
            )
        ) {
            if (isGoogleLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.login_google_loading),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.login_continue_with_google),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Sign up link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.ui_loginscreen_7),
                fontSize = 12.sp,
                color = ColorTokens.textSecondary()
            )
            Spacer(Modifier.width(4.dp))
            Text(
                stringResource(R.string.ui_loginscreen_8),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorTokens.accent(),
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onNavigateToSignup
                )
            )
        }
    }
}

@Composable
private fun MinimalModeSwitcher(selectedMode: Int, onModeChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ColorTokens.surfaceVariant())
            .padding(3.dp)
    ) {
        ModeButton(
            label = stringResource(R.string.login_mode_email),
            icon = Icons.Outlined.Email,
            selected = selectedMode == 0,
            onClick = { onModeChange(0) },
            modifier = Modifier.weight(1f)
        )
        ModeButton(
            label = stringResource(R.string.login_mode_phone),
            icon = Icons.Outlined.Phone,
            selected = selectedMode == 1,
            onClick = { onModeChange(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val bg by animateColorAsState(
        if (selected) ColorTokens.background() else Color.Transparent,
        tween(200),
        label = "bg"
    )
    val color by animateColorAsState(
        if (selected) ColorTokens.accent() else ColorTokens.textSecondary(),
        tween(200),
        label = "color"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(7.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

@Composable
private fun CleanTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontSize = 13.sp, color = ColorTokens.textSecondary().copy(alpha = 0.5f)) },
        leadingIcon = {
            Icon(icon, null, tint = ColorTokens.textSecondary(), modifier = Modifier.size(18.dp))
        },
        prefix = prefix?.let { { Text("$it ", fontSize = 13.sp, color = ColorTokens.textSecondary()) } },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = ColorTokens.textPrimary()),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ColorTokens.accent(),
            unfocusedBorderColor = ColorTokens.border().copy(alpha = 0.3f),
            cursorColor = ColorTokens.accent()
        ),
        shape = RoundedCornerShape(10.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
private fun CleanPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    passwordVisible: Boolean,
    onVisibilityToggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontSize = 13.sp, color = ColorTokens.textSecondary().copy(alpha = 0.5f)) },
        leadingIcon = {
            Icon(Icons.Outlined.Lock, null, tint = ColorTokens.textSecondary(), modifier = Modifier.size(18.dp))
        },
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle, modifier = Modifier.size(36.dp)) {
                Text(if (passwordVisible) "👁️" else "🙈", fontSize = 18.sp)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = ColorTokens.textPrimary()),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ColorTokens.accent(),
            unfocusedBorderColor = ColorTokens.border().copy(alpha = 0.3f),
            cursorColor = ColorTokens.accent()
        ),
        shape = RoundedCornerShape(10.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}

@Composable
private fun TrustBadges() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            stringResource(R.string.ui_loginscreen_9),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = ColorTokens.textSecondary(),
            letterSpacing = 0.5.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TrustBadge(Icons.Outlined.Shield, stringResource(R.string.ui_loginscreen_15), Modifier.weight(1f))
            TrustBadge(Icons.Outlined.VisibilityOff, stringResource(R.string.ui_loginscreen_16), Modifier.weight(1f))
            TrustBadge(Icons.Outlined.Security, stringResource(R.string.ui_loginscreen_17), Modifier.weight(1f))
        }
    }
}

@Composable
private fun TrustBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ColorTokens.surface())
            .border(0.5.dp, ColorTokens.border().copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, tint = ColorTokens.accent(), modifier = Modifier.size(20.dp))
        Text(
            text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = ColorTokens.textPrimary(),
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}
