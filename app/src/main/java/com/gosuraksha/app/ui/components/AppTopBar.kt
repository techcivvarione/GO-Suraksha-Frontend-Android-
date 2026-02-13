package com.gosuraksha.app.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.Modifier
import com.gosuraksha.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onCyberSosClick: () -> Unit
) {
    TopAppBar(
        title = {
            Icon(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color.Unspecified
            )
        },
        actions = {

            IconButton(onClick = onToggleDarkMode) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme"
                )
            }

            IconButton(onClick = onCyberSosClick) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Cyber SOS",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}
