package com.gosuraksha.app.ui.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R

@Composable
fun LoginForm(
    isDark: Boolean,
    state: LoginFormState,
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
        modifier = Modifier.fillMaxSize().background(if (isDark) DarkBg else LightBg).padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.ui_loginscreen_3), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = if (isDark) DarkTextPri else LightTextPri)
            Text(stringResource(R.string.ui_loginscreen_4), fontSize = 14.sp, color = if (isDark) DarkTextSec else LightTextSec)
        }
        ModeSwitcher(loginMode = state.loginMode, isDark = isDark, onChange = onModeChange)
        if (state.loginMode == 0) {
            LoginTextField(state.email, onEmailChange, "Email Address", stringResource(R.string.ui_loginscreen_12), Icons.Outlined.Email, KeyboardType.Email, null, isDark)
            PasswordField(state.password, onPasswordChange, stringResource(R.string.ui_loginscreen_13), state.passwordVisible, onPasswordToggle, isDark)
        } else {
            LoginTextField(state.phone, onPhoneChange, "Phone Number", stringResource(R.string.ui_loginscreen_14), Icons.Outlined.Phone, KeyboardType.Phone, "+91", isDark)
        }
        LoginStates(state.errorMessage)
        val ctaEnabled = if (state.loginMode == 0) state.email.isNotBlank() && state.password.isNotBlank() && !state.isLoading else state.phone.isNotBlank() && !state.isLoading
        Button(
            onClick = onLogin,
            enabled = ctaEnabled,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green400, disabledContainerColor = Color(0xFF1E5C35)),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = if (isDark) Color(0xFF0A1A0D) else Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.ui_loginscreen_6), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF0A1A0D) else Color.White)
            } else {
                Text(if (state.loginMode == 0) stringResource(R.string.ui_loginscreen_1) else stringResource(R.string.login_send_otp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF051209) else Color.White)
                Spacer(Modifier.size(8.dp))
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(if (isDark) Color(0xFF0A2010) else Color.White.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                    Text("→", fontSize = 11.sp, color = if (isDark) Green400 else Color.White)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HorizontalDivider(Modifier.weight(1f), color = if (isDark) DarkBorder else LightBorder)
            Text(stringResource(R.string.login_or_continue_with), fontSize = 9.5.sp, color = if (isDark) DarkTextSec else LightTextSec)
            HorizontalDivider(Modifier.weight(1f), color = if (isDark) DarkBorder else LightBorder)
        }
        GoogleAuthSection(isDark, state.isGoogleLoading, !state.isLoading && !state.isGoogleLoading, onGoogleLogin)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.ui_loginscreen_7), fontSize = 11.sp, color = if (isDark) DarkTextSec else LightTextSec)
            Spacer(Modifier.size(4.dp))
            Text(stringResource(R.string.ui_loginscreen_8), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Green400, modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onNavigateToSignup))
        }
        Spacer(Modifier.weight(1f))
        TrustBadges(isDark)
    }
}

@Composable
private fun ModeSwitcher(loginMode: Int, isDark: Boolean, onChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (isDark) DarkTabTray else LightTabTray).padding(3.dp)) {
        listOf(stringResource(R.string.login_mode_email) to Icons.Outlined.Email, stringResource(R.string.login_mode_phone) to Icons.Outlined.Phone).forEachIndexed { index, (label, icon) ->
            val active = loginMode == index
            val bgColor by animateColorAsState(if (active && isDark) DarkTabActive else if (active) LightSurface else Color.Transparent, tween(180), label = "tabBg$index")
            val contentColor by animateColorAsState(when { active && isDark -> Green400; active -> Color(0xFF177A3C); isDark -> DarkTextTert; else -> LightTextSec }, tween(180), label = "tabText$index")
            Box(modifier = Modifier.weight(1f).height(28.dp).clip(RoundedCornerShape(8.dp)).background(bgColor).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onChange(index) }, contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(icon, null, tint = contentColor, modifier = Modifier.size(13.dp))
                    Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
                }
            }
        }
    }
}

@Composable
private fun LoginTextField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String, leadingIcon: ImageVector, keyboardType: KeyboardType, prefix: String?, isDark: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isDark) DarkTextSec else LightTextSec)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 12.sp, color = if (isDark) DarkTextTert else LightTextTert) },
            leadingIcon = { Icon(leadingIcon, null, tint = if (value.isNotEmpty()) Green400 else if (isDark) DarkIconTint else LightTextTert, modifier = Modifier.size(17.dp)) },
            prefix = prefix?.let { { Text("$it ", fontSize = 12.sp, color = if (isDark) DarkTextSec else LightTextSec) } },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (isDark) DarkTextPri else LightTextPri),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green400,
                unfocusedBorderColor = if (isDark) DarkBorder else LightBorder,
                focusedContainerColor = if (isDark) DarkSurface else LightSurface,
                unfocusedContainerColor = if (isDark) DarkSurface else LightSurface,
                cursorColor = Green400,
                focusedTextColor = if (isDark) DarkTextPri else LightTextPri,
                unfocusedTextColor = if (isDark) DarkTextPri else LightTextPri
            ),
            shape = RoundedCornerShape(11.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
    }
}

@Composable
private fun PasswordField(value: String, onValueChange: (String) -> Unit, placeholder: String, passwordVisible: Boolean, onVisibilityToggle: () -> Unit, isDark: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text("PASSWORD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isDark) DarkTextSec else LightTextSec)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 12.sp, color = if (isDark) DarkTextTert else LightTextTert) },
            leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = if (value.isNotEmpty()) Green400 else if (isDark) DarkIconTint else LightTextTert, modifier = Modifier.size(17.dp)) },
            trailingIcon = {
                IconButton(onClick = onVisibilityToggle, modifier = Modifier.size(36.dp)) {
                    Icon(if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, null, tint = if (isDark) DarkIconTint else LightTextTert, modifier = Modifier.size(17.dp))
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (isDark) DarkTextPri else LightTextPri),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green400,
                unfocusedBorderColor = if (isDark) DarkBorder else LightBorder,
                focusedContainerColor = if (isDark) DarkSurface else LightSurface,
                unfocusedContainerColor = if (isDark) DarkSurface else LightSurface,
                cursorColor = Green400,
                focusedTextColor = if (isDark) DarkTextPri else LightTextPri,
                unfocusedTextColor = if (isDark) DarkTextPri else LightTextPri
            ),
            shape = RoundedCornerShape(11.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
    }
}

@Composable
private fun TrustBadges(isDark: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        data class Badge(val icon: ImageVector, val line1: String, val line2: String)
        listOf(
            Badge(Icons.Outlined.Shield, "E2E", "Encrypted"),
            Badge(Icons.Outlined.VisibilityOff, "Zero Data", "Stored"),
            Badge(Icons.Outlined.Security, "ISO", "27001")
        ).forEach { badge ->
            Column(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).background(if (isDark) DarkBadgeBg else LightBadgeBg).border(1.5.dp, if (isDark) DarkBadgeBorder else LightBorder, RoundedCornerShape(9.dp)).padding(vertical = 7.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(badge.icon, null, tint = if (isDark) Green400.copy(alpha = 0.7f) else Green400.copy(alpha = 0.65f), modifier = Modifier.size(14.dp))
                Text("${badge.line1}\n${badge.line2}", fontSize = 7.5.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) DarkTextSec else LightTextSec, textAlign = TextAlign.Center, lineHeight = 10.sp)
            }
        }
    }
}
