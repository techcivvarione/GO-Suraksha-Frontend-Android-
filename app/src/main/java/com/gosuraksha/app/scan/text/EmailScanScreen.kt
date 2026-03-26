package com.gosuraksha.app.scan.text

import androidx.compose.runtime.Composable
import com.gosuraksha.app.scan.core.ScanCategory
import com.gosuraksha.app.scan.core.TextScanViewModel

@Composable
fun EmailScanScreen(
    viewModel: TextScanViewModel,
    onUpgradePlan: () -> Unit = {},
) {
    TextScanScreen(
        title       = "Email Breach Check",
        placeholder = "Enter email address to check…",
        category    = ScanCategory.EMAIL,
        onAnalyze   = { viewModel.analyzeEmail(it) },
        viewModel   = viewModel,
        onUpgradePlan = onUpgradePlan,
    )
}
