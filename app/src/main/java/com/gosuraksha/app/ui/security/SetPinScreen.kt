package com.gosuraksha.app.ui.security

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SetPinScreen(
    pinManager: PinManager,
    onPinCreated: () -> Unit
) {

    var step by remember { mutableStateOf(1) }
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val currentInput = if (step == 1) pin else confirm

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF061C14),
                        Color(0xFF0B2F1F),
                        Color.Black
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "GO Suraksha",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF6EE48E),
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                AnimatedContent(targetState = step) { target ->
                    Text(
                        text = if (target == 1)
                            "Create 6-Digit Security PIN"
                        else
                            "Confirm Your PIN",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(6) { index ->
                        val filled = index < currentInput.length
                        val alpha by animateFloatAsState(if (filled) 1f else 0.3f)

                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (filled) Color(0xFF6EE48E)
                                    else Color.White.copy(alpha = 0.2f)
                                )
                                .alpha(alpha)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                NumericKeypad(
                    onNumberClick = { number ->
                        if (currentInput.length < 6) {
                            if (step == 1) pin += number else confirm += number
                        }
                    },
                    onDelete = {
                        if (step == 1 && pin.isNotEmpty()) {
                            pin = pin.dropLast(1)
                        } else if (step == 2 && confirm.isNotEmpty()) {
                            confirm = confirm.dropLast(1)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (step == 1) {
                            if (pin.length == 6) {
                                step = 2
                                error = null
                            } else {
                                error = "PIN must be 6 digits"
                            }
                        } else {
                            if (confirm != pin) {
                                error = "PINs do not match"
                                confirm = ""
                            } else {
                                pinManager.savePin(pin)
                                onPinCreated()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (step == 1) "Continue" else "Create PIN")
                }

                error?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onDelete: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val rows = listOf(
            listOf("1","2","3"),
            listOf("4","5","6"),
            listOf("7","8","9"),
            listOf("","0","⌫")
        )

        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable(enabled = key.isNotEmpty()) {
                                when (key) {
                                    "⌫" -> onDelete()
                                    else -> onNumberClick(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
