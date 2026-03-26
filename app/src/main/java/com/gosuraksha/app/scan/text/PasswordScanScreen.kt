package com.gosuraksha.app.scan.text

import androidx.compose.runtime.Composable
import com.gosuraksha.app.scan.core.ScanCategory
import com.gosuraksha.app.scan.core.TextScanViewModel

@Composable
fun PasswordScanScreen(
    viewModel: TextScanViewModel,
    onUpgradePlan: () -> Unit = {},
) {
    TextScanScreen(
        title       = "Password Check",
        placeholder = "Enter password to analyze…",
        category    = ScanCategory.PASSWORD,
        onAnalyze   = { viewModel.analyzePassword(it) },
        viewModel   = viewModel,
        onUpgradePlan = onUpgradePlan,
    )
}
