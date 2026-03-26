package com.gosuraksha.app.ui.main

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gosuraksha.app.R
import com.gosuraksha.app.profile.model.ProfileResponse
import com.gosuraksha.app.design.tokens.ColorTokens

@Composable
fun ProfileHeaderSection(
    isDark: Boolean,
    profile: ProfileResponse?,
    fallbackName: String,
    imageUri: String?,
    sessionImageUrl: String?,
    isPremium: Boolean,
    planLabel: String,
    planRaw: String,
    onEditPhoto: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        ProfileHero(
            isDark = isDark,
            profile = profile,
            fallback = fallbackName,
            imageUri = imageUri,
            sessionImageUrl = sessionImageUrl,
            isPremium = isPremium,
            planLabel = planLabel,
            planRaw = planRaw,
            onEditPhoto = onEditPhoto
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .offset(y = 46.dp)
                .fillMaxWidth()
        ) {
            ProfileScoreCard(isDark = isDark)
        }
    }
}

@Composable
fun ProfileHero(
    isDark: Boolean,
    profile: ProfileResponse?,
    fallback: String,
    imageUri: String?,
    sessionImageUrl: String?,
    isPremium: Boolean,
    planLabel: String,
    planRaw: String,
    onEditPhoto: () -> Unit
) {
    val name = profile?.name?.ifBlank { fallback } ?: fallback
    val phone = profile?.phone.orEmpty()
    val avatarModel = imageUri ?: profile?.profile_image_url ?: sessionImageUrl
    val context = LocalContext.current
    val initials = name.trim().split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifEmpty { "M" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(PC.HeroStart, PC.HeroMid, PC.HeroEnd),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .padding(bottom = 56.dp)
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-50).dp)
                .background(PC.Green.copy(alpha = 0.06f), CircleShape)
        )

        Column(modifier = Modifier.padding(start = 18.dp, top = 20.dp, end = 18.dp)) {
            Text(
                text = "Profile",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.30f),
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(modifier = Modifier.size(68.dp), contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!avatarModel.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(avatarModel)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(initials, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Box(
                        modifier = Modifier.size(20.dp).clip(CircleShape)
                            .background(Color(0xFF0D4020))
                            .border(1.5.dp, PC.Green.copy(alpha = 0.4f), CircleShape)
                            .clickable(onClick = onEditPhoto),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, null, tint = PC.Green, modifier = Modifier.size(10.dp))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = name.ifBlank { stringResource(R.string.profile_guest) },
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.4).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (phone.isNotBlank()) {
                        Text(phone, fontSize = 11.sp, color = Color.White.copy(alpha = 0.45f))
                    }
                    // Plan badge — gradient for ULTRA, solid for PRO, ghost for FREE
                    val badgeModifier = when (planRaw) {
                        "GO_ULTRA" -> Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFFD700), Color(0xFFFF9500), Color(0xFFFFD700))
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                        "GO_PRO"   -> Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PC.Green.copy(alpha = 0.15f))
                            .border(0.5.dp, PC.Green.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                        else       -> Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    }
                    Box(modifier = badgeModifier) {
                        Text(
                            text = when (planRaw) {
                                "GO_ULTRA" -> "✦  GO ULTRA"
                                "GO_PRO"   -> "★  GO PRO"
                                else       -> "◆  Free Plan"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (planRaw) {
                                "GO_ULTRA" -> Color(0xFF3B1C00)   // dark on gold gradient
                                "GO_PRO"   -> PC.Green
                                else       -> Color.White.copy(alpha = 0.55f)
                            },
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScoreCard(isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(0.06f), spotColor = Color.Black.copy(0.06f))
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) PC.DarkCard else PC.LightCard)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreCell(18, 100, "Risk Score", PC.Red, isDark)
        Box(modifier = Modifier.width(0.5.dp).height(36.dp).background(if (isDark) PC.DarkBorder else PC.LightBorder))
        ScoreCell(71, 100, "Scans", PC.Green, isDark)
        Box(modifier = Modifier.width(0.5.dp).height(36.dp).background(if (isDark) PC.DarkBorder else PC.LightBorder))
        ScoreCell(3, 10, "Threats", PC.Amber, isDark)
    }
}

@Composable
fun ScoreCell(value: Int, max: Int, label: String, color: Color, isDark: Boolean) {
    val sweep by androidx.compose.animation.core.animateFloatAsState(
        targetValue = (value.toFloat() / max) * 360f,
        animationSpec = androidx.compose.animation.core.spring(
            androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "sweep"
    )
    val trackColor = if (isDark) PC.DarkBorder else PC.LightBorder

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                val stroke = Stroke(4.5f, cap = StrokeCap.Round)
                val inset = 4.5f / 2
                val rect = androidx.compose.ui.geometry.Rect(inset, inset, size.width - inset, size.height - inset)
                drawArc(trackColor, 0f, 360f, false, rect.topLeft, rect.size, style = stroke)
                drawArc(color, -90f, sweep, false, rect.topLeft, rect.size, style = stroke)
            }
            Text("$value", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
        Text(label, fontSize = 8.sp, color = PC.subText(isDark), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EdgyLoadingAnimation() {
    val infinite = rememberInfiniteTransition(label = "load")
    val rotation by infinite.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "spin"
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .rotate(rotation)
                .border(4.dp, Brush.sweepGradient(listOf(ColorTokens.accent(), Color.Transparent)), CircleShape)
        )
    }
}
