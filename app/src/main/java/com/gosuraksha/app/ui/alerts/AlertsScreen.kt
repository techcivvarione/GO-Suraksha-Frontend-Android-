package com.gosuraksha.app.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.alerts.AlertsViewModel
import com.gosuraksha.app.alerts.AlertsViewModelFactory
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.scam.ui.rememberScamNetworkViewModel
import com.gosuraksha.app.trusted.TrustedContactsViewModel
import com.gosuraksha.app.trusted.TrustedContactsViewModelFactory

@Composable
fun AlertsScreen(
    onOpenScamNetwork: () -> Unit = {},
    onOpenScamDetail: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val app = LocalContext.current.applicationContext as android.app.Application

    val alertsViewModel: AlertsViewModel = viewModel(factory = AlertsViewModelFactory(app))
    val trustedViewModel: TrustedContactsViewModel = viewModel(factory = TrustedContactsViewModelFactory(app))
    val scamViewModel = rememberScamNetworkViewModel()
    val scamUiState by scamViewModel.uiState.collectAsStateWithLifecycle()

    val alerts by alertsViewModel.alerts.collectAsStateWithLifecycle()
    val summary by alertsViewModel.summary.collectAsStateWithLifecycle()
    val summaryLoading by alertsViewModel.summaryLoading.collectAsStateWithLifecycle()
    val summaryFailed by alertsViewModel.summaryFailed.collectAsStateWithLifecycle()
    val trustedAlerts by alertsViewModel.trusted.collectAsStateWithLifecycle()
    val familyActivity by alertsViewModel.familyActivity.collectAsStateWithLifecycle()
    val contacts by trustedViewModel.contacts.collectAsStateWithLifecycle()
    val loading by alertsViewModel.loading.collectAsStateWithLifecycle()
    val error by alertsViewModel.error.collectAsStateWithLifecycle()
    val trustedLoading by trustedViewModel.loading.collectAsStateWithLifecycle()
    val trustedError by trustedViewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        alertsViewModel.loadAlerts()
        alertsViewModel.loadSummary()
    }
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            trustedViewModel.loadContacts()
            trustedViewModel.loadAlerts()
            alertsViewModel.loadTrusted()
            alertsViewModel.loadFamilyActivity()
            alertsViewModel.loadFamilyFeed()
        } else if (selectedTab == 2) {
            scamViewModel.loadTrendingScams()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorTokens.background())
    ) {
        AlertsHeader(onRefresh = { alertsViewModel.refreshAlerts() })
        AlertsTabs(selectedTab = selectedTab, onTabChange = { selectedTab = it })
        Spacer(Modifier.height(SpacingTokens.md))
        when (selectedTab) {
            0 -> AlertsList(
                alerts = alerts,
                summary = summary,
                loading = loading,
                summaryLoading = summaryLoading,
                summaryFailed = summaryFailed,
                error = error,
                onRetry = { alertsViewModel.retrySummary() },
            )
            1 -> FamilyAlertsTab(
                contacts = contacts,
                trustedAlerts = trustedAlerts,
                familyActivity = familyActivity,
                loading = trustedLoading,
                error = trustedError,
                onDeleteContact = { id -> trustedViewModel.deleteContact(id) },
                onAddContact = { name, email, phone -> trustedViewModel.addContact(name, email, phone) },
                onMarkRead = { id -> alertsViewModel.markTrustedRead(id) }
            )
            2 -> ScamHubTab(
                uiState = scamUiState,
                onOpenScamNetwork = onOpenScamNetwork,
                onOpenScamDetail = onOpenScamDetail
            )
        }
    }
}
