package com.gosuraksha.app.design.layouts

import com.gosuraksha.app.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gosuraksha.app.design.components.AppCard
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.design.tokens.TypographyTokens

/**
 * GO SURAKSHA - SCREEN LAYOUT TEMPLATES
 *
 * Reusable screen structure templates following PhonePe/UMANG patterns.
 * All screens should use these templates for consistency.
 *
 * DESIGN PRINCIPLES:
 * - Consistent screen structure
 * - Clear visual hierarchy
 * - Predictable spacing
 * - Optimized for vertical scrolling
 * - Clean, white backgrounds
 */

object ScreenLayouts {

    // ═══════════════════════════════════════════════════════════════
    // STANDARD SCREEN LAYOUT (with top bar)
    // ═══════════════════════════════════════════════════════════════

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun StandardScreen(
        title: String,
        onBackClick: (() -> Unit)? = null,
        actions: @Composable RowScope.() -> Unit = {},
        floatingActionButton: @Composable () -> Unit = {},
        content: @Composable (PaddingValues) -> Unit
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = TypographyTokens.screenTitle
                        )
                    },
                    navigationIcon = {
                        if (onBackClick != null) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = actions,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ColorTokens.surface(),
                        titleContentColor = ColorTokens.textPrimary()
                    )
                )
            },
            floatingActionButton = floatingActionButton,
            containerColor = ColorTokens.background()
        ) { padding ->
            content(padding)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SCROLLABLE SCREEN LAYOUT (LazyColumn)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun ScrollableScreen(
        title: String,
        onBackClick: (() -> Unit)? = null,
        actions: @Composable RowScope.() -> Unit = {},
        content: LazyListScope.() -> Unit
    ) {
        StandardScreen(
            title = title,
            onBackClick = onBackClick,
            actions = actions
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    horizontal = SpacingTokens.screenPaddingHorizontal,
                    vertical = SpacingTokens.screenPaddingVertical
                ),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.cardSpacing)
            ) {
                content()
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CENTERED CONTENT SCREEN (for empty states, loading, errors)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun CenteredContentScreen(
        title: String,
        onBackClick: (() -> Unit)? = null,
        content: @Composable BoxScope.() -> Unit
    ) {
        StandardScreen(
            title = title,
            onBackClick = onBackClick
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION HEADER (for screen sections)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun SectionHeader(
        title: String,
        modifier: Modifier = Modifier,
        action: @Composable (() -> Unit)? = null
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TypographyTokens.sectionHeader,
                color = ColorTokens.textPrimary()
            )

            if (action != null) {
                action()
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // LOADING STATE
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun LoadingState(
        message: String = "Loading..."
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md)
        ) {
            CircularProgressIndicator(
                color = ColorTokens.accent()
            )
            Text(
                text = message,
                style = TypographyTokens.bodyMedium,
                color = ColorTokens.textSecondary()
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EMPTY STATE
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun EmptyState(
        icon: @Composable () -> Unit,
        title: String,
        description: String? = null,
        action: @Composable (() -> Unit)? = null
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md)
        ) {
            icon()

            Text(
                text = title,
                style = TypographyTokens.titleLarge,
                color = ColorTokens.textPrimary()
            )

            if (description != null) {
                Text(
                    text = description,
                    style = TypographyTokens.bodyMedium,
                    color = ColorTokens.textSecondary()
                )
            }

            if (action != null) {
                Spacer(modifier = Modifier.height(SpacingTokens.md))
                action()
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ERROR STATE
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun ErrorState(
        message: String,
        onRetry: (() -> Unit)? = null
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = ColorTokens.error(),
                modifier = Modifier.size(SpacingTokens.iconSizeLarge)
            )

            Text(
                text = stringResource(R.string.ui_screenlayouts_3),
                style = TypographyTokens.titleLarge,
                color = ColorTokens.textPrimary()
            )

            Text(
                text = message,
                style = TypographyTokens.bodyMedium,
                color = ColorTokens.textSecondary()
            )

            if (onRetry != null) {
                Spacer(modifier = Modifier.height(SpacingTokens.md))
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.ui_screenlayouts_1))
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CARD SECTION (reusable card with header)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun CardSection(
        title: String? = null,
        action: @Composable (() -> Unit)? = null,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sectionHeader)
        ) {
            if (title != null) {
                SectionHeader(
                    title = title,
                    action = action
                )
            }

            AppCard(
                colors = CardDefaults.cardColors(
                    containerColor = ColorTokens.surface()
                ),
                contentPadding = PaddingValues(SpacingTokens.cardPadding)
            ) {
                content()
            }
        }
    }
}

/**
 * USAGE EXAMPLES:
 *
 * // Standard screen with scrollable content
 * @Composable
 * fun MyScreen() {
 *     ScreenLayouts.ScrollableScreen(
 *         title = stringResource(R.string.ui_screenlayouts_4),
 *         onBackClick = { navController.popBackStack() }
 *     ) {
 *         item {
 *             ScreenLayouts.SectionHeader("Status")
 *         }
 *         item {
 *             Card { ... }
 *         }
 *     }
 * }
 *
 * // Loading state
 * @Composable
 * fun MyScreen() {
 *     ScreenLayouts.CenteredContentScreen(title = stringResource(R.string.ui_screenlayouts_5)) {
 *         ScreenLayouts.LoadingState("Loading security data...")
 *     }
 * }
 *
 * // Empty state
 * @Composable
 * fun EmptyAlerts() {
 *     ScreenLayouts.EmptyState(
 *         icon = { Icon(...) },
 *         title = stringResource(R.string.ui_screenlayouts_6),
 *         description = "You're all caught up!",
 *         action = {
 *             Button(onClick = { }) {
 *                 Text(stringResource(R.string.ui_screenlayouts_2))
 *             }
 *         }
 *     )
 * }
 */
