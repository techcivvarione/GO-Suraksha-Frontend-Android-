package com.gosuraksha.app.scan.text

import androidx.compose.runtime.Composable
import com.gosuraksha.app.scan.core.ScanCategory
import com.gosuraksha.app.scan.core.TextScanViewModel

@Composable
fun ThreatScanScreen(
    viewModel: TextScanViewModel,
    onUpgradePlan: () -> Unit = {},
) {
    TextScanScreen(
        title         = "Threat Scan",
        placeholder   = "Paste suspicious text, URLs or forwarded messages…",
        category      = ScanCategory.THREAT,
        onAnalyze     = { viewModel.analyzeThreat(it) },
        viewModel     = viewModel,
        onUpgradePlan = onUpgradePlan,
    )
}