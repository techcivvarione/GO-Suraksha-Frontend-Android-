package com.gosuraksha.app.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.risk.RiskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskScreen(
    viewModel: RiskViewModel = viewModel()
) {

    val score by viewModel.score.collectAsState()
    val timeline by viewModel.timeline.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadScore()
        viewModel.loadTimeline()
        viewModel.loadInsights()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Risk Intelligence") })
        }
    ) { padding ->

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ---------------- SCORE ----------------

            item {
                score?.let {
                    Card {
                        Column(Modifier.padding(16.dp)) {

                            Text("Risk Score", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Score: ${it.score}",
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Text("Level: ${it.risk_level}")
                            Text("Window: ${it.window}")
                            Text("Total Scans: ${it.total_scans}")
                            Text("Generated At: ${it.generated_at}")

                            Spacer(Modifier.height(8.dp))
                            Text(it.summary)
                        }
                    }
                }
            }

            // ---------------- TIMELINE ----------------

            item {
                if (timeline.isNotEmpty()) {
                    Card {
                        Column(Modifier.padding(16.dp)) {

                            Text("Risk Timeline", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))

                            timeline.forEach {
                                Text(
                                    "${it.date} → Score: ${it.score} | H:${it.high} M:${it.medium} L:${it.low}"
                                )
                            }
                        }
                    }
                }
            }

            // ---------------- INSIGHTS ----------------

            item {
                insights?.let { summary ->

                    Card {
                        Column(Modifier.padding(16.dp)) {

                            Text("Insights", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(12.dp))

                            summary.peak_risk_days?.let {
                                Text("Peak Risk Days:")
                                Spacer(Modifier.height(4.dp))
                                it.forEach { day ->
                                    Text("${day.date} → H:${day.high} M:${day.medium} L:${day.low}")
                                }
                                Spacer(Modifier.height(12.dp))
                            }

                            summary.top_scan_keywords?.let {
                                Text("Top Keywords:")
                                Spacer(Modifier.height(4.dp))
                                it.forEach { keyword ->
                                    Text("${keyword.keyword} (${keyword.count})")
                                }
                                Spacer(Modifier.height(12.dp))
                            }

                            summary.recommendations?.let {
                                Text("Recommendations:")
                                Spacer(Modifier.height(4.dp))
                                it.forEach { rec ->
                                    Text("• $rec")
                                }
                            }
                        }
                    }
                }
            }

            // ---------------- ERROR ----------------

            item {
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
