package com.gosuraksha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LockedCyberCard(
    onUpgradeClick: () -> Unit
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
            .background(gradient, RoundedCornerShape(24.dp))
            .clickable { onUpgradeClick() },
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = "Upgrade to Premium to Unlock\nYour Cyber Card",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
    }
}
