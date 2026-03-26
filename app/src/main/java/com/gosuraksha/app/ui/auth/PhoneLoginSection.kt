package com.gosuraksha.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PhoneLoginSection(
    phone: String,
    isLoading: Boolean,
    onPhoneChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Phone number") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onContinue,
            enabled = phone.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue with Phone")
        }
    }
}
