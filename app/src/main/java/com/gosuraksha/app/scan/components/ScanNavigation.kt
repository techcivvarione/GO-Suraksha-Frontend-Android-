package com.gosuraksha.app.scan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.scan.design.ScanShapes
import com.gosuraksha.app.scan.design.ScanTheme

@Immutable
data class ScanNavItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun BottomNavigationBar(
    items: List<ScanNavItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors     = ScanTheme.colors
    val spacing    = ScanTheme.spacing
    val typography = ScanTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, ScanShapes.screen)
            .padding(horizontal = spacing.sm, vertical = spacing.xs),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val selected = item.key == selectedKey
            Surface(
                onClick = { onSelect(item.key) },
                shape   = CircleShape,
                color   = if (selected) colors.blueTint else colors.surface,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector  = item.icon,
                        contentDescription = null,
                        tint         = if (selected) colors.primaryBlue else colors.textTertiary,
                    )
                    if (selected) {
                        Text(
                            text  = item.label,
                            style = typography.chipLabel,
                            color = colors.primaryBlue,
                        )
                    }
                }
            }
        }
    }
}