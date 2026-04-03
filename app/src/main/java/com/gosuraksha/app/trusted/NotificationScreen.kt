package com.gosuraksha.app.ui.trusted

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.trusted.TrustedContactsViewModel
import com.gosuraksha.app.trusted.TrustedContactsViewModelFactory

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NotificationScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as android.app.Application
    val viewModel: TrustedContactsViewModel = viewModel(factory = TrustedContactsViewModelFactory(app))
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                item {
                    NotificationSection("Invites") {
                        if (notifications.invites.isEmpty()) {
                            Text("No invite notifications.", color = ColorTokens.textSecondary())
                        } else {
                            notifications.invites.forEach { invite ->
                                NotificationRow(
                                    title = invite.contact_name ?: "Trusted contact invite",
                                    body = "From ${invite.sender_name ?: invite.sender_phone ?: "family member"}"
                                )
                            }
                        }
                    }
                }
                item {
                    NotificationSection("Alerts") {
                        if (notifications.alerts.isEmpty()) {
                            Text("No alert notifications.", color = ColorTokens.textSecondary())
                        } else {
                            notifications.alerts.forEach { alert ->
                                NotificationRow(
                                    title = (alert.alert_type ?: "Alert").replace("_", " "),
                                    body = alert.member_name ?: alert.message ?: "High-risk activity detected"
                                )
                            }
                        }
                    }
                }
                item {
                    NotificationSection("System Events") {
                        if (notifications.system_events.isEmpty()) {
                            Text("No system events.", color = ColorTokens.textSecondary())
                        } else {
                            notifications.system_events.forEach { event ->
                                NotificationRow(title = event.title, body = event.description)
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
private fun NotificationSection(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorTokens.surface())
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ColorTokens.textPrimary())
            content()
        }
    }
}

@Composable
private fun NotificationRow(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorTokens.background(), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, color = ColorTokens.textPrimary())
        Text(body, style = MaterialTheme.typography.bodySmall, color = ColorTokens.textSecondary())
    }
}
