package com.gosuraksha.app.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke  // ✅ FIX 2: missing Stroke import
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.scan.ScanViewModel
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// Palette
// ─────────────────────────────────────────────────────────────────────────────
private val CyberTeal     = Color(0xFF00E5C3)
private val CyberTealDim  = Color(0xFF00B89C)
private val DangerRed     = Color(0xFFFF3B5C)
private val WarnAmber     = Color(0xFFFFB020)
private val SafeGreen     = Color(0xFF00D68F)
private val SurfaceDark   = Color(0xFF0D1117)
private val CardDark      = Color(0xFF161B22)
private val CardBorder    = Color(0xFF30363D)
private val TextPrimary   = Color(0xFFE6EDF3)
private val TextSecondary = Color(0xFF8B949E)

// ─────────────────────────────────────────────────────────────────────────────
// Model
// ─────────────────────────────────────────────────────────────────────────────
enum class ScanType(val label: String, val apiType: String) {
    MESSAGES("Messages", "THREAT"),
    EMAIL("Email Checker", "EMAIL"),
    PASSWORD("Password Checker", "PASSWORD")
}

// ─────────────────────────────────────────────────────────────────────────────
// ScanScreen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ScanScreen(viewModel: ScanViewModel = viewModel()) {

    val result          by viewModel.result.collectAsState()
    val loading         by viewModel.loading.collectAsState()
    val error           by viewModel.error.collectAsState()
    val upgradeRequired by viewModel.upgradeRequired.collectAsState()
    val aiExplanation   by viewModel.aiExplanation.collectAsState()

    var selectedType by remember { mutableStateOf(ScanType.MESSAGES) }
    var input        by remember { mutableStateOf("") }

    // ✅ FIX 1 & 3: Resolve ALL MaterialTheme values here — in @Composable scope.
    //    Canvas runs inside DrawScope which is NOT @Composable.
    //    Pass resolved colors as plain Color params to Canvas-containing composables.
    val primaryColor          = MaterialTheme.colorScheme.primary
    val surfaceVariantColor   = MaterialTheme.colorScheme.surfaceVariant
    val onPrimaryColor        = MaterialTheme.colorScheme.onPrimary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor            = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
    ) {

        // ✅ primaryColor passed as param — no MaterialTheme inside Canvas
        AnimatedCyberBackground(dotColor = primaryColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── HEADER ────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(CyberTeal.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = CyberTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Scan Intelligence",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Threats · Breaches · Leaks",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        letterSpacing = 0.4.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── SEGMENTED CONTROL ─────────────────────────────────────
            // ✅ Passing resolved colors as params — no MaterialTheme inside non-composable lambdas
            SegmentedControl(
                selected = selectedType,
                onSelected = { selectedType = it; input = "" },
                primaryColor = primaryColor,
                onPrimaryColor = onPrimaryColor,
                surfaceVariantColor = surfaceVariantColor,
                onSurfaceVariantColor = onSurfaceVariantColor
            )

            Spacer(Modifier.height(20.dp))

            // ── INPUT ─────────────────────────────────────────────────
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Enter ${selectedType.label.lowercase()}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (selectedType == ScanType.MESSAGES)
                            Modifier.height(120.dp)
                        else Modifier
                    ),
                singleLine = selectedType != ScanType.MESSAGES,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberTeal,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = CyberTeal,
                    focusedLabelColor = CyberTeal,
                    unfocusedLabelColor = TextSecondary,
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(14.dp))

            // ── SCAN BUTTON ───────────────────────────────────────────
            val canScan = input.isNotBlank() && !loading
            Button(
                onClick = { viewModel.analyze(selectedType.apiType, input) },
                enabled = canScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (canScan)
                                Brush.horizontalGradient(listOf(CyberTeal, CyberTealDim))
                            else
                                Brush.horizontalGradient(listOf(CardBorder, CardBorder)),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Analyze",
                        color = if (canScan) SurfaceDark else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── LOADER ────────────────────────────────────────────────
            if (loading) {
                // ✅ primaryColor passed as param — no MaterialTheme inside Canvas
                ShieldBuildingLoader(arcColor = primaryColor)
            }

            // ── RESULT CARD ───────────────────────────────────────────
            result?.let { scan ->

                val riskColor = when (scan.risk.lowercase()) {
                    "high"   -> DangerRed
                    "medium" -> WarnAmber
                    else     -> SafeGreen
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardDark)
                ) {
                    // Colored top strip
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

                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        // Risk label + score circle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "SCAN RESULT",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(Modifier.height(5.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(riskColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        scan.risk.uppercase(),
                                        color = riskColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(Modifier.weight(1f))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(riskColor.copy(alpha = 0.10f))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "${scan.score}",
                                        color = riskColor,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "score",
                                        color = riskColor.copy(alpha = 0.7f),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        Divider(color = CardBorder, thickness = 1.dp)

                        // ── REASONS ───────────────────────────────────
                        if (scan.reasons.isNotEmpty()) {
                            SectionLabel("Findings")
                            scan.reasons.forEach { reason ->
                                BulletRow(text = reason, dotColor = riskColor)
                            }
                        }

                        // ── BREACH COUNT ───────────────────────────────
                        scan.count?.let { count ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DangerRed.copy(alpha = 0.07f))
                                    .padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Breaches Found",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "$count",
                                    color = DangerRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        // ── SITES (paid — scan.sites flat list) ────────
                        val flatSites = scan.sites
                        if (!upgradeRequired && !flatSites.isNullOrEmpty()) {
                            SectionLabel("Breached Sites")
                            flatSites.forEach { site ->
                                SiteRow(site = site)
                            }
                        }

                        // ── BREACH ANALYSIS (paid — nested categories) ─
                        val breachAnalysis = scan.breach_analysis
                        if (!upgradeRequired && breachAnalysis != null) {

                            // Category breakdown
                            breachAnalysis.categories?.forEach { (category, detail) ->
                                if (detail.sites.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    // Category header chip
                                    val sevColor = when (detail.severity.lowercase()) {
                                        "high"   -> DangerRed
                                        "medium" -> WarnAmber
                                        else     -> SafeGreen
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            category.uppercase(),
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(sevColor.copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                detail.severity.uppercase(),
                                                color = sevColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    detail.sites.forEach { site ->
                                        SiteRow(site = site)
                                    }
                                }
                            }
                        }

                        // ── DOMAINS (paid — alternate field) ──────────
                        val domains = scan.domains
                        if (!upgradeRequired && !domains.isNullOrEmpty()) {
                            SectionLabel("Affected Domains")
                            domains.forEach { domain ->
                                SiteRow(site = domain)
                            }
                        }

                        // ── UPGRADE GATE (free users) ──────────────────
                        if (upgradeRequired) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(WarnAmber.copy(alpha = 0.07f))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🔒", fontSize = 26.sp)
                                    Text(
                                        "Upgrade to Pro",
                                        color = WarnAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        scan.upgrade?.message
                                            ?: "Upgrade to view full breach details, affected sites & AI analysis.",
                                        color = WarnAmber.copy(alpha = 0.75f),
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }

                        // ── AI EXPLAIN (paid only) ─────────────────────
                        if (!upgradeRequired && scan.id != null) {
                            OutlinedButton(
                                onClick = { viewModel.loadAiExplanation(scan.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, CyberTeal.copy(alpha = 0.5f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = CyberTeal
                                )
                            ) {
                                Text(
                                    "AI Explain",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── AI EXPLANATION CARD ───────────────────────────────────
            aiExplanation?.let { explanation ->
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardDark)
                        .padding(1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(CyberTeal, CyberTeal.copy(alpha = 0.2f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "AI ANALYSIS",
                            color = CyberTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            explanation,
                            color = TextPrimary.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // ── ERROR ─────────────────────────────────────────────────
            error?.let {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DangerRed.copy(alpha = 0.08f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        it,
                        color = DangerRed,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = TextSecondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun BulletRow(text: String, dotColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDark)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun SiteRow(site: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDark)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "🌐",
            fontSize = 13.sp
        )
        Spacer(Modifier.width(10.dp))
        Text(
            site,
            color = TextPrimary,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SegmentedControl(
    selected: ScanType,
    onSelected: (ScanType) -> Unit,
    primaryColor: Color,
    onPrimaryColor: Color,
    surfaceVariantColor: Color,
    onSurfaceVariantColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        ScanType.entries.forEach { type ->
            val isSelected = type == selected

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) CyberTeal.copy(alpha = 0.15f) else Color.Transparent,
                animationSpec = tween(220),
                label = "seg_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) CyberTeal else TextSecondary,
                animationSpec = tween(220),
                label = "seg_text"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .clickable { onSelected(type) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.label,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ShieldBuildingLoader
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ShieldBuildingLoader(
    arcColor: Color  // ✅ FIX 3: color passed as param, NOT accessed inside Canvas
) {
    val infinite = rememberInfiniteTransition(label = "loader")

    val progress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "arc_rot"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            // Track ring
            drawCircle(
                color = arcColor.copy(alpha = 0.08f),
                style = Stroke(width = 8f)  // ✅ FIX 2: Stroke now imported correctly
            )
            // Spinning arc
            drawArc(
                color = arcColor,
                startAngle = progress,
                sweepAngle = 110f,
                useCenter = false,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
        }

        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = arcColor,  // ✅ arcColor is a plain Color param
            modifier = Modifier.size(48.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AnimatedCyberBackground
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AnimatedCyberBackground(
    dotColor: Color  // ✅ FIX 1: color passed as param, NOT accessed inside Canvas
) {
    val infinite = rememberInfiniteTransition(label = "bg_anim")

    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(22000, easing = LinearEasing)),
        label = "bg_angle"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val outerRadius = size.minDimension / 2.3f
        // Outer ring of dots
        for (i in 0..20) {
            val a = Math.toRadians((angle + i * 18.0))
            drawCircle(
                color = dotColor.copy(alpha = 0.045f),
                radius = 5f,
                center = Offset(
                    x = center.x + outerRadius * cos(a).toFloat(),
                    y = center.y + outerRadius * sin(a).toFloat()
                )
            )
        }
        // Inner counter-rotating ring
        val innerRadius = size.minDimension / 3.6f
        for (i in 0..12) {
            val a = Math.toRadians((-angle * 0.55 + i * 27.7))
            drawCircle(
                color = dotColor.copy(alpha = 0.06f),
                radius = 3f,
                center = Offset(
                    x = center.x + innerRadius * cos(a).toFloat(),
                    y = center.y + innerRadius * sin(a).toFloat()
                )
            )
        }
    }
}