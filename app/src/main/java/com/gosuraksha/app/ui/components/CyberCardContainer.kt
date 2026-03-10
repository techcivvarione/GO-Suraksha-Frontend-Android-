package com.gosuraksha.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun CyberCardContainer(
    userName: String,
    cardNumber: String,
    cyberScore: Int,
    generatedOn: String,
    validTill: String
) {
    var flipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "card_flip"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { flipped = !flipped }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
    ) {
        if (rotation <= 90f) {
            // Front side
            CyberCardFrontNew(
                userName = userName,
                cardNumber = cardNumber,
                cyberScore = cyberScore,
                generatedOn = generatedOn,
                validTill = validTill
            )
        } else {
            // Back side (flipped)
            Box(
                modifier = Modifier.graphicsLayer { rotationY = 180f }
            ) {
                CyberCardBackNew()
            }
        }
    }
}