package com.gosuraksha.app.ui.alerts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.ShapeTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.design.tokens.TypographyTokens

@Composable
fun AlertsTabs(
    selectedTab: Int,
    onTabChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.screenPaddingHorizontal)
            .clip(ShapeTokens.cardCompact)
            .background(ColorTokens.surface())
            .padding(SpacingTokens.xxs),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xxs)
    ) {
        alertsTabs().forEachIndexed { index, tab ->
            val isSelected = selectedTab == index
            val bgColor by animateColorAsState(
                if (isSelected) ColorTokens.accent().copy(alpha = 0.12f) else ColorTokens.transparent(),
                tween(200),
                label = "alerts_tab_bg_$index"
            )
            val textColor by animateColorAsState(
                if (isSelected) ColorTokens.accent() else ColorTokens.textSecondary(),
                tween(200),
                label = "alerts_tab_text_$index"
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(ShapeTokens.input)
                    .background(bgColor)
                    .clickable(remember { MutableInteractionSource() }, null) { onTabChange(index) }
                    .padding(vertical = SpacingTokens.xs),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(tab.icon, null, tint = textColor, modifier = Modifier.size(SpacingTokens.iconSizeSmall))
                Spacer(Modifier.width(SpacingTokens.xs))
                Text(tab.label, color = textColor, style = TypographyTokens.labelSmall)
            }
        }
    }
}
