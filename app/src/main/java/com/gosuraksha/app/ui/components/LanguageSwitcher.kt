package com.gosuraksha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gosuraksha.app.R
import com.gosuraksha.app.core.SUPPORTED_LANGUAGES
import com.gosuraksha.app.core.changeLanguage
import com.gosuraksha.app.core.getCurrentLanguage
import com.gosuraksha.app.design.tokens.ColorTokens
import kotlinx.coroutines.launch

@Composable
fun LanguageSwitcher(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentLang = getCurrentLanguage()
    val accent = ColorTokens.accent()
    val cardColor = ColorTokens.surface()
    val headerColor = ColorTokens.surfaceVariant()
    val borderColor = ColorTokens.border()
    val primaryText = ColorTokens.textPrimary()
    val secondaryText = ColorTokens.textSecondary()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(cardColor)
                .border(1.dp, borderColor, RoundedCornerShape(24.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Language, null, tint = accent, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        stringResource(R.string.profile_section_language),
                        color = primaryText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.profile_language_description),
                        color = secondaryText,
                        fontSize = 12.sp
                    )
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(borderColor))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SUPPORTED_LANGUAGES.forEach { lang ->
                    val isSelected = lang.code == currentLang.code

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    changeLanguage(context, lang.code)
                                    onDismiss()
                                }
                            }
                            .background(if (isSelected) accent.copy(alpha = 0.08f) else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(lang.flag, fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                lang.name,
                                color = if (isSelected) accent else primaryText,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                lang.nativeName,
                                color = if (isSelected) accent.copy(alpha = 0.7f) else secondaryText,
                                fontSize = 13.sp
                            )
                        }

                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    if (lang != SUPPORTED_LANGUAGES.last()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .padding(horizontal = 16.dp)
                                .background(borderColor.copy(alpha = 0.5f))
                        )
                    }
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(borderColor))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(stringResource(R.string.common_close), color = secondaryText, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun LanguageRow(onClick: () -> Unit) {
    val currentLang = getCurrentLanguage()
    val accent = ColorTokens.accent()
    val surfaceVariant = ColorTokens.surfaceVariant()
    val borderColor = ColorTokens.border()
    val primaryText = ColorTokens.textPrimary()
    val secondaryText = ColorTokens.textSecondary()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceVariant)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(currentLang.flag, fontSize = 24.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(currentLang.name, color = primaryText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(currentLang.nativeName, color = secondaryText, fontSize = 12.sp)
        }

        Text(stringResource(R.string.common_edit), color = accent, fontSize = 13.sp)
    }
}
