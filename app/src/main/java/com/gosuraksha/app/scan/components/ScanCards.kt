package com.gosuraksha.app.scan.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.scan.design.ScanColors
import com.gosuraksha.app.scan.design.ScanShapes
import com.gosuraksha.app.scan.design.ScanTheme
enum class ScanToolVariant { THREAT, EMAIL, PASSWORD, QR, DEEPFAKE }

// ─── Color helpers per variant ────────────────────────────────────────────────
private fun ScanColors.bandColor(v: ScanToolVariant) = when (v) {
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

private fun ScanColors.iconTint(v: ScanToolVariant) = when (v) {
    ScanToolVariant.THREAT   -> accentThreat
    ScanToolVariant.EMAIL    -> accentEmail
    ScanToolVariant.PASSWORD -> accentPassword
    ScanToolVariant.QR       -> accentQR
    ScanToolVariant.DEEPFAKE -> accentDeepfake
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

// ─── ScanToolCard ─────────────────────────────────────────────────────────────
@Composable
fun ScanToolCard(
    model: ScanToolCardModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors     = ScanTheme.colors
    val spacing    = ScanTheme.spacing
    val typography = ScanTheme.typography
    val variant    = model.variant

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.985f else 1f,
        animationSpec = spring(stiffness = 800f, dampingRatio = 0.8f),
        label         = "card_scale",
    )

    val cardShape = RoundedCornerShape(20.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(cardShape)
            .background(colors.surface)
            .border(1.dp, colors.border, cardShape)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Colored left band
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(96.dp)
                .background(colors.bandColor(variant))
        )

        // Card content
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.md, vertical = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(colors.iconBg(variant), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = model.icon,
                    contentDescription = null,
                    tint               = colors.iconTint(variant),
                    modifier           = Modifier.size(22.dp),
                )
            }

            // Text + tags
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = model.title,
                    style = typography.cardTitle,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = model.description,
                    style = typography.bodySmall,
                    color = colors.textSecondary,
                )
                if (model.tags.isNotEmpty()) {
                    Spacer(Modifier.height(spacing.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        model.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(colors.tagBg(variant), RoundedCornerShape(7.dp))
                                    .padding(horizontal = 9.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text  = tag,
                                    style = typography.chipLabel,
                                    color = colors.tagText(variant),
                                )
                            }
                        }
                    }
                }
            }

            // Chevron
            Icon(
                imageVector        = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint               = colors.textTertiary,
                modifier           = Modifier.size(13.dp),
            )
        }
    }
}

// ─── ScanGridCard ──────────────────────────────────────────────────────────────
// Compact vertical-layout card for the 2-column scan-hub grid.
// Uses the same private color helpers as ScanToolCard (same file).
@Composable
fun ScanGridCard(
    model:    ScanToolCardModel,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors     = ScanTheme.colors
    val typography = ScanTheme.typography
    val variant    = model.variant

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed         by interactionSource.collectIsPressedAsState()
    val scale             by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 800f, dampingRatio = 0.8f),
        label         = "grid_card_scale",
    )

    Column(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 16.dp),
    ) {
        // Accent top band
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(colors.bandColor(variant), RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.height(12.dp))

        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(colors.iconBg(variant), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = model.icon,
                contentDescription = null,
                tint               = colors.iconTint(variant),
                modifier           = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.height(10.dp))

        Text(
            text     = model.title,
            style    = typography.cardTitle,
            color    = colors.textPrimary,
            maxLines = 2,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text     = model.description,
            style    = typography.bodySmall,
            color    = colors.textSecondary,
            maxLines = 3,
        )

        if (model.tags.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                model.tags.take(2).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(colors.tagBg(variant), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text  = tag,
                            style = typography.chipLabel,
                            color = colors.tagText(variant),
                        )
                    }
                }
            }
        }
    }
}

// ─── EvidenceBlock ────────────────────────────────────────────────────────────
@Composable
fun EvidenceBlock(
    title: String,
    items: List<String>,
    tone: ScanRiskTone,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val colors      = ScanTheme.colors
    val spacing     = ScanTheme.spacing
    val typography  = ScanTheme.typography
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