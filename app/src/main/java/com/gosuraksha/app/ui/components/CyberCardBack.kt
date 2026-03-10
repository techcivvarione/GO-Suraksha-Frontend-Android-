package com.gosuraksha.app.ui.components

import com.gosuraksha.app.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun CyberCardBackNew() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4A5568),
                            Color(0xFF2D3748),
                            Color(0xFF1A202C)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        )

        // Wavy pattern
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val waveCount = 15
            val waveHeight = height / waveCount

            for (i in 0 until waveCount) {
                val path = Path()
                val yOffset = i * waveHeight
                path.moveTo(0f, yOffset)

                val wavelength = width / 3f
                var x = 0f
                while (x <= width) {
                    val y = yOffset + (sin((x / wavelength) * 2 * PI) * 8).toFloat()
                    path.lineTo(x, y)
                    x += 5f
                }

                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.08f),
                    style = Stroke(width = 1.5f)
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.ui_cybercardfront_2),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 3.sp
                )
                Text(
                    text = stringResource(R.string.cybercard_back_validation_compliance),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // Three columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Column 1
                ComplianceColumn(
                    title = stringResource(R.string.cybercard_back_generated_using),
                    items = listOf(
                        stringResource(R.string.cybercard_back_item_risk_behavior),
                        stringResource(R.string.cybercard_back_item_link_domain),
                        stringResource(R.string.cybercard_back_item_exposure),
                        stringResource(R.string.cybercard_back_item_unreported_events),
                        stringResource(R.string.cybercard_back_item_pattern_anomaly),
                        "",
                        stringResource(R.string.cybercard_back_dynamic_score_note)
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                // Column 2
                ComplianceColumn(
                    title = stringResource(R.string.cybercard_back_data_privacy_commitment),
                    items = listOf(
                        stringResource(R.string.cybercard_back_does_not),
                        "",
                        stringResource(R.string.cybercard_back_item_no_gov_db),
                        stringResource(R.string.cybercard_back_item_no_biometrics),
                        stringResource(R.string.cybercard_back_item_no_sell_share)
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                // Column 3
                ComplianceColumn(
                    title = stringResource(R.string.cybercard_back_data_handling_principles),
                    items = listOf(
                        stringResource(R.string.cybercard_back_item_encrypted_comm),
                        stringResource(R.string.cybercard_back_item_minimal_storage),
                        stringResource(R.string.cybercard_back_item_privacy_first),
                        stringResource(R.string.cybercard_back_item_user_controlled)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Legal section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.cybercard_back_legal_compliance),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.cybercard_back_alignment_with),
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.cybercard_back_laws_standards),
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // Disclaimer
            Text(
                text = stringResource(R.string.ui_cybercardback_3),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )

            Spacer(Modifier.height(8.dp))

            // Logo at bottom right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = stringResource(R.string.cybercard_back_logo_go),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00D68F),
                    letterSpacing = (-1).sp
                )
                Text(
                    text = stringResource(R.string.cybercard_back_logo_suraksha),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00D68F),
                    modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ComplianceColumn(
    title: String,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 10.sp
        )
        items.forEach { item ->
            if (item.isNotBlank()) {
                Text(
                    text = item,
                    fontSize = 7.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 9.sp
                )
            } else {
                Spacer(Modifier.height(2.dp))
            }
        }
    }
}

