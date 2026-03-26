package com.gosuraksha.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R
import com.gosuraksha.app.ui.components.CyberCardNew
import com.gosuraksha.app.ui.components.LockedCyberCard
import com.gosuraksha.app.ui.main.levelLabel
import com.gosuraksha.app.ui.main.riskColor

data class CyberCardUiState(
    val userName: String,
    val cardNumber: String,
    val cyberScore: Int,
    val generatedOn: String,
    val validTill: String,
    val level: String? = null        // machine key: EXCELLENT, MOSTLY_SAFE, …
)

@Composable
fun CyberCardSection(
    isPremium: Boolean,
    activeCard: CyberCardUiState?,
    onUpgrade: () -> Unit
) {
    when {
        isPremium && activeCard != null -> {
            // Full card for PRO/ULTRA with active score
            CyberCardNew(
                userName    = activeCard.userName,
                cardNumber  = activeCard.cardNumber,
                cyberScore  = activeCard.cyberScore,
                generatedOn = activeCard.generatedOn,
                validTill   = activeCard.validTill
            )
            // Level chip (V2) — shows "Mostly Safe", "Excellent", etc.
            if (activeCard.level != null) {
                val levelText  = levelLabel(activeCard.level)
                val levelColor = riskColor(activeCard.level)
                Box(
                    modifier = Modifier
                        .background(levelColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        levelText,
                        color      = levelColor,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            PremiumBadge()
        }
        isPremium -> {
            // PRO/ULTRA user but score not yet calculated — show calculating state
            // NEVER show the FREE locked card to a paying user
            CyberScoreCalculatingCard()
        }
        else -> {
            // FREE user — show blurred locked preview + upgrade banner
            LockedCyberCard(onUpgradeClick = onUpgrade)
            UpgradeBanner(onUpgrade = onUpgrade)
        }
    }
}

@Composable
private fun CyberScoreCalculatingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(Color(0xFF1E3A5F), Color(0xFF0F1A10))),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(
                modifier    = Modifier.size(28.dp),
                strokeWidth = 2.5.dp,
                color       = Color(0xFF22C55E)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Your Cyber Score is being calculated...",
                    color      = Color.White,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Complete a scan to generate your score",
                    color    = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun PremiumBadge() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))), shape = RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Diamond, null, tint = Color.Black, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    stringResource(R.string.profile_premium_active),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    stringResource(R.string.profile_premium_active_subtitle),
                    fontSize = 11.sp,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun UpgradeBanner(onUpgrade: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(PC.HeroStart, PC.HeroMid)), shape = RoundedCornerShape(14.dp))
            .border(0.5.dp, PC.Green.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .clickable(onClick = onUpgrade)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Upgrade, null, tint = PC.Green, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.profile_upgrade_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    stringResource(R.string.profile_upgrade_subtitle),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = PC.Green.copy(alpha = 0.6f))
        }
    }
}
