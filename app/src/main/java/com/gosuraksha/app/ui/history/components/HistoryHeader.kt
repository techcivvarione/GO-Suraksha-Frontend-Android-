package com.gosuraksha.app.ui.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R
import com.gosuraksha.app.design.tokens.ColorTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTopBar(
    scanCount: Int,
    threatCount: Int,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorTokens.surface())
    ) {
        TopAppBar(
            // Zero insets: the outer MainScaffold/Scaffold already adds top padding
            // via innerPadding (from EnterpriseTopBar). Without this, TopAppBar
            // adds its own status-bar insets, creating a visible empty gap.
            windowInsets = WindowInsets(0),
            title = {
                Column {
                    Text(
                        text = stringResource(R.string.ui_historyscreen_1),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ColorTokens.textPrimary()
                        )
                    )
                    if (scanCount > 0) {
                        Text(
                            text = buildString {
                                append("$scanCount scans")
                                if (threatCount > 0) {
                                    append(" · $threatCount threat${if (threatCount > 1) "s" else ""}")
                                }
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (threatCount > 0) ColorTokens.error()
                                else ColorTokens.textSecondary()
                            )
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.ui_historyscreen_4),
                        tint = ColorTokens.textPrimary()
                    )
                }
            },
            actions = {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(36.dp)
                        .background(
                            color = ColorTokens.accent().copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 18.sp)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ColorTokens.surface()
            )
        )

        if (threatCount > 0) {
            ThreatAlertBanner(count = threatCount)
        }

        Divider(color = ColorTokens.border().copy(alpha = 0.5f), thickness = 1.dp)
    }
}

@Composable
fun ThreatAlertBanner(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorTokens.error().copy(alpha = 0.08f))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("🚨", fontSize = 16.sp)
        Text(
            text = "$count active threat${if (count > 1) "s" else ""} detected — review below",
            style = MaterialTheme.typography.labelMedium.copy(
                color = ColorTokens.error(),
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
