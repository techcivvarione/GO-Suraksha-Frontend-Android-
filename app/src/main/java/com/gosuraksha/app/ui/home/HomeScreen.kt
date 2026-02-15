package com.gosuraksha.app.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.home.HomeViewModel
import com.gosuraksha.app.data.SessionManager

// ── Palette ───────────────────────────────────────────────────────────────────
private val HomeBg        = Color(0xFF0D1117)
private val CardDark      = Color(0xFF161B22)
private val CardBorder    = Color(0xFF30363D)
private val CyberTeal     = Color(0xFF00E5C3)
private val DangerRed     = Color(0xFFFF3B5C)
private val WarnAmber     = Color(0xFFFFB020)
private val SafeGreen     = Color(0xFF00D68F)
private val TextPrimary   = Color(0xFFE6EDF3)
private val TextSecondary = Color(0xFF8B949E)

@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToRisk: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel()
    val overview by viewModel.overview.collectAsState()
    val loading  by viewModel.loading.collectAsState()
    val user     by SessionManager.user.collectAsState()

    // Resolve theme color in composable scope
    val primaryColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) { viewModel.loadOverview() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
            .verticalScroll(rememberScrollState())
    ) {

        // ── HERO HEADER ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            // Background gradient mesh
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0A2E2A),
                                Color(0xFF0D1117)
                            )
                        )
                    )
            )
            // Teal radial glow top-right
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp, y = (-40).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                CyberTeal.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 22.dp, bottom = 20.dp)
            ) {
                Text(
                    text = "Good ${getGreeting()}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    letterSpacing = 0.4.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = user?.name?.split(" ")?.firstOrNull() ?: "User",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                // Status pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(SafeGreen.copy(alpha = 0.12f))
                        .border(1.dp, SafeGreen.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PulsingDot(color = SafeGreen)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Protected",
                        color = SafeGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

        }

        Spacer(Modifier.height(20.dp))

        // ── SECURITY SNAPSHOT ─────────────────────────────────────────
        if (loading) {
            SnapshotShimmer()
        }

        overview?.let { data ->
            val snap = data.security_snapshot
            val riskColor = when (snap.overall_risk.lowercase()) {
                "high"   -> DangerRed
                "medium" -> WarnAmber
                else     -> SafeGreen
            }

            // Snapshot card
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            ) {
                // Color strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(riskColor, riskColor.copy(alpha = 0.2f))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "SECURITY SNAPSHOT",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        SnapshotStat(
                            label = "Scans Done",
                            value = "${snap.scans_done}",
                            color = CyberTeal
                        )
                        Spacer(Modifier.height(8.dp))
                        SnapshotStat(
                            label = "Threats Detected",
                            value = "${snap.threats_detected}",
                            color = riskColor
                        )
                    }

                    // Risk gauge — passes resolved color, no MaterialTheme in Canvas
                    HomeRiskGauge(
                        risk = snap.overall_risk,
                        riskColor = riskColor,
                        trackColor = CardBorder
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── QUICK ACTIONS ─────────────────────────────────────────────
        Text(
            "Quick Actions",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            QuickActionCard(
                title    = "Risk\nIntelligence",
                icon     = Icons.Filled.Security,
                color    = CyberTeal,
                modifier = Modifier.weight(1f),
                onClick  = onNavigateToRisk
            )
            QuickActionCard(
                title    = "Scan\nHistory",
                icon     = Icons.Filled.History,
                color    = WarnAmber,
                modifier = Modifier.weight(1f),
                onClick  = onNavigateToHistory
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── TIPS STRIP ────────────────────────────────────────────────
        CyberTipCard(
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(100.dp)) // nav bar clearance
    }
}

// ── SNAPSHOT STAT ROW ──────────────────────────────────────────────────────────

@Composable
private fun SnapshotStat(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            value,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── QUICK ACTION CARD ──────────────────────────────────────────────────────────

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(22.dp))
            .background(CardDark)
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(18.dp)
    ) {
        // Soft color glow corner
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-30).dp)
                .background(
                    Brush.radialGradient(listOf(color.copy(alpha = 0.18f), Color.Transparent))
                )
        )

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("View", color = color, fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

// ── CYBER TIP CARD ────────────────────────────────────────────────────────────

private val tips = listOf(
    "Never reuse passwords across accounts.",
    "Enable 2FA on all your important accounts.",
    "Be cautious of unsolicited emails asking for info.",
    "Update your apps regularly to patch vulnerabilities.",
    "Use a VPN on public Wi-Fi networks."
)

@Composable
private fun CyberTipCard(modifier: Modifier = Modifier) {
    val tip = remember { tips.random() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CyberTeal.copy(alpha = 0.07f))
            .border(1.dp, CyberTeal.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CyberTeal.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = CyberTeal,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                "CYBER TIP",
                color = CyberTeal,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(3.dp))
            Text(
                tip,
                color = TextPrimary.copy(alpha = 0.85f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// ── RISK GAUGE ────────────────────────────────────────────────────────────────

@Composable
private fun HomeRiskGauge(
    risk: String,
    riskColor: Color,
    trackColor: Color    // ✅ resolved outside Canvas
) {
    val progress = when (risk.lowercase()) {
        "high"   -> 0.85f
        "medium" -> 0.55f
        else     -> 0.25f
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(86.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = trackColor, style = Stroke(width = 10f))
            drawArc(
                color      = riskColor,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter  = false,
                style      = Stroke(width = 10f, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                risk.uppercase(),
                color = riskColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text("risk", color = TextSecondary, fontSize = 9.sp)
        }
    }
}

// ── SHIMMER ───────────────────────────────────────────────────────────────────

@Composable
private fun SnapshotShimmer() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue  = -600f,
        targetValue   = 600f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label         = "sx"
    )
    val brush = Brush.linearGradient(
        colors = listOf(CardDark, Color(0xFF2A2A3E), CardDark),
        start  = androidx.compose.ui.geometry.Offset(shimmerX - 300f, 0f),
        end    = androidx.compose.ui.geometry.Offset(shimmerX + 300f, 0f)
    )
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(brush)
    )
}

// ── PULSING DOT ───────────────────────────────────────────────────────────────

@Composable
private fun PulsingDot(color: Color) {
    val pulse = rememberInfiniteTransition(label = "dot")
    val scale by pulse.animateFloat(
        initialValue  = 0.8f,
        targetValue   = 1.3f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label         = "dot_scale"
    )
    Box(
        modifier = Modifier
            .size(7.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(color)
    )
}

// ── GREETING ──────────────────────────────────────────────────────────────────

private fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Morning,"
        hour < 17 -> "Afternoon,"
        else      -> "Evening,"
    }
}