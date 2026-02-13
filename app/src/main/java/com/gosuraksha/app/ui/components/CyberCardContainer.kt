package com.gosuraksha.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.gosuraksha.app.auth.model.UserResponse

@Composable
fun CyberCardContainer(
    user: UserResponse,
    cyberScore: Int,
    maxScore: Int,
    riskLevel: String,
    generatedOn: String,
    validTill: String,
    signals: Map<String, Any>? = null
) {

    var flipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { flipped = !flipped }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            }
    ) {

        if (rotation <= 90f) {
            CyberCardFront(
                user = user,
                cyberScore = cyberScore,
                maxScore = maxScore,
                riskLevel = riskLevel,
                generatedOn = generatedOn,
                validTill = validTill
            )
        } else {
            Box(
                modifier = Modifier.graphicsLayer {
                    rotationY = 180f
                }
            ) {
                CyberCardBack(signals = signals)
            }
        }
    }
}
