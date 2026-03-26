package com.gosuraksha.app.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.gosuraksha.app.R
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.ui.theme.LocalThemeState

@Composable
fun CyberSosForm(
    scamType: String,
    description: String,
    lossAmount: String,
    source: String,
    isLoading: Boolean,
    error: String?,
    onDescChange: (String) -> Unit,
    onLossChange: (String) -> Unit,
    onSourceChange: (String) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        CyberSosHero(
            isDark = isDark(),
            condensed = true,
            titleText = stringResource(R.string.ui_cybersosscreen_7),
            subtitleText = "$scamType · ${stringResource(R.string.ui_cybersosscreen_8, scamType)}",
            onBack = onBack,
            showStats = false
        )

        CyberSosStepper(currentStep = 2)
        Spacer(Modifier.height(14.dp))

        FloatingCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.ui_cybersosscreen_7),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ColorTokens.textPrimary()
                        )
                    )
                    Text(
                        text = stringResource(R.string.ui_cybersosscreen_8, scamType),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ColorTokens.textSecondary()
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SosRed.copy(alpha = 0.1f))
                        .border(1.dp, SosRed.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = scamType,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SosRed
                        ),
                        maxLines = 1
                    )
                }
            }

            Divider(color = ColorTokens.border().copy(alpha = 0.5f), thickness = 1.dp)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescChange,
                    label = { Text(stringResource(R.string.cybersos_describe_required)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 110.dp),
                    minLines = 4,
                    maxLines = 8,
                    shape = RoundedCornerShape(12.dp),
                    colors = sosTextFieldColors()
                )

                OutlinedTextField(
                    value = lossAmount,
                    onValueChange = onLossChange,
                    label = { Text(stringResource(R.string.ui_cybersosscreen_17)) },
                    placeholder = { Text(stringResource(R.string.cybersos_loss_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ColorTokens.textSecondary()
                            )
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = sosTextFieldColors()
                )

                OutlinedTextField(
                    value = source,
                    onValueChange = onSourceChange,
                    label = { Text(stringResource(R.string.ui_cybersosscreen_18)) },
                    placeholder = { Text(stringResource(R.string.cybersos_source_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = sosTextFieldColors()
                )

                if (error != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SosRed.copy(alpha = 0.08f))
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = null,
                            tint = SosRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.labelMedium.copy(color = SosRed)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, ColorTokens.border()),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ColorTokens.textSecondary()
                        )
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.common_back), fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onSubmit,
                        modifier = Modifier.weight(1.4f).height(50.dp),
                        enabled = !isLoading && description.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SosRed,
                            contentColor = Color.White,
                            disabledContainerColor = SosRed.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.cybersos_submitting), fontWeight = FontWeight.Bold)
                        } else {
                            Text(stringResource(R.string.cybersos_submit), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun sosTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TealAccent,
    unfocusedBorderColor = ColorTokens.border(),
    focusedLabelColor = TealAccent,
    unfocusedLabelColor = ColorTokens.textSecondary(),
    focusedTextColor = ColorTokens.textPrimary(),
    unfocusedTextColor = ColorTokens.textPrimary(),
    cursorColor = TealAccent,
    focusedContainerColor = ColorTokens.surface(),
    unfocusedContainerColor = ColorTokens.surface()
)

@Composable
private fun isDark(): Boolean = LocalThemeState.current.isDark
