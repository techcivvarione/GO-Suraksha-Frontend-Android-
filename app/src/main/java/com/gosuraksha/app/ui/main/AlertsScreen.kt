package com.gosuraksha.app.ui.main

import com.gosuraksha.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.alerts.AlertsViewModel
import com.gosuraksha.app.alerts.model.AlertEvent
import com.gosuraksha.app.alerts.model.AlertsSummaryResponse
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
import com.gosuraksha.app.trusted.TrustedContactsViewModel
import com.gosuraksha.app.trusted.model.TrustedContact
import com.gosuraksha.app.ui.components.localizedUiMessage

@Composable
fun AlertsScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    val alertsViewModel: AlertsViewModel = viewModel()
    val trustedViewModel: TrustedContactsViewModel = viewModel()

    val alerts by alertsViewModel.alerts.collectAsState()
    val summary by alertsViewModel.summary.collectAsState()
    val trustedAlerts by alertsViewModel.trusted.collectAsState()
    val contacts by trustedViewModel.contacts.collectAsState()
    val loading by alertsViewModel.loading.collectAsState()
    val error by alertsViewModel.error.collectAsState()
    val trustedLoading by trustedViewModel.loading.collectAsState()
    val trustedError by trustedViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        alertsViewModel.loadAlerts()
        alertsViewModel.loadSummary()
    }
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            trustedViewModel.loadContacts()
            trustedViewModel.loadAlerts()
            alertsViewModel.loadTrusted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorTokens.background())
    ) {
        AlertsHeader(
            selectedTab = selectedTab,
            onTabChange = { selectedTab = it },
            onRefresh = { alertsViewModel.refreshAlerts() }
        )
        when (selectedTab) {
            0 -> AlertsTab(
                alerts = alerts,
                summary = summary,
                loading = loading,
                error = error,
                onRefresh = { alertsViewModel.refreshAlerts() }
            )
            1 -> FamilyTab(
                contacts = contacts,
                trustedAlerts = trustedAlerts,
                loading = trustedLoading,
                error = trustedError,
                onDeleteContact = { id -> trustedViewModel.deleteContact(id) },
                onAddContact = { name, email, phone -> trustedViewModel.addContact(name, email, phone) },
                onMarkRead = { id -> alertsViewModel.markTrustedRead(id) }
            )
        }
    }
}

@Composable
private fun neutralCardColors() = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surfaceVariant
)

@Composable
private fun neutralCardBorder() = BorderStroke(
    width = 1.dp,
    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
)

private val neutralCardShape = RoundedCornerShape(14.dp)

@Composable
private fun AlertsHeader(
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        ColorTokens.surfaceVariant().copy(alpha = 0.6f),
                        ColorTokens.background()
                    )
                )
            )
            .padding(horizontal = SpacingTokens.screenPaddingHorizontal, vertical = SpacingTokens.md)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.ui_alertsscreen_1), color = ColorTokens.textPrimary(), style = TypographyTokens.screenTitle)
                Text(stringResource(R.string.ui_alertsscreen_2), color = ColorTokens.textSecondary(), style = TypographyTokens.bodySmall)
            }
            AppOutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.size(SpacingTokens.minTouchTarget)
            ) {
                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(SpacingTokens.iconSizeSmall))
            }
        }

        Spacer(Modifier.height(SpacingTokens.sm))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ShapeTokens.cardCompact)
                .background(ColorTokens.surface())
                .padding(SpacingTokens.xxs),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xxs)
        ) {
            listOf(
                Pair(stringResource(R.string.alerts_tab_alerts), Icons.Outlined.NotificationsActive),
                Pair(stringResource(R.string.alerts_tab_family), Icons.Outlined.Groups)
            ).forEachIndexed { index, (label, icon) ->
                val isSelected = selectedTab == index
                val bgColor by animateColorAsState(
                    if (isSelected) ColorTokens.accent().copy(alpha = 0.12f) else ColorTokens.transparent(),
                    tween(200),
                    label = stringResource(R.string.ui_alertsscreen_7, index)
                )
                val textColor by animateColorAsState(
                    if (isSelected) ColorTokens.accent() else ColorTokens.textSecondary(),
                    tween(200),
                    label = stringResource(R.string.ui_alertsscreen_8, index)
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(ShapeTokens.input)
                        .background(bgColor)
                        .clickable(remember { MutableInteractionSource() }, null) { onTabChange(index) }
                        .padding(vertical = SpacingTokens.xs),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = textColor, modifier = Modifier.size(SpacingTokens.iconSizeSmall))
                    Spacer(Modifier.width(SpacingTokens.xs))
                    Text(label, color = textColor, style = TypographyTokens.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun AlertsTab(
    alerts: List<AlertEvent>,
    summary: AlertsSummaryResponse?,
    loading: Boolean,
    error: String?,
    onRefresh: () -> Unit
) {
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
        summary?.let { s ->
            item {
                AlertsSummaryCard(summary = s)
                Spacer(Modifier.height(SpacingTokens.xxs))
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(SpacingTokens.xxs).clip(CircleShape).background(ColorTokens.error()))
                Spacer(Modifier.width(SpacingTokens.xs))
                Text(stringResource(R.string.ui_alertsscreen_3), color = ColorTokens.textSecondary(), style = TypographyTokens.labelSmall)
            }
            Spacer(Modifier.height(SpacingTokens.xs))
        }

        if (alerts.isEmpty()) {
            item { EmptyState(Icons.Outlined.NotificationsOff, "No alerts right now. You are all clear.") }
        }

        items(alerts) { alert -> AlertCard(alert) }

        error?.let { item { ErrorBanner(it) } }
        item { Spacer(Modifier.height(SpacingTokens.xxxl)) }
    }
}

@Composable
private fun AlertsSummaryCard(summary: AlertsSummaryResponse) {
    val riskColor = when (summary.risk_level_today.lowercase()) {
        "high" -> ColorTokens.error()
        "medium" -> ColorTokens.warning()
        else -> ColorTokens.success()
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = neutralCardColors(),
        border = neutralCardBorder(),
        shape = neutralCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.xs)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(riskColor.copy(alpha = 0.08f))
                    .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.xs)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, null, tint = riskColor, modifier = Modifier.size(SpacingTokens.iconSizeSmall))
                    Spacer(Modifier.width(SpacingTokens.xs))
                    Text(stringResource(R.string.ui_alertsscreen_4), color = ColorTokens.textSecondary(), style = TypographyTokens.labelSmall)
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(ShapeTokens.badge)
                            .background(riskColor.copy(alpha = 0.12f))
                            .padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs)
                    ) {
                        Text(summary.risk_level_today.uppercase(), color = riskColor, style = TypographyTokens.labelSmall)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingTokens.md),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryPill(label = stringResource(R.string.ui_alertsscreen_9), value = "${summary.unread_alerts.high}", color = ColorTokens.error())
                VerticalDivider()
                SummaryPill(label = stringResource(R.string.ui_alertsscreen_10), value = "${summary.unread_alerts.medium}", color = ColorTokens.warning())
                VerticalDivider()
                SummaryPill(label = stringResource(R.string.ui_alertsscreen_11), value = "${summary.unread_alerts.low}", color = ColorTokens.success())
            }
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(modifier = Modifier.width(ShapeTokens.Border.thin).height(SpacingTokens.lg).background(ColorTokens.border()))
}

@Composable
private fun SummaryPill(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, style = TypographyTokens.headlineSmall)
        Text(label, color = ColorTokens.textSecondary(), style = TypographyTokens.labelSmall)
    }
}

@Composable
private fun AlertCard(alert: AlertEvent) {
    val severityLabel = when {
        (alert.risk_score ?: 0) >= 70 -> "HIGH"
        (alert.risk_score ?: 0) >= 40 -> "MEDIUM"
        (alert.risk_score ?: 0) > 0 -> "LOW"
        alert.status.equals("HIGH", ignoreCase = true) -> "HIGH"
        alert.status.equals("MEDIUM", ignoreCase = true) -> "MEDIUM"
        else -> "LOW"
    }
    val severityColor = when {
        severityLabel == "HIGH" -> ColorTokens.error()
        severityLabel == "MEDIUM" -> ColorTokens.warning()
        severityLabel == "LOW" -> ColorTokens.success()
        else -> ColorTokens.accent()
    }
    val severityIcon = when {
        severityLabel == "HIGH" -> Icons.Filled.Warning
        severityLabel == "MEDIUM" -> Icons.Filled.Info
        else -> Icons.Filled.CheckCircle
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = neutralCardColors(),
        border = neutralCardBorder(),
        shape = neutralCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.xs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(SpacingTokens.iconSizeLarge)
                    .clip(ShapeTokens.cardCompact)
                    .background(severityColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(severityIcon, null, tint = severityColor, modifier = Modifier.size(SpacingTokens.iconSizeSmall))
            }

            Spacer(Modifier.width(SpacingTokens.sm))

            Column(Modifier.weight(1f)) {
                Text(
                    text = (alert.analysis_type ?: alert.status ?: "ALERT").replace("_", " ").uppercase(),
                    color = ColorTokens.textPrimary(),
                    style = TypographyTokens.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                alert.risk_score?.let {
                    Spacer(Modifier.height(SpacingTokens.xxs))
                    Text("Risk score: $it", color = ColorTokens.textSecondary(), style = TypographyTokens.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(SpacingTokens.xs))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(ShapeTokens.badge)
                            .background(severityColor.copy(alpha = 0.12f))
                            .padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs)
                    ) {
                        Text(severityLabel, color = severityColor, style = TypographyTokens.labelSmall)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(formatDate(alert.created_at), color = ColorTokens.textSecondary(), style = TypographyTokens.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun FamilyTab(
    contacts: List<TrustedContact>,
    trustedAlerts: List<TrustedAlertItem>,
    loading: Boolean,
    error: String?,
    onDeleteContact: (String) -> Unit,
    onAddContact: (String, String?, String?) -> Unit,
    onMarkRead: (String) -> Unit
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
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
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                colors = neutralCardColors(),
                border = neutralCardBorder(),
                shape = neutralCardShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(remember { MutableInteractionSource() }, null) { showAddSheet = !showAddSheet }
                        .padding(SpacingTokens.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (showAddSheet) Icons.Filled.Info else Icons.Outlined.PersonAdd,
                        null,
                        tint = ColorTokens.accent(),
                        modifier = Modifier.size(SpacingTokens.iconSizeSmall)
                    )
                    Spacer(Modifier.width(SpacingTokens.sm))
                    Text(
                        if (showAddSheet) stringResource(R.string.alerts_cancel) else stringResource(R.string.alerts_add_contact),
                        color = ColorTokens.accent(),
                        style = TypographyTokens.labelMedium
                    )
                }
            }
        }

        if (showAddSheet) {
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = neutralCardColors(),
                    border = neutralCardBorder(),
                    shape = neutralCardShape
                ) {
                    Column(
                        modifier = Modifier.padding(SpacingTokens.md),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)
                    ) {
                        AddContactField(name, { name = it }, stringResource(R.string.ui_alertsscreen_17), Icons.Outlined.Person)
                        AddContactField(email, { email = it }, stringResource(R.string.ui_alertsscreen_18), Icons.Outlined.Email)
                        AddContactField(phone, { phone = it }, stringResource(R.string.ui_alertsscreen_19), Icons.Outlined.Phone)
                        Spacer(Modifier.height(SpacingTokens.xs))
                        AppButton(
                            onClick = {
                                if (name.isNotBlank()) {
                                    onAddContact(name, email.ifBlank { null }, phone.ifBlank { null })
                                    name = ""; email = ""; phone = ""; showAddSheet = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(SpacingTokens.authButtonHeight)
                        ) {
                            Text(stringResource(R.string.ui_alertsscreen_5), style = TypographyTokens.buttonText)
                        }
                    }
                }
            }
        }

        item { SectionLabel(stringResource(R.string.ui_alertsscreen_12), contacts.size) }

        if (contacts.isEmpty()) {
            item { EmptyState(Icons.Outlined.Group, stringResource(R.string.ui_alertsscreen_15)) }
        }
        items(contacts) { contact ->
            ContactCard(contact) { contact.id?.let { onDeleteContact(it) } }
        }

        item {
            Spacer(Modifier.height(SpacingTokens.xs))
            SectionLabel(stringResource(R.string.ui_alertsscreen_13), trustedAlerts.size)
        }

        if (trustedAlerts.isEmpty()) {
            item { EmptyState(Icons.Outlined.NotificationsNone, stringResource(R.string.ui_alertsscreen_16)) }
        }
        items(trustedAlerts) { alert ->
            TrustedAlertCard(alert) { alert.id.let { onMarkRead(it) } }
        }

        error?.let { item { ErrorBanner(it) } }
        item { Spacer(Modifier.height(SpacingTokens.xxxl)) }
    }
}

@Composable
private fun ContactCard(contact: TrustedContact, onDelete: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = neutralCardColors(),
        border = neutralCardBorder(),
        shape = neutralCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.xs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(SpacingTokens.iconSizeLarge)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    contact.contact_name?.firstOrNull()?.toString() ?: stringResource(R.string.alerts_unknown_initial),
                    color = ColorTokens.accent(),
                    style = TypographyTokens.labelMedium
                )
            }

            Spacer(Modifier.width(SpacingTokens.sm))

            Column(Modifier.weight(1f)) {
                Text(contact.contact_name ?: stringResource(R.string.alerts_contact_unnamed), color = ColorTokens.textPrimary(), style = TypographyTokens.labelMedium)
                contact.contact_email?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = ColorTokens.textSecondary(), style = TypographyTokens.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                contact.contact_phone?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = ColorTokens.textSecondary(), style = TypographyTokens.bodySmall)
                }
            }

            val statusColor = if (contact.status == "ACTIVE") ColorTokens.success() else ColorTokens.textSecondary()
            val statusLabel = when (contact.status?.uppercase()) {
                "ACTIVE" -> stringResource(R.string.alerts_status_active)
                "PENDING" -> stringResource(R.string.alerts_status_pending)
                else -> contact.status ?: stringResource(R.string.alerts_status_pending)
            }
            Box(
                modifier = Modifier
                    .clip(ShapeTokens.badge)
                    .background(statusColor.copy(alpha = 0.1f))
                    .padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs)
            ) {
                Text(statusLabel, color = statusColor, style = TypographyTokens.labelSmall)
            }

            Spacer(Modifier.width(SpacingTokens.xs))

            AppOutlinedButton(
                onClick = onDelete,
                modifier = Modifier.size(SpacingTokens.minTouchTargetSmall)
            ) {
                Icon(Icons.Filled.Delete, null, modifier = Modifier.size(SpacingTokens.iconSizeSmall))
            }
        }
    }
}

@Composable
private fun TrustedAlertCard(alert: TrustedAlertItem, onMarkRead: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = neutralCardColors(),
        border = neutralCardBorder(),
        shape = neutralCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.xs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(SpacingTokens.iconSizeLarge)
                    .clip(CircleShape)
                    .background(ColorTokens.warning().copy(alpha = 0.12f))
                    .border(ShapeTokens.Border.thin, ColorTokens.warning().copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    alert.contact_name.firstOrNull()?.toString() ?: stringResource(R.string.alerts_unknown_initial),
                    color = ColorTokens.warning(),
                    style = TypographyTokens.labelMedium
                )
            }

            Spacer(Modifier.width(SpacingTokens.sm))

            Column(Modifier.weight(1f)) {
                Text(alert.contact_name, color = ColorTokens.textPrimary(), style = TypographyTokens.labelMedium)
                Spacer(Modifier.height(SpacingTokens.xxs))
                Text(
                    alert.alert_type.replace("_", " "),
                    color = ColorTokens.textSecondary(),
                    style = TypographyTokens.bodySmall
                )
                alert.contact_email?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(SpacingTokens.xxs))
                    Text(it, color = ColorTokens.textSecondary(), style = TypographyTokens.labelSmall)
                }
                Spacer(Modifier.height(SpacingTokens.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatDate(alert.created_at), color = ColorTokens.textSecondary(), style = TypographyTokens.labelSmall)
                    Box(
                        modifier = Modifier
                            .clip(ShapeTokens.badge)
                            .background(ColorTokens.warning().copy(alpha = 0.12f))
                            .padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs)
                    ) {
                        Text(alert.alert_type.uppercase(), color = ColorTokens.warning(), style = TypographyTokens.labelSmall)
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
        Box(
            modifier = Modifier
                .clip(ShapeTokens.badge)
                .background(ColorTokens.accent().copy(alpha = 0.1f))
                .padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs)
        ) {
            Text(stringResource(R.string.ui_alertsscreen_6, count), color = ColorTokens.accent(), style = TypographyTokens.labelSmall)
        }
    }
}

@Composable
private fun AddContactField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        leadingIcon = icon,
        textStyle = TypographyTokens.bodySmall
    )
}

@Composable
private fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SpacingTokens.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(SpacingTokens.authLogoMedium)
                    .clip(CircleShape)
                    .background(ColorTokens.surfaceVariant())
                    .border(ShapeTokens.Border.thin, ColorTokens.border(), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = ColorTokens.textSecondary(), modifier = Modifier.size(SpacingTokens.iconSizeLarge))
            }
            Spacer(Modifier.height(SpacingTokens.sm))
            Text(message, color = ColorTokens.textSecondary(), style = TypographyTokens.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeTokens.cardCompact)
            .background(ColorTokens.error().copy(alpha = 0.08f))
            .border(ShapeTokens.Border.thin, ColorTokens.error().copy(alpha = 0.2f), ShapeTokens.cardCompact)
            .padding(SpacingTokens.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Warning, null, tint = ColorTokens.error(), modifier = Modifier.size(SpacingTokens.iconSizeSmall))
        Spacer(Modifier.width(SpacingTokens.xs))
        Text(localizedUiMessage(message), color = ColorTokens.error(), style = TypographyTokens.bodySmall)
    }
}

private fun formatDate(dateStr: String): String {
    return try { dateStr.substring(0, 10) } catch (e: Exception) { dateStr }
}
