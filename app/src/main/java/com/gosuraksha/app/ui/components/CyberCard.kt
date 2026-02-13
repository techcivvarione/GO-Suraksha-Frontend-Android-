package com.gosuraksha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.auth.model.UserResponse

@Composable
fun CyberCard(
    user: UserResponse
) {

    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF145A32),
            Color(0xFF0B2F1F)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .padding(20.dp)
    ) {

        Column {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "GO Suraksha",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )

                PlanBadge(plan = user.plan)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = user.name.uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Cyber ID: ${formatId(user.id)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun PlanBadge(plan: String) {

    val backgroundColor = if (plan == "PAID") {
        Color(0xFFFFC107)
    } else {
        Color.White.copy(alpha = 0.2f)
    }

    val textColor = if (plan == "PAID") {
        Color.Black
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = plan,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

private fun formatId(id: String): String {
    return if (id.length >= 8) {
        "${id.take(4)}-${id.takeLast(4)}"
    } else {
        id
    }
}
