package com.gosuraksha.app.scan.reality

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.scan.SharedScanPayload
import com.gosuraksha.app.scan.design.GoSurakshaScanTheme
import com.gosuraksha.app.scan.design.ScanTheme

@Composable
fun RealityScanHubScreen(
    viewModel: RealityScanViewModel,
    sharedPayload: SharedScanPayload? = null,
    onSharedPayloadConsumed: () -> Unit = {},
    onUpgradePlan: () -> Unit = {},
) {
    val context          = LocalContext.current.applicationContext
    val isScanning       by viewModel.isScanning.collectAsState()
    val aiExplanation    by viewModel.aiExplanation.collectAsStateWithLifecycle()
    val aiExplainLoading by viewModel.aiExplainLoading.collectAsStateWithLifecycle()

    GoSurakshaScanTheme {
        val colors     = ScanTheme.colors
        val typography = ScanTheme.typography

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // ── Header ─────────────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text  = "REALITY SCAN",
                        style = typography.chipLabel,
                        color = colors.accentDeepfake,
                    )
                    Text(
                        text  = "Image Authenticity Check",
                        style = typography.sectionHeading,
                        color = colors.textPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = "Detect AI-generated images and photo manipulation in seconds.",
                        style = typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
            }

            // ── Privacy note ───────────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .size(6.dp)
                            .background(colors.primaryBlue, CircleShape)
                    )
                    Text(
                        text     = "No media stored on our servers — files are analysed and discarded immediately.",
                        style    = typography.bodySmall,
                        color    = colors.textSecondary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Image scan UI ──────────────────────────────────────────────
            item {
                ImageRealityScanScreen(
                    sharedPayload           = sharedPayload,
                    onSharedPayloadConsumed = onSharedPayloadConsumed,
                    onScan                  = { uri, mime, onSuccess, onError ->
                        viewModel.clearExplanation()
                        viewModel.scanRealityMedia(context, uri, mime, onSuccess, onError)
                    },
                    onExplain           = { result -> viewModel.explainImageResult(result) },
                    aiExplanation       = aiExplanation,
                    aiExplainLoading    = aiExplainLoading,
                    accent              = colors.primaryBlue,
                    onUpgradePlan       = onUpgradePlan,
                    isViewModelScanning = isScanning,
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
