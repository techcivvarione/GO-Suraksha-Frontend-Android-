package com.gosuraksha.app.ui.main

import android.R.attr.id
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.scan.ScanViewModel

@Composable
fun ScanScreen(
    viewModel: ScanViewModel = viewModel()
) {

    val result by viewModel.result.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val upgradeRequired by viewModel.upgradeRequired.collectAsState()
    val aiExplanation by viewModel.aiExplanation.collectAsState()

    var input by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text("Analyze", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Enter content") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                viewModel.analyze("TEXT", input)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Analyze")
        }

        Spacer(Modifier.height(20.dp))

        if (loading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        result?.let { scan ->

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {

                    Text("Risk: ${scan.risk}")
                    Text("Score: ${scan.score}")

                    Spacer(Modifier.height(8.dp))

                    scan.reasons.forEach {
                        Text("• $it")
                    }

                    Spacer(Modifier.height(12.dp))

                    if (!upgradeRequired) {
                        Button(
                            onClick = {
                                scan.id?.let { scanId ->
                                    viewModel.loadAiExplanation(scanId = scanId)
                                }
                                      },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("AI Explain")
                        }
                    }

                    if (upgradeRequired) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Upgrade required for this feature.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        aiExplanation?.let {
            Spacer(Modifier.height(16.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("AI Explanation", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(it)
                }
            }
        }

        error?.let {
            Spacer(Modifier.height(16.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
