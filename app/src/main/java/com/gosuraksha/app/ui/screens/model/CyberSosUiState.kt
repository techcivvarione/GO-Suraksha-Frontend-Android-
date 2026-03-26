package com.gosuraksha.app.ui.screens.model

import androidx.compose.ui.graphics.vector.ImageVector

data class ScamTypeItem(
    val labelRes: Int,
    val icon: ImageVector,
    val emoji: String
)

data class CyberSosSuccessUiState(
    val scamType: String,
    val referenceId: String,
    val submittedAt: String
)
