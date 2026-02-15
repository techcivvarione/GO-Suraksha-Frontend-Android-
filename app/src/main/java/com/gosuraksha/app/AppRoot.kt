package com.gosuraksha.app

import androidx.compose.runtime.Composable
import com.gosuraksha.app.navigation.AppNavGraph
import com.gosuraksha.app.ui.theme.GOSurakshaTheme

@Composable
fun AppRoot() {

    GOSurakshaTheme {
        AppNavGraph()
    }
}
