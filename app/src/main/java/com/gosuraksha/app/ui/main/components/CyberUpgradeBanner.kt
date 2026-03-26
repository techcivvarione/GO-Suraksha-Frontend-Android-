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
                    Text("??? / 999", color = onSurf(isDark), fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text("Get Your Cyber Score", color = onSurf(isDark), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Know how safe you are from scams and online threats", color = subText(isDark), fontSize = 14.sp)
                    PrimaryCyberCardButton("Upgrade to GO PRO", onUpgradePlan)
                }
            }
        }
    }
}

// Shown when user has CYBER_CARD feature but the API returned null (score still computing or network delay)
@Composable
fun CalculatingCyberCardState(isDark: Boolean, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SectionHeading(isDark, "Your Cyber Safety Score", "See how safe you are online")
        StatusPanel(
            isDark   = isDark,
            icon     = Icons.Rounded.Security,
            title    = "Your Cyber Score is being calculated...",
            message  = "We are checking your scan history and activity. Complete a scan to generate your score."
        )
        PrimaryCyberCardButton("Check Again", onRetry)
    }
}

@Composable
fun PendingCyberCardState(isDark: Boolean, onNavigateToScan: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SectionHeading(
            isDark,
            "Start Your Security Scan",
            "Your Cyber Safety Score will be generated after your first scan"
        )
        StatusPanel(
            isDark,
            Icons.Rounded.Security,
            "No scans yet",
            "Run an email or password scan to generate your personal Cyber Safety Score. It takes under 30 seconds."
        )
        PrimaryCyberCardButton("Start Security Scan", onNavigateToScan)
    }
}

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
        CyberScoreSection(isDark, card.score ?: 600, card.max_score ?: 999, card.risk_level ?: "Locked", card.score_month ?: "--", card.card_id ?: "--")
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
