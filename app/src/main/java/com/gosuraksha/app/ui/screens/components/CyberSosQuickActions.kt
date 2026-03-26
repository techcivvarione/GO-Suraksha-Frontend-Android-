package com.gosuraksha.app.ui.screens.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R
import com.gosuraksha.app.design.tokens.ColorTokens

@Composable
fun CyberSosQuickActions(context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionCard(
            icon = Icons.Outlined.Call,
            title = stringResource(R.string.cybersos_quick_call_1930),
            subtitle = stringResource(R.string.cybersos_helpline),
            modifier = Modifier.weight(1f),
            onClick = { openDial1930(context) }
        )
        QuickActionCard(
            icon = Icons.Outlined.Lock,
            title = stringResource(R.string.cybersos_quick_freeze),
            subtitle = stringResource(R.string.cybersos_contact_bank),
            modifier = Modifier.weight(1f),
            onClick = { openBankHelp(context) }
        )
        QuickActionCard(
            icon = Icons.Outlined.Report,
            title = stringResource(R.string.cybersos_quick_report_online),
            subtitle = stringResource(R.string.cybersos_report_portal),
            modifier = Modifier.weight(1f),
            onClick = { openCyberCrimePortal(context) }
        )
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ColorTokens.surface())
            .border(1.dp, ColorTokens.border(), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SosRed.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SosRed,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorTokens.textPrimary(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = ColorTokens.textSecondary(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun openDial1930(context: Context) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930")))
    }
}

private fun openBankHelp(context: Context) {
    runCatching {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.rbi.org.in/Scripts/BS_PressReleaseDisplay.aspx?prid=57490")
            )
        )
    }
}

private fun openCyberCrimePortal(context: Context) {
    runCatching {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://cybercrime.gov.in")
            )
        )
    }
}
