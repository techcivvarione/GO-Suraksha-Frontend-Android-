package com.gosuraksha.app.ui.components

// =============================================================================
// EnterpriseTopBar.kt — "Void Glass" redesign
//
// Visual spec:
//   • Frosted glass panel — semi-transparent surface with inset highlight
//   • Subtle teal→blue gradient mesh inside the bar background
//   • Logo carousel: GoSuraksha → GoSafe → GoSecure (2.5s, slide-up/fade) — UNCHANGED
//   • Pulsing live status dot next to logo (teal, signals active protection)
//   • Avatar: translucent glass circle with teal ring border
//   • Right side: vertical divider · theme toggle · SOS pill with drop shadow
//   • Bottom: 1px gradient line (transparent → teal → blue → transparent)
//   • Dark  → glass surface over dark bg, teal/blue tints
//   • Light → glass surface over light bg, reduced opacity, same structure
//
// Wiring: ALL UNCHANGED
//   • Theme toggle: LocalThemeState.current.toggle()
//   • SOS:         onCyberSosClick lambda
//   • Logo assets: R.drawable.logo / go_safe / go_secure
//   • User:        SessionManager.user
// =============================================================================

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gosuraksha.app.R
import com.gosuraksha.app.core.periodicTickFlow
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.ui.theme.LocalThemeState

// ─────────────────────────────────────────────────────────────────────────────
// Design constants
// ─────────────────────────────────────────────────────────────────────────────
private val TopBarHeight  = 64.dp
private val HorizontalPad = 16.dp
private val SosRed        = Color(0xFFEF4444)
private val TealGlass     = Color(0xFF1BBE9B)
private val BlueGlass     = Color(0xFF4F8AFF)

// =============================================================================
// Public entry-points — backward-compatible, signatures unchanged
// =============================================================================

@Composable
fun AppTopBar(onCyberSosClick: () -> Unit, onMenuClick: (() -> Unit)? = null) {
    EnterpriseTopBar(onCyberSosClick = onCyberSosClick, onMenuClick = onMenuClick)
}

@Composable
fun EnterpriseTopBar(
    onCyberSosClick: () -> Unit,
    onMenuClick: (() -> Unit)? = null
) {
    val themeState = LocalThemeState.current
    val isDark     = themeState.isDark
    val user       by SessionManager.user.collectAsStateWithLifecycle()

    // ── Logo carousel — UNCHANGED ─────────────────────────────────────────────
    val logos = remember {
        listOf(R.drawable.logo, R.drawable.go_safe, R.drawable.go_secure)
    }
    var currentLogo by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, logos.size) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            periodicTickFlow(periodMillis = 2_500L).collect {
                currentLogo = (currentLogo + 1) % logos.size
            }
        }
    }

    // ── Bottom separator line — bar is fully transparent, no artifacts ─────────
    val lineAlpha = if (isDark) 0.28f else 0.16f
    val lineBrush = Brush.horizontalGradient(
        colorStops = arrayOf(
            0.00f to Color.Transparent,
            0.20f to TealGlass.copy(alpha = lineAlpha),
            0.50f to BlueGlass.copy(alpha = lineAlpha * 0.7f),
            0.80f to TealGlass.copy(alpha = lineAlpha * 0.4f),
            1.00f to Color.Transparent
        )
    )

    // ── Root box — matches screen bg on every device, dark + light ───────────
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(TopBarHeight)
            .background(ColorTokens.background())
            .drawBehind {
                // Only bottom separator — no circle, no border, no mesh
                drawLine(
                    brush       = lineBrush,
                    start       = Offset(0f, size.height),
                    end         = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPad)
                .align(Alignment.Center),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // ── LEFT: Avatar + Status dot + Logo carousel ─────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Avatar with teal glass ring
                if (onMenuClick != null) {
                    IconButton(
                        onClick  = onMenuClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        UserAvatar(
                            name     = user?.name?.ifBlank { "User" } ?: "User",
                            imageUrl = user?.profileImageUrl,
                            isDark   = isDark,
                            size     = 38.dp
                        )
                    }
                }

                // Pulsing live status dot
                LiveStatusDot()

                // Logo carousel — slide-up/fade, UNCHANGED logic
                Box(
                    modifier        = Modifier
                        .height(18.dp)
                        .width(100.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AnimatedContent(
                        targetState  = logos[currentLogo],
                        transitionSpec = {
                            (
                                    slideInVertically(
                                        animationSpec = tween(320, easing = FastOutSlowInEasing)
                                    ) { it } + fadeIn(tween(280))
                                    ).togetherWith(
                                    slideOutVertically(
                                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                                    ) { -it } + fadeOut(tween(200))
                                )
                        },
                        label = "logo_carousel"
                    ) { logoRes ->
                        Icon(
                            painter            = painterResource(id = logoRes),
                            contentDescription = null,
                            modifier           = Modifier
                                .height(18.dp)
                                .graphicsLayer {
                                    scaleX          = 0.85f
                                    scaleY          = 0.85f
                                    transformOrigin = TransformOrigin(0f, 0.5f)
                                },
                            tint = Color.Unspecified
                        )
                    }
                }
            }

            // ── RIGHT: Divider + Theme toggle + SOS ──────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassVerticalDivider(isDark = isDark)
                GlassThemeToggle(isDark = isDark, onClick = { themeState.toggle() })
                GlassSosButton(onClick = onCyberSosClick)
            }
        }
    }
}

// =============================================================================
// LiveStatusDot — pulsing teal circle, signals app is actively protecting
// =============================================================================
@Composable
private fun LiveStatusDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "status_dot")
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 0.25f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(TealGlass.copy(alpha = alpha))
    )
}

// =============================================================================
// UserAvatar — glass style with teal ring border
// =============================================================================
@Composable
fun UserAvatar(
    name:     String,
    imageUrl: String?,
    isDark:   Boolean,
    size:     androidx.compose.ui.unit.Dp = 38.dp
) {
    val context = LocalContext.current
    val initial = name.trim().firstOrNull()?.uppercase() ?: "U"

    val ringColor    = TealGlass.copy(alpha = if (isDark) 0.55f else 0.40f)
    val avatarBg     = if (isDark)
        Brush.linearGradient(
            colors = listOf(TealGlass.copy(alpha = 0.18f), BlueGlass.copy(alpha = 0.15f))
        )
    else
        Brush.linearGradient(
            colors = listOf(TealGlass.copy(alpha = 0.12f), BlueGlass.copy(alpha = 0.10f))
        )

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(brush = avatarBg)
            .border(
                width = 1.5.dp,
                color = ringColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "User Avatar",
                modifier           = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text  = initial,
                color = TealGlass,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp
                )
            )
        }
    }
}

// =============================================================================
// GlassVerticalDivider — hairline, matches glass panel opacity
// =============================================================================
@Composable
private fun GlassVerticalDivider(isDark: Boolean) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(20.dp)
            .background(
                color = if (isDark)
                    Color.White.copy(alpha = 0.08f)
                else
                    Color.Black.copy(alpha = 0.08f),
                shape = RoundedCornerShape(1.dp)
            )
    )
}

// =============================================================================
// GlassThemeToggle — glass circle, teal tint in dark / neutral in light
// =============================================================================
@Composable
private fun GlassThemeToggle(
    isDark:  Boolean,
    onClick: () -> Unit
) {
    val iconTint = if (isDark) BlueGlass.copy(alpha = 0.85f) else Color(0xFF5C6B77)

    IconButton(
        onClick  = onClick,
        modifier = Modifier.size(34.dp)
    ) {
        Icon(
            imageVector        = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDark) "Switch to light mode" else "Switch to dark mode",
            tint               = iconTint,
            modifier           = Modifier.size(16.dp)
        )
    }
}

// =============================================================================
// GlassSosButton — full pill shape, red gradient, drop shadow glow
// No FilledTonalButton — plain Button to avoid tonal layer artifacts
// =============================================================================
@Composable
private fun GlassSosButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed           by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue   = if (pressed) 0.94f else 1.00f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "sos_scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Button(
            onClick           = onClick,
            modifier          = Modifier.height(34.dp),
            interactionSource = interactionSource,
            shape             = RoundedCornerShape(20.dp),   // Full pill
            colors            = ButtonDefaults.buttonColors(
                containerColor = SosRed,
                contentColor   = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
            elevation      = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Icon(
                imageVector        = Icons.Default.Warning,
                contentDescription = null,
                modifier           = Modifier.size(12.dp),
                tint               = Color.White
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text          = stringResource(R.string.ui_apptopbar_1),
                fontSize      = 12.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }
    }
}