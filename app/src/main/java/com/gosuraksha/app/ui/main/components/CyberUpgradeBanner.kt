package com.gosuraksha.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.ui.components.CyberCardNew
import com.gosuraksha.app.ui.components.LockedCyberCard

@Composable
fun LoadingCyberCardState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Green400, strokeWidth = 2.5.dp, modifier = Modifier.size(36.dp))
    }
}

@Composable
fun FreeCyberCardState(isDark: Boolean, onUpgradePlan: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SectionHeading(isDark, "Your Cyber Safety Score", "See how safe you are online")
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.blur(12.dp)) {
                CyberCardNew("GO Suraksha User", "•••• •••• ••••", 999, "--", "--")
            }
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xE6080E08) else Color(0xE6FFFFFF),
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("??? / 1000", color = onSurf(isDark), fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text("Get Your Cyber Score", color = onSurf(isDark), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Know how safe you are from scams and online threats", color = subText(isDark), fontSize = 14.sp)
                    PrimaryCyberCardButton("Upgrade to GO PRO", onUpgradePlan)
                }
            }
        }
    }
}

// Shown when the API returned null — score is computing or there was a network hiccup.
// onRetry calls pollCard() — no loading spinner, no flashing.
@Composable
fun CalculatingCyberCardState(
    isDark: Boolean,
    onRetry: (() -> Unit)? = null
) {
    // No auto-poll here — user taps "Check Now" manually.
    // Auto-polling caused the loading spinner to flash repeatedly.

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SectionHeading(isDark, "Your Cyber Safety Score", "See how safe you are online")
        StatusPanel(
            isDark   = isDark,
            icon     = Icons.Rounded.Security,
            title    = "Preparing your safety score",
            message  = "We're analysing your scan history and protection signals. This usually takes just a moment."
        )
        if (onRetry != null) {
            PrimaryCyberCardButton("Check Now", onRetry)
        }
    }
}

/**
 * Shown when the user has CYBER_CARD feature but hasn't completed enough distinct scan types.
 * [distinctScanTypes] = 0 → first-time user; 1 → almost there; eligible=true → computing.
 */
@Composable
fun PendingCyberCardState(
    isDark: Boolean,
    onNavigateToScan: () -> Unit,
    eligible: Boolean = false,
    distinctScanTypes: Int = 0,
    onRetry: (() -> Unit)? = null
) {
    // No auto-poll — user taps "Check Now" manually.
    // Removed auto-polling: it caused a spinning/flashing loader every 4s.
    val (heading, headingSub, panelTitle, panelBody, buttonLabel) = when {
        // Eligible but score still being computed (backend returned eligible=true)
        eligible -> PendingCopy(
            heading      = "Your Cyber Safety Score",
            headingSub   = "Your score is ready soon",
            panelTitle   = "Preparing your safety score",
            panelBody    = "We have your scan data. Tap 'Check Now' to generate your score — it takes just a second.",
            buttonLabel  = "Check Now"
        )
        // Has some scans but not enough variety — nudge without "no scans" language
        distinctScanTypes > 0 -> PendingCopy(
            heading      = "Your Cyber Safety Score",
            headingSub   = "Run one more type of scan to unlock",
            panelTitle   = "Almost there",
            panelBody    = "You've completed $distinctScanTypes scan type${if (distinctScanTypes > 1) "s" else ""}. Try an email check or password scan to generate your full safety score.",
            buttonLabel  = "Continue Scanning"
        )
        // No scans at all — first time
        else -> PendingCopy(
            heading      = "Your Cyber Safety Score",
            headingSub   = "Start your first scan to unlock",
            panelTitle   = "Build your safety score",
            panelBody    = "Run an email breach check or password scan to generate your personal Cyber Safety Score. Takes under 30 seconds.",
            buttonLabel  = "Start Security Scan"
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SectionHeading(isDark, heading, headingSub)
        StatusPanel(isDark, Icons.Rounded.Security, panelTitle, panelBody)
        if (eligible && onRetry != null) {
            // Eligible users get "Check Now" (score might already be ready)
            PrimaryCyberCardButton("Check Now", onRetry)
        } else {
            PrimaryCyberCardButton(buttonLabel, onNavigateToScan)
        }
    }
}

private data class PendingCopy(
    val heading: String,
    val headingSub: String,
    val panelTitle: String,
    val panelBody: String,
    val buttonLabel: String
)

@Composable
fun LockedCyberCardState(isDark: Boolean, card: CyberCardResponse, onUpgradePlan: () -> Unit) {
    val signals = card.signals.toCyberSignals()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SectionHeading(isDark, "Cyber Card Locked", card.message ?: "Your Cyber Card is currently locked.")
        Box(contentAlignment = Alignment.Center) {
            LockedCyberCard(onUpgradeClick = onUpgradePlan)
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(if (isDark) Color(0xC0220E15) else Color(0xC0FFE4E6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Lock, "Locked", tint = if (isDark) Color.White else Color(0xFF991B1B), modifier = Modifier.size(34.dp))
            }
        }
        CyberScoreSection(isDark, card.score ?: 600, card.max_score ?: 1000, card.risk_level ?: "Locked", card.score_month ?: "--", card.card_id ?: "--")
        InfoPanel(isDark, "Lock reason", signals.lockReason ?: card.message ?: "This Cyber Card is not accessible yet.")
    }
}

@Composable
fun ErrorCyberCardState(isDark: Boolean, message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = onSurf(isDark), fontSize = 15.sp, fontWeight = FontWeight.Medium)
        androidx.compose.foundation.layout.Spacer(Modifier.size(16.dp))
        PrimaryCyberCardButton("Retry", onRetry)
    }
}

@Composable
private fun SectionHeading(isDark: Boolean, title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, color = onSurf(isDark), fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text(subtitle, color = subText(isDark), fontSize = 13.sp)
    }
}

@Composable
private fun StatusPanel(isDark: Boolean, icon: ImageVector, title: String, message: String) {
    Surface(color = cardBg(isDark), shape = RoundedCornerShape(20.dp), tonalElevation = 0.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Green400.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Green400, modifier = Modifier.size(22.dp))
            }
            Text(title, color = onSurf(isDark), fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(message, color = subText(isDark), fontSize = 13.sp)
        }
    }
}

@Composable
private fun PrimaryCyberCardButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Green400, contentColor = Color(0xFF051209)),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
