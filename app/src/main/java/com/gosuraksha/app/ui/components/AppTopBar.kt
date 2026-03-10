package com.gosuraksha.app.ui.components

// =============================================================================
// EnterpriseTopBar.kt — "Obsidian Glass" design
//
// Visual spec:
//   • Seamless transparent background — inherits screen bg, zero surface gap
//   • Logo carousel: GoSuraksha → GoSafe → GoSecure (2.5s, slide-up/fade)
//   • Right side: vertical divider · theme toggle (moon/sun icon) · SOS button
//   • Bottom: single-pixel gradient line (transparent → brand-blue → transparent)
//   • Dark  → brand-blue tint on icon btn, glow shadow on SOS
//   • Light → same structure, tones reduced, no glow
//
// Wiring:
//   • Theme toggle: LocalThemeState.current.toggle() — UNCHANGED
//   • SOS:         onCyberSosClick lambda — UNCHANGED
//   • Logo assets: R.drawable.logo / go_safe / go_secure — UNCHANGED
// =============================================================================

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.ui.theme.LocalThemeState
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
// Design constants — local to this file only
// ─────────────────────────────────────────────────────────────────────────────
private val BrandBlue     = Color(0xFF4F8AFF)
private val SosRed        = Color(0xFFEF4444)
private val TopBarHeight  = 64.dp
private val HorizontalPad = 20.dp

// =============================================================================
// Public entry-points — same signatures as before, fully backward-compatible
// =============================================================================

@Composable
fun AppTopBar(onCyberSosClick: () -> Unit) {
    EnterpriseTopBar(onCyberSosClick = onCyberSosClick)
}

@Composable
fun EnterpriseTopBar(onCyberSosClick: () -> Unit) {

    val themeState = LocalThemeState.current
    val isDark     = themeState.isDark

    // ── Logo carousel state ───────────────────────────────────────────────────
    val logos = remember {
        listOf(R.drawable.logo, R.drawable.go_safe, R.drawable.go_secure)
    }
    var currentLogo by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2_500)
            currentLogo = (currentLogo + 1) % logos.size
        }
    }

    // ── Gradient-line colors ──────────────────────────────────────────────────
    // Bottom separator: transparent → brand-blue (18%) → transparent
    val lineAlpha   = if (isDark) 0.18f else 0.10f
    val lineBrush   = Brush.horizontalGradient(
        colorStops = arrayOf(
            0.00f to Color.Transparent,
            0.35f to BrandBlue.copy(alpha = lineAlpha),
            0.65f to BrandBlue.copy(alpha = lineAlpha),
            1.00f to Color.Transparent
        )
    )

    // ── Root box ──────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(TopBarHeight)
            // Seamless: matches screen background exactly — no surface, no shadow
            .background(ColorTokens.background())
            // Draw bottom gradient line via Canvas (zero recomposition cost)
            .drawBehind {
                val lineY = size.height
                drawLine(
                    brush       = lineBrush,
                    start       = Offset(0f, lineY),
                    end         = Offset(size.width, lineY),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPad)
                .align(Alignment.Center),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // ── LEFT: Animated logo carousel ─────────────────────────────────
            // Hard-clip box: 18dp tall, 100dp wide max.
            // graphicsLayer scale brings the asset down without affecting layout.
            Box(
                modifier = Modifier
                    .height(18.dp)
                    .width(100.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                AnimatedContent(
                    targetState = logos[currentLogo],
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
                                // Scale down to 85% so asset internal whitespace doesn't inflate size
                                scaleX = 0.85f
                                scaleY = 0.85f
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                            },
                        tint               = Color.Unspecified
                    )
                }
            }

            // ── RIGHT: Divider + Theme toggle + SOS ──────────────────────────
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Vertical divider
                VerticalDivider(isDark = isDark)

                // Theme toggle icon button
                ThemeToggleButton(
                    isDark  = isDark,
                    onClick = { themeState.toggle() }
                )

                // CyberSOS button
                CyberSosButton(onClick = onCyberSosClick)
            }
        }
    }
}

// =============================================================================
// VerticalDivider
// 1dp × 20dp hairline, brand-blue tinted.
// =============================================================================
@Composable
private fun VerticalDivider(isDark: Boolean) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(20.dp)
            .background(
                color = if (isDark)
                    Color.White.copy(alpha = 0.10f)
                else
                    Color(0xFF0D0F1A).copy(alpha = 0.10f),
                shape = RoundedCornerShape(1.dp)
            )
    )
}

// =============================================================================
// ThemeToggleButton
// 36dp circular icon button.
// Dark  → brand-blue tinted background + brand-blue icon tint.
// Light → same structure, reduced opacity.
// =============================================================================
@Composable
private fun ThemeToggleButton(
    isDark: Boolean,
    onClick: () -> Unit
) {
    val bgColor   = if (isDark)
        BrandBlue.copy(alpha = 0.12f)
    else
        BrandBlue.copy(alpha = 0.08f)

    val iconTint  = if (isDark) BrandBlue else Color(0xFF2563EB)

    IconButton(
        onClick  = onClick,
        modifier = Modifier
            .size(36.dp)
            .background(color = bgColor, shape = CircleShape)
    ) {
        Icon(
            imageVector       = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDark) "Switch to light mode" else "Switch to dark mode",
            tint              = iconTint,
            modifier          = Modifier.size(18.dp)
        )
    }
}

// =============================================================================
// CyberSosButton
// Plain Button (NOT FilledTonalButton — that renders a tonal layer circle artifact).
// Press-spring scale animation only. No drawBehind glow (was bleeding red circle).
// =============================================================================
@Composable
private fun CyberSosButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue   = if (pressed) 0.94f else 1.00f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "sos_scale"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Button(
            onClick           = onClick,
            modifier          = Modifier.height(34.dp),
            interactionSource = interactionSource,
            shape             = RoundedCornerShape(10.dp),
            colors            = ButtonDefaults.buttonColors(
                containerColor = SosRed,
                contentColor   = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 13.dp, vertical = 0.dp),
            elevation      = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Icon(
                imageVector        = Icons.Default.Warning,
                contentDescription = null,
                modifier           = Modifier.size(13.dp),
                tint               = Color.White
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text          = stringResource(R.string.ui_apptopbar_1),
                fontSize      = 13.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
        }
    }
}