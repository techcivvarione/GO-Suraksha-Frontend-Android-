package com.gosuraksha.app.ui.qr

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.gosuraksha.app.R
import com.gosuraksha.app.domain.usecase.ScanUseCaseProvider
import com.gosuraksha.app.scan.design.GoSurakshaScanTheme
import com.gosuraksha.app.scan.qr.QrAnalysisUiState
import com.gosuraksha.app.scan.qr.QrScanViewModel
import com.gosuraksha.app.scan.qr.QrScanViewModelFactory
import com.gosuraksha.app.ui.qr.components.QrAnalysisResult
import com.gosuraksha.app.ui.qr.components.QrCameraPreview
import com.gosuraksha.app.ui.qr.components.QrFrameAnalyzer
import com.gosuraksha.app.ui.qr.components.openUpiIntent
import com.gosuraksha.app.ui.qr.components.processGalleryQr
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun QrAnalyzerScreen(
    onUpgradePlan: () -> Unit = {},
    qrViewModel: QrScanViewModel = run {
        val ctx = LocalContext.current.applicationContext
        val provider = ctx as ScanUseCaseProvider
        viewModel(factory = QrScanViewModelFactory(provider.scanUseCases().analyzeQr))
    },
) {
    val state by qrViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scannerOptions = remember { BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build() }
    val barcodeScanner = remember { BarcodeScanning.getClient(scannerOptions) }
    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanGate = remember { AtomicBoolean(true) }

    var hasCameraPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
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
        scanGate.set(shouldShowScannerStage && !scanningPaused && !detectionAnimating && !isAnalyzing && pendingDetectedRaw == null)
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
            onFailure = { message -> scanningPaused = false; qrViewModel.onScanFailure(message) }
        )
    }

    LaunchedEffect(boundCamera, flashEnabled, hasFlash) {
        boundCamera?.cameraControl?.enableTorch(hasFlash && flashEnabled)
    }

    LaunchedEffect(pendingDetectedRaw) {
        val raw = pendingDetectedRaw ?: return@LaunchedEffect
        detectionAnimating = true
        kotlinx.coroutines.delay(450)
        qrViewModel.onQrRawDetected(raw)
        kotlinx.coroutines.delay(180)
        detectionAnimating = false
        pendingDetectedRaw = null
    }

    DisposableEffect(previewView, hasCameraPermission, lifecycleOwner, shouldShowScannerStage) {
        if (!hasCameraPermission || !shouldShowScannerStage || previewView == null) {
            return@DisposableEffect onDispose { }
        }
        val preview = Preview.Builder().build()
        val imageAnalysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
        imageAnalysis.setAnalyzer(analyzerExecutor, QrFrameAnalyzer(scanner = barcodeScanner, isActive = { scanGate.get() }, onQrDetected = { raw ->
            if (pendingDetectedRaw == null) {
                scanningPaused = true
                pendingDetectedRaw = raw
            }
        }))
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null
        providerFuture.addListener({
            try {
                cameraProvider = providerFuture.get()
                cameraProvider?.unbindAll()
                preview.setSurfaceProvider(previewView?.surfaceProvider)
                val camera = cameraProvider?.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                boundCamera = camera
                hasFlash = camera?.cameraInfo?.hasFlashUnit() == true
                if (!hasFlash) flashEnabled = false
                camera?.cameraControl?.enableTorch(hasFlash && flashEnabled)
            } catch (_: Exception) {
                qrViewModel.onScanFailure(context.getString(R.string.qr_unable_start_camera))
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            imageAnalysis.clearAnalyzer()
            runCatching { cameraProvider?.unbindAll() }
            boundCamera = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            barcodeScanner.close()
            analyzerExecutor.shutdown()
        }
    }

    Crossfade(targetState = shouldShowScannerStage, label = "qr_stage") { showScanner ->
        if (showScanner) {
            GoSurakshaScanTheme(darkTheme = true) {
                QrCameraPreview(
                    cameraPermissionGranted = hasCameraPermission,
                    isLoading = state.isScannerLoading || isAnalyzing,
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
                    detectionAnimating = detectionAnimating
                )
            }
        } else {
            GoSurakshaScanTheme {
                QrAnalysisResult(
                    state            = state,
                    onReport         = { qrViewModel.onScanFailure("qr_scam_warning") },
                    onProceedPayment = { raw -> openUpiIntent(context, raw) { qrViewModel.onScanFailure(it) } },
                    onScanAnother    = {
                        qrViewModel.startNewScan()
                        scanningPaused = false
                        pendingDetectedRaw = null
                        detectionAnimating = false
                    },
                    onUpgradePlan    = onUpgradePlan,
                )
            }
        }
    }
}
