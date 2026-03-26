package com.gosuraksha.app.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.ui.screens.model.CyberSosSuccessUiState
import com.gosuraksha.app.ui.theme.LocalThemeState

@Composable
fun CyberSosSuccess(
    successState: CyberSosSuccessUiState,
    onDone: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isDark = isDark()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) HeroBrushMutedDark else HeroBrushMutedLight)
                .padding(top = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(if (isDark) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.15f))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "CyberSOS",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(LiveGreen.copy(alpha = 0.15f))
                        .border(2.dp, LiveGreen.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = LiveGreen,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.ui_cybersosscreen_10),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = null,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.4).sp
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.ui_cybersosscreen_11),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.75f)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 5.dp)
                )

                Spacer(Modifier.height(16.dp))
            }
        }

        CyberSosStepper(currentStep = 3)
        Spacer(Modifier.height(14.dp))

        FloatingCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Complaint Reference",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ColorTokens.textPrimary()
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorTokens.background())
                        .border(1.dp, ColorTokens.border(), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TicketRow(label = "Reference ID", value = "#${successState.referenceId}", valueColor = TealAccent, mono = true)
                    Divider(color = ColorTokens.border().copy(alpha = 0.5f), thickness = 1.dp)
                    TicketRow(label = "Fraud Type", value = successState.scamType)
                    TicketRow(label = "Filed On", value = successState.submittedAt)
                    TicketRow(label = "Status", value = "🕐 Under Review", valueColor = AmberStatus)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TealAccent.copy(alpha = 0.06f))
                        .border(1.dp, TealAccent.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Your Next Steps",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TealAccent
                        )
                    )
                    NextStepItem(1, "Call 1930 with your reference ID to escalate your case")
                    NextStepItem(2, "Contact your bank immediately to freeze your account")
                    NextStepItem(3, "File an official FIR at cybercrime.gov.in")
                }

                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealAccent,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.ui_cybersosscreen_12),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun TicketRow(
    label: String,
    value: String,
    valueColor: Color = ColorTokens.textPrimary(),
    mono: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = ColorTokens.textSecondary()
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
                fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default
            )
        )
    }
}

@Composable
fun NextStepItem(num: Int, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(TealAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$num",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TealAccent,
                    fontSize = 9.sp
                )
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = ColorTokens.textSecondary(),
                lineHeight = 18.sp
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun isDark(): Boolean = LocalThemeState.current.isDark
