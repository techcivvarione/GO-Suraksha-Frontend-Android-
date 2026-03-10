package com.gosuraksha.app.ui.main

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.gosuraksha.app.R
import com.gosuraksha.app.data.repository.QrRepository
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.network.QrAnalyzeResponse
import com.gosuraksha.app.presentation.qr.QrAnalysisUiState
import com.gosuraksha.app.presentation.qr.QrUiState
import com.gosuraksha.app.presentation.qr.QrViewModel
import com.gosuraksha.app.presentation.qr.QrViewModelFactory
import com.gosuraksha.app.ui.components.localizedUiMessage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val UPI_DEBUG_LOGS = false

@Composable
fun QrAnalyzerScreen(
    qrViewModel: QrViewModel = viewModel(
        factory = QrViewModelFactory(QrRepository(ApiClient.qrApi))
    )
) {
    val state by qrViewModel.state.collectAsState()

    val rsBg = ColorTokens.background()
    val rsCard = ColorTokens.surface()
    val rsBorder = ColorTokens.border()
    val neuralBlue = Color(0xFF3B82F6)
    val neuralPurp = Color(0xFF8B5CF6)
    val dangerRed = ColorTokens.error()
    val safeGreen = ColorTokens.success()
    val warnAmber = ColorTokens.warning()
    val rsText = ColorTokens.textPrimary()
    val rsMuted = ColorTokens.textSecondary()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val scannerOptions = remember {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    }
    val barcodeScanner = remember { BarcodeScanning.getClient(scannerOptions) }
    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanGate = remember { AtomicBoolean(true) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasFlash by remember { mutableStateOf(false) }
    var flashEnabled by rememberSaveable { mutableStateOf(false) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var scanningPaused by remember { mutableStateOf(false) }
    var pendingDetectedRaw by remember { mutableStateOf<String?>(null) }
    var detectionAnimating by remember { mutableStateOf(false) }

    val shouldShowScannerStage = state.parsedQr == null && state.analysisState !is QrAnalysisUiState.Success

    val isAnalyzing = state.analysisState is QrAnalysisUiState.Loading
    LaunchedEffect(shouldShowScannerStage, scanningPaused, detectionAnimating, isAnalyzing, pendingDetectedRaw) {
        scanGate.set(
            shouldShowScannerStage &&
                !scanningPaused &&
                !detectionAnimating &&
                !isAnalyzing &&
                pendingDetectedRaw == null
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
        if (granted) {
            qrViewModel.clearError()
            scanningPaused = false
        } else {
            qrViewModel.onScanFailure(context.getString(R.string.qr_error_camera_permission_required))
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        scanningPaused = true
        processGalleryQr(
            context = context,
            uri = uri,
            scanner = barcodeScanner,
            onStart = { qrViewModel.setScannerLoading(true) },
            onComplete = { qrViewModel.setScannerLoading(false) },
            onSuccess = { raw -> pendingDetectedRaw = raw },
            onFailure = { message ->
                scanningPaused = false
                qrViewModel.onScanFailure(message)
            }
        )
    }

    LaunchedEffect(boundCamera, flashEnabled, hasFlash) {
        boundCamera?.cameraControl?.enableTorch(hasFlash && flashEnabled)
    }

    LaunchedEffect(pendingDetectedRaw) {
        val raw = pendingDetectedRaw ?: return@LaunchedEffect
        detectionAnimating = true
        delay(450)
        qrViewModel.onQrRawDetected(raw)
        delay(180)
        detectionAnimating = false
        pendingDetectedRaw = null
    }

    DisposableEffect(previewView, hasCameraPermission, lifecycleOwner, shouldShowScannerStage) {
        if (!hasCameraPermission || !shouldShowScannerStage || previewView == null) {
            return@DisposableEffect onDispose { }
        }

        val preview = Preview.Builder().build()
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(
            analyzerExecutor,
            QrFrameAnalyzer(
                scanner = barcodeScanner,
                isActive = { scanGate.get() },
                onQrDetected = { raw ->
                    if (pendingDetectedRaw == null) {
                        scanningPaused = true
                        pendingDetectedRaw = raw
                    }
                }
            )
        )

        val providerFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null

        providerFuture.addListener(
            {
                try {
                    cameraProvider = providerFuture.get()
                    cameraProvider?.unbindAll()
                    preview.setSurfaceProvider(previewView?.surfaceProvider)

                    val camera = cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                    boundCamera = camera
                    hasFlash = camera?.cameraInfo?.hasFlashUnit() == true
                    if (!hasFlash) flashEnabled = false
                    camera?.cameraControl?.enableTorch(hasFlash && flashEnabled)
                } catch (_: Exception) {
            qrViewModel.onScanFailure(context.getString(R.string.qr_unable_start_camera))
                }
            },
            ContextCompat.getMainExecutor(context)
        )

        onDispose {
            imageAnalysis.clearAnalyzer()
            try {
                cameraProvider?.unbindAll()
            } catch (_: Exception) {
            }
            boundCamera = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            barcodeScanner.close()
            analyzerExecutor.shutdown()
        }
    }

    Crossfade(targetState = shouldShowScannerStage, label = "qr_scan_stage") { showScanner ->
        if (showScanner) {
            QrScannerStage(
                modifier = Modifier.fillMaxSize().background(rsBg),
                cameraPermissionGranted = hasCameraPermission,
                isLoading = state.isScannerLoading || state.analysisState is QrAnalysisUiState.Loading,
                hasFlash = hasFlash,
                flashEnabled = flashEnabled,
                errorMessage = (state.analysisState as? QrAnalysisUiState.Error)?.message,
                canRetry = (state.analysisState as? QrAnalysisUiState.Error)?.canRetry == true,
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onFlashToggle = { if (hasFlash) flashEnabled = !flashEnabled },
                onImportGallery = { qrViewModel.clearError(); galleryLauncher.launch("image/*") },
                onPreviewReady = { previewView = it },
                onRetry = { qrViewModel.retryAnalyze() },
                onResumeScanning = {
                    qrViewModel.startNewScan()
                    scanningPaused = false
                    pendingDetectedRaw = null
                    detectionAnimating = false
                },
                rsCard = rsCard,
                rsBorder = rsBorder,
                dangerRed = dangerRed,
                neuralBlue = neuralBlue,
                neuralPurp = neuralPurp,
                rsText = rsText,
                rsMuted = rsMuted,
                detectionAnimating = detectionAnimating
            )
        } else {
            QrResultStage(
                state = state,
                rsBg = rsBg,
                rsCard = rsCard,
                rsBorder = rsBorder,
                rsText = rsText,
                rsMuted = rsMuted,
                dangerRed = dangerRed,
                safeGreen = safeGreen,
                warnAmber = warnAmber,
                neuralBlue = neuralBlue,
                onReport = { qrViewModel.onScanFailure("qr_scam_warning") },
                onProceedPayment = { raw ->
                    openUpiIntent(context, raw) { qrViewModel.onScanFailure(it) }
                },
                onScanAnother = {
                    qrViewModel.startNewScan()
                    scanningPaused = false
                    pendingDetectedRaw = null
                    detectionAnimating = false
                },
                onRetry = { qrViewModel.retryAnalyze() },
                onDismissError = { qrViewModel.clearError() }
            )
        }
    }
}

@Composable
private fun QrScannerStage(
    modifier: Modifier,
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
    rsCard: Color,
    rsBorder: Color,
    dangerRed: Color,
    neuralBlue: Color,
    neuralPurp: Color,
    rsText: Color,
    rsMuted: Color,
    detectionAnimating: Boolean
) {
    Box(modifier = modifier) {
        if (cameraPermissionGranted) {
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        onPreviewReady(this)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { onPreviewReady(it) }
            )

            ScannerOverlay(
                modifier = Modifier.fillMaxSize(),
                accent = neuralBlue,
                secondaryAccent = neuralPurp
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.qr_scan_upi_title), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.qr_scan_upi_subtitle), color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScannerControlButton(
                        onClick = onImportGallery,
                        icon = {
                            Icon(Icons.Default.PhotoLibrary, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    )
                    ScannerControlButton(
                        onClick = onFlashToggle,
                        enabled = hasFlash,
                        icon = {
                            Icon(
                                imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = null,
                                tint = if (hasFlash) Color.White else Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                neuralBlue.copy(alpha = 0.16f),
                                Color.Black.copy(alpha = 0.92f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(rsCard)
                        .border(1.dp, rsBorder, RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.QrCodeScanner, null, tint = neuralBlue, modifier = Modifier.size(30.dp))
                    Text(stringResource(R.string.qr_camera_permission_required), color = rsText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        stringResource(R.string.qr_camera_permission_message),
                        color = rsMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onImportGallery, border = BorderStroke(1.dp, rsBorder)) { Text(stringResource(R.string.qr_import)) }
                        Button(onClick = onRequestPermission) { Text(stringResource(R.string.qr_allow_camera)) }
                    }
                }
            }
        }

        if (detectionAnimating) {
            DetectionPulse(
                modifier = Modifier.align(Alignment.Center),
                accent = neuralBlue,
                textColor = Color.White
            )
        }

        if (isLoading) {
            QrLoadingChip(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 116.dp),
                accent = neuralPurp
            )
        }

        errorMessage?.let {
            QrFloatingErrorCard(
                message = localizedUiMessage(it),
                canRetry = canRetry,
                onRetry = onRetry,
                onScanAgain = onResumeScanning,
                dangerRed = dangerRed,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            )
        }
    }
}

@Composable
private fun QrResultStage(
    state: QrUiState,
    rsBg: Color,
    rsCard: Color,
    rsBorder: Color,
    rsText: Color,
    rsMuted: Color,
    dangerRed: Color,
    safeGreen: Color,
    warnAmber: Color,
    neuralBlue: Color,
    onReport: () -> Unit,
    onProceedPayment: (String) -> Unit,
    onScanAnother: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(rsBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(stringResource(R.string.qr_analyzer_title), color = rsText, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Text(stringResource(R.string.qr_scan_complete), color = rsMuted, fontSize = 12.sp)
            }

            OutlinedButton(onClick = onScanAnother, border = BorderStroke(1.dp, rsBorder)) {
                Text(stringResource(R.string.qr_scan_another))
            }
        }

        val errorState = state.analysisState as? QrAnalysisUiState.Error
        errorState?.let { err ->
            QrErrorBanner(
                message = localizedUiMessage(err.message),
                dangerRed = dangerRed,
                showRetry = err.canRetry,
                onRetry = onRetry,
                onDismiss = onDismissError
            )
        }

        val successState = state.analysisState as? QrAnalysisUiState.Success
        successState?.let { success ->
            QrResultCard(
                analysis = success.data,
                rawPayload = state.parsedQr?.rawPayload,
                rsCard = rsCard,
                rsText = rsText,
                rsMuted = rsMuted,
                rsBorder = rsBorder,
                neuralBlue = neuralBlue,
                dangerRed = dangerRed,
                safeGreen = safeGreen,
                warnAmber = warnAmber,
                onReport = onReport,
                onProceedPayment = onProceedPayment
            )
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun ScannerOverlay(
    modifier: Modifier,
    accent: Color,
    secondaryAccent: Color
) {
    val infinite = rememberInfiniteTransition(label = "qr_overlay")
    val lineProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_line"
    )
    val cornerPulse by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corner_pulse"
    )

    Canvas(
        modifier = modifier.graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
    ) {
        val cutoutWidth = size.width * 0.72f
        val cutoutHeight = cutoutWidth
        val left = (size.width - cutoutWidth) / 2f
        val top = (size.height - cutoutHeight) / 2f - (size.height * 0.06f)
        val cornerRadius = 36f
        val cornerLen = cutoutWidth * 0.12f

        drawRect(Color.Black.copy(alpha = 0.58f))
        drawRoundRect(
            color = Color.Transparent,
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(cutoutWidth, cutoutHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
            blendMode = BlendMode.Clear
        )

        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(accent.copy(alpha = 0.35f), secondaryAccent.copy(alpha = 0.35f))),
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(cutoutWidth, cutoutHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = 2.5f)
        )

        val cornerColor = accent.copy(alpha = cornerPulse)

        drawLine(cornerColor, androidx.compose.ui.geometry.Offset(left, top + cornerLen), androidx.compose.ui.geometry.Offset(left, top), 7f, StrokeCap.Round)
        drawLine(cornerColor, androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(left + cornerLen, top), 7f, StrokeCap.Round)
        drawLine(cornerColor, androidx.compose.ui.geometry.Offset(left + cutoutWidth - cornerLen, top), androidx.compose.ui.geometry.Offset(left + cutoutWidth, top), 7f, StrokeCap.Round)
        drawLine(cornerColor, androidx.compose.ui.geometry.Offset(left + cutoutWidth, top), androidx.compose.ui.geometry.Offset(left + cutoutWidth, top + cornerLen), 7f, StrokeCap.Round)
        drawLine(cornerColor, androidx.compose.ui.geometry.Offset(left, top + cutoutHeight - cornerLen), androidx.compose.ui.geometry.Offset(left, top + cutoutHeight), 7f, StrokeCap.Round)
        drawLine(cornerColor, androidx.compose.ui.geometry.Offset(left, top + cutoutHeight), androidx.compose.ui.geometry.Offset(left + cornerLen, top + cutoutHeight), 7f, StrokeCap.Round)
        drawLine(cornerColor, androidx.compose.ui.geometry.Offset(left + cutoutWidth - cornerLen, top + cutoutHeight), androidx.compose.ui.geometry.Offset(left + cutoutWidth, top + cutoutHeight), 7f, StrokeCap.Round)
        drawLine(cornerColor, androidx.compose.ui.geometry.Offset(left + cutoutWidth, top + cutoutHeight - cornerLen), androidx.compose.ui.geometry.Offset(left + cutoutWidth, top + cutoutHeight), 7f, StrokeCap.Round)

        val scanY = top + (cutoutHeight * lineProgress)
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    accent.copy(alpha = 0.9f),
                    secondaryAccent.copy(alpha = 0.9f),
                    Color.Transparent
                )
            ),
            start = androidx.compose.ui.geometry.Offset(left + 24f, scanY),
            end = androidx.compose.ui.geometry.Offset(left + cutoutWidth - 24f, scanY),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun DetectionPulse(
    modifier: Modifier,
    accent: Color,
    textColor: Color
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "detection_scale"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            color = accent,
            strokeWidth = 2.5.dp
        )
        Text(stringResource(R.string.qr_detected), color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun QrLoadingChip(
    modifier: Modifier,
    accent: Color
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(60.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(60.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            color = accent,
            strokeWidth = 2.dp
        )
        Text(stringResource(R.string.qr_verifying_payment), color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun ScannerControlButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.42f))
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.24f else 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            icon()
        }
    }
}

@Composable
private fun QrFloatingErrorCard(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onScanAgain: () -> Unit,
    dangerRed: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.76f))
            .border(1.dp, dangerRed.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Error, contentDescription = null, tint = dangerRed, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(message, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))

        if (canRetry) {
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.ui_screenlayouts_1), color = dangerRed)
            }
        }

        TextButton(onClick = onScanAgain) {
            Text(stringResource(R.string.home_reality_cta), color = Color.White)
        }
    }
}

@Composable
private fun QrResultCard(
    analysis: QrAnalyzeResponse,
    rawPayload: String?,
    rsCard: Color,
    rsText: Color,
    rsMuted: Color,
    rsBorder: Color,
    neuralBlue: Color,
    dangerRed: Color,
    safeGreen: Color,
    warnAmber: Color,
    onReport: () -> Unit,
    onProceedPayment: (String) -> Unit
) {
    val riskLevel = analysis.risk_level.uppercase()
    val topColor = when (riskLevel) {
        "HIGH" -> dangerRed
        "MEDIUM" -> warnAmber
        else -> safeGreen
    }
    val isUpiDetected = analysis.detected_type.uppercase() == "UPI"
    val hasUpiScheme = rawPayload?.trim()?.startsWith("upi://", ignoreCase = true) == true
    var showMediumRiskConfirm by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(analysis.detected_type, rawPayload) {
        if (UPI_DEBUG_LOGS) {
            Log.d("UPI_DEBUG", "detected_type=${analysis.detected_type}")
            Log.d("UPI_DEBUG", "rawPayloadStartsWithUpi=$hasUpiScheme")
        }
    }

    if (showMediumRiskConfirm) {
        AlertDialog(
            onDismissRequest = { showMediumRiskConfirm = false },
            title = { Text("Potential risk detected") },
            text = { Text("Potential risk detected. Proceed carefully.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMediumRiskConfirm = false
                        rawPayload?.let(onProceedPayment)
                    }
                ) {
                    Text("Proceed")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMediumRiskConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(rsCard)
            .border(1.5.dp, topColor.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Brush.horizontalGradient(listOf(topColor, topColor.copy(alpha = 0.2f))))
        )

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(topColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = topColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.qr_analysis),
                        color = rsText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (analysis.is_flagged) stringResource(R.string.qr_suspicious_detected) else stringResource(R.string.qr_no_scam_signal),
                        color = rsMuted,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(topColor.copy(alpha = 0.1f))
                        .border(1.dp, topColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        riskLevel,
                        color = topColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            QrSectionBlock(
                "Detected Type",
                analysis.detected_type,
                rsBorder,
                rsText,
                rsMuted
            )
            QrSectionBlock(
                "Risk Score",
                analysis.risk_score.toString(),
                rsBorder,
                rsText,
                rsMuted
            )

            if (analysis.is_flagged) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(dangerRed.copy(alpha = 0.08f))
                        .border(1.dp, dangerRed.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = dangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.qr_scam_warning),
                        color = dangerRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (riskLevel == "HIGH") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(dangerRed.copy(alpha = 0.08f))
                        .border(1.dp, dangerRed.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "High risk detected. Payment blocked for your safety.",
                        color = dangerRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, dangerRed.copy(alpha = 0.35f))
                        ) {
                            Text("Do Not Pay", color = dangerRed)
                        }
                        Button(
                            onClick = onReport,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = dangerRed)
                        ) {
                            Text("Report QR", color = Color.White)
                        }
                    }
                }
            }

            if (analysis.reasons.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Reasons", color = rsMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    analysis.reasons.forEach { reason ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(topColor)
                            )
                            Text(reason, color = rsText, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            QrSectionBlock("Recommended Action", analysis.recommended_action, rsBorder, rsText, rsMuted)

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        when (riskLevel) {
                            "HIGH" -> Unit
                            "MEDIUM" -> if (hasUpiScheme) showMediumRiskConfirm = true
                            else -> if (hasUpiScheme) rawPayload?.let(onProceedPayment)
                        }
                    },
                    enabled = riskLevel != "HIGH" && isUpiDetected && hasUpiScheme && !rawPayload.isNullOrBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUpiDetected && hasUpiScheme && !rawPayload.isNullOrBlank()) neuralBlue else rsBorder
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Send,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (!isUpiDetected) "Proceed unavailable for this QR type"
                            else if (!hasUpiScheme) "Invalid UPI payload"
                            else if (riskLevel == "HIGH") "Payment Blocked"
                            else stringResource(R.string.qr_proceed_payment),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QrSectionBlock(
    title: String,
    value: String,
    rsBorder: Color,
    rsText: Color,
    rsMuted: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, rsBorder.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(title.uppercase(), color = rsMuted, fontSize = 10.sp, letterSpacing = 1.1.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(value, color = rsText, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun QrErrorBanner(
    message: String,
    dangerRed: Color,
    showRetry: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(dangerRed.copy(alpha = 0.08f))
            .border(1.dp, dangerRed.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Error, contentDescription = null, tint = dangerRed, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(message, color = dangerRed, fontSize = 12.sp, modifier = Modifier.weight(1f))

        if (showRetry) {
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.ui_screenlayouts_1), color = dangerRed)
            }
        }

        TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.common_close), color = dangerRed)
        }
    }
}

private class QrFrameAnalyzer(
    private val scanner: BarcodeScanner,
    private val isActive: () -> Boolean,
    private val onQrDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val isProcessing = AtomicBoolean(false)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!isActive()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        if (!isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val raw = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
                if (!raw.isNullOrBlank()) onQrDetected(raw)
            }
            .addOnCompleteListener {
                isProcessing.set(false)
                imageProxy.close()
            }
    }
}

private fun processGalleryQr(
    context: Context,
    uri: Uri,
    scanner: BarcodeScanner,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    onStart()
    try {
        val inputImage = InputImage.fromFilePath(context, uri)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val raw = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
                if (raw.isNullOrBlank()) onFailure(context.getString(R.string.qr_no_qr_found))
                else onSuccess(raw)
            }
            .addOnFailureListener {
                onFailure(context.getString(R.string.qr_unable_read_selected))
            }
            .addOnCompleteListener {
                onComplete()
            }
    } catch (_: Exception) {
        onFailure(context.getString(R.string.qr_unable_open_selected))
        onComplete()
    }
}

private fun openUpiIntent(
    context: Context,
    rawUpi: String,
    onError: (String) -> Unit
) {
    try {
        val sanitized = rawUpi.trim()
        val startsWithUpi = sanitized.startsWith("upi://", ignoreCase = true)
        if (!startsWithUpi) {
            if (UPI_DEBUG_LOGS) {
                Log.d("UPI_DEBUG", "launchPayloadStartsWithUpi=false")
            }
            onError(context.getString(R.string.qr_error_invalid_upi))
            return
        }

        val upiUri = Uri.parse(sanitized)
        val paymentIntent = Intent(Intent.ACTION_VIEW, upiUri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
        val resolvedPayment = paymentIntent.resolveActivity(context.packageManager)

        if (UPI_DEBUG_LOGS) {
            Log.d("UPI_DEBUG", "launchPayloadStartsWithUpi=true")
            Log.d("UPI_DEBUG", "paymentResolveNull=${resolvedPayment == null}")
        }

        // Hardcoded probe for diagnosis (payload vs manifest/device issue).
        if (UPI_DEBUG_LOGS) {
            val hardcoded = "upi://pay?pa=test@oksbi&pn=Test&am=1&cu=INR"
            val hardcodedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(hardcoded)).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            val hardcodedResolved = hardcodedIntent.resolveActivity(context.packageManager)
            Log.d("UPI_DEBUG", "hardcodedUpiResolveNull=${hardcodedResolved == null}")
        }

        if (resolvedPayment != null) {
            val chooserIntent = Intent.createChooser(paymentIntent, context.getString(R.string.qr_proceed_payment))
            val chooserResolved = chooserIntent.resolveActivity(context.packageManager)
            if (UPI_DEBUG_LOGS) {
                Log.d("UPI_DEBUG", "chooserResolveNull=${chooserResolved == null}")
            }
            if (chooserResolved != null) {
                context.startActivity(chooserIntent)
            } else {
                onError(context.getString(R.string.qr_no_upi_app))
            }
        } else {
            onError(context.getString(R.string.qr_no_upi_app))
        }
    } catch (_: ActivityNotFoundException) {
        onError(context.getString(R.string.qr_no_upi_app))
    } catch (_: Exception) {
        onError(context.getString(R.string.qr_unable_launch_upi))
    }
}
