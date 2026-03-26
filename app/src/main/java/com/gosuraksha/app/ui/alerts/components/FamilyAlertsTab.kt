package com.gosuraksha.app.ui.alerts

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.R
import androidx.compose.ui.res.stringResource
import com.gosuraksha.app.alerts.model.FamilyActivityItem
import com.gosuraksha.app.alerts.model.TrustedAlertItem
import com.gosuraksha.app.design.components.AppButton
import com.gosuraksha.app.design.components.AppCard
import com.gosuraksha.app.design.components.AppOutlinedButton
import com.gosuraksha.app.design.components.AppTextField
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.ElevationTokens
import com.gosuraksha.app.design.tokens.ShapeTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.design.tokens.TypographyTokens
import com.gosuraksha.app.trusted.model.TrustedContact
import com.gosuraksha.app.ui.components.localizedUiMessage
import java.util.Locale

// ── Live risk status derived from family activity data ───────────────────────
private enum class ContactRiskStatus { SAFE, ATTENTION, RISK }

private fun contactRiskStatus(
    contactName: String?,
    activities: List<FamilyActivityItem>,
): ContactRiskStatus {
    val nameLower = contactName?.trim()?.lowercase() ?: return ContactRiskStatus.SAFE
    val matches = activities.filter { it.member_name?.trim()?.lowercase() == nameLower }
    return when {
        matches.any { it.risk_level?.uppercase() == "HIGH" }   -> ContactRiskStatus.RISK
        matches.any { it.risk_level?.uppercase() == "MEDIUM" } -> ContactRiskStatus.ATTENTION
        else                                                    -> ContactRiskStatus.SAFE
    }
}

@Composable
fun FamilyAlertsTab(
    contacts: List<TrustedContact>,
    trustedAlerts: List<TrustedAlertItem>,
    familyActivity: List<FamilyActivityItem> = emptyList(),
    loading: Boolean,
    error: String?,
    onDeleteContact: (String) -> Unit,
    onAddContact: (String, String?, String?) -> Unit,
    onMarkRead: (String) -> Unit
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ColorTokens.accent())
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = SpacingTokens.screenPaddingHorizontal, vertical = SpacingTokens.md),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)
    ) {
        item {
            AppCard(modifier = Modifier.fillMaxWidth(), colors = neutralCardColors(), border = neutralCardBorder(), shape = neutralCardShape) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable(remember { MutableInteractionSource() }, null) { showAddSheet = !showAddSheet }.padding(SpacingTokens.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(if (showAddSheet) Icons.Filled.Info else Icons.Outlined.PersonAdd, null, tint = ColorTokens.accent(), modifier = Modifier.size(SpacingTokens.iconSizeSmall))
                    Spacer(Modifier.width(SpacingTokens.sm))
                    Text(if (showAddSheet) stringResource(R.string.alerts_cancel) else stringResource(R.string.alerts_add_contact), color = ColorTokens.accent(), style = TypographyTokens.labelMedium)
                }
            }
        }

        if (showAddSheet) {
            item {
                AppCard(modifier = Modifier.fillMaxWidth(), colors = neutralCardColors(), border = neutralCardBorder(), shape = neutralCardShape) {
                    Column(modifier = Modifier.padding(SpacingTokens.md), verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
                        AddContactField(name, { name = it }, stringResource(R.string.ui_alertsscreen_17), Icons.Outlined.Person)
                        AddContactField(phone, { phone = it }, stringResource(R.string.ui_alertsscreen_19), Icons.Outlined.Phone)
                        Spacer(Modifier.height(SpacingTokens.xs))
                        AppButton(
                            onClick = {
                                if (name.isNotBlank() && phone.isNotBlank()) {
                                    onAddContact(name, null, phone.ifBlank { null })
                                    name = ""; phone = ""; showAddSheet = false
                                }
                            },
                            enabled = name.isNotBlank() && phone.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(SpacingTokens.authButtonHeight)
                        ) {
                            Text(stringResource(R.string.ui_alertsscreen_5), style = TypographyTokens.buttonText)
                        }
                    }
                }
            }
        }

        item { SectionLabel(stringResource(R.string.ui_alertsscreen_12), contacts.size) }
        if (contacts.isEmpty()) item { FamilyEmptyState(Icons.Outlined.Group, stringResource(R.string.ui_alertsscreen_15)) }
        items(contacts) { contact ->
            val status = contactRiskStatus(contact.name, familyActivity)
            val memberActivity = familyActivity.filter {
                it.member_name?.trim()?.lowercase() == contact.name?.trim()?.lowercase()
            }
            ContactCard(
                contact      = contact,
                riskStatus   = status,
                memberActivity = memberActivity,
                onDelete     = { contact.id?.let { onDeleteContact(it) } },
            )
        }

        item {
            Spacer(Modifier.height(SpacingTokens.xs))
            SectionLabel(stringResource(R.string.ui_alertsscreen_13), trustedAlerts.size)
        }

        if (trustedAlerts.isEmpty()) item { FamilyEmptyState(Icons.Outlined.NotificationsNone, stringResource(R.string.ui_alertsscreen_16)) }
        items(trustedAlerts) { alert -> TrustedAlertCard(alert) { onMarkRead(alert.id) } }

        // ── Family scan activity (MEDIUM / HIGH only) ─────────────────────
        if (familyActivity.isNotEmpty()) {
            item {
                Spacer(Modifier.height(SpacingTokens.xs))
                SectionLabel("Recent Activity", familyActivity.size)
            }
            items(familyActivity) { item -> FamilyActivityCard(item) }
        }

        error?.let { item { FamilyErrorBanner(it) } }
        item { Spacer(Modifier.height(SpacingTokens.xxxl)) }
    }
}

@Composable
private fun ContactCard(
    contact:        TrustedContact,
    riskStatus:     ContactRiskStatus,
    memberActivity: List<FamilyActivityItem>,
    onDelete:       () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    // Risk-based status label + color + icon
    val (riskLabel, riskColor, riskIcon) = when (riskStatus) {
        ContactRiskStatus.RISK      -> Triple("🚨 Risk Detected", ColorTokens.error(), Icons.Outlined.Warning)
        ContactRiskStatus.ATTENTION -> Triple("⚠️ Needs Attention", ColorTokens.warning(), Icons.Outlined.Security)
        ContactRiskStatus.SAFE      -> Triple("✅ Safe", ColorTokens.success(), Icons.Outlined.CheckCircle)
    }

    AppCard(
        modifier  = Modifier.fillMaxWidth(),
        colors    = neutralCardColors(),
        border    = neutralCardBorder(),
        shape     = neutralCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.xs),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Main row — always visible ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(remember { MutableInteractionSource() }, null) { expanded = !expanded }
                    .padding(SpacingTokens.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar circle with risk-colour border
                Box(
                    modifier = Modifier
                        .size(SpacingTokens.iconSizeLarge)
                        .clip(CircleShape)
                        .background(riskColor.copy(alpha = 0.1f))
                        .border(1.dp, riskColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        contact.name?.firstOrNull()?.toString()
                            ?: stringResource(R.string.alerts_unknown_initial),
                        color = riskColor,
                        style = TypographyTokens.labelMedium,
                    )
                }

                Spacer(Modifier.width(SpacingTokens.sm))

                Column(Modifier.weight(1f)) {
                    Text(
                        contact.name ?: stringResource(R.string.alerts_contact_unnamed),
                        color = ColorTokens.textPrimary(),
                        style = TypographyTokens.labelMedium,
                    )
                    contact.phone?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = ColorTokens.textSecondary(), style = TypographyTokens.bodySmall)
                    }
                }

                // Live risk status badge
                Box(
                    modifier = Modifier
                        .clip(ShapeTokens.badge)
                        .background(riskColor.copy(alpha = 0.1f))
                        .padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs),
                ) {
                    Text(riskLabel, color = riskColor, style = TypographyTokens.labelSmall)
                }

                Spacer(Modifier.width(SpacingTokens.xs))

                // Expand/collapse chevron (only if there's activity to show)
                if (memberActivity.isNotEmpty()) {
                    Icon(
                        if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = ColorTokens.textSecondary(),
                        modifier = Modifier.size(SpacingTokens.iconSizeSmall),
                    )
                    Spacer(Modifier.width(SpacingTokens.xs))
                }

                AppOutlinedButton(onClick = onDelete, modifier = Modifier.size(SpacingTokens.minTouchTargetSmall)) {
                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(SpacingTokens.iconSizeSmall))
                }
            }

            // ── Expandable detail — recent activity for this member ───────
            AnimatedVisibility(
                visible = expanded && memberActivity.isNotEmpty(),
                enter   = fadeIn() + expandVertically(),
                exit    = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.sm),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                ) {
                    Text(
                        "Recent Activity",
                        color = ColorTokens.textSecondary(),
                        style = TypographyTokens.labelSmall,
                    )
                    Divider(color = ColorTokens.border(), thickness = 0.5.dp)
                    Spacer(Modifier.height(SpacingTokens.xxs))
                    memberActivity.take(3).forEach { item ->
                        val itemRisk  = item.risk_level?.uppercase() ?: "UNKNOWN"
                        val itemColor = when (itemRisk) {
                            "HIGH"   -> ColorTokens.error()
                            "MEDIUM" -> ColorTokens.warning()
                            else     -> ColorTokens.textSecondary()
                        }
                        val emoji = when (itemRisk) { "HIGH" -> "🚨"; "MEDIUM" -> "⚠️"; else -> "🟡" }
                        val phrase = when (item.scan_type?.uppercase()) {
                            "IMAGE"         -> "received a suspicious image"
                            "EMAIL"         -> "checked a suspicious email"
                            "QR"            -> "scanned a suspicious QR code"
                            "THREAT","TEXT" -> "received a suspicious message"
                            else            -> "triggered a security scan"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                        ) {
                            Text(emoji, style = TypographyTokens.bodySmall)
                            Text(
                                phrase,
                                color    = ColorTokens.textPrimary(),
                                style    = TypographyTokens.bodySmall,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Box(
                                modifier = Modifier
                                    .clip(ShapeTokens.badge)
                                    .background(itemColor.copy(alpha = 0.1f))
                                    .padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs),
                            ) {
                                Text(itemRisk, color = itemColor, style = TypographyTokens.labelSmall)
                            }
                        }
                    }
                    if (memberActivity.size > 3) {
                        Text(
                            "+${memberActivity.size - 3} more",
                            color = ColorTokens.accent(),
                            style = TypographyTokens.labelSmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrustedAlertCard(alert: TrustedAlertItem, onMarkRead: () -> Unit) {
    val contactName = alert.contact_name?.takeIf { it.isNotBlank() } ?: "Unknown Contact"
    val alertType = alert.alert_type?.takeIf { it.isNotBlank() } ?: run {
        Log.w("GO_SURAKSHA_ALERTS", "Trusted alert missing type: ${alert.id}")
        "security"
    }
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = neutralCardColors(),
        border = neutralCardBorder(),
        shape = neutralCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.xs)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(SpacingTokens.md), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(SpacingTokens.iconSizeLarge).clip(CircleShape).background(ColorTokens.warning().copy(alpha = 0.12f)).border(ShapeTokens.Border.thin, ColorTokens.warning().copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(contactName.firstOrNull()?.toString() ?: stringResource(R.string.alerts_unknown_initial), color = ColorTokens.warning(), style = TypographyTokens.labelMedium)
            }
            Spacer(Modifier.width(SpacingTokens.sm))
            Column(Modifier.weight(1f)) {
                Text(contactName, color = ColorTokens.textPrimary(), style = TypographyTokens.labelMedium)
                Spacer(Modifier.height(SpacingTokens.xxs))
                Text(alertType.replace("_", " "), color = ColorTokens.textSecondary(), style = TypographyTokens.bodySmall)
                Spacer(Modifier.height(SpacingTokens.xs))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(formatAlertDate(alert.created_at), color = ColorTokens.textSecondary(), style = TypographyTokens.labelSmall)
                    Box(modifier = Modifier.clip(ShapeTokens.badge).background(ColorTokens.warning().copy(alpha = 0.12f)).padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs)) {
                        Text(alertType.uppercase(Locale.getDefault()), color = ColorTokens.warning(), style = TypographyTokens.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = ColorTokens.textSecondary(), style = TypographyTokens.labelSmall)
        Spacer(Modifier.width(SpacingTokens.xs))
        Box(modifier = Modifier.clip(ShapeTokens.badge).background(ColorTokens.accent().copy(alpha = 0.1f)).padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs)) {
            Text(stringResource(R.string.ui_alertsscreen_6, count), color = ColorTokens.accent(), style = TypographyTokens.labelSmall)
        }
    }
}

@Composable
private fun AddContactField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    AppTextField(value = value, onValueChange = onValueChange, label = label, leadingIcon = icon, textStyle = TypographyTokens.bodySmall)
}

@Composable
private fun FamilyActivityCard(item: FamilyActivityItem) {
    val riskRaw   = (item.risk_level ?: "UNKNOWN").uppercase(Locale.getDefault())
    val riskColor = when (riskRaw) {
        "HIGH"   -> ColorTokens.error()
        "MEDIUM" -> ColorTokens.warning()
        "LOW"    -> ColorTokens.success()
        else     -> ColorTokens.textSecondary()
    }
    val riskEmoji = when (riskRaw) {
        "HIGH"   -> "🔴"
        "MEDIUM" -> "⚠️"
        else     -> "🟡"
    }
    // STEP 7: human-readable "received a suspicious X" description
    val actionPhrase = when (item.scan_type?.uppercase()) {
        "IMAGE"          -> "received a suspicious image"
        "EMAIL"          -> "checked a suspicious email"
        "QR"             -> "scanned a suspicious QR code"
        "PASSWORD"       -> "checked a risky password"
        "THREAT","TEXT"  -> "received a suspicious message"
        "SMS"            -> "received a suspicious SMS"
        else             -> "triggered a security scan"
    }
    val riskLabel = when (riskRaw) {
        "HIGH"   -> "High risk"
        "MEDIUM" -> "Medium risk"
        "LOW"    -> "Low risk"
        else     -> "Unknown risk"
    }
    val memberName = item.member_name ?: "A family member"
    // Full sentence: "Ravi received a suspicious message ⚠️ Medium risk"
    val headline = "$memberName $actionPhrase"
    val subline  = "$riskEmoji $riskLabel"

    AppCard(
        modifier  = Modifier.fillMaxWidth(),
        colors    = neutralCardColors(),
        border    = neutralCardBorder(),
        shape     = neutralCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.xs)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier          = Modifier
                    .size(SpacingTokens.iconSizeLarge)
                    .clip(CircleShape)
                    .background(riskColor.copy(alpha = 0.12f))
                    .border(1.dp, riskColor.copy(alpha = 0.25f), CircleShape),
                contentAlignment  = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Security, null,
                    tint     = riskColor,
                    modifier = Modifier.size(SpacingTokens.iconSizeSmall)
                )
            }
            Spacer(Modifier.width(SpacingTokens.sm))
            Column(Modifier.weight(1f)) {
                // STEP 7: "Ravi received a suspicious message"
                Text(
                    headline,
                    color    = ColorTokens.textPrimary(),
                    style    = TypographyTokens.labelMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(SpacingTokens.xxs))
                // "⚠️ Medium risk"
                Text(
                    subline,
                    color = riskColor,
                    style = TypographyTokens.bodySmall
                )
                item.scan_input?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(SpacingTokens.xxs))
                    Text(
                        if (it.length > 50) it.take(47) + "…" else it,
                        color    = ColorTokens.textSecondary(),
                        style    = TypographyTokens.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(SpacingTokens.xs))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    Text(
                        formatAlertDate(item.created_at),
                        color = ColorTokens.textSecondary(),
                        style = TypographyTokens.labelSmall
                    )
                    Box(
                        modifier = Modifier
                            .clip(ShapeTokens.badge)
                            .background(riskColor.copy(alpha = 0.12f))
                            .padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs)
                    ) {
                        Text(riskRaw, color = riskColor, style = TypographyTokens.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilyEmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = SpacingTokens.xl), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(SpacingTokens.authLogoMedium).clip(CircleShape).background(ColorTokens.surfaceVariant()).border(ShapeTokens.Border.thin, ColorTokens.border(), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = ColorTokens.textSecondary(), modifier = Modifier.size(SpacingTokens.iconSizeLarge))
            }
            Spacer(Modifier.height(SpacingTokens.sm))
            Text(message, color = ColorTokens.textSecondary(), style = TypographyTokens.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun FamilyErrorBanner(message: String) {
    Row(modifier = Modifier.fillMaxWidth().clip(ShapeTokens.cardCompact).background(ColorTokens.error().copy(alpha = 0.08f)).border(ShapeTokens.Border.thin, ColorTokens.error().copy(alpha = 0.2f), ShapeTokens.cardCompact).padding(SpacingTokens.sm), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Warning, null, tint = ColorTokens.error(), modifier = Modifier.size(SpacingTokens.iconSizeSmall))
        Spacer(Modifier.width(SpacingTokens.xs))
        Text(localizedUiMessage(message), color = ColorTokens.error(), style = TypographyTokens.bodySmall)
    }
}
