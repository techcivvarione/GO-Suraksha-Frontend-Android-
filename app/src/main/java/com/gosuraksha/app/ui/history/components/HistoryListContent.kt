package com.gosuraksha.app.ui.history.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R
import com.gosuraksha.app.design.components.AppCard
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.history.model.HistoryItem
import com.gosuraksha.app.ui.history.model.HistoryListItem
import com.gosuraksha.app.ui.history.model.RiskLevel
import com.gosuraksha.app.ui.history.model.containerColor
import com.gosuraksha.app.ui.history.model.contentColor
import com.gosuraksha.app.ui.history.model.inferScanType
import com.gosuraksha.app.ui.history.model.label
import com.gosuraksha.app.ui.history.model.scoreBarFraction
import com.gosuraksha.app.ui.history.model.toFormattedTime
import com.gosuraksha.app.ui.history.model.toRiskLevel

@Composable
fun HistoryList(
    items: List<HistoryListItem>,
    onDelete: (HistoryItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { item ->
            when (item) {
                is HistoryListItem.Header -> "header_${item.label}"
                is HistoryListItem.Entry -> item.item.id
            }
        }) { listItem ->
            when (listItem) {
                is HistoryListItem.Header -> DateGroupHeader(label = listItem.label)
                is HistoryListItem.Entry -> {
                    var showDeleteDialog by remember { mutableStateOf(false) }

                    SignalFeedCard(
                        item = listItem.item,
                        onDeleteClick = { showDeleteDialog = true }
                    )

                    if (showDeleteDialog) {
                        DeleteConfirmDialog(
                            onConfirm = {
                                showDeleteDialog = false
                                onDelete(listItem.item)
                            },
                            onDismiss = { showDeleteDialog = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateGroupHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            color = ColorTokens.textSecondary(),
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
    )
}

@Composable
fun SignalFeedCard(
    item: HistoryItem,
    onDeleteClick: () -> Unit
) {
    val riskLevel = remember(item.risk) { item.risk.toRiskLevel() }
    val scanType = remember(item.input_text) { inferScanType(item.input_text) }
    val formattedTime = remember(item.created_at) { item.created_at.toFormattedTime() }
    var showMenu by remember { mutableStateOf(false) }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp),
        onClick = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 4.dp, top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(riskLevel.containerColor()),
                contentAlignment = Alignment.Center
            ) {
                Text(scanType.emoji, fontSize = 17.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scanType.label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = ColorTokens.textPrimary()
                    )
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ColorTokens.textSecondary()
                    )
                )
            }

            RiskStatusBadge(riskLevel = riskLevel)

            // Overflow menu anchor
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = ColorTokens.textSecondary(),
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = ColorTokens.error(),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    stringResource(R.string.ui_historyscreen_5),
                                    color = ColorTokens.error()
                                )
                            }
                        },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }

        Divider(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            color = ColorTokens.border().copy(alpha = 0.5f),
            thickness = 1.dp
        )

        Column(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Risk Score",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ColorTokens.textSecondary()
                    )
                )
                Text(
                    text = "${item.score}/100",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = riskLevel.contentColor()
                    )
                )
            }

            AnimatedScoreBar(score = item.score, riskLevel = riskLevel)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content preview — full width, no delete button cluttering the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ColorTokens.background())
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = item.input_text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = ColorTokens.textSecondary()
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RiskStatusBadge(riskLevel: RiskLevel) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(riskLevel.containerColor())
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (riskLevel == RiskLevel.THREAT) {
                PulsingDot(color = riskLevel.contentColor())
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(riskLevel.contentColor())
                )
            }
            Text(
                text = riskLevel.label(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = riskLevel.contentColor(),
                    letterSpacing = 0.4.sp
                )
            )
        }
    }
}

@Composable
fun PulsingDot(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
fun AnimatedScoreBar(score: Int, riskLevel: RiskLevel) {
    val animatedFraction by animateFloatAsState(
        targetValue = riskLevel.scoreBarFraction(score),
        animationSpec = tween(durationMillis = 600, easing = EaseOut),
        label = "score_bar"
    )

    val trackColor = ColorTokens.border().copy(alpha = 0.4f)
    val fillColor = riskLevel.contentColor()
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(fillColor, fillColor.copy(alpha = 0.7f))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedFraction)
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(gradientBrush)
        )
    }
}

@Composable
fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorTokens.surface(),
        title = {
            Text(
                "Delete Scan?",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ColorTokens.textPrimary()
                )
            )
        },
        text = {
            Text(
                "This scan record will be permanently removed from your history.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ColorTokens.textSecondary()
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = ColorTokens.error())
            ) {
                Text("Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = ColorTokens.textSecondary())
            ) {
                Text("Cancel")
            }
        }
    )
}
