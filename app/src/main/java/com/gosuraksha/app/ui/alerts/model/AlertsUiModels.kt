package com.gosuraksha.app.ui.alerts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class AlertsTabSpec(
    val label: String,
    val icon: ImageVector
)

@Composable
fun alertsTabs(): List<AlertsTabSpec> = listOf(
    AlertsTabSpec("Alerts", Icons.Outlined.NotificationsActive),
    AlertsTabSpec("Family", Icons.Outlined.Groups),
    AlertsTabSpec("Scam Network", Icons.Outlined.Warning)
)

@Composable
fun neutralCardColors(): CardColors = CardDefaults.cardColors(
    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
)

@Composable
fun neutralCardBorder(): BorderStroke = BorderStroke(
    width = 1.dp,
    color = androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
)

val neutralCardShape = RoundedCornerShape(14.dp)

fun formatAlertDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "-"
    return try {
        dateStr.substring(0, 10)
    } catch (_: Exception) {
        dateStr
    }
}
