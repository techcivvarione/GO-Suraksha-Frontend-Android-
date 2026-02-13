package com.gosuraksha.app.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun AlertsScreen() {

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Alerts", "Family")

    Column {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> Text("Alerts Content")
            1 -> Text("Family Content")
        }
    }
}
