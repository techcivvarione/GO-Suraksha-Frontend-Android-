package com.gosuraksha.app.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.GppBad
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.R
import com.gosuraksha.app.design.components.AppCard
import com.gosuraksha.app.risk.RiskViewModel
import com.gosuraksha.app.risk.RiskViewModelFactory
import com.gosuraksha.app.ui.components.localizedUiMessage
import com.gosuraksha.app.ui.motion.MotionSpec
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RiskScreen(
    viewModel: RiskViewModel = viewModel(
        factory = RiskViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val score by viewModel.score.collectAsStateWithLifecycle()
    val timeline by viewModel.timeline.collectAsStateWithLifecycle()
    val insights by viewModel.insights.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadScore()
        viewModel.loadTimeline()
        viewModel.loadInsights()
    }

    if (loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val latest = timeline.lastOrNull()
    val riskLevel = score?.risk_level ?: stringResource(R.string.profile_needs_attention)
    val riskScore = score?.score ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .background(Color.Transparent),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
            ),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(
                defaultElevation = 4.dp,
                pressedElevation = 4.dp
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp)
        ) {
            Text(
                text = stringResource(R.string.ui_riskscreen_2),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            RiskGauge(score = riskScore)

            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = riskLevel.uppercase(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            SignalsGrid(
                totalScans = score?.total_scans ?: 0,
                high = latest?.high ?: 0,
                medium = latest?.medium ?: 0,
                low = latest?.low ?: 0
            )
        }

        if (timeline.isNotEmpty()) {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.ui_riskscreen_7),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                timeline.takeLast(5).forEach { point ->
                    Text(
                        text = stringResource(
                            R.string.ui_riskscreen_8,
                            point.date,
                            point.score,
                            point.high,
                            point.medium,
                            point.low
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        insights?.let { summary ->
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.ui_riskscreen_9),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                summary.recommendations?.take(3)?.forEach { rec ->
                    Text(
                        text = stringResource(R.string.ui_riskscreen_15, rec),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        error?.let {
            Text(
                text = localizedUiMessage(it),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun RiskGauge(score: Int) {
    val progressTarget = (score.coerceIn(0, 100) / 100f)
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val gradientPrimary = MaterialTheme.colorScheme.primary
    val gradientSecondary = MaterialTheme.colorScheme.primaryContainer
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 700),
        label = "risk_gauge_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val stroke = 10.dp.toPx()
            drawArc(
                color = outlineColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(
                        gradientPrimary,
                        gradientSecondary
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                targetState = score,
                transitionSpec = {
                    fadeIn(animationSpec = tween(MotionSpec.NORMAL_MS)) togetherWith
                        fadeOut(animationSpec = tween(MotionSpec.NORMAL_MS))
                },
                label = "risk_score_value"
            ) { value ->
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stringResource(R.string.ui_riskscreen_1),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SignalsGrid(
    totalScans: Int,
    high: Int,
    medium: Int,
    low: Int
) {
    val signals = listOf(
        SignalItem(Icons.Rounded.DataUsage, stringResource(R.string.profile_stat_scans), totalScans.toString()),
        SignalItem(Icons.Rounded.GppBad, stringResource(R.string.scan_risk_high), high.toString()),
        SignalItem(Icons.Rounded.Bolt, stringResource(R.string.scan_risk_medium), medium.toString()),
        SignalItem(Icons.Rounded.Security, stringResource(R.string.scan_risk_low), low.toString())
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        signals.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    SignalMiniCard(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private data class SignalItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val value: String
)

@Composable
private fun SignalMiniCard(
    item: SignalItem,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedContent(
                    targetState = item.value,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(MotionSpec.NORMAL_MS)) togetherWith
                            fadeOut(animationSpec = tween(MotionSpec.NORMAL_MS))
                    },
                    label = "signal_value_${item.label}"
                ) { value ->
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


