package com.gosuraksha.app.ui.screens

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.ui.viewmodel.CyberSosViewModel
import com.gosuraksha.app.data.remote.dto.CyberSosRequest


@Composable
fun CyberSosScreen(
    viewModel: CyberSosViewModel = viewModel()
) {

    val state = viewModel.uiState

    var scamType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var lossAmount by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("🚨 Cyber SOS", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = scamType,
            onValueChange = { scamType = it },
            label = { Text("Scam Type") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = lossAmount,
            onValueChange = { lossAmount = it },
            label = { Text("Loss Amount (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = source,
            onValueChange = { source = it },
            label = { Text("Source (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.triggerSos(
                    CyberSosRequest(
                        scam_type = scamType,
                        incident_date = java.time.LocalDate.now().toString(),
                        description = description,
                        loss_amount = lossAmount.ifBlank { null },
                        source = source.ifBlank { null }
                    )
                )
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isLoading) "Processing..." else "Trigger Cyber SOS")
        }

        state.error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (state.success) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cyber SOS Successfully Triggered")
        }
    }
}
