package com.gosuraksha.app.ui.screens

import com.gosuraksha.app.R
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.data.remote.dto.CyberSosRequest
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.ui.viewmodel.CyberSosViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border

private data class QuickAction(@StringRes val titleRes: Int, @StringRes val subtitleRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)
private data class ScamType(@StringRes val labelRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun CyberSosScreen(
    viewModel: CyberSosViewModel = viewModel(),
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val state = viewModel.uiState
    var scamType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var lossAmount by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) }

    LaunchedEffect(state.success) {
        if (state.success) step = 3
    }

    Box(modifier = Modifier.fillMaxSize().background(ColorTokens.background())) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ✅ PREMIUM TOP BAR
            Surface(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                color = ColorTokens.background(),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onBack?.invoke() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = ColorTokens.textPrimary(),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.ui_cybersosscreen_2),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTokens.textPrimary()
                        )
                        Text(
                            text = stringResource(R.string.cybersos_emergency_help),
                            fontSize = 12.sp,
                            color = ColorTokens.textSecondary()
                        )
                    }
                }
            }

            // ✅ SCROLLABLE CONTENT
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 0.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Emergency Alert
                EmergencyAlert()

                // Quick Actions
                QuickActions()

                // Step Content
                when (step) {
                    1 -> SelectScamType(
                        selected = scamType,
                        onSelect = { scamType = it; step = 2 }
                    )
                    2 -> FillDetails(
                        scamType = scamType,
                        description = description,
                        lossAmount = lossAmount,
                        source = source,
                        isLoading = state.isLoading,
                        error = state.error,
                        onDescriptionChange = { description = it },
                        onLossAmountChange = { lossAmount = it },
                        onSourceChange = { source = it },
                        onBack = { step = 1 },
                        onSubmit = {
                            viewModel.triggerSos(
                                CyberSosRequest(
                                    scam_type = scamType,
                                    incident_date = java.time.LocalDate.now().toString(),
                                    description = description,
                                    loss_amount = lossAmount.ifBlank { null },
                                    source = source.ifBlank { null }
                                )
                            )
                        }
                    )
                    3 -> ReportSuccess(onDone = {
                        step = 1
                        scamType = ""
                        description = ""
                        lossAmount = ""
                        source = ""
                    })
                }
            }
        }
    }
}

@Composable
private fun EmergencyAlert() {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val alpha by pulse.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        ColorTokens.error().copy(alpha = alpha),
                        ColorTokens.error().copy(alpha = alpha * 0.7f)
                    )
                )
            )
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.cybersos_emergency_title),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.cybersos_emergency_subtitle),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }
        }
    }
}

@Composable
private fun QuickActions() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = stringResource(R.string.home_quick_actions),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTokens.textPrimary()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            QuickActionCard(
                title = stringResource(R.string.cybersos_quick_call_1930),
                subtitle = stringResource(R.string.cybersos_helpline),
                icon = Icons.Outlined.Call,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = stringResource(R.string.cybersos_quick_freeze),
                subtitle = stringResource(R.string.cybersos_contact_bank),
                icon = Icons.Outlined.Lock,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ColorTokens.surface())
            .border(1.dp, ColorTokens.border(), RoundedCornerShape(14.dp))
            .clickable { }
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ColorTokens.error().copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ColorTokens.error(),
                    modifier = Modifier.size(24.dp)
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

@Composable
private fun SelectScamType(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.cybersos_select_fraud_type),
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTokens.textPrimary()
        )

        val scamTypes = listOf(
            Pair(stringResource(R.string.cybersos_scam_upi), Icons.Outlined.Security),
            Pair(stringResource(R.string.cybersos_scam_phishing), Icons.Outlined.Shield),
            Pair(stringResource(R.string.cybersos_scam_otp), Icons.Outlined.SmsFailed),
            Pair(stringResource(R.string.cybersos_scam_investment), Icons.Outlined.CrisisAlert),
            Pair(stringResource(R.string.cybersos_scam_job), Icons.Outlined.Work),
            Pair(stringResource(R.string.cybersos_scam_other), Icons.Outlined.Report)
        )

        scamTypes.forEach { (label, icon) ->
            ScamOption(
                label = label,
                icon = icon,
                selected = selected == label,
                onClick = { onSelect(label) }
            )
        }
    }
}

@Composable
private fun ScamOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) ColorTokens.error().copy(alpha = 0.08f) else ColorTokens.surface())
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) ColorTokens.error() else ColorTokens.border(),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (selected) ColorTokens.error().copy(alpha = 0.15f) else ColorTokens.error().copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ColorTokens.error(),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = ColorTokens.textPrimary(),
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = ColorTokens.error(),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun FillDetails(
    scamType: String,
    description: String,
    lossAmount: String,
    source: String,
    isLoading: Boolean,
    error: String?,
    onDescriptionChange: (String) -> Unit,
    onLossAmountChange: (String) -> Unit,
    onSourceChange: (String) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.ui_cybersosscreen_7),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTokens.textPrimary()
            )
            Text(
                text = stringResource(R.string.ui_cybersosscreen_8, scamType),
                fontSize = 13.sp,
                color = ColorTokens.textSecondary()
            )
        }

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.cybersos_describe_required)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorTokens.accent(),
                unfocusedBorderColor = ColorTokens.border(),
                focusedLabelColor = ColorTokens.accent()
            ),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = lossAmount,
            onValueChange = onLossAmountChange,
            label = { Text(stringResource(R.string.ui_cybersosscreen_17)) },
            placeholder = { Text(stringResource(R.string.cybersos_loss_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorTokens.accent(),
                unfocusedBorderColor = ColorTokens.border()
            ),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = source,
            onValueChange = onSourceChange,
            label = { Text(stringResource(R.string.ui_cybersosscreen_18)) },
            placeholder = { Text(stringResource(R.string.cybersos_source_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorTokens.accent(),
                unfocusedBorderColor = ColorTokens.border()
            ),
            shape = RoundedCornerShape(12.dp)
        )

        error?.let {
            Text(
                text = it,
                color = ColorTokens.error(),
                fontSize = 13.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ColorTokens.border())
            ) {
                Text(stringResource(R.string.common_back), fontSize = 15.sp)
            }
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                enabled = !isLoading && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorTokens.error()
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isLoading) stringResource(R.string.cybersos_submitting) else stringResource(R.string.cybersos_submit),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ReportSuccess(onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(ColorTokens.success().copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = ColorTokens.success(),
                modifier = Modifier.size(48.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.ui_cybersosscreen_10),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTokens.textPrimary(),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.ui_cybersosscreen_11),
                fontSize = 14.sp,
                color = ColorTokens.textSecondary(),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorTokens.accent()
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(R.string.ui_cybersosscreen_12),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
