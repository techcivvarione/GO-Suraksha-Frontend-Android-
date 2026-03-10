package com.gosuraksha.app.ui.main

import com.gosuraksha.app.R
import androidx.compose.ui.res.stringResource
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.scan.SharedScanPayload
import com.gosuraksha.app.ui.components.localizedUiMessage
import java.util.Locale
import kotlin.math.*

// Models
data class RealityScanResult(
    val riskLevel: String,
    val confidence: Float?,
    val reasons: List<String>,
    val recommendation: String
)

enum class RsState { IDLE, SCANNING, DONE, ERROR }

@Composable
fun RealityScanScreen(
    sharedPayload: SharedScanPayload? = null,
    onSharedPayloadConsumed: () -> Unit = {},
    onScan: (ByteArray, String, (RealityScanResult) -> Unit, (String) -> Unit) -> Unit
) {
    // Design system colors
    val rsBg = ColorTokens.background()
    val rsCard = ColorTokens.surface()
    val rsBorder = ColorTokens.border()
    val neuralBlue = Color(0xFF3B82F6)
    val neuralPurp = Color(0xFF8B5CF6)
    val tealAccent = ColorTokens.accent()
    val dangerRed = ColorTokens.error()
    val safeGreen = ColorTokens.success()
    val warnAmber = ColorTokens.warning()
    val rsText = ColorTokens.textPrimary()
    val rsMuted = ColorTokens.textSecondary()

    val context = LocalContext.current
    var uri by remember { mutableStateOf<Uri?>(null) }
    var selectedMimeType by remember { mutableStateOf<String?>(null) }
    var rsState by remember { mutableStateOf(RsState.IDLE) }
    var result by remember { mutableStateOf<RealityScanResult?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sharedPayload) {
        val payload = sharedPayload ?: return@LaunchedEffect
        uri = payload.uri
        selectedMimeType = payload.mimeType
        result = null
        errorMsg = null
        rsState = RsState.IDLE
        onSharedPayloadConsumed()
    }

    // Simplified animations
    val infinite = rememberInfiniteTransition(label = stringResource(R.string.ui_realityscanscreen_13))
    val pulse1 by infinite.animateFloat(
        0.3f, 1f,
        infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = stringResource(R.string.ui_realityscanscreen_14)
    )
    val beamY by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = stringResource(R.string.ui_realityscanscreen_15)
    )
    val dot1 by infinite.animateFloat(
        0.2f, 1f,
        infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = stringResource(R.string.ui_realityscanscreen_16)
    )
    val dot2 by infinite.animateFloat(
        0.2f, 1f,
        infiniteRepeatable(tween(500, 140, FastOutSlowInEasing), RepeatMode.Reverse),
        label = stringResource(R.string.ui_realityscanscreen_17)
    )
    val dot3 by infinite.animateFloat(
        0.2f, 1f,
        infiniteRepeatable(tween(500, 280, FastOutSlowInEasing), RepeatMode.Reverse),
        label = stringResource(R.string.ui_realityscanscreen_18)
    )

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { u ->
        u?.let {
            val mimeType = context.contentResolver.getType(it)?.lowercase(Locale.ROOT).orEmpty()
            if (isSupportedMedia(it, mimeType)) {
                uri = it
                selectedMimeType = mimeType
                result = null
                errorMsg = null
                rsState = RsState.IDLE
            } else {
                errorMsg = "Unsupported file. Use jpg/jpeg/png/webp, mp4/webm, or wav/mp3/aac."
                rsState = RsState.ERROR
            }
        }
    }
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        val recorded = activityResult.data?.data
        if (recorded != null) {
            uri = recorded
            selectedMimeType = "audio/*"
            result = null
            errorMsg = null
            rsState = RsState.IDLE
        } else {
            errorMsg = "No audio captured"
            rsState = RsState.ERROR
        }
    }

    Box(Modifier.fillMaxSize().background(rsBg)) {

        // Simplified background - just subtle gradient glow
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.28f

            drawCircle(
                brush = Brush.radialGradient(
                    listOf(neuralPurp.copy(alpha = 0.05f * pulse1), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(cx, cy),
                    radius = size.width * 0.6f
                ),
                center = androidx.compose.ui.geometry.Offset(cx, cy),
                radius = size.width * 0.6f
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Header icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(neuralBlue.copy(alpha = 0.2f), neuralPurp.copy(alpha = 0.2f))))
                    .border(1.dp, Brush.linearGradient(listOf(neuralBlue.copy(alpha = 0.4f), neuralPurp.copy(alpha = 0.4f))), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.DocumentScanner, null, tint = neuralPurp, modifier = Modifier.size(34.dp))
            }

            Spacer(Modifier.height(16.dp))

            Text(stringResource(R.string.ui_realityscanscreen_1), color = rsText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.ui_realityscanscreen_2), color = rsMuted, fontSize = 13.sp, textAlign = TextAlign.Center)

            Spacer(Modifier.height(6.dp))

            // Capability chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RsChip(stringResource(R.string.ui_realityscanscreen_20), neuralBlue)
                RsChip(stringResource(R.string.ui_realityscanscreen_21), neuralPurp)
                RsChip(stringResource(R.string.ui_realityscanscreen_22), tealAccent)
            }

            Spacer(Modifier.height(16.dp))

            // Image zone
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(270.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(rsCard)
                    .border(
                        1.5.dp,
                        if (uri != null)
                            Brush.linearGradient(listOf(neuralBlue.copy(alpha = 0.5f), neuralPurp.copy(alpha = 0.5f)))
                        else
                            Brush.linearGradient(listOf(rsBorder, rsBorder)),
                        RoundedCornerShape(28.dp)
                    )
                    .clickable(remember { MutableInteractionSource() }, null) {
                        if (rsState != RsState.SCANNING) launcher.launch("*/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                if (uri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(uri).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (rsState == RsState.SCANNING) {
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)))

                        // Scan beam
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .align(Alignment.TopStart)
                                .offset(y = (270.dp * beamY))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            neuralBlue.copy(alpha = 0.5f),
                                            neuralPurp,
                                            neuralBlue.copy(alpha = 0.5f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(dot1, dot2, dot3).forEach { a ->
                                    Box(Modifier.size(6.dp).clip(CircleShape).background(neuralPurp.copy(alpha = a)))
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(stringResource(R.string.ui_realityscanscreen_3), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text(stringResource(R.string.ui_realityscanscreen_4), color = neuralPurp.copy(alpha = 0.7f), fontSize = 11.sp, letterSpacing = 1.sp)
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(neuralPurp.copy(alpha = 0.15f), neuralBlue.copy(alpha = 0.05f)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.AddPhotoAlternate, null, tint = neuralPurp, modifier = Modifier.size(30.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(stringResource(R.string.ui_realityscanscreen_5), color = rsText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.ui_realityscanscreen_6), color = rsMuted, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { if (rsState != RsState.SCANNING) launcher.launch("*/*") },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, rsBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = rsMuted)
                ) {
                    Icon(Icons.Outlined.FolderOpen, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (uri == null) "Scan From Gallery" else stringResource(R.string.reality_action_change),
                        fontSize = 13.sp
                    )
                }

                val canScan = uri != null && rsState != RsState.SCANNING
                Button(
                    onClick = {
                        uri?.let { u ->
                            rsState = RsState.SCANNING
                            val bytes = runCatching {
                                context.contentResolver.openInputStream(u)?.use { it.readBytes() }
                            }.getOrNull()

                            val mimeType = (selectedMimeType ?: context.contentResolver.getType(u)?.lowercase(Locale.ROOT)).orEmpty()
                            if (bytes != null && bytes.isNotEmpty() && mimeType.isNotBlank() && isSupportedMedia(u, mimeType)) {
                                runCatching {
                                    onScan(
                                        bytes,
                                        mimeType,
                                        { r -> rsState = RsState.DONE; result = r; errorMsg = null },
                                        { e ->
                                            rsState = RsState.ERROR
                                            errorMsg = if (e.isBlank()) "error_generic" else e
                                        }
                                    )
                                }.onFailure {
                                    rsState = RsState.ERROR
                                    errorMsg = "error_generic"
                                }
                            } else {
                                rsState = RsState.ERROR
                                errorMsg = "Unsupported or unreadable media file."
                            }
                        }
                    },
                    enabled = canScan,
                    modifier = Modifier.weight(2.2f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            if (canScan) Brush.horizontalGradient(listOf(neuralBlue, neuralPurp))
                            else Brush.horizontalGradient(listOf(rsBorder, rsBorder)),
                            RoundedCornerShape(14.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CenterFocusWeak, null, tint = Color.White, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(7.dp))
                            Text(
                                if (rsState == RsState.SCANNING) stringResource(R.string.reality_scanning) else stringResource(R.string.reality_scan_reality),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    val intent = android.content.Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                    voiceLauncher.launch(intent)
                },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, rsBorder)
            ) {
                Icon(Icons.Outlined.Mic, null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(8.dp))
                Text("Scan Voice Message", fontSize = 13.sp)
            }

            Spacer(Modifier.height(16.dp))

            when (rsState) {
                RsState.DONE -> result?.let {
                    RsResultCard(it, rsCard, rsText, rsMuted, rsBorder, neuralBlue, neuralPurp, dangerRed, safeGreen, warnAmber)
                }
                RsState.ERROR -> RsErrorBanner(localizedUiMessage(errorMsg ?: "error_generic"), dangerRed)
                else -> RsInfoCard(rsCard, rsBorder, rsMuted, rsText, neuralBlue, neuralPurp, tealAccent, warnAmber)
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun RsResultCard(
    r: RealityScanResult,
    rsCard: Color,
    rsText: Color,
    rsMuted: Color,
    rsBorder: Color,
    neuralBlue: Color,
    neuralPurp: Color,
    dangerRed: Color,
    safeGreen: Color,
    warnAmber: Color
) {
    val level = r.riskLevel.uppercase(Locale.ROOT)
    val (mainColor, icon, title, subtitle) = when (level) {
        "HIGH" -> Quad(
            dangerRed,
            Icons.Filled.SmartToy,
            "High risk detected",
            "Potentially manipulated or unsafe media."
        )
        "MEDIUM" -> Quad(
            warnAmber,
            Icons.Filled.HelpCenter,
            "Medium risk detected",
            "Review carefully before trusting this media."
        )
        else -> Quad(
            safeGreen,
            Icons.Filled.PhotoCamera,
            "Low risk detected",
            "No strong risk signals found."
        )
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(rsCard)
            .border(1.5.dp, mainColor.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(4.dp)
                .background(Brush.horizontalGradient(listOf(mainColor, mainColor.copy(alpha = 0.2f))))
        )

        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(18.dp))
                        .background(mainColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = mainColor, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, color = rsText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = rsMuted, fontSize = 11.sp, lineHeight = 15.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(mainColor.copy(alpha = 0.1f))
                        .border(1.dp, mainColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${((r.confidence ?: 0f) * 100).toInt()}%", color = mainColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(stringResource(R.string.ui_realityscanscreen_8), color = mainColor.copy(alpha = 0.6f), fontSize = 9.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.ui_realityscanscreen_9), color = rsMuted, fontSize = 11.sp)
                    Text("Risk: $level", color = rsMuted, fontSize = 10.sp)
                }
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)).background(rsBorder)) {
                    val anim by animateFloatAsState(
                        (r.confidence ?: 0f).coerceIn(0f, 1f),
                        tween(800, easing = FastOutSlowInEasing),
                        label = stringResource(R.string.ui_realityscanscreen_19)
                    )
                    Box(
                        modifier = Modifier.fillMaxHeight().fillMaxWidth(anim).clip(RoundedCornerShape(4.dp))
                            .background(Brush.horizontalGradient(listOf(neuralBlue, neuralPurp)))
                    )
                }
            }

            if (r.reasons.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(rsBorder))
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.ui_realityscanscreen_10), color = rsMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                Spacer(Modifier.height(8.dp))
                r.reasons.forEach { s ->
                    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier.size(5.dp).offset(y = 7.dp).clip(CircleShape)
                                .background(Brush.linearGradient(listOf(neuralBlue, neuralPurp)))
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(s, color = rsText, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(neuralPurp.copy(alpha = 0.07f))
                    .border(1.dp, neuralPurp.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Info, null, tint = neuralPurp, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(r.recommendation, color = neuralPurp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RsInfoCard(
    rsCard: Color,
    rsBorder: Color,
    rsMuted: Color,
    rsText: Color,
    neuralBlue: Color,
    neuralPurp: Color,
    tealAccent: Color,
    warnAmber: Color
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(rsCard)
            .border(1.dp, rsBorder, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(stringResource(R.string.ui_realityscanscreen_12), color = rsMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        RsStep(stringResource(R.string.ui_realityscanscreen_23), stringResource(R.string.reality_step_1), neuralBlue, rsText)
        RsStep(stringResource(R.string.ui_realityscanscreen_24), stringResource(R.string.reality_step_2), neuralPurp, rsText)
        RsStep(stringResource(R.string.ui_realityscanscreen_25), stringResource(R.string.reality_step_3), tealAccent, rsText)
        RsStep(stringResource(R.string.ui_realityscanscreen_26), stringResource(R.string.reality_step_4), warnAmber, rsText)
    }
}

@Composable
private fun RsStep(num: String, label: String, color: Color, rsText: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(24.dp).clip(CircleShape)
                .background(color.copy(alpha = 0.12f))
                .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(num, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Text(label, color = rsText, fontSize = 13.sp)
    }
}

@Composable
private fun RsErrorBanner(msg: String, dangerRed: Color) {
    Row(
        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(dangerRed.copy(alpha = 0.08f))
            .border(1.dp, dangerRed.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Error, null, tint = dangerRed, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(msg, color = dangerRed, fontSize = 12.sp)
    }
}

@Composable
private fun RsChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun isSupportedMedia(uri: Uri, mimeType: String): Boolean {
    val mime = mimeType.lowercase(Locale.ROOT)
    val ext = uri.lastPathSegment
        ?.substringAfterLast('.', "")
        ?.lowercase(Locale.ROOT)
        .orEmpty()

    val supportedMime = setOf(
        "image/jpeg", "image/jpg", "image/png", "image/webp",
        "video/mp4", "video/webm",
        "audio/wav", "audio/x-wav", "audio/mpeg", "audio/mp3", "audio/aac", "audio/mp4"
    )
    val supportedExt = setOf("jpg", "jpeg", "png", "webp", "mp4", "webm", "wav", "mp3", "aac")

    return mime in supportedMime || ext in supportedExt
}



