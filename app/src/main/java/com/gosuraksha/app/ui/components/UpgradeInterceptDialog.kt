package com.gosuraksha.app.ui.components

// =============================================================================
// UpgradeInterceptDialog.kt — Global upgrade prompt shown whenever a user
// hits a plan limit or tries to access a restricted feature.
//
// Usage:
//   UpgradeInterceptDialog(
//       visible  = showUpgrade,
//       trigger  = UpgradeTrigger.ScanLimitFree,   // or .ScanLimitPro / .FeatureLocked
//       onDismiss = { showUpgrade = false },
//       onSelectPro   = { /* open billing for PRO */ },
//       onSelectUltra = { /* open billing for ULTRA */ },
//   )
// =============================================================================

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ── Colour palette (matches app-wide dark theme) ──────────────────────────────
private val UpgDark    = Color(0xFF0A0F1C)
private val UpgCard    = Color(0xFF0F1A2E)
private val UpgBorder  = Color(0xFF1E2D45)
private val UpgGreen   = Color(0xFF00E676)
private val UpgGold    = Color(0xFFFFD700)
private val UpgMuted   = Color(0xFF8888AA)
private val UpgText    = Color(0xFFEEEEFF)
private val UpgRed     = Color(0xFFFF4444)

/** Reason why the upgrade dialog was shown — drives title/subtitle copy. */
enum class UpgradeTrigger {
    /** FREE user ran out of their limited quota. */
    ScanLimitFree,
    /** PRO user hit a daily cap (shouldn't normally be shown for ULTRA). */
    ScanLimitPro,
    /** Feature is gated (AI explain, breach details, etc.). */
    FeatureLocked,
    /** Generic upgrade prompt with no specific trigger. */
    Generic,
}

private data class PlanCard(
    val name: String,
    val tagline: String,
    val price: String,
    val accentColor: Color,
    val features: List<Pair<ImageVector, String>>,
    val isRecommended: Boolean = false,
)

private val PRO_CARD = PlanCard(
    name        = "GO PRO",
    tagline     = "Daily protection + AI insights",
    price       = "₹199/mo",
    accentColor = UpgGreen,
    features    = listOf(
        Icons.Outlined.Shield        to "Scan as many messages & links as you need",
        Icons.Outlined.AutoAwesome   to "AI explains exactly why something is risky",
        Icons.Outlined.Contacts      to "Add 3 trusted contacts for family safety",
        Icons.Outlined.Notifications to "Get warned before you get scammed",
        Icons.Outlined.FlashOn       to "Priority SOS — faster fraud response",
    ),
    isRecommended = true,
)

private val ULTRA_CARD = PlanCard(
    name        = "GO ULTRA",
    tagline     = "Protect your entire family in real-time",
    price       = "₹499/mo",
    accentColor = UpgGold,
    features    = listOf(
        Icons.Outlined.Shield        to "Everything in GO PRO — unlimited",
        Icons.Outlined.AutoAwesome   to "Unlimited AI — no daily cap ever",
        Icons.Outlined.Contacts      to "6 trusted contacts + family dashboard",
        Icons.Outlined.Notifications to "Real-time alerts for you & your family",
        Icons.Outlined.Star          to "Family threat monitor — see who needs help",
    ),
)

@Composable
fun UpgradeInterceptDialog(
    visible: Boolean,
    trigger: UpgradeTrigger = UpgradeTrigger.Generic,
    onDismiss: () -> Unit,
    onSelectPro: () -> Unit,
    onSelectUltra: () -> Unit,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress      = true,
            dismissOnClickOutside   = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(200)) + scaleIn(tween(220), initialScale = 0.92f),
            exit    = fadeOut(tween(150)) + scaleOut(tween(150)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(UpgCard)
                    .border(1.dp, UpgBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

                    // ── Header ────────────────────────────────────────────────
                    Row(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment   = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = trigger.headline(),
                                fontSize   = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color      = UpgText,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text     = trigger.subline(),
                                fontSize = 12.sp,
                                color    = UpgMuted,
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.Close, contentDescription = "Dismiss", tint = UpgMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Plan cards ────────────────────────────────────────────
                    UpgradePlanCard(card = PRO_CARD,   onClick = onSelectPro)
                    Spacer(Modifier.height(10.dp))
                    UpgradePlanCard(card = ULTRA_CARD, onClick = onSelectUltra)

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text      = "🔒  Secure payment · No commitment · Cancel anytime",
                        fontSize  = 10.sp,
                        color     = UpgMuted,
                        modifier  = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun UpgradePlanCard(card: PlanCard, onClick: () -> Unit) {
    val borderBrush = Brush.linearGradient(
        listOf(card.accentColor.copy(alpha = 0.5f), card.accentColor.copy(alpha = 0.1f))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(UpgDark)
            .border(
                width  = 1.dp,
                brush  = borderBrush,
                shape  = RoundedCornerShape(14.dp),
            )
            .clickable(
                indication     = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick        = onClick,
            )
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Card title row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text       = card.name,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color      = card.accentColor,
                        )
                        if (card.isRecommended) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(card.accentColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text("Recommended", fontSize = 9.sp, color = card.accentColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Text(card.tagline, fontSize = 11.sp, color = UpgMuted)
                }
                Text(
                    text       = card.price,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = card.accentColor,
                )
            }

            Divider(color = UpgBorder, thickness = 0.5.dp)

            // Feature list
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                card.features.forEach { (icon, label) ->
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(icon, contentDescription = null, tint = card.accentColor, modifier = Modifier.size(13.dp))
                        Text(label, fontSize = 11.sp, color = UpgText)
                    }
                }
            }

            // CTA button
            Button(
                onClick   = onClick,
                modifier  = Modifier.fillMaxWidth().height(40.dp),
                shape     = RoundedCornerShape(10.dp),
                colors    = ButtonDefaults.buttonColors(
                    containerColor = card.accentColor,
                    contentColor   = UpgDark,
                ),
            ) {
                Text("Upgrade to ${card.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Copy helpers ───────────────────────────────────────────────────────────────

private fun UpgradeTrigger.headline(): String = when (this) {
    UpgradeTrigger.ScanLimitFree  -> "Scammers don't stop. Neither should you."
    UpgradeTrigger.ScanLimitPro   -> "You've hit today's limit — threats haven't"
    UpgradeTrigger.FeatureLocked  -> "You're leaving yourself unprotected"
    UpgradeTrigger.Generic        -> "Complete cyber protection for ₹199/mo"
}

private fun UpgradeTrigger.subline(): String = when (this) {
    UpgradeTrigger.ScanLimitFree  -> "Free scans used up this week. Upgrade to stay protected without waiting."
    UpgradeTrigger.ScanLimitPro   -> "GO ULTRA removes all limits. Scan everything, all day, every day."
    UpgradeTrigger.FeatureLocked  -> "This insight could prevent fraud. Unlock with GO PRO — starts at ₹199/mo."
    UpgradeTrigger.Generic        -> "Unlimited scans, AI insights & real-time alerts. Used by thousands daily."
}
