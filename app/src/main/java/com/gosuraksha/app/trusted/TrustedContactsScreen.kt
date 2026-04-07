package com.gosuraksha.app.ui.trusted

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PriorityHigh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.trusted.TrustedContactsViewModel
import com.gosuraksha.app.trusted.TrustedContactsViewModelFactory
import com.gosuraksha.app.trusted.model.NotificationAlert
import com.gosuraksha.app.trusted.model.NotificationFeedResponse
import com.gosuraksha.app.trusted.model.PendingInvite
import com.gosuraksha.app.trusted.model.SecureNowItem
import com.gosuraksha.app.trusted.model.TrustedContact

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TrustedContactsScreen(
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit = {},
) {
    val app = LocalContext.current.applicationContext as android.app.Application
    val viewModel: TrustedContactsViewModel = viewModel(factory = TrustedContactsViewModelFactory(app))

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    TrustedContactsScreen(
        viewModel = viewModel,
        onBack = onBack,
        onOpenNotifications = onOpenNotifications,
    )
}

@Composable
fun TrustedContactsScreen(
    viewModel: TrustedContactsViewModel,
    onBack: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
) {
    FamilyProtectionScreen(
        loading = viewModel.loading.collectAsStateWithLifecycle().value,
        error = viewModel.error.collectAsStateWithLifecycle().value,
        statusMessage = viewModel.statusMessage.collectAsStateWithLifecycle().value,
        contacts = viewModel.contacts.collectAsStateWithLifecycle().value,
        pendingInvites = viewModel.pendingInvites.collectAsStateWithLifecycle().value,
        ownSecureNow = viewModel.ownSecureNow.collectAsStateWithLifecycle().value,
        notifications = viewModel.notifications.collectAsStateWithLifecycle().value,
        onBack = onBack,
        onOpenNotifications = onOpenNotifications,
        onInvite = viewModel::sendInvite,
        onSetPrimary = viewModel::setPrimaryContact,
        onDeleteContact = viewModel::deleteContact,
        onAcceptInvite = { inviteId -> viewModel.respondToInvite(inviteId, "ACCEPT", true) },
        onRejectInvite = { inviteId -> viewModel.respondToInvite(inviteId, "REJECT", false) },
        onCompleteSecureNow = viewModel::completeSecureNow,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FamilyProtectionScreen(
    loading: Boolean,
    error: String?,
    statusMessage: String?,
    contacts: List<TrustedContact>,
    pendingInvites: List<PendingInvite>,
    ownSecureNow: List<SecureNowItem>,
    notifications: NotificationFeedResponse,
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit,
    onInvite: (String, String, Boolean) -> Unit,
    onSetPrimary: (String) -> Unit,
    onDeleteContact: (String) -> Unit,
    onAcceptInvite: (String) -> Unit,
    onRejectInvite: (String) -> Unit,
    onCompleteSecureNow: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        Log.d("APP_VERSION", "FamilyProtectionScreen ACTIVE")
    }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Protection", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = onOpenNotifications) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                        }
                        if (notifications.unread_count > 0) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp),
                                shape = CircleShape,
                                color = Color(0xFFD32F2F)
                            ) {
                                Text(
                                    text = notifications.unread_count.coerceAtMost(99).toString(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorTokens.surface())
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ColorTokens.background())
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                statusMessage?.let { item { MessageStrip(message = it, color = Color(0xFFE8F5E9), textColor = Color(0xFF1B5E20)) } }
                error?.let { item { MessageStrip(message = it, color = Color(0xFFFFEBEE), textColor = Color(0xFFB71C1C)) } }

                item {
                    SectionCard(title = "Secure Now (${ownSecureNow.count { (it.status ?: "PENDING") == "PENDING" }} actions needed)") {
                        if (ownSecureNow.isEmpty()) {
                            EmptyState("No Secure Now tasks right now.")
                        } else {
                            ownSecureNow.forEach { item ->
                                SecureNowRow(item = item, onCompleteSecureNow = onCompleteSecureNow)
                            }
                        }
                    }
                }

                item {
                    SectionCard(
                        title = "Trusted Contacts",
                        action = {
                            Button(
                                onClick = {
                                    onInvite(name.trim(), phone.trim(), true)
                                    name = ""
                                    phone = ""
                                },
                                enabled = name.isNotBlank() && phone.isNotBlank()
                            ) {
                                Icon(Icons.Outlined.PersonAdd, contentDescription = null)
                                Spacer(Modifier.size(8.dp))
                                Text("Add Contact")
                            }
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Contact name") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Phone number") },
                                singleLine = true
                            )
                            if (contacts.isEmpty()) {
                                EmptyState("No trusted contacts added yet.")
                            } else {
                                contacts.forEach { contact ->
                                    TrustedContactRow(
                                        contact = contact,
                                        onSetPrimary = { contact.id?.let(onSetPrimary) },
                                        onDelete = { contact.id?.let(onDeleteContact) }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    SectionCard(title = "Pending Invites") {
                        if (pendingInvites.isEmpty()) {
                            EmptyState("No pending invites.")
                        } else {
                            pendingInvites.forEach { invite ->
                                PendingInviteCard(
                                    invite = invite,
                                    onAccept = { onAcceptInvite(invite.id) },
                                    onReject = { onRejectInvite(invite.id) }
                                )
                            }
                        }
                    }
                }

                item {
                    SectionCard(title = "Family Alerts") {
                        val alerts = notifications.alerts.filter {
                            val type = (it.alert_type ?: "").uppercase()
                            (it.risk_level ?: "").equals("high", ignoreCase = true) || type.contains("SOS")
                        }
                        if (alerts.isEmpty()) {
                            EmptyState("No family alerts right now.")
                        } else {
                            alerts.forEach { alert ->
                                FamilyAlertRow(alert = alert)
                            }
                        }
                    }
                }
            }

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    action: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorTokens.surface()),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ColorTokens.textPrimary())
                action?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun SecureNowRow(
    item: SecureNowItem,
    onCompleteSecureNow: (String) -> Unit,
) {
    val isDone = (item.status ?: "PENDING") == "DONE"
    val accent = when ((item.risk_level ?: "high").uppercase()) {
        "HIGH" -> Color(0xFFC62828)
        "MEDIUM" -> Color(0xFFEF6C00)
        else -> Color(0xFF2E7D32)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDone) { onCompleteSecureNow(item.id) }
            .background(ColorTokens.background(), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(22.dp),
            shape = CircleShape,
            color = if (isDone) accent else Color.Transparent,
            tonalElevation = 0.dp,
            border = androidx.compose.foundation.BorderStroke(2.dp, accent)
        ) {}
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontWeight = FontWeight.SemiBold, color = ColorTokens.textPrimary())
            Text(item.description, style = MaterialTheme.typography.bodySmall, color = ColorTokens.textSecondary())
        }
        Text(
            text = if (isDone) "Done" else "Open",
            color = accent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TrustedContactRow(
    contact: TrustedContact,
    onSetPrimary: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorTokens.background(), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name ?: "Trusted contact", fontWeight = FontWeight.SemiBold, color = ColorTokens.textPrimary())
                Text(contact.phone ?: contact.email ?: "", style = MaterialTheme.typography.bodySmall, color = ColorTokens.textSecondary())
            }
            if (contact.is_primary == true) {
                RiskPill(label = "Primary", color = Color(0xFF1B5E20), background = Color(0xFFE8F5E9))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onSetPrimary, enabled = contact.id != null && contact.is_primary != true) {
                Text("Set Primary")
            }
            OutlinedButton(onClick = onDelete, enabled = contact.id != null) {
                Text("Remove")
            }
        }
    }
}

@Composable
private fun PendingInviteCard(
    invite: PendingInvite,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorTokens.background(), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("You were added as trusted contact", fontWeight = FontWeight.SemiBold, color = ColorTokens.textPrimary())
        Text(
            "Requested by ${invite.sender_name ?: invite.sender_phone ?: "a family member"}",
            style = MaterialTheme.typography.bodySmall,
            color = ColorTokens.textSecondary()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onAccept, modifier = Modifier.weight(1f)) { Text("Accept") }
            OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) { Text("Reject") }
        }
    }
}

@Composable
private fun FamilyAlertRow(alert: NotificationAlert) {
    val riskLevel = (alert.risk_level ?: "high").uppercase()
    val accent = when (riskLevel) {
        "HIGH" -> Color(0xFFC62828)
        "MEDIUM" -> Color(0xFFEF6C00)
        else -> Color(0xFF2E7D32)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorTokens.background(), RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Outlined.PriorityHigh, contentDescription = null, tint = accent)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = alert.message ?: buildString {
                    append(alert.member_name ?: if (alert.source == "SELF") "Your device" else "Family member")
                    append(" triggered ")
                    append((alert.alert_type ?: "alert").replace("_", " ").lowercase())
                },
                fontWeight = FontWeight.SemiBold,
                color = ColorTokens.textPrimary()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                RiskPill(label = riskLevel, color = accent, background = accent.copy(alpha = 0.12f))
                Text(
                    text = "Score ${alert.risk_score}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorTokens.textSecondary()
                )
            }
        }
    }
}

@Composable
private fun RiskPill(label: String, color: Color, background: Color) {
    Surface(shape = RoundedCornerShape(999.dp), color = background) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    Text(message, color = ColorTokens.textSecondary(), style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun MessageStrip(message: String, color: Color, textColor: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = color)) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
