package com.gosuraksha.app.ui.qr.components

import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.gosuraksha.app.scan.components.ScanErrorBanner
import com.gosuraksha.app.scan.design.ScanShapes
import com.gosuraksha.app.scan.design.ScanTheme
import com.gosuraksha.app.ui.components.localizedUiMessage

@Composable
fun QrCameraPreview(
    cameraPermissionGranted: Boolean,
    isLoading: Boolean,
    hasFlash: Boolean,
    flashEnabled: Boolean,
    errorMessage: String?,
    canRetry: Boolean,
    onRequestPermission: () -> Unit,
    onFlashToggle: () -> Unit,
    onImportGallery: () -> Unit,
    onPreviewReady: (PreviewView) -> Unit,
    onRetry: () -> Unit,
    onResumeScanning: () -> Unit,
    detectionAnimating: Boolean,
) {
    val colors = ScanTheme.colors
    val spacing = ScanTheme.spacing
    val typography = ScanTheme.typography
    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        if (cameraPermissionGranted) {
            AndroidView(
                factory = { ctx -> PreviewView(ctx).also { it.implementationMode = PreviewView.ImplementationMode.COMPATIBLE; it.scaleType = PreviewView.ScaleType.FILL_CENTER; onPreviewReady(it) } },
                modifier = Modifier.fillMaxSize(),
                update = { onPreviewReady(it) },
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)))
            ScannerOverlay(modifier = Modifier.fillMaxSize(), animateLine = !isLoading)
            Column(modifier = Modifier.align(Alignment.TopStart).fillMaxWidth().padding(horizontal = spacing.lg, vertical = 48.dp), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Text("QR SECURITY SCAN", style = typography.chipLabel, color = Color.White.copy(alpha = 0.7f))
                    Text("Point & Verify", style = typography.sectionHeading, color = Color.White)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    QrControlButton(onClick = onImportGallery) { Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, tint = Color.White) }
                    QrControlButton(onClick = onFlashToggle, enabled = hasFlash) {
                        Icon(if (flashEnabled) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff, contentDescription = null, tint = if (hasFlash) Color.White else Color.White.copy(alpha = 0.4f))
                    }
                }
            }
            Surface(modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = spacing.lg, vertical = 140.dp), color = Color(0xFF1C1C1E).copy(alpha = 0.88f), shape = ScanShapes.screen) {
                Column(modifier = Modifier.padding(spacing.lg), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Text("Align the QR inside the frame", style = typography.cardTitle, color = Color.White)
                    Text("We verify the payload before any payment handoff.", style = typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    if (isLoading) {
                        Spacer(Modifier.height(4.dp))
                        Text("Verifying…", style = typography.chipLabel, color = colors.primaryBlue)
                    }
                }
            }
        } else {
            Column(modifier = Modifier.align(Alignment.Center).padding(spacing.lg).background(colors.surface, ScanShapes.screen).border(1.dp, colors.border, ScanShapes.screen).padding(spacing.xl), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                Icon(Icons.Outlined.QrCodeScanner, null, tint = colors.primaryBlue, modifier = Modifier.size(30.dp))
                Text("Camera Permission Required", style = typography.cardTitle, color = colors.textPrimary)
                Text("Allow camera access to scan QR codes, or import one from your gallery.", style = typography.bodySmall, color = colors.textSecondary, textAlign = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    OutlinedButton(onClick = onImportGallery, shape = ScanShapes.card, colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryBlue)) { Text("Import", style = typography.buttonText) }
                    Button(onClick = onRequestPermission, shape = ScanShapes.card, colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBlue)) { Text("Allow Camera", style = typography.buttonText) }
                }
            }
        }
        AnimatedVisibility(visible = detectionAnimating, modifier = Modifier.align(Alignment.Center)) {
            Surface(color = Color(0xFF1C1C1E).copy(alpha = 0.90f), shape = ScanShapes.cardExtraLarge) {
                Text("QR detected", modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md), style = typography.cardTitle, color = colors.primaryBlue)
            }
        }
        AnimatedVisibility(visible = errorMessage != null, modifier = Modifier.align(Alignment.BottomCenter)) {
            Column(modifier = Modifier.padding(horizontal = spacing.lg, vertical = 140.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                ScanErrorBanner(message = localizedUiMessage(errorMessage.orEmpty()))
                if (canRetry) {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        OutlinedButton(onClick = onRetry, modifier = Modifier.weight(1f), shape = ScanShapes.card, colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryBlue)) { Text("Retry", style = typography.buttonText) }
                        Button(onClick = onResumeScanning, modifier = Modifier.weight(1f), shape = ScanShapes.card, colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBlue)) { Text("Scan Again", style = typography.buttonText) }
                    }
                }
            }
        }
    }
}
