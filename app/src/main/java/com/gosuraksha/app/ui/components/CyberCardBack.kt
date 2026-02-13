package com.gosuraksha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CyberCardBack(
    signals: Map<String, Any>?
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
            .height(260.dp) // 🔥 increased height
            .padding(4.dp)
            .background(
                brush = gradient,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // 🔥 prevents cut
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ========================
            // HEADER + BREAKDOWN
            // ========================
            Column {

                Text(
                    text = "CYBER CARD",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Score Breakdown",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(18.dp))

                signals?.let {

                    BreakdownRow("Scam Reports", it["scam_reports"])
                    BreakdownRow("Email Breaches", it["email_breaches"])
                    BreakdownRow("Password Breaches", it["password_breaches"])
                    BreakdownRow("Scan Reward Points", it["scan_reward_points"])
                    BreakdownRow("OCR Bonus", it["ocr_bonus"])
                }
            }

            Spacer(Modifier.height(16.dp))

            // ========================
            // LEGAL DISCLAIMER
            // ========================
            Text(
                text = "This Cyber Card is generated using your security activity including Email, Password, Threat scans, OCR usage and Scam Reports. It represents a digital risk profile and is NOT a government-issued identity document.",
                color = Color.White.copy(alpha = 0.65f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: Any?) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = formatNumber(value),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatNumber(value: Any?): String {
    return when (value) {
        is Double -> value.toInt().toString()
        is Int -> value.toString()
        is Long -> value.toString()
        else -> "0"
    }
}
