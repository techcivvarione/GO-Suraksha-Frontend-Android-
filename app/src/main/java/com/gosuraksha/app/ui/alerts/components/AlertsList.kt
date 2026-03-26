package com.gosuraksha.app.ui.alerts

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.R
import androidx.compose.ui.res.stringResource
import com.gosuraksha.app.alerts.model.AlertEvent
import com.gosuraksha.app.alerts.model.AlertsSummaryResponse
import com.gosuraksha.app.design.components.AppCard
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.ElevationTokens
import com.gosuraksha.app.design.tokens.ShapeTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.design.tokens.TypographyTokens
import com.gosuraksha.app.ui.components.localizedUiMessage
import java.util.Locale

@Composable
fun AlertsList(
    alerts: List<AlertEvent>,
    summary: AlertsSummaryResponse,       // non-nullable — ViewModel always provides a value
    loading: Boolean,
    summaryLoading: Boolean,              // STEP 3: drives shimmer skeleton on the summary card
    summaryFailed: Boolean,               // STEP 4: shows manual retry banner
    error: String?,
    onRetry: () -> Unit = {},
) {
    // STEP 6 — frontend debug log (DEBUG builds only)
    LaunchedEffect(summary) {
        android.util.Log.d(
            "ALERTS_SUMMARY",
            "UI received → total=${summary.total_alerts}  " +
            "high=${summary.high_risk}  medium=${summary.medium_risk}  " +
            "low=${summary.low_risk}  level=${summary.risk_level_today}"
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = SpacingTokens.screenPaddingHorizontal, vertical = SpacingTokens.md),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)
    ) {
        // STEP 3: show shimmer skeleton while summary is loading, real card once data arrives
        item {
            if (summaryLoading) {
                AlertsSummaryCardSkeleton()
            } else {
                AlertsSummaryCard(summary = summary)
            }
            Spacer(Modifier.height(SpacingTokens.xxs))
        }

        // STEP 4: manual retry banner when both auto-retry attempts have failed
        if (summaryFailed) {
            item { AlertsRetryBanner(onRetry = onRetry) }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(SpacingTokens.xxs).clip(CircleShape).background(ColorTokens.error()))
                Spacer(Modifier.width(SpacingTokens.xs))
                Text(stringResource(R.string.ui_alertsscreen_3), color = ColorTokens.textSecondary(), style = TypographyTokens.labelSmall)
            }
            Spacer(Modifier.height(SpacingTokens.xs))
        }

        if (loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = SpacingTokens.lg), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ColorTokens.accent())
                }
            }
        } else {
            if (alerts.isEmpty()) {
                item { AlertsEmptyState(Icons.Outlined.NotificationsOff, "No alerts right now. You are all clear.") }
            }
            items(alerts) { alert -> AlertCard(alert) }
            error?.let { item { AlertsErrorBanner(it) } }
        }

        item { Spacer(Modifier.height(SpacingTokens.xxxl)) }
    }
}

// ── STEP 3: Shimmer skeleton for the summary card ────────────────────────────

@Composable
private fun AlertsSummaryCardSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "summary_shimmer")
    val alpha = infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue  = 0.30f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmerAlpha",
    ).value
    val skeletonBase = ColorTokens.textSecondary().copy(alpha = alpha)

    AppCard(
        modifier  = Modifier.fillMaxWidth(),
        colors    = neutralCardColors(),
        border    = neutralCardBorder(),
        shape     = neutralCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.xs),
    ) {
        Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
            // Header bar placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(skeletonBase.copy(alpha = alpha * 0.6f))
            )
            // Three pill placeholders
            Row(
                modifier = Modifier.fillMaxWidth().padding(SpacingTokens.md),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                repeat(3) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(40.dp).height(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(skeletonBase)
                        )
                        Spacer(Modifier.height(SpacingTokens.xxs))
                        Box(
                            modifier = Modifier
                                .width(28.dp).height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(skeletonBase.copy(alpha = alpha * 0.5f))
                        )
                    }
                }
            }
        }
    }
}

// ── STEP 4: Manual retry banner ───────────────────────────────────────────────

@Composable
private fun AlertsRetryBanner(onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeTokens.cardCompact)
            .background(ColorTokens.warning().copy(alpha = 0.08f))
            .border(ShapeTokens.Border.thin, ColorTokens.warning().copy(alpha = 0.25f), ShapeTokens.cardCompact)
            .padding(SpacingTokens.sm)
            .clickable(remember { MutableInteractionSource() }, null, onClick = onRetry),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Refresh, null, tint = ColorTokens.warning(), modifier = Modifier.size(SpacingTokens.iconSizeSmall))
        Spacer(Modifier.width(SpacingTokens.xs))
        Text(
            "Couldn't load alerts. Tap to try again.",
            color = ColorTokens.warning(),
            style = TypographyTokens.bodySmall,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Summary card ──────────────────────────────────────────────────────────────

@Composable
private fun AlertsSummaryCard(summary: AlertsSummaryResponse) {
    val riskLevel = summary.risk_level_today.lowercase(Locale.getDefault())
    val riskColor = when (riskLevel) {
        "high"   -> ColorTokens.error()
        "medium" -> ColorTokens.warning()
        "low"    -> ColorTokens.success()
        else     -> ColorTokens.textSecondary()
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = neutralCardColors(),
        border = neutralCardBorder(),
        shape = neutralCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.xs)
    ) {
        Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(riskColor.copy(alpha = 0.08f))
                    .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.xs)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, null, tint = riskColor, modifier = Modifier.size(SpacingTokens.iconSizeSmall))
                    Spacer(Modifier.width(SpacingTokens.xs))
                    Text(stringResource(R.string.ui_alertsscreen_4), color = ColorTokens.textSecondary(), style = TypographyTokens.labelSmall)
                    Spacer(Modifier.weight(1f))
                    Box(modifier = Modifier.clip(ShapeTokens.badge).background(riskColor.copy(alpha = 0.12f)).padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs)) {
                        Text(summary.risk_level_today.uppercase(Locale.getDefault()), color = riskColor, style = TypographyTokens.labelSmall)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(SpacingTokens.md), horizontalArrangement = Arrangement.SpaceEvenly) {
                SummaryPill(stringResource(R.string.ui_alertsscreen_9), "${summary.high_risk}", ColorTokens.error())
                VerticalDivider()
                SummaryPill(stringResource(R.string.ui_alertsscreen_10), "${summary.medium_risk}", ColorTokens.warning())
                VerticalDivider()
                SummaryPill(stringResource(R.string.ui_alertsscreen_11), "${summary.low_risk}", ColorTokens.success())
            }
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(modifier = Modifier.width(ShapeTokens.Border.thin).height(SpacingTokens.lg).background(ColorTokens.border()))
}

@Composable
private fun SummaryPill(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, style = TypographyTokens.headlineSmall)
        Text(label, color = ColorTokens.textSecondary(), style = TypographyTokens.labelSmall)
    }
}

@Composable
private fun AlertCard(alert: AlertEvent) {
    LaunchedEffect(alert.id, alert.severity, alert.title, alert.description) {
        if (alert.severity.isNullOrBlank()) Log.w("GO_SURAKSHA_ALERTS", "Alert missing severity: ${alert.id}")
        if (alert.title.isNullOrBlank()) Log.w("GO_SURAKSHA_ALERTS", "Alert missing title: ${alert.id}")
        if (alert.description.isNullOrBlank()) Log.w("GO_SURAKSHA_ALERTS", "Alert missing description: ${alert.id}")
    }

    val title = alert.title?.takeIf { it.isNotBlank() } ?: "Security Alert"
    val description = alert.description?.takeIf { it.isNotBlank() } ?: "A security event was detected."
    val severityRaw = alert.severity?.takeIf { it.isNotBlank() } ?: alert.status?.takeIf { it.isNotBlank() } ?: "unknown"
    val severity = severityRaw.lowercase(Locale.getDefault())
    val alertType = alert.alert_type?.takeIf { it.isNotBlank() } ?: alert.analysis_type?.takeIf { it.isNotBlank() } ?: "security"
    val severityLabel = severity.uppercase(Locale.getDefault())
    val severityColor = when (severity) {
        "high" -> ColorTokens.error()
        "medium" -> ColorTokens.warning()
        "low" -> ColorTokens.success()
        else -> ColorTokens.textSecondary()
    }
    val severityIcon = when (severity) {
        "high" -> Icons.Filled.Warning
        "medium" -> Icons.Filled.Info
        "low" -> Icons.Filled.CheckCircle
        else -> Icons.Filled.Info
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = neutralCardColors(),
        border = neutralCardBorder(),
        shape = neutralCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.xs)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(SpacingTokens.md), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(SpacingTokens.iconSizeLarge).clip(ShapeTokens.cardCompact).background(severityColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(severityIcon, null, tint = severityColor, modifier = Modifier.size(SpacingTokens.iconSizeSmall))
            }
            Spacer(Modifier.width(SpacingTokens.sm))
            Column(Modifier.weight(1f)) {
                Text(title, color = ColorTokens.textPrimary(), style = TypographyTokens.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(SpacingTokens.xxs))
                Text(description, color = ColorTokens.textSecondary(), style = TypographyTokens.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                alert.risk_score?.let {
                    Spacer(Modifier.height(SpacingTokens.xxs))
                    Text("Risk score: $it", color = ColorTokens.textSecondary(), style = TypographyTokens.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(SpacingTokens.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.clip(ShapeTokens.badge).background(severityColor.copy(alpha = 0.12f)).padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs)) {
                        Text(severityLabel, color = severityColor, style = TypographyTokens.labelSmall)
                    }
                    Box(modifier = Modifier.clip(ShapeTokens.badge).background(ColorTokens.accent().copy(alpha = 0.1f)).padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs)) {
                        Text(alertType, color = ColorTokens.accent(), style = TypographyTokens.labelSmall)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(formatAlertDate(alert.created_at), color = ColorTokens.textSecondary(), style = TypographyTokens.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun AlertsEmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = SpacingTokens.xl), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(SpacingTokens.authLogoMedium).clip(CircleShape).background(ColorTokens.surfaceVariant()).border(ShapeTokens.Border.thin, ColorTokens.border(), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = ColorTokens.textSecondary(), modifier = Modifier.size(SpacingTokens.iconSizeLarge))
            }
            Spacer(Modifier.height(SpacingTokens.sm))
            Text(message, color = ColorTokens.textSecondary(), style = TypographyTokens.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AlertsErrorBanner(message: String) {
    Row(modifier = Modifier.fillMaxWidth().clip(ShapeTokens.cardCompact).background(ColorTokens.error().copy(alpha = 0.08f)).border(ShapeTokens.Border.thin, ColorTokens.error().copy(alpha = 0.2f), ShapeTokens.cardCompact).padding(SpacingTokens.sm), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Warning, null, tint = ColorTokens.error(), modifier = Modifier.size(SpacingTokens.iconSizeSmall))
        Spacer(Modifier.width(SpacingTokens.xs))
        Text(localizedUiMessage(message), color = ColorTokens.error(), style = TypographyTokens.bodySmall)
    }
}
