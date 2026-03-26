package com.gosuraksha.app.ui.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.ui.components.EnterpriseTopBar

@Composable
fun MainScaffold(
    drawerState: DrawerState,
    drawerContent: @Composable () -> Unit,
    onMenuClick: () -> Unit,
    onCyberSosClick: () -> Unit,
    bottomBar: @Composable () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { drawerContent() }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0.dp),
            containerColor = ColorTokens.background(),
            topBar = {
                EnterpriseTopBar(
                    onMenuClick = onMenuClick,
                    onCyberSosClick = onCyberSosClick
                )
            },
            bottomBar = bottomBar
        ) { innerPadding ->
            content(Modifier.fillMaxSize().padding(innerPadding))
        }
    }
}
