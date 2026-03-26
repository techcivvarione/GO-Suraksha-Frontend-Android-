package com.gosuraksha.app.ui.signup.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.ui.signup.model.SignupGreen400

@Composable
fun OtpVerificationSection(
    isVisible: Boolean,
    emailVerified: Boolean,
    otp: String,
    isVerifyingOtp: Boolean,
    otpError: String?,
    isDark: Boolean,
    onOtpChange: (String) -> Unit,
    onVerifyOtp: () -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!emailVerified) {
                SignupTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6) onOtpChange(it) },
                    label = "Email OTP",
                    placeholder = "6-digit code",
                    leadingIcon = Icons.Outlined.Lock,
                    keyboardType = KeyboardType.Number,
                    isDark = isDark
                )
                Button(
                    onClick = onVerifyOtp,
                    enabled = otp.length == 6 && !isVerifyingOtp && !emailVerified,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SignupGreen400,
                        disabledContainerColor = if (isDark) Color(0xFF1E5C35) else Color(0xFFB0D8C0)
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    if (isVerifyingOtp) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.size(7.dp))
                        Text("Verifying...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    } else {
                        Text("Verify OTP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SignupGreen400.copy(alpha = if (isDark) 0.10f else 0.08f))
                        .border(1.dp, SignupGreen400.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, null, tint = SignupGreen400, modifier = Modifier.size(15.dp))
                    Text("Email verified successfully", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SignupGreen400)
                }
            }
            if (otpError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.10f))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.Error, null, tint = Color(0xFFEF4444), modifier = Modifier.size(13.dp))
                    Text(otpError, fontSize = 11.sp, color = Color(0xFFEF4444))
                }
            }
        }
    }
}
