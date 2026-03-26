package com.gosuraksha.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.history.HistoryViewModel
import com.gosuraksha.app.history.HistoryViewModelFactory
import com.gosuraksha.app.ui.history.components.HistoryEmptyState
import com.gosuraksha.app.ui.history.components.HistoryErrorState
import com.gosuraksha.app.ui.history.components.HistoryList
import com.gosuraksha.app.ui.history.components.HistoryLoadingState
import com.gosuraksha.app.ui.history.components.HistoryTopBar
import com.gosuraksha.app.ui.history.model.RiskLevel
import com.gosuraksha.app.ui.history.model.toGroupedListItems
import com.gosuraksha.app.ui.history.model.toRiskLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as android.app.Application
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(app))
    val history by viewModel.history.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadHistory() }

    val safeHistory = history.orEmpty()
    val threatCount = safeHistory.count { it.risk.toRiskLevel() == RiskLevel.THREAT }
    val listItems = remember(safeHistory) { safeHistory.toGroupedListItems() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorTokens.background())
    ) {
        HistoryTopBar(
            scanCount = safeHistory.size,
            threatCount = threatCount,
            onBack = onBack
        )
        when {
            loading -> HistoryLoadingState()
            error != null -> HistoryErrorState(message = error ?: "Unknown error")
            safeHistory.isEmpty() -> HistoryEmptyState()
            else -> HistoryList(
                items = listItems,
                onDelete = { item -> viewModel.deleteHistory(item.id) }
            )
        }
    }
}
