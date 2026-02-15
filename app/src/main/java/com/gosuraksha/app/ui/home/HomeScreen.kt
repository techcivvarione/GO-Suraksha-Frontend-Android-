package com.gosuraksha.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.home.HomeViewModel
import androidx.compose.material3.MaterialTheme


@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToRisk: () -> Unit
) {

    val viewModel: HomeViewModel = viewModel()
    val overview by viewModel.overview.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOverview()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // ---------------- HEADER ----------------

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(20.dp)
        ) {

            Column {
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Text(
                    text = "Your Personal Cyber Home",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            }

            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // ---------------- ACTION CARDS ----------------

            HomeActionCard(
                title = "Risk Intelligence",
                subtitle = "View risk score, timeline & insights",
                icon = Icons.Default.Security,
                onClick = onNavigateToRisk
            )

            HomeActionCard(
                title = "Scan History",
                subtitle = "View past scans & delete records",
                icon = Icons.Default.History,
                onClick = onNavigateToHistory
            )

            // ---------------- SECURITY SNAPSHOT ----------------

            overview?.let { data ->

                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        Text(
                            text = "Security Snapshot",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Scans Done: ${data.security_snapshot.scans_done}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    "Threats Detected: ${data.security_snapshot.threats_detected}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            StaticRiskGauge(
                                risk = data.security_snapshot.overall_risk
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(Icons.Default.ArrowForwardIos, contentDescription = null)
        }
    }
}

@Composable
fun StaticRiskGauge(risk: String) {

    val progress = when (risk.lowercase()) {
        "high" -> 0.85f
        "medium" -> 0.55f
        else -> 0.25f
    }

    val riskColor = when (risk.lowercase()) {
        "high" -> Color(0xFFE53935)
        "medium" -> Color(0xFFFFB74D)
        else -> Color(0xFF4CAF50)
    }

    // ✅ Resolve the theme color here in @Composable scope — NOT inside Canvas/DrawScope
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(90.dp)
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {

            // ✅ Background track circle — uses trackColor resolved above, no @Composable call here
            drawCircle(
                color = trackColor,
                style = Stroke(width = 12f)
            )

            // ✅ Foreground arc showing risk level
            drawArc(
                color = riskColor,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = 12f)
            )
        }

        Text(
            text = risk.uppercase(),
            color = riskColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}