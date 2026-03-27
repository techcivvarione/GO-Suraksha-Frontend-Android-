package com.gosuraksha.app.scan.components

// =============================================================================
// ScanCards.kt — Modern fintech-style scan cards
//
// ScanToolCard   — full-width horizontal card (used for QR + any single tool)
// ScanGridCard   — compact vertical card for 2-column hub grid
// EvidenceBlock  — evidence list inside result views (unchanged logic)
//
// Design: No left band. Clean surfaces, subtle borders, colored icon boxes,
//         spring press scale, light/dark adaptive via ScanTheme.
// =============================================================================

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.scan.design.ScanColors
import com.gosuraksha.app.scan.design.ScanShapes
import com.gosuraksha.app.scan.design.ScanTheme

// ─── Variant enum ─────────────────────────────────────────────────────────────
enum class ScanToolVariant { THREAT, EMAIL, PASSWORD, QR, DEEPFAKE }

// ─── Color helpers ────────────────────────────────────────────────────────────
private fun ScanColors.accentColor(v: ScanToolVariant) = when (v) {
    ScanToolVariant.THREAT   -> accentThreat
    ScanToolVariant.EMAIL    -> accentEmail
    ScanToolVariant.PASSWORD -> accentPassword
    ScanToolVariant.QR       -> accentQR
    ScanToolVariant.DEEPFAKE -> accentDeepfake
}
private fun ScanColors.iconBg(v: ScanToolVariant) = when (v) {
    ScanToolVariant.THREAT   -> accentThreatBg
    ScanToolVariant.EMAIL    -> accentEmailBg
    ScanToolVariant.PASSWORD -> accentPasswordBg
    ScanToolVariant.QR       -> accentQRBg
    ScanToolVariant.DEEPFAKE -> accentDeepfakeBg
}
private fun ScanColors.tagBg(v: ScanToolVariant) = when (v) {
    ScanToolVariant.THREAT   -> accentThreatTagBg
    ScanToolVariant.EMAIL    -> accentEmailTagBg
    ScanToolVariant.PASSWORD -> accentPasswordTagBg
    ScanToolVariant.QR       -> accentQRTagBg
    ScanToolVariant.DEEPFAKE -> accentDeepfakeTagBg
}
private fun ScanColors.tagText(v: ScanToolVariant) = when (v) {
    ScanToolVariant.THREAT   -> accentThreatText
    ScanToolVariant.EMAIL    -> accentEmailText
    ScanToolVariant.PASSWORD -> accentPasswordText
    ScanToolVariant.QR       -> accentQRText
    ScanToolVariant.DEEPFAKE -> accentDeepfakeText
}

// ─── ScanToolCard — full-width horizontal card ────────────────────────────────
@Composable
fun ScanToolCard(
    model:    ScanToolCardModel,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors    = ScanTheme.colors
    val spacing   = ScanTheme.spacing
    val typography = ScanTheme.typography
    val variant   = model.variant
    val accent    = colors.accentColor(variant)

    val interSrc  = remember { MutableInteractionSource() }
    val isPressed by interSrc.collectIsPressedAsState()
    val scale     by animateFloatAsState(
        targetValue   = if (isPressed) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.75f),
        label         = "tool_card_scale",
    )

    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(elevation = if (isPressed) 0.dp else 2.dp, shape = shape, ambientColor = accent.copy(alpha = 0.08f))
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
            .clickable(
                interactionSource = interSrc,
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ── Colored icon box ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(colors.iconBg(variant), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = model.icon,
                contentDescription = null,
                tint               = accent,
                modifier           = Modifier.size(22.dp),
            )
        }

        // ── Text block ────────────────────────────────────────────────────
        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text       = model.title,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = colors.textPrimary,
            )
            Text(
                text     = model.description,
                fontSize = 12.sp,
                color    = colors.textSecondary,
                maxLines = 2,
            )
            if (model.tags.isNotEmpty()) {
                Spacer(Modifier.height(5.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    model.tags.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(colors.tagBg(variant), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text       = tag,
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color      = colors.tagText(variant),
                            )
                        }
                    }
                }
            }
        }

        // ── Chevron ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(colors.surface2, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForwardIos, null,
                tint     = colors.textTertiary,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

// ─── ScanGridCard — compact vertical card for 2-column grid ──────────────────
@Composable
fun ScanGridCard(
    model:    ScanToolCardModel,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors  = ScanTheme.colors
    val variant = model.variant
    val accent  = colors.accentColor(variant)

    val interSrc  = remember { MutableInteractionSource() }
    val isPressed by interSrc.collectIsPressedAsState()
    val scale     by animateFloatAsState(
        targetValue   = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.75f),
        label         = "grid_card_scale",
    )

    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(elevation = if (isPressed) 0.dp else 1.5.dp, shape = shape, ambientColor = accent.copy(alpha = 0.06f))
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
            .clickable(
                interactionSource = interSrc,
                indication        = null,
                onClick           = onClick,
            )
            .padding(14.dp),
    ) {
        // ── Icon + arrow row ──────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(colors.iconBg(variant), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = model.icon,
                    contentDescription = null,
                    tint               = accent,
                    modifier           = Modifier.size(21.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(colors.surface2, RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForwardIos, null,
                    tint     = colors.textTertiary,
                    modifier = Modifier.size(10.dp),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Title + description ───────────────────────────────────────────
        Text(
            text       = model.title,
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color      = colors.textPrimary,
            maxLines   = 2,
            lineHeight = 17.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = model.description,
            fontSize   = 11.sp,
            color      = colors.textSecondary,
            maxLines   = 3,
            lineHeight = 15.sp,
        )

        // ── Tags ──────────────────────────────────────────────────────────
        if (model.tags.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                model.tags.take(2).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(colors.tagBg(variant), RoundedCornerShape(5.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text       = tag,
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = colors.tagText(variant),
                        )
                    }
                }
            }
        }
    }
}

// ─── EvidenceBlock — unchanged logic, minor polish ────────────────────────────
@Composable
fun EvidenceBlock(
    title:    String,
    items:    List<String>,
    tone:     ScanRiskTone,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val colors     = ScanTheme.colors
    val spacing    = ScanTheme.spacing
    val typography = ScanTheme.typography
    val bulletColor = tone.contentColor(colors)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, ScanShapes.cardExtraLarge)
            .border(1.dp, colors.border, ScanShapes.cardExtraLarge)
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Text(
            text  = title,
            style = typography.chipLabel,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(2.dp))
        items.forEachIndexed { index, item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment     = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .size(7.dp)
                        .background(bulletColor, CircleShape),
                )
                Text(
                    text     = item,
                    style    = typography.bodySmall,
                    color    = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
            }
            if (index < items.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.border)
                )
            }
        }
    }
}
