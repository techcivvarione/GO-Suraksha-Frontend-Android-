package com.gosuraksha.app.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R
import com.gosuraksha.app.design.tokens.ColorTokens

@Composable
fun SecuritySection(
    isDark: Boolean,
    securityExpanded: Boolean,
    currentPass: String,
    newPass: String,
    confirmPass: String,
    showCurrentPass: Boolean,
    showNewPass: Boolean,
    showConfirmPass: Boolean,
    securityMessage: String?,
    onSecurityToggle: () -> Unit,
    onCurrentPassChange: (String) -> Unit,
    onNewPassChange: (String) -> Unit,
    onConfirmPassChange: (String) -> Unit,
    onToggleCurrentPass: () -> Unit,
    onToggleNewPass: () -> Unit,
    onToggleConfirmPass: () -> Unit,
    onUpdatePassword: () -> Unit,
    onShowLanguage: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ProfileSectionCard(header = "SECURITY STATUS", isDark = isDark) {
        ProfileRow(Icons.Default.Lock, PC.iconBgRed(isDark), PC.Red, "Password Breach", "3 found", PC.Red, isDark, false)
        ProfileRow(Icons.Default.Shield, PC.iconBgGreen(isDark), PC.Green, "Two-Factor Auth", "Active", PC.Green, isDark, false)
        ProfileRow(Icons.Default.Email, PC.iconBgBlue(isDark), PC.Blue, "Email Monitor", "Live", PC.Blue, isDark, true)
    }

    ProfileSectionCard(header = stringResource(R.string.profile_section_security).uppercase(), isDark = isDark) {
        ProfileExpandRow(
            icon = Icons.Outlined.Lock,
            title = stringResource(R.string.profile_section_security),
            expanded = securityExpanded,
            onToggle = onSecurityToggle,
            isDark = isDark,
            isLast = true
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileTextField(
                    currentPass,
                    onCurrentPassChange,
                    stringResource(R.string.profile_field_current_password),
                    Icons.Outlined.Lock,
                    isDark,
                    visualTransformation = if (showCurrentPass) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onToggleCurrentPass) {
                            Icon(if (showCurrentPass) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null, tint = PC.subText(isDark))
                        }
                    }
                )
                ProfileTextField(
                    newPass,
                    onNewPassChange,
                    stringResource(R.string.profile_field_new_password),
                    Icons.Outlined.Lock,
                    isDark,
                    visualTransformation = if (showNewPass) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onToggleNewPass) {
                            Icon(if (showNewPass) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null, tint = PC.subText(isDark))
                        }
                    }
                )
                if (newPass.isNotEmpty()) NeonPasswordStrength(newPass)
                ProfileTextField(
                    confirmPass,
                    onConfirmPassChange,
                    stringResource(R.string.profile_field_confirm_password),
                    Icons.Outlined.Lock,
                    isDark,
                    visualTransformation = if (showConfirmPass) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onToggleConfirmPass) {
                            Icon(if (showConfirmPass) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null, tint = PC.subText(isDark))
                        }
                    }
                )
                Button(
                    onClick = onUpdatePassword,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PC.Green, contentColor = Color(0xFF051209)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 0.dp)
                ) {
                    Text(stringResource(R.string.profile_btn_update_password), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                securityMessage?.let { ProfileSuccessBanner(it, isDark) }
            }
        }
    }

    ProfileSectionCard(header = "TRUSTED DEVICES", isDark = isDark) {
        ProfileRow(Icons.Default.PhoneAndroid, PC.iconBgBlue(isDark), PC.Blue, "This Device", "Current", PC.Green, isDark, false)
        ProfileRow(Icons.Default.Laptop, PC.iconBgSlate(isDark), PC.subText(isDark), "Chrome on Windows", "2d ago", PC.subText(isDark), isDark, true)
    }

    ProfileSectionCard(header = "MORE", isDark = isDark) {
        ProfileActionRow(Icons.Outlined.Language, PC.iconBgBlue(isDark), PC.Blue, stringResource(R.string.home_quick_language), isDark, onClick = onShowLanguage)
        ProfileActionRow(Icons.Outlined.Notifications, PC.iconBgAmber(isDark), PC.Amber, "Notification Settings", isDark, onClick = {})
        ProfileActionRow(Icons.Outlined.Help, PC.iconBgGreen(isDark), PC.Green, stringResource(R.string.profile_help_support), isDark, onClick = {})
        ProfileActionRow(Icons.Outlined.Info, PC.iconBgSlate(isDark), PC.subText(isDark), stringResource(R.string.profile_about_us), isDark, isLast = true, onClick = {})
    }

    ProfileDangerZone(isDark = isDark, onLogoutClick = onLogoutClick, onDeleteClick = onDeleteClick)
}

@Composable
fun ProfileRow(icon: ImageVector, iconBg: Color, iconTint: Color, title: String, rightText: String = "", rightColor: Color = Color.Unspecified, isDark: Boolean, isLast: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Text(title, modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = PC.onSurf(isDark))
        if (rightText.isNotBlank()) {
            Text(rightText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (rightColor == Color.Unspecified) PC.subText(isDark) else rightColor)
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = PC.muted(isDark), modifier = Modifier.size(15.dp))
    }
    if (!isLast) Box(modifier = Modifier.fillMaxWidth().padding(start = 59.dp).height(0.5.dp).background(if (isDark) PC.DarkBorder else PC.LightBorder))
}

@Composable
fun ProfileActionRow(icon: ImageVector, iconBg: Color, iconTint: Color, title: String, isDark: Boolean, highlight: Boolean = false, isLast: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Text(title, modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (highlight) PC.Green else PC.onSurf(isDark))
        Icon(Icons.Outlined.ChevronRight, null, tint = PC.muted(isDark), modifier = Modifier.size(15.dp))
    }
    if (!isLast) Box(modifier = Modifier.fillMaxWidth().padding(start = 59.dp).height(0.5.dp).background(if (isDark) PC.DarkBorder else PC.LightBorder))
}

@Composable
fun ProfileExpandRow(icon: ImageVector, title: String, expanded: Boolean, onToggle: () -> Unit, isDark: Boolean, isLast: Boolean = false, content: @Composable () -> Unit) {
    val rotation by animateFloatAsState(if (expanded) 90f else 0f, tween(260), label = "chev")
    Column {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(if (expanded) PC.iconBgGreen(isDark) else PC.iconBgSlate(isDark)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = if (expanded) PC.Green else PC.subText(isDark), modifier = Modifier.size(16.dp))
            }
            Text(title, modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (expanded) PC.Green else PC.onSurf(isDark))
            Icon(Icons.Outlined.ChevronRight, null, tint = PC.muted(isDark), modifier = Modifier.size(15.dp).graphicsLayer { rotationZ = rotation })
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(200)) + expandVertically(tween(280, easing = EaseOutQuart)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
        ) {
            Column(modifier = Modifier.background(if (isDark) PC.DarkCard else Color(0xFFF9FAFB))) {
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(if (isDark) PC.DarkBorder else PC.LightBorder))
                content()
            }
        }
        if (!isLast) Box(modifier = Modifier.fillMaxWidth().padding(start = 59.dp).height(0.5.dp).background(if (isDark) PC.DarkBorder else PC.LightBorder))
    }
}

@Composable
fun ProfileDangerZone(isDark: Boolean, onLogoutClick: () -> Unit, onDeleteClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (isDark) PC.DarkCard else PC.LightCard)
            .border(0.5.dp, PC.Red.copy(alpha = 0.18f), RoundedCornerShape(16.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(Icons.Outlined.Warning, null, tint = PC.Red, modifier = Modifier.size(15.dp))
            Text(stringResource(R.string.profile_danger_zone), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PC.Red, letterSpacing = 1.sp)
        }
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PC.Red),
            border = BorderStroke(0.5.dp, PC.Red.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.Logout, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.profile_btn_logout_all), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PC.Red),
            border = BorderStroke(0.5.dp, PC.Red.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.DeleteForever, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.profile_delete_title), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun NeonPasswordStrength(password: String) {
    val strength = when {
        password.length >= 12 && password.any { it.isUpperCase() } && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 4
        password.length >= 10 && password.any { it.isUpperCase() } && password.any { it.isDigit() } -> 3
        password.length >= 8 -> 2
        else -> 1
    }
    val label = listOf("", stringResource(R.string.profile_password_weak), stringResource(R.string.profile_password_fair), stringResource(R.string.profile_password_strong), stringResource(R.string.profile_password_very_strong))[strength]
    val color = listOf(Color.Transparent, ColorTokens.error(), ColorTokens.warning(), ColorTokens.success(), ColorTokens.accent())[strength]
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(4) { i ->
                Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(if (i < strength) color else ColorTokens.border()))
            }
        }
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
