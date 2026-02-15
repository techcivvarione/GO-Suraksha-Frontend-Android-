package com.gosuraksha.app.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import com.gosuraksha.app.trusted.TrustedContactsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen() {

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Alerts", "Family")

    val trustedViewModel: TrustedContactsViewModel = viewModel()

    val contacts by trustedViewModel.contacts.collectAsState()
    val trustedAlerts by trustedViewModel.alerts.collectAsState()
    val loading by trustedViewModel.loading.collectAsState()
    val error by trustedViewModel.error.collectAsState()

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            trustedViewModel.loadContacts()
            trustedViewModel.loadAlerts()
        }
    }

    Column {

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {

            // ---------------- NORMAL ALERTS ----------------
            0 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("Alerts Content")
                }
            }

            // ---------------- FAMILY / TRUSTED ----------------
            1 -> {

                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@Column
                }

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ---------- CONTACTS ----------
                    item {
                        Text(
                            "Trusted Contacts",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(contacts) { contact ->
                        Card {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(contact.contact_name ?: "Unnamed Contact")

                                    contact.contact_email?.takeIf { it.isNotBlank() }?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        contact.id?.let {
                                            trustedViewModel.deleteContact(it)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete"
                                    )
                                }
                            }
                        }
                    }

                    // ---------- ALERTS ----------
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Trusted Alerts",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(trustedAlerts) { alert ->
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(alert.message ?: "No message")

                                if (!alert.created_at.isNullOrBlank()) {
                                    Text(
                                        alert.created_at,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
