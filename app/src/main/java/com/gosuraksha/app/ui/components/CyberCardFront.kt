package com.gosuraksha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.auth.model.UserResponse

@Composable
fun CyberCardFront(
    user: UserResponse,
    cyberScore: Int,
    maxScore: Int,
    riskLevel: String,
    generatedOn: String,
    validTill: String
) {

    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6E7685),
            Color(0xFF2B3440),
            Color(0xFF0B1118)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(4.dp)
            .background(gradient, shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "GO Suraksha",
                    color = Color(0xFF00D084),
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "CYBER CARD",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "CYBER CARD NUMBER",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = user.id,
                color = Color.Black,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        text = "Name",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = user.name,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.End) {

                    Text(
                        text = cyberScore.toString(),
                        color = scoreColor(riskLevel),
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Text(
                        text = "CYBER SCORE",
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Text(
                        text = "$cyberScore / $maxScore",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = riskLevel,
                        color = scoreColor(riskLevel),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Generated on: $generatedOn",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Valid till: $validTill",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun scoreColor(level: String): Color {
    return when (level) {
        "Elite" -> Color(0xFF00E676)
        "Safe" -> Color(0xFF4CAF50)
        "Medium Risk" -> Color(0xFFFF9800)
        "High Risk" -> Color.Red
        "Critical" -> Color(0xFFB71C1C)
        else -> Color.White
    }
}
