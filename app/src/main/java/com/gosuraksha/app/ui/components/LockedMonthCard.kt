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
fun LockedMonthCard(
    score: Int,
    message: String
) {

    val gradient = Brush.linearGradient(
        colors = listOf(Color.Gray, Color.DarkGray)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .background(gradient)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "CYBER CARD LOCKED",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Red
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = message,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Score remains: $score",
                color = Color.White
            )
        }
    }
}
