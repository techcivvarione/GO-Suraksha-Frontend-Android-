package com.gosuraksha.app.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberCardHeader(isDark: Boolean, onBack: () -> Unit) {
    val topBarBg = if (isDark) Color(0xFF080E08) else Color(0xFFF5F7F5)
    val topBarTitle = if (isDark) Color(0xFFD4EDD9) else Color(0xFF0D1F14)
    val topBarIcon = if (isDark) Color(0xFF2EC472) else Color(0xFF17753D)

    TopAppBar(
        title = {
            Column {
                Text("Your Cyber Safety Score", color = topBarTitle, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("See how safe you are online", color = subText(isDark), fontSize = 11.sp)
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = topBarIcon)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = topBarBg,
            navigationIconContentColor = topBarIcon,
            titleContentColor = topBarTitle
        )
    )
}
