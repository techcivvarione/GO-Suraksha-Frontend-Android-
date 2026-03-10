package com.gosuraksha.app.ui.components

import androidx.compose.ui.res.stringResource
import com.gosuraksha.app.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun CyberCardFrontNew(
    userName: String,
    cardNumber: String,
    cyberScore: Int,
    generatedOn: String,
    validTill: String
) {
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
                            Color(0xFF4A5568), // Gray-blue
                            Color(0xFF2D3748), // Dark gray
                            Color(0xFF1A202C)  // Almost black
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        )

        // Wavy lines pattern overlay
        WavyPattern()

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // GO Suraksha logo text
                Text(
                    text = stringResource(R.string.cybercard_brand_go),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00D68F), // Green accent
                    letterSpacing = (-1).sp
                )
                Text(
                    text = stringResource(R.string.cybercard_brand_suraksha),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00D68F),
                    modifier = Modifier.offset(x = (-8).dp, y = 8.dp)
                )

                // CYBER CARD text
                Text(
                    text = stringResource(R.string.ui_cybercardfront_2),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            }

            // Card number section
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.ui_cybercardfront_3),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 2.sp
                )
                Text(
                    text = cardNumber,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black, // Yes, black text on dark bg (matches your design)
                    letterSpacing = 4.sp
                )
            }

            // Bottom row - Name and Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Name section
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.ui_cybercardfront_4),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = userName.uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Cyber Score
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = cyberScore.toString(),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00FF7F), // Bright green
                        letterSpacing = (-2).sp
                    )
                    Text(
                        text = stringResource(R.string.ui_cybercardfront_5),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Footer - Generated and Valid dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.ui_cybercardfront_7, generatedOn),
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = stringResource(R.string.ui_cybercardfront_8, validTill),
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun WavyPattern() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Draw multiple wavy curves
        val waveCount = 15
        val waveHeight = height / waveCount

        for (i in 0 until waveCount) {
            val path = Path()
            val yOffset = i * waveHeight

            // Start from left
            path.moveTo(0f, yOffset)

            // Create smooth wave
            val wavelength = width / 3f
            var x = 0f
            while (x <= width) {
                val y = yOffset + (sin((x / wavelength) * 2 * PI) * 8).toFloat()
                path.lineTo(x, y)
                x += 5f
            }

            // Draw the wave
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.08f),
                style = Stroke(width = 1.5f)
            )
        }
    }
}
