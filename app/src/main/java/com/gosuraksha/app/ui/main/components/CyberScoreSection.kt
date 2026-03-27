package com.gosuraksha.app.ui.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActiveCyberCardContent(isDark: Boolean, card: CyberCardResponse, userName: String) {
    val score     = card.score ?: 0
    val maxScore  = card.max_score ?: 1000
    val riskLevel = card.risk_level ?: "Unknown"
    // Prefer V2 machine level for color; fall back to human risk_level
    val scoreColor = riskColor(card.level ?: riskLevel)

    // ── Score counting animation: 0 → score over 900 ms ──────────────────────
    var animStarted by remember { mutableStateOf(false) }
    val animatedScore by animateIntAsState(
        targetValue    = if (animStarted) score else 0,
        animationSpec  = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label          = "cyber_score_count"
    )
    LaunchedEffect(score) { animStarted = true }

    val humanLevel = levelLabel(card.level).takeIf { card.level != null } ?: riskLevel

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // ── Hero card — tap to flip; already shows animated score ─────────────
        com.gosuraksha.app.ui.components.CyberCardNew(
            userName    = userName.ifBlank { card.name ?: "GO Suraksha User" },
            cardNumber  = card.card_id ?: "CYBER CARD",
            cyberScore  = animatedScore,
            generatedOn = card.score_month ?: "--",
            validTill   = card.score_version ?: "--",
            level       = card.level ?: ""
        )

        // ── Score summary panel — ring + level label + month strip ────────────
        Surface(
            color          = cardBg(isDark),
            shape          = RoundedCornerShape(20.dp),
            tonalElevation = 0.dp,
            modifier       = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier              = Modifier
                    .padding(horizontal = 18.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: label + level badge + month
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Based on your recent activity",
                        color    = subText(isDark),
                        fontSize = 11.sp
                    )
                    Box(
                        modifier = Modifier
                            .background(scoreColor.copy(alpha = 0.13f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            humanLevel,
                            color      = scoreColor,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        card.score_month ?: "--",
                        color    = subText(isDark).copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }

                // Right: progress ring
                CyberScoreRing(
                    targetScore   = score,
                    animatedScore = animatedScore,
                    maxScore      = maxScore,
                    color         = scoreColor,
                    isDark        = isDark
                )
            }
        }

        // ── 4-segment progress strip ──────────────────────────────────────────
        CyberScoreSection(
            isDark     = isDark,
            score      = animatedScore,
            maxScore   = maxScore,
            riskLevel  = humanLevel,
            scoreMonth = card.score_month ?: "--",
            cardId     = card.card_id ?: "--"
        )

        ScoreBreakdownPanel(isDark = isDark, factors = card.factors)
        CyberScoreExplanationSection(isDark = isDark)
        CyberImprovementTipsSection(isDark = isDark)
    }
}

@Composable
fun CyberScoreSection(
    isDark: Boolean,
    score: Int,
    maxScore: Int,
    riskLevel: String,
    scoreMonth: String,
    cardId: String
) {
    val color    = riskColor(riskLevel)
    val progress = (score.toFloat() / maxScore.coerceAtLeast(1)).coerceIn(0f, 1f)
    val segBg    = if (isDark) Color(0xFF1F2A37) else Color(0xFFCCE3D4)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val litCount = when {
                progress >= 0.75f -> 4
                progress >= 0.50f -> 3
                progress >= 0.25f -> 2
                else              -> 1
            }
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .background(
                            if (index < litCount) color else segBg,
                            RoundedCornerShape(2.5.dp)
                        )
                )
            }
        }
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Poor",      fontSize = 9.sp, color = subText(isDark))
            Text("Excellent", fontSize = 9.sp, color = subText(isDark))
        }
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricChip(isDark, "Score", "$score / $maxScore", color,       Modifier.weight(1f))
            MetricChip(isDark, "Level", riskLevel,            color,       Modifier.weight(1f))
            MetricChip(isDark, "Month", scoreMonth,           onSurf(isDark), Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricChip(
    isDark: Boolean,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(chipBg(isDark), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(label, color = subText(isDark), fontSize = 10.sp)
        Text(
            value,
            color      = valueColor,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines   = 1
        )
    }
}

@Composable
private fun CyberScoreRing(
    targetScore: Int,
    animatedScore: Int,
    maxScore: Int,
    color: Color,
    isDark: Boolean
) {
    // Arc sweeps from 0 → final progress, driven by the same animatedScore
    val animatedProgress = animatedScore.coerceAtLeast(0).toFloat() /
            maxScore.coerceAtLeast(1).toFloat()

    val trackColor = if (isDark)
        Color.White.copy(alpha = 0.08f)
    else
        Color(0xFF0D1F14).copy(alpha = 0.08f)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
        CircularProgressIndicator(
            progress      = { 1f },
            color         = trackColor,
            strokeWidth   = 9.dp,
            modifier      = Modifier.fillMaxSize(),
            trackColor    = Color.Transparent,
            strokeCap     = StrokeCap.Round
        )
        CircularProgressIndicator(
            progress      = { animatedProgress.coerceIn(0f, 1f) },
            color         = color,
            strokeWidth   = 9.dp,
            modifier      = Modifier.fillMaxSize(),
            trackColor    = Color.Transparent,
            strokeCap     = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                animatedScore.toString(),
                color      = onSurf(isDark),
                fontSize   = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text("/ $maxScore", color = subText(isDark), fontSize = 10.sp)
        }
    }
}

// =============================================================================
// CyberScoreExplanationSection — "How this helps you"
// =============================================================================
@Composable
fun CyberScoreExplanationSection(isDark: Boolean) {
    Surface(
        color          = cardBg(isDark),
        shape          = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        modifier       = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Green400.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle, null,
                        tint     = Green400,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    "How this helps you",
                    color      = onSurf(isDark),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(divider(isDark))
            )
            ExplanationItem(
                isDark, "Finds risky behavior",
                "If you clicked a fake link or opened a suspicious message, your score drops — so you know."
            )
            ExplanationItem(
                isDark, "Tracks your exposure to scams",
                "Shows how many threats were found in your recent scans and how safe you are."
            )
            ExplanationItem(
                isDark, "Helps you stay safe every day",
                "Run scans regularly. The more you scan, the better protected you are."
            )
        }
    }
}

@Composable
private fun ExplanationItem(isDark: Boolean, title: String, detail: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(Green400, CircleShape)
                .align(Alignment.Top)
                .padding(top = 6.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title,  color = onSurf(isDark),  fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = subText(isDark), fontSize = 12.sp)
        }
    }
}

// =============================================================================
// CyberImprovementTipsSection — "How to improve your score"
// =============================================================================
@Composable
fun CyberImprovementTipsSection(isDark: Boolean) {
    Surface(
        color          = cardBg(isDark),
        shape          = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        modifier       = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFFBBF24).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.TipsAndUpdates, null,
                        tint     = Color(0xFFFBBF24),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    "How to improve your score",
                    color      = onSurf(isDark),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(divider(isDark))
            )
            TipItem(
                isDark, "1", "Avoid clicking unknown links",
                "If a message says 'click here to win a prize' — do not click. Run a scan first."
            )
            TipItem(
                isDark, "2", "Use strong passwords",
                "Use different passwords for different apps. Do not use your name or birthday."
            )
            TipItem(
                isDark, "3", "Check messages before trusting",
                "If you receive a message from an unknown number, scan it in GO Suraksha first."
            )
        }
    }
}

@Composable
private fun TipItem(isDark: Boolean, number: String, title: String, detail: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(chipBg(isDark), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = subText(isDark), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title,  color = onSurf(isDark),  fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = subText(isDark), fontSize = 12.sp)
        }
    }
}

// =============================================================================
// ScoreBreakdownPanel — Exposure / Behavior / Protection / Activity bars
// Shows the component breakdown so the score feels real and believable.
// =============================================================================

@Composable
fun ScoreBreakdownPanel(isDark: Boolean, factors: Map<String, Any?>?) {
    if (factors.isNullOrEmpty()) return

    @Suppress("UNCHECKED_CAST")
    fun factorMap(key: String): Map<*, *>? = factors[key] as? Map<*, *>
    fun Number?.int(): Int = this?.toInt() ?: 0
    fun Any?.bool(): Boolean = this as? Boolean ?: false
    @Suppress("UNCHECKED_CAST")
    fun Any?.strList(): List<String> = (this as? List<*>)?.filterIsInstance<String>() ?: emptyList()

    val exposure   = factorMap("exposure")
    val behavior   = factorMap("behavior")
    val protection = factorMap("protection")
    val activity   = factorMap("activity")

    val expDeduction = (exposure?.get("deduction")        as? Number).int()
    val behDeduction = (behavior?.get("deduction")        as? Number).int()
    val protBonus    = (protection?.get("bonus")          as? Number).int()
    val actBonus     = (activity?.get("bonus")            as? Number).int()

    // Convert to "score achieved" out of each component's max
    val expScore  = (400 - expDeduction).coerceAtLeast(0)
    val behScore  = (250 - behDeduction).coerceAtLeast(0)
    val protScore = protBonus.coerceIn(0, 200)
    val actScore  = actBonus.coerceIn(0, 100)

    // ── Contextual labels ────────────────────────────────────────────────────
    val emailHigh  = (exposure?.get("email_breaches_high")   as? Number).int()
    val emailMed   = (exposure?.get("email_breaches_medium")  as? Number).int()
    val weakPw     = (exposure?.get("weak_passwords")         as? Number).int()
    val highRisk   = (behavior?.get("high_risk_scans")        as? Number).int()
    val medRisk    = (behavior?.get("medium_risk_scans")      as? Number).int()
    val riskyQr    = (behavior?.get("risky_qr_codes")         as? Number).int()
    val phoneVerif = protection?.get("phone_verified").bool()
    val trustedCtc = (protection?.get("trusted_contacts")     as? Number).int()
    val covTypes   = activity?.get("scan_types_covered").strList()
    val covCount   = (activity?.get("coverage_count")         as? Number).int()

    val expContext: String = when {
        emailHigh > 0 && weakPw > 0 -> "Email in $emailHigh breach${if (emailHigh > 1) "es" else ""} · weak password"
        emailHigh > 0               -> "Email found in $emailHigh breach${if (emailHigh > 1) "es" else ""}"
        emailMed  > 0 && weakPw > 0 -> "Minor breach + weak password detected"
        emailMed  > 0               -> "Email found in minor breach"
        weakPw    > 0               -> "Weak or compromised password detected"
        else                        -> "No known breaches or weak passwords"
    }
    val behContext: String = when {
        highRisk > 0 && riskyQr > 0 -> "$highRisk risky link${if (highRisk > 1) "s" else ""} · $riskyQr risky QR"
        highRisk > 0                -> "$highRisk high-risk link${if (highRisk > 1) "s" else ""} detected"
        medRisk  > 0                -> "$medRisk suspicious message${if (medRisk > 1) "s" else ""} found"
        riskyQr  > 0                -> "$riskyQr risky QR code${if (riskyQr > 1) "s" else ""} detected"
        else                        -> "No risky behaviour detected"
    }
    val protContext: String = when {
        phoneVerif && trustedCtc >= 2 -> "Phone verified · $trustedCtc trusted contacts"
        phoneVerif                    -> "Phone verified · add more trusted contacts"
        trustedCtc >= 1               -> "$trustedCtc contact${if (trustedCtc > 1) "s" else ""} added · phone not verified"
        else                          -> "Phone not verified · no trusted contacts"
    }
    val actContext: String = when {
        covCount >= 4 -> "All scan types covered"
        covCount == 0 -> "No scans completed yet"
        else -> covTypes
            .joinToString(" · ") { it.replaceFirstChar { c -> c.uppercase() } }
            .let { "$it scanned" }
    }

    fun barColor(value: Int, max: Int): Color {
        val pct = value.toFloat() / max.toFloat()
        return when {
            pct >= 0.75f -> Color(0xFF4ADE80)
            pct >= 0.40f -> Color(0xFFFBBF24)
            else         -> Color(0xFFF87171)
        }
    }

    Surface(
        color          = cardBg(isDark),
        shape          = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        modifier       = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Score Breakdown",
                color      = onSurf(isDark),
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(divider(isDark)))

            BreakdownRow(isDark, "Exposure Safety", expScore,  400, expContext,  barColor(expScore,  400))
            BreakdownRow(isDark, "Behaviour",        behScore,  250, behContext,  barColor(behScore,  250))
            BreakdownRow(isDark, "Protection",        protScore, 200, protContext, barColor(protScore, 200))
            BreakdownRow(isDark, "Activity",          actScore,  100, actContext,  barColor(actScore,  100))
        }
    }
}

@Composable
private fun BreakdownRow(
    isDark: Boolean,
    label: String,
    value: Int,
    max: Int,
    context: String,
    color: Color
) {
    val progress = (value.toFloat() / max.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(label,           color = onSurf(isDark), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("$value / $max", color = color,          fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        // Contextual explanation
        Text(context, color = subText(isDark), fontSize = 11.sp, lineHeight = 15.sp)
        // Animated progress track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(chipBg(isDark), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(5.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}
