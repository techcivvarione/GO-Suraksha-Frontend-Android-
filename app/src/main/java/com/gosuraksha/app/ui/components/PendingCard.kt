package com.gosuraksha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PendingCard(message: String) {

    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF444444), Color(0xFF222222))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .background(gradient)
            .padding(20.dp)
    ) {
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
