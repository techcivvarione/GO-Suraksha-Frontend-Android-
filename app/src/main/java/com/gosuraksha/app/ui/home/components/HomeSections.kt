package com.gosuraksha.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HeroBannerSection(
    isDark: Boolean,
    name: String,
    onOpenDashboard: () -> Unit
) {
    val gradStart = if (isDark) Color(0xFF041A0A) else Color(0xFF052E10)
    val gradMid = if (isDark) Color(0xFF0D4020) else Color(0xFF15803D)
    val gradEnd = if (isDark) Color(0xFF041A0A) else Color(0xFF052E10)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(
                Brush.linearGradient(
                    listOf(gradStart, gradMid, gradEnd),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-30).dp)
                .background(Color(0xFF22C55E).copy(alpha = 0.08f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(75.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-20).dp, y = 20.dp)
                .background(Color(0xFF22C55E).copy(alpha = 0.05f), CircleShape)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 14.dp)
                .size(56.dp)
                .background(
                    Color(0xFF22C55E).copy(alpha = 0.18f),
                    RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = Color(0xFF86EFAC),
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 18.dp, top = 20.dp)
        ) {
            Text(
                text = "GO Suraksha · Security",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF86EFAC).copy(alpha = 0.75f),
                letterSpacing = 0.3.sp
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "Stay Protected\nfrom Cyber Threats",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp,
                lineHeight = 26.sp
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .clickable(onClick = onOpenDashboard)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "View Dashboard",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

@Composable
fun ScoreRow(
    isDark: Boolean,
    scans: Int,
    threats: Int,
    risk: String
) {
    val riskNorm = risk.lowercase()
    val scoreInt = when (riskNorm) { "high" -> 35; "medium" -> 58; else -> 82 }
    val riskLabel = when (riskNorm) { "high" -> "High Risk"; "medium" -> "Medium"; else -> "Low Risk" }
    val riskColor = when (riskNorm) { "high" -> GS.Red; "medium" -> GS.Amber; else -> GS.Green500 }
    val scoreSubtitle = when (riskNorm) {
        "high" -> "⚠️ Risks found — act now"
        "medium" -> "Stay alert — check your scans"
        else -> "You're protected ✓"
    }
    val divColor = if (isDark) GS.DarkBorder else GS.LightBorder

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) GS.DarkSurface else GS.LightSurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            val trackColor = if (isDark) GS.DarkBorder else GS.LightBorder
            Canvas(Modifier.fillMaxSize()) {
                val stroke = androidx.compose.ui.graphics.drawscope.Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                drawArc(trackColor, -220f, 260f, false, style = stroke)
                drawArc(riskColor, -220f, (scoreInt / 100f) * 260f, false, style = stroke)
            }
            Text(
                text = "$scoreInt",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GS.onSurf(isDark),
                lineHeight = 13.sp
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = scoreSubtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GS.onSurf(isDark),
                letterSpacing = (-0.2).sp
            )
            Text(
                text = riskLabel,
                fontSize = 9.sp,
                color = riskColor,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ScoreStat("$scans", "Scans", GS.Green500, isDark)
            Box(modifier = Modifier.width(0.5.dp).height(22.dp).background(divColor))
            ScoreStat("$threats", "Threats", if (threats > 0) GS.Red else GS.Green500, isDark)
            Box(modifier = Modifier.width(0.5.dp).height(22.dp).background(divColor))
            ScoreStat(riskLabel.split(" ").first(), "Risk", riskColor, isDark)
        }
    }
}

@Composable
fun ScoreStat(value: String, label: String, color: Color, isDark: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 8.sp, color = GS.mutedText(isDark))
    }
}

@Composable
fun ScoreRowLoading(isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) GS.DarkSurface else GS.LightSurface)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = GS.Green500, strokeWidth = 2.5.dp)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Loading security status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GS.onSurf(isDark))
            Text("Refreshing your dashboard", fontSize = 9.sp, color = GS.mutedText(isDark))
        }
    }
}

@Composable
fun ScoreRowEmpty(isDark: Boolean, onStartScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) GS.DarkSurface else GS.LightSurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = GS.Green500,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "Your safety is unknown",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GS.onSurf(isDark)
        )
        Text(
            text = "Scan a message or link now to find out if you're at risk. Takes 10 seconds.",
            fontSize = 10.sp,
            color = GS.mutedText(isDark),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onStartScan,
            modifier = Modifier.fillMaxWidth().height(38.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GS.Green700, contentColor = Color.White)
        ) {
            Text("Check if I'm Safe →", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ScoreRowError(isDark: Boolean, onRetry: () -> Unit, onHistory: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) GS.DarkSurface else GS.LightSurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Dashboard unavailable", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GS.onSurf(isDark))
        Text("Couldn't load your security snapshot.", fontSize = 10.sp, color = GS.mutedText(isDark))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f).height(38.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GS.Green700, contentColor = Color.White)
            ) { Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            OutlinedButton(
                onClick = onHistory,
                modifier = Modifier.weight(1f).height(38.dp),
                shape = RoundedCornerShape(10.dp)
            ) { Text("History", fontSize = 11.sp) }
        }
    }
}

@Composable
fun QuickToolsSection(isDark: Boolean, tools: List<QuickTool>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) GS.DarkSurface else GS.LightSurface)
            .padding(top = 16.dp, bottom = 18.dp)
    ) {
        Text(
            text = "Quick Tools",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = GS.onSurf(isDark),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(14.dp))
        tools.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowItems.forEach { tool -> QuickToolTile(tool = tool, isDark = isDark) }
                repeat(4 - rowItems.size) { Box(modifier = Modifier.width(58.dp)) }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
fun QuickToolTile(tool: QuickTool, isDark: Boolean) {
    val iconBg = if (isDark) tool.iconBgDark else tool.iconBgLight
    val iconTint = if (isDark) tool.iconTintDark else tool.iconTintLight

    Column(
        modifier = Modifier.width(58.dp).clickable(onClick = tool.onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier.size(50.dp).background(iconBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = tool.icon, contentDescription = tool.label, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Text(
            text = tool.label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = GS.mutedText(isDark),
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun LiveThreatsSection(
    isDark: Boolean,
    onSeeAll: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val threats = listOf(
        ThreatItem("Phishing cluster · Mumbai", "32 reports · 2 min ago", "HIGH", Icons.Rounded.Warning, onNavigateToAlerts),
        ThreatItem("UPI scam wave · Delhi", "18 reports · 5 min ago", "MED", Icons.Rounded.CreditCard, onNavigateToAlerts),
        ThreatItem("Vishing attempt · Pune", "9 reports · 11 min ago", "LOW", Icons.Rounded.Notifications, onNavigateToAlerts)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) GS.DarkSurface else GS.LightSurface)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Live Threats", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GS.onSurf(isDark))
            Text(
                text = "See all →",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = GS.Green600,
                modifier = Modifier.clickable(onClick = onSeeAll)
            )
        }
        Spacer(Modifier.height(12.dp))
        threats.forEachIndexed { index, threat ->
            ThreatRow(threat = threat, isDark = isDark, showDivider = index < threats.lastIndex)
        }
    }
}

@Composable
fun ThreatRow(threat: ThreatItem, isDark: Boolean, showDivider: Boolean) {
    val (severityBg, severityText) = when (threat.severity) {
        "HIGH" -> if (isDark) GS.DarkIconRed to GS.Red else GS.LightIconRed to GS.RedLight
        "MED" -> if (isDark) GS.DarkIconAmber to GS.Amber else GS.LightIconAmber to GS.AmberLight
        else -> if (isDark) GS.DarkIconGreen to GS.Green500 else GS.LightIconGreen to GS.Green600
    }
    val iconTint = severityText

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = threat.onClick)
                .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(38.dp).background(severityBg, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = threat.icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = threat.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GS.onSurf(isDark), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = threat.subtitle, fontSize = 9.sp, color = GS.mutedText(isDark))
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(severityBg).padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(text = threat.severity, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = severityText)
            }
        }
        if (showDivider) {
            Box(
                modifier = Modifier.fillMaxWidth().height(0.5.dp).padding(horizontal = 16.dp).background(if (isDark) GS.DarkBorder else GS.LightBorder)
            )
        }
    }
}

@Composable
fun SectionGap(isDark: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth().height(8.dp).background(if (isDark) GS.DarkGap else GS.LightGap)
    )
}

@Composable
fun FeatureShortcutSection(title: String, isDark: Boolean, tools: List<SectionTool>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) GS.DarkSurface else GS.LightSurface)
            .padding(top = 16.dp, bottom = 18.dp)
    ) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GS.onSurf(isDark), modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(14.dp))
        tools.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowItems.forEach { tool ->
                    val iconBg = if (isDark) tool.iconBgDark else tool.iconBgLight
                    val iconTint = if (isDark) tool.iconTintDark else tool.iconTintLight
                    Column(
                        modifier = Modifier.width(58.dp).clickable(onClick = tool.onClick),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Box(modifier = Modifier.size(50.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(imageVector = tool.icon, contentDescription = tool.label, tint = iconTint, modifier = Modifier.size(24.dp))
                        }
                        Text(
                            text = tool.label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = GS.mutedText(isDark),
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                repeat(4 - rowItems.size) { Box(modifier = Modifier.width(58.dp)) }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
fun CyberCardEntryPanel(
    isDark: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val surface = if (isDark) Color(0xFF0D1F14) else Color(0xFFFFFFFF)
    val border = if (isDark) GS.Green500.copy(alpha = 0.20f) else GS.Green500.copy(alpha = 0.28f)
    val onSurf = if (isDark) Color(0xFFD4EDD9) else Color(0xFF0D1F14)
    val subClr = if (isDark) Color(0xFF6BAA80) else Color(0xFF5A8A6A)
    val segBg = if (isDark) Color(0xFF1F2A37) else Color(0xFFCCE3D4)

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        shadowElevation = if (isDark) 12.dp else 3.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.size(36.dp).background(Color(0xFF0D1F14), RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Shield, null, tint = GS.Green500, modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Cyber Card", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onSurf, letterSpacing = (-0.2).sp)
                    Text("Live security score", fontSize = 10.sp, color = subClr)
                }
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(if (isDark) Color(0xFF1F2A37) else Color(0xFFE2F0E8)).clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Text("×", fontSize = 16.sp, color = subClr, lineHeight = 16.sp)
                }
            }
            Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("418", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = GS.Red, lineHeight = 30.sp, letterSpacing = (-1).sp)
                Text("/ 1000", fontSize = 11.sp, color = subClr, modifier = Modifier.padding(bottom = 4.dp))
                Spacer(Modifier.weight(1f))
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(GS.Red.copy(alpha = 0.11f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text("High Risk", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = GS.Red)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(GS.Red))
                    repeat(3) {
                        Box(modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(segBg))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Poor", fontSize = 8.sp, color = subClr)
                    Text("Excellent", fontSize = 8.sp, color = subClr)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniStat("Scans", "12", isDark, Modifier.weight(1f))
                MiniStat("Threats", "3", isDark, Modifier.weight(1f))
                MiniStat("Breaches", "1", isDark, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MiniStat(label: String, value: String, isDark: Boolean, modifier: Modifier = Modifier) {
    val bg = if (isDark) Color(0xFF132318) else Color(0xFFF0F6F2)
    val valClr = if (isDark) Color(0xFFD4EDD9) else Color(0xFF0D1F14)
    val lblClr = if (isDark) Color(0xFF5A8A6A) else Color(0xFF8BB89A)
    Column(
        modifier = modifier.clip(RoundedCornerShape(9.dp)).background(bg).padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valClr, lineHeight = 13.sp)
        Text(label, fontSize = 8.sp, color = lblClr, textAlign = TextAlign.Center)
    }
}

@Composable
fun CyberCardPeekTab(isDark: Boolean, onClick: () -> Unit) {
    val surface = if (isDark) Color(0xFF0D1F14) else Color(0xFFFFFFFF)
    val border = if (isDark) GS.Green500.copy(alpha = 0.22f) else GS.Green500.copy(alpha = 0.30f)
    val subClr = if (isDark) Color(0xFF6BAA80) else Color(0xFF5A8A6A)

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp, topEnd = 0.dp, bottomEnd = 0.dp),
        color = surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        shadowElevation = if (isDark) 10.dp else 3.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Rounded.Shield, "Open Cyber Card", tint = GS.Green500, modifier = Modifier.size(18.dp))
            Text("418", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GS.Red, lineHeight = 11.sp)
            Text("Score", fontSize = 8.sp, color = subClr)
        }
    }
}
