package com.gosuraksha.app.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gosuraksha.app.R
import com.gosuraksha.app.design.components.AppOutlinedButton
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.design.tokens.TypographyTokens

@Composable
fun AlertsHeader(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        ColorTokens.surfaceVariant().copy(alpha = 0.6f),
                        ColorTokens.background()
                    )
                )
            )
            .padding(horizontal = SpacingTokens.screenPaddingHorizontal, vertical = SpacingTokens.md)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.ui_alertsscreen_1), color = ColorTokens.textPrimary(), style = TypographyTokens.screenTitle)
                Text(stringResource(R.string.ui_alertsscreen_2), color = ColorTokens.textSecondary(), style = TypographyTokens.bodySmall)
            }
            AppOutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.size(SpacingTokens.minTouchTarget)
            ) {
                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(SpacingTokens.iconSizeSmall))
            }
        }
        Spacer(Modifier.height(SpacingTokens.sm))
    }
}
