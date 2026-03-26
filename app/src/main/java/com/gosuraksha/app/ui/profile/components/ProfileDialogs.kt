package com.gosuraksha.app.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gosuraksha.app.R
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.ui.components.LanguageSwitcher

@Composable
fun ProfileDialogs(
    dialogState: ProfileDialogUiState,
    deleteConfirmationInput: String,
    currentUserName: String,
    isDeleting: Boolean,
    onDeleteConfirmationInputChange: (String) -> Unit,
    onDismissLogout: () -> Unit,
    onDismissDelete: () -> Unit,
    onDismissLanguage: () -> Unit,
    onConfirmLogout: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    if (dialogState.showLogoutDialog) {
        EdgyDialog(
            title = stringResource(R.string.dialog_logout_title),
            message = stringResource(R.string.dialog_logout_message),
            confirmText = stringResource(R.string.dialog_logout_confirm),
            dismissText = stringResource(R.string.dialog_logout_dismiss),
            isDanger = true,
            onConfirm = onConfirmLogout,
            onDismiss = onDismissLogout
        )
    }
    if (dialogState.showDeleteDialog) {
        DeleteAccountDialog(
            input = deleteConfirmationInput,
            currentUserName = currentUserName,
            isDeleting = isDeleting,
            onInputChange = onDeleteConfirmationInputChange,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete
        )
    }
    if (dialogState.showLanguageDialog) {
        LanguageSwitcher(onDismiss = onDismissLanguage)
    }
}

@Composable
private fun DeleteAccountDialog(
    input: String,
    currentUserName: String,
    isDeleting: Boolean,
    onInputChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = ColorTokens.LocalAppDarkMode.current
    val normalizedInput = input.trim()
    val normalizedName = currentUserName.trim()
    val isMatch = normalizedInput.isNotEmpty() && normalizedInput == normalizedName
    val showMismatch = normalizedInput.isNotEmpty() && !isMatch

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) PC.DarkCard else PC.LightCard, RoundedCornerShape(20.dp))
                .border(0.5.dp, PC.Red.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                .padding(22.dp)
        ) {
            Column {
                Icon(Icons.Default.Warning, null, tint = PC.Red, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.profile_delete_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PC.onSurf(isDark)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.profile_delete_dialog_message),
                    fontSize = 13.sp,
                    color = PC.subText(isDark),
                    lineHeight = 19.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.profile_delete_dialog_instruction),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PC.onSurf(isDark)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isDeleting,
                    label = { Text(stringResource(R.string.profile_delete_name_label)) },
                    isError = showMismatch,
                    supportingText = {
                        if (showMismatch) {
                            Text(stringResource(R.string.profile_delete_name_mismatch))
                        }
                    }
                )
                if (isDeleting) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.profile_delete_in_progress),
                        fontSize = 12.sp,
                        color = PC.subText(isDark)
                    )
                }
                Spacer(Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isDeleting,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, if (isDark) PC.DarkBorder else PC.LightBorder)
                    ) {
                        Text(stringResource(R.string.common_cancel), fontSize = 13.sp)
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = isMatch && !isDeleting,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PC.Red,
                            disabledContainerColor = PC.Red.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.common_delete),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EdgyDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    isDanger: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = ColorTokens.LocalAppDarkMode.current
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.fillMaxWidth().background(if (isDark) PC.DarkCard else PC.LightCard, RoundedCornerShape(20.dp))
                .border(
                    0.5.dp,
                    if (isDanger) PC.Red.copy(0.25f) else if (isDark) PC.DarkBorder else PC.LightBorder,
                    RoundedCornerShape(20.dp)
                )
                .padding(22.dp)
        ) {
            Column {
                if (isDanger) {
                    Icon(Icons.Default.Warning, null, tint = PC.Red, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(10.dp))
                }
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PC.onSurf(isDark))
                Spacer(Modifier.height(10.dp))
                Text(message, fontSize = 13.sp, color = PC.subText(isDark), lineHeight = 19.sp)
                Spacer(Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, if (isDark) PC.DarkBorder else PC.LightBorder)
                    ) { Text(dismissText, fontSize = 13.sp) }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDanger) PC.Red else PC.Green),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(confirmText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
