package com.gosuraksha.app.ui.qr.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import com.gosuraksha.app.R
import java.util.concurrent.atomic.AtomicBoolean

internal class QrFrameAnalyzer(
    private val scanner: BarcodeScanner,
    private val isActive: () -> Boolean,
    private val onQrDetected: (String) -> Unit,
) : ImageAnalysis.Analyzer {
    private val isProcessing = AtomicBoolean(false)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!isActive()) { imageProxy.close(); return }
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        if (!isProcessing.compareAndSet(false, true)) { imageProxy.close(); return }
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val raw = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
                if (!raw.isNullOrBlank()) onQrDetected(raw)
            }
            .addOnCompleteListener { isProcessing.set(false); imageProxy.close() }
    }
}

internal fun processGalleryQr(
    context: Context,
    uri: Uri,
    scanner: BarcodeScanner,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit,
) {
    onStart()
    try {
        val inputImage = InputImage.fromFilePath(context, uri)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val raw = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
                if (raw.isNullOrBlank()) onFailure(context.getString(R.string.qr_no_qr_found)) else onSuccess(raw)
            }
            .addOnFailureListener { onFailure(context.getString(R.string.qr_unable_read_selected)) }
            .addOnCompleteListener { onComplete() }
    } catch (_: Exception) {
        onFailure(context.getString(R.string.qr_unable_open_selected))
        onComplete()
    }
}

internal fun openUpiIntent(context: Context, rawUpi: String, onError: (String) -> Unit) {
    try {
        val sanitized = rawUpi.trim()
        if (!sanitized.startsWith("upi://", ignoreCase = true)) {
            onError(context.getString(R.string.qr_error_invalid_upi))
            return
        }
        val chooser = Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(sanitized)).apply { addCategory(Intent.CATEGORY_BROWSABLE) }, context.getString(R.string.qr_proceed_payment))
        if (chooser.resolveActivity(context.packageManager) != null) context.startActivity(chooser) else onError(context.getString(R.string.qr_no_upi_app))
    } catch (_: ActivityNotFoundException) {
        onError(context.getString(R.string.qr_no_upi_app))
    } catch (_: Exception) {
        onError(context.getString(R.string.qr_unable_launch_upi))
    }
}
