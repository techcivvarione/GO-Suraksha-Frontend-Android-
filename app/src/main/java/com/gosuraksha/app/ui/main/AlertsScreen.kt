package com.gosuraksha.app.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.alerts.AlertsViewModel
import com.gosuraksha.app.alerts.model.AlertItem
import com.gosuraksha.app.alerts.model.AlertsSummaryResponse
import com.gosuraksha.app.alerts.model.TrustedAlertItem
import com.gosuraksha.app.trusted.TrustedContactsViewModel
import com.gosuraksha.app.trusted.model.TrustedContact

// ── Palette ───────────────────────────────────────────────────────────────────
private val ScreenBg      = Color(0xFF0D1117)
private val CardDark      = Color(0xFF161B22)
private val CardBorder    = Color(0xFF30363D)
private val CyberTeal     = Color(0xFF00E5C3)
private val DangerRed     = Color(0xFFFF3B5C)
private val WarnAmber     = Color(0xFFFFB020)
private val SafeGreen     = Color(0xFF00D68F)
private val TextPrimary   = Color(0xFFE6EDF3)
private val TextSecondary = Color(0xFF8B949E)

// =============================================================================
// AlertsScreen
// =============================================================================
@Composable
fun AlertsScreen() {

    var selectedTab by remember { mutableStateOf(0) }

    val alertsViewModel:  AlertsViewModel          = viewModel()
    val trustedViewModel: TrustedContactsViewModel = viewModel()

    val alerts         by alertsViewModel.alerts.collectAsState()
    val summary        by alertsViewModel.summary.collectAsState()
    val trustedAlerts  by alertsViewModel.trusted.collectAsState()
    val contacts       by trustedViewModel.contacts.collectAsState()
    val loading        by alertsViewModel.loading.collectAsState()
    val error          by alertsViewModel.error.collectAsState()
    val trustedLoading by trustedViewModel.loading.collectAsState()
    val trustedError   by trustedViewModel.error.collectAsState()

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
            .background(ScreenBg)
    ) {
        AlertsHeader(
            selectedTab = selectedTab,
            onTabChange = { selectedTab = it },
            onRefresh   = { alertsViewModel.refreshAlerts() }
        )
        when (selectedTab) {
            0 -> AlertsTab(
                alerts  = alerts,
                summary = summary,
                loading = loading,
                error   = error,
                onRefresh = { alertsViewModel.refreshAlerts() }
            )
            1 -> FamilyTab(
                contacts        = contacts,
                trustedAlerts   = trustedAlerts,
                loading         = trustedLoading,
                error           = trustedError,
                onDeleteContact = { id -> trustedViewModel.deleteContact(id) },
                onAddContact    = { name, email, phone -> trustedViewModel.addContact(name, email, phone) },
                onMarkRead      = { id -> alertsViewModel.markTrustedRead(id) }
            )
        }
    }
}

// =============================================================================
// Header + Tabs
// =============================================================================
@Composable
private fun AlertsHeader(
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    onRefresh:   () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D1F2D), ScreenBg)))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Threat Center", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Live alerts & trusted network", color = TextSecondary, fontSize = 12.sp)
            }
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(CyberTeal.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.Refresh, null, tint = CyberTeal, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardDark)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                Pair("Alerts", Icons.Outlined.NotificationsActive),
                Pair("Family", Icons.Outlined.Groups)
            ).forEachIndexed { index, (label, icon) ->
                val isSelected = selectedTab == index
                val bgColor   by animateColorAsState(if (isSelected) CyberTeal.copy(alpha = 0.15f) else Color.Transparent, tween(200), label = "tab_bg_$index")
                val textColor by animateColorAsState(if (isSelected) CyberTeal else TextSecondary, tween(200), label = "tab_txt_$index")
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .clickable(remember { MutableInteractionSource() }, null) { onTabChange(index) }
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = textColor, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(label, color = textColor, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
    }
}

// =============================================================================
// Alerts Tab
// =============================================================================
@Composable
private fun AlertsTab(
    alerts:    List<AlertItem>,
    summary:   AlertsSummaryResponse?,
    loading:   Boolean,
    error:     String?,
    onRefresh: () -> Unit
) {
    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CyberTeal, strokeWidth = 2.dp)
        }
        return
    }

    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        summary?.let { s ->
            item {
                AlertsSummaryCard(summary = s)
                Spacer(Modifier.height(4.dp))
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(DangerRed))
                Spacer(Modifier.width(8.dp))
                Text("LIVE THREATS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            }
            Spacer(Modifier.height(8.dp))
        }

        if (alerts.isEmpty()) {
            item { EmptyState(Icons.Outlined.NotificationsOff, "No alerts right now.\nYou're all clear.") }
        }

        items(alerts) { alert -> AlertCard(alert) }

        error?.let { item { ErrorBanner(it) } }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// =============================================================================
// Alerts Summary Card  ← uses actual model fields
// AlertsSummaryResponse has: risk_level_today, unread_alerts { high, medium, low }, generated_at
// =============================================================================
@Composable
private fun AlertsSummaryCard(summary: AlertsSummaryResponse) {
    // Derive color from risk_level_today
    val riskColor = when (summary.risk_level_today.lowercase()) {
        "high"   -> DangerRed
        "medium" -> WarnAmber
        else     -> SafeGreen
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .border(1.dp, riskColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
    ) {
        // Risk level header strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(riskColor.copy(alpha = 0.08f))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Shield, null, tint = riskColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("TODAY'S RISK LEVEL", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(riskColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(summary.risk_level_today.uppercase(), color = riskColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Unread counts row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryPill(label = "High",   value = "${summary.unread_alerts.high}",   color = DangerRed)
            VerticalDivider()
            SummaryPill(label = "Medium", value = "${summary.unread_alerts.medium}", color = WarnAmber)
            VerticalDivider()
            SummaryPill(label = "Low",    value = "${summary.unread_alerts.low}",    color = SafeGreen)
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(modifier = Modifier.width(1.dp).height(32.dp).background(CardBorder))
}

@Composable
private fun SummaryPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 10.sp)
    }
}

// =============================================================================
// Alert Card  ← uses actual AlertItem fields: id, alert_type, created_at, message
// =============================================================================
@Composable
private fun AlertCard(alert: AlertItem) {
    // Derive severity color from alert_type string
    val severityColor = when {
        alert.alert_type.contains("HIGH",   ignoreCase = true) -> DangerRed
        alert.alert_type.contains("MEDIUM", ignoreCase = true) -> WarnAmber
        alert.alert_type.contains("LOW",    ignoreCase = true) -> SafeGreen
        else                                                    -> CyberTeal
    }
    val severityIcon = when {
        alert.alert_type.contains("HIGH",   ignoreCase = true) -> Icons.Filled.Warning
        alert.alert_type.contains("MEDIUM", ignoreCase = true) -> Icons.Filled.Info
        else                                                    -> Icons.Filled.CheckCircle
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, severityColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(severityColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(severityIcon, null, tint = severityColor, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            // Title from alert_type
            Text(
                text = alert.alert_type.replace("_", " ").uppercase(),
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Message (nullable)
            alert.message?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(3.dp))
                Text(it, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Type chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(severityColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        alert.alert_type.uppercase(),
                        color = severityColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(formatDate(alert.created_at), color = TextSecondary, fontSize = 10.sp)
            }
        }
    }
}

// =============================================================================
// Family Tab
// =============================================================================
@Composable
private fun FamilyTab(
    contacts:        List<TrustedContact>,
    trustedAlerts:   List<TrustedAlertItem>,
    loading:         Boolean,
    error:           String?,
    onDeleteContact: (String) -> Unit,
    onAddContact:    (String, String?, String?) -> Unit,
    onMarkRead:      (String) -> Unit
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var name  by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CyberTeal, strokeWidth = 2.dp)
        }
        return
    }

    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add contact toggle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CyberTeal.copy(alpha = 0.08f))
                    .border(1.dp, CyberTeal.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                    .clickable(remember { MutableInteractionSource() }, null) { showAddSheet = !showAddSheet }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (showAddSheet) Icons.Default.ExpandLess else Icons.Default.PersonAdd,
                    null, tint = CyberTeal, modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (showAddSheet) "Cancel" else "Add Trusted Contact",
                    color = CyberTeal, fontWeight = FontWeight.SemiBold, fontSize = 14.sp
                )
            }
        }

        // Inline add form
        if (showAddSheet) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AddContactField(name,  { name = it },  "Full Name",     Icons.Outlined.Person)
                    AddContactField(email, { email = it }, "Email Address", Icons.Outlined.Email)
                    AddContactField(phone, { phone = it }, "Phone Number",  Icons.Outlined.Phone)
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onAddContact(name, email.ifBlank { null }, phone.ifBlank { null })
                                name = ""; email = ""; phone = ""; showAddSheet = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(CyberTeal, Color(0xFF00B89C))), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Add Contact", color = ScreenBg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        item { SectionLabel("TRUSTED CONTACTS", contacts.size) }

        if (contacts.isEmpty()) {
            item { EmptyState(Icons.Outlined.Group, "No trusted contacts yet.\nAdd someone to protect.") }
        }
        items(contacts) { contact ->
            ContactCard(contact) { contact.id?.let { onDeleteContact(it) } }
        }

        item {
            Spacer(Modifier.height(8.dp))
            SectionLabel("FAMILY ALERTS", trustedAlerts.size)
        }

        if (trustedAlerts.isEmpty()) {
            item { EmptyState(Icons.Outlined.NotificationsNone, "No family alerts yet.") }
        }
        items(trustedAlerts) { alert ->
            TrustedAlertCard(alert) { alert.id.let { onMarkRead(it) } }
        }

        error?.let { item { ErrorBanner(it) } }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// =============================================================================
// Contact Card
// =============================================================================
@Composable
private fun ContactCard(contact: TrustedContact, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CyberTeal.copy(alpha = 0.12f))
                .border(1.dp, CyberTeal.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                contact.contact_name?.firstOrNull()?.toString() ?: "?",
                color = CyberTeal, fontSize = 16.sp, fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(contact.contact_name ?: "Unnamed", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            contact.contact_email?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            contact.contact_phone?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = TextSecondary, fontSize = 12.sp)
            }
        }

        val statusColor = if (contact.status == "ACTIVE") SafeGreen else TextSecondary
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(statusColor.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(contact.status ?: "PENDING", color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(DangerRed.copy(alpha = 0.08f))
        ) {
            Icon(Icons.Default.Delete, null, tint = DangerRed, modifier = Modifier.size(16.dp))
        }
    }
}

// =============================================================================
// Trusted Alert Card  ← uses actual TrustedAlertItem fields:
// id, alert_type, created_at, contact_name, contact_email, contact_phone
// NO message field, NO is_read field
// =============================================================================
@Composable
private fun TrustedAlertCard(alert: TrustedAlertItem, onMarkRead: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, WarnAmber.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar from contact_name initial
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(WarnAmber.copy(alpha = 0.1f))
                .border(1.dp, WarnAmber.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                alert.contact_name.firstOrNull()?.toString() ?: "?",
                color = WarnAmber, fontSize = 15.sp, fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            // Contact name as title
            Text(alert.contact_name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            // Alert type as description
            Text(
                alert.alert_type.replace("_", " "),
                color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp
            )
            // Contact email/phone if available
            alert.contact_email?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, color = TextSecondary, fontSize = 11.sp)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(formatDate(alert.created_at), color = TextSecondary, fontSize = 10.sp)
                // alert_type chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(WarnAmber.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(alert.alert_type.uppercase(), color = WarnAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =============================================================================
// Helpers
// =============================================================================

@Composable
private fun SectionLabel(label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(CyberTeal.copy(alpha = 0.1f))
                .padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
            Text("$count", color = CyberTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AddContactField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector) {
    OutlinedTextField(
        value          = value,
        onValueChange  = onValueChange,
        label          = { Text(label, fontSize = 12.sp) },
        leadingIcon    = { Icon(icon, null, tint = CyberTeal, modifier = Modifier.size(17.dp)) },
        modifier       = Modifier.fillMaxWidth(),
        singleLine     = true,
        shape          = RoundedCornerShape(12.dp),
        colors         = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = CyberTeal,
            unfocusedBorderColor    = CardBorder,
            focusedTextColor        = TextPrimary,
            unfocusedTextColor      = TextPrimary,
            cursorColor             = CyberTeal,
            focusedLabelColor       = CyberTeal,
            unfocusedLabelColor     = TextSecondary,
            focusedContainerColor   = ScreenBg,
            unfocusedContainerColor = ScreenBg
        )
    )
}

@Composable
private fun EmptyState(icon: ImageVector, message: String) {
    Box(
        modifier        = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(CardDark).border(1.dp, CardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(message, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DangerRed.copy(alpha = 0.08f))
            .border(1.dp, DangerRed.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Warning, null, tint = DangerRed, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(message, color = DangerRed, fontSize = 12.sp)
    }
}

private fun formatDate(dateStr: String): String {
    return try { dateStr.substring(0, 10) } catch (e: Exception) { dateStr }
}