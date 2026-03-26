package com.gosuraksha.app.ui.auth

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.presentation.auth.AuthViewModel
import com.gosuraksha.app.ui.components.FullScreenLoader

@Composable
fun VerifyPhoneScreen(
    viewModel: AuthViewModel,
    onOtpSent: () -> Unit,
    onBack: () -> Unit
) {
    val isDark = ColorTokens.LocalAppDarkMode.current
    val pendingPhone by viewModel.pendingPhone.collectAsStateWithLifecycle()
    val isSendingOtp by viewModel.isSendingOtp.collectAsStateWithLifecycle()
    val vmError by viewModel.otpError.collectAsStateWithLifecycle()

    var phone by remember(pendingPhone) {
        mutableStateOf(
            pendingPhone
                ?.removePrefix("+91")
                ?.removePrefix("91")
                ?.trim()
                ?: ""
        )
    }
    var localError by remember { mutableStateOf<String?>(null) }

    val error = vmError ?: localError

    BackHandler(onBack = onBack)

    LaunchedEffect(isSendingOtp) {
        if (BuildConfig.DEBUG) {
            Log.d("AUTH_DEBUG", "Loader state: $isSendingOtp")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AuthColors.bg(isDark))
        ) {
            AuthHero(
                isDark = isDark,
                title = "One more\nstep",
                subtitle = "Your account needs phone verification",
                height = 170.dp,
                showShield = true
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AuthCardSurface(isDark = isDark) {
                    Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Verify your phone",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.4).sp,
                            color = AuthColors.textPri(isDark)
                        )
                        Text(
                            text = "Add your phone number to complete setup",
                            fontSize = 12.sp,
                            color = AuthColors.textSec(isDark)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AuthColors.Accent.copy(alpha = 0.07f))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your phone number helps verify your identity and protect your account",
                            fontSize = 11.sp,
                            color = AuthColors.textSec(isDark),
                            lineHeight = 16.sp
                        )
                    }

                    AuthPhoneField(
                        value = phone,
                        onValueChange = { phone = it; localError = null },
                        isDark = isDark
                    )

                    AuthPrimaryButton(
                        text = "Send OTP",
                        onClick = {
                            localError = null
                            viewModel.sendOtp(
                                phone = "+91$phone",
                                onSuccess = onOtpSent,
                                onError = { localError = it }
                            )
                        },
                        enabled = phone.length == 10,
                        isLoading = isSendingOtp,
                        isDark = isDark
                    )

                    error?.let { AuthErrorRow(it, isDark) }

                    Text(
                        text = "Your phone number is encrypted and never shared",
                        fontSize = 10.sp,
                        color = AuthColors.textTert(isDark),
                        modifier = Modifier.fillMaxSize()
                    )

                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        FullScreenLoader(visible = isSendingOtp)
    }
}
