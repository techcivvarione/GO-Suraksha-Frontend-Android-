package com.gosuraksha.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.gosuraksha.app.core.*
import kotlinx.coroutines.launch

// ── Palette ───────────────────────────────────────────────────────────────────
private val LangBg        = Color(0xFF0D1117)
private val LangCard      = Color(0xFF161B22)
private val LangBorder    = Color(0xFF30363D)
private val CyberTeal     = Color(0xFF00E5C3)
private val TextPrimary   = Color(0xFFE6EDF3)
private val TextSecondary = Color(0xFF8B949E)

// ═════════════════════════════════════════════════════════════════════════════
// LanguageSwitcher — Dropdown Dialog
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun LanguageSwitcher(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentLang = getCurrentLanguage()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(LangCard)
                .border(1.dp, LangBorder, RoundedCornerShape(24.dp))
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LangBg)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Language, null, tint = CyberTeal, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        stringResource(R.string.profile_section_language),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.profile_language_description),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(LangBorder))

            // Language List
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
                            .background(if (isSelected) CyberTeal.copy(alpha = 0.08f) else Color.Transparent)
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Flag
                        Text(lang.flag, fontSize = 28.sp)
                        Spacer(Modifier.width(14.dp))

                        // Language Names
                        Column(Modifier.weight(1f)) {
                            Text(
                                lang.name,
                                color = if (isSelected) CyberTeal else TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                lang.nativeName,
                                color = if (isSelected) CyberTeal.copy(alpha = 0.7f) else TextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        // Checkmark
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = CyberTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    if (lang != SUPPORTED_LANGUAGES.last()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .padding(horizontal = 20.dp)
                                .background(LangBorder.copy(alpha = 0.5f))
                        )
                    }
                }
            }

            // Close Button
            Box(Modifier.fillMaxWidth().height(1.dp).background(LangBorder))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(stringResource(R.string.common_close), color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// LanguageRow — For ProfileScreen accordion
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun LanguageRow(onClick: () -> Unit) {
    val currentLang = getCurrentLanguage()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LangBg)
            .border(1.dp, LangBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flag + Language
        Text(currentLang.flag, fontSize = 24.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(currentLang.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(currentLang.nativeName, color = TextSecondary, fontSize = 12.sp)
        }

        // Change indicator
        Text(stringResource(R.string.common_edit), color = CyberTeal, fontSize = 13.sp)
    }
}