package com.gosuraksha.app.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CrisisAlert
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SmsFailed
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.ui.screens.model.ScamTypeItem
import com.gosuraksha.app.ui.theme.LocalThemeState

@Composable
fun CyberSosSelectStep(
    selectedType: String,
    onTypeSelect: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: (() -> Unit)?
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        CyberSosHero(
            isDark = isDark(),
            condensed = false,
            titleText = stringResource(R.string.ui_cybersosscreen_2),
            subtitleText = stringResource(R.string.cybersos_emergency_help),
            onBack = onBack,
            showStats = true
        )

        CyberSosStepper(currentStep = 1)
        CyberSosQuickActions(context = androidx.compose.ui.platform.LocalContext.current)
        Spacer(Modifier.height(14.dp))

        FloatingCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(R.string.cybersos_select_fraud_type),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ColorTokens.textPrimary()
                    )
                )
                Text(
                    text = stringResource(R.string.cybersos_emergency_subtitle),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ColorTokens.textSecondary()
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Divider(color = ColorTokens.border().copy(alpha = 0.5f), thickness = 1.dp)

            ScamTypeGrid(
                selectedType = selectedType,
                onSelect = onTypeSelect,
                modifier = Modifier.padding(14.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(54.dp),
            enabled = selectedType.isNotBlank(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SosRed,
                contentColor = Color.White,
                disabledContainerColor = SosRed.copy(alpha = 0.35f),
                disabledContentColor = Color.White.copy(alpha = 0.45f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation  = 4.dp,
                pressedElevation  = 0.dp,
                disabledElevation = 0.dp
            )
        ) {
            Text(
                text       = "🚨  Report Incident Now",
                fontSize   = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.2.sp
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun CyberSosStepper(currentStep: Int) {
    val isDark = isDark()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorTokens.surface())
            .drawBehind {
                drawLine(
                    color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.08f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperItem(number = 1, label = "Type", currentStep = currentStep, flex = 1f)
        StepperLine(isComplete = currentStep > 1)
        StepperItem(number = 2, label = "Details", currentStep = currentStep, flex = 1f)
        StepperLine(isComplete = currentStep > 2)
        StepperItem(number = 3, label = "Done", currentStep = currentStep, flex = 1f)
    }
}

@Composable
fun FloatingCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val dark = isDark()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ColorTokens.surface()),
        border = BorderStroke(1.dp, ColorTokens.border()),
        elevation = CardDefaults.cardElevation(defaultElevation = if (dark) 4.dp else 2.dp),
        content = content
    )
}

@Composable
fun ScamTypeGrid(
    selectedType: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scamTypes = remember {
        listOf(
            ScamTypeItem(R.string.cybersos_scam_upi, Icons.Outlined.Security, "💳"),
            ScamTypeItem(R.string.cybersos_scam_phishing, Icons.Outlined.Shield, "🎣"),
            ScamTypeItem(R.string.cybersos_scam_otp, Icons.Outlined.SmsFailed, "📱"),
            ScamTypeItem(R.string.cybersos_scam_investment, Icons.Outlined.CrisisAlert, "📈"),
            ScamTypeItem(R.string.cybersos_scam_job, Icons.Outlined.Work, "💼"),
            ScamTypeItem(R.string.cybersos_scam_other, Icons.Outlined.Report, "⚠️")
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        scamTypes.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    val label = stringResource(item.labelRes)
                    ScamTypeCell(
                        emoji = item.emoji,
                        icon = item.icon,
                        label = label,
                        selected = selectedType == label,
                        onClick = { onSelect(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun ScamTypeCell(
    emoji: String,
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) SosRed.copy(alpha = 0.1f) else ColorTokens.background()
    val borderColor = if (selected) SosRed else ColorTokens.border()
    val borderWidth = if (selected) 2.dp else 1.dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) SosRed else ColorTokens.textPrimary(),
                fontSize = 10.sp
            ),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun RowScope.StepperItem(
    number: Int,
    label: String,
    currentStep: Int,
    flex: Float
) {
    val isDone = currentStep > number
    val isActive = currentStep == number
    val dotColor = when {
        isDone -> TealAccent
        isActive -> SosRed
        else -> ColorTokens.surface()
    }
    val dotBorder = when {
        isDone || isActive -> Color.Transparent
        else -> ColorTokens.border()
    }
    val labelColor = when {
        isActive -> SosRed
        isDone -> ColorTokens.textPrimary()
        else -> ColorTokens.textSecondary()
    }

    Row(
        modifier = Modifier.weight(flex),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(dotColor)
                .border(1.5.dp, dotBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            } else {
                Text(
                    text = "$number",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isActive) Color.White else ColorTokens.textSecondary(),
                        fontSize = 10.sp
                    )
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isActive || isDone) FontWeight.SemiBold else FontWeight.Normal,
                color = labelColor,
                fontSize = 10.sp,
                letterSpacing = 0.3.sp
            )
        )
    }
}

@Composable
private fun RowScope.StepperLine(isComplete: Boolean) {
    Box(
        modifier = Modifier
            .weight(0.3f)
            .height(2.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(if (isComplete) TealAccent else ColorTokens.border())
    )
}

@Composable
private fun isDark(): Boolean = LocalThemeState.current.isDark
