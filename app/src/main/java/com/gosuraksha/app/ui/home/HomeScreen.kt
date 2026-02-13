package com.gosuraksha.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.home.HomeViewModel
import androidx.compose.runtime.collectAsState


@Composable
fun HomeScreen(
    onLogout: () -> Unit
) {

    val viewModel: HomeViewModel = viewModel()
    val overview by viewModel.overview.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOverview()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Home Dashboard", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator()
        }

        overview?.let { data ->

            Text("Scans Done: ${data.security_snapshot.scans_done}")
            Text("Threats Detected: ${data.security_snapshot.threats_detected}")
            Text("Overall Risk: ${data.security_snapshot.overall_risk}")

            Spacer(Modifier.height(16.dp))

            data.financial_impact?.global?.payload?.let { impact ->
                Text("Global Cyber Loss (${impact.year})")
                Text(impact.display_text)
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}
