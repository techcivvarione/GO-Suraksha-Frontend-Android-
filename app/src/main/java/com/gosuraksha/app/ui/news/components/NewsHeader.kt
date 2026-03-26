package com.gosuraksha.app.ui.news

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R

@Composable
fun NewsHeader(
    categories: List<NewsCategory>,
    selected: NewsCategory,
    onSelect: (NewsCategory) -> Unit,
    isDark: Boolean
) {
    Spacer(Modifier.padding(top = 12.dp))
    AuroraCategoryBar(
        categories = categories,
        selected = selected,
        onSelect = onSelect,
        isDark = isDark
    )
}

@Composable
fun AuroraOrbLayer(orb1Color: Color, orb2Color: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .blur(radius = 80.dp)
                .background(brush = Brush.radialGradient(listOf(orb1Color, Color.Transparent)), shape = CircleShape)
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
                .blur(radius = 70.dp)
                .background(brush = Brush.radialGradient(listOf(orb2Color, Color.Transparent)), shape = CircleShape)
        )
    }
}

@Composable
private fun AuroraCategoryBar(
    categories: List<NewsCategory>,
    selected: NewsCategory,
    onSelect: (NewsCategory) -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selected
            val bgColor by animateColorAsState(
                targetValue = when {
                    isSelected && isDark -> AuroraViolet.copy(alpha = 0.28f)
                    isSelected -> AuroraViolet.copy(alpha = 0.18f)
                    isDark -> Color.White.copy(alpha = 0.06f)
                    else -> Color.Black.copy(alpha = 0.05f)
                },
                animationSpec = tween(200),
                label = "cat_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200),
                label = "cat_text"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) AuroraViolet.copy(alpha = 0.6f) else Color.Transparent,
                animationSpec = tween(200),
                label = "cat_border"
            )
            val label = when (category) {
                NewsCategory.ALL -> stringResource(R.string.news_category_all)
                NewsCategory.AI -> stringResource(R.string.news_category_ai)
                NewsCategory.CYBER -> stringResource(R.string.news_category_cyber)
                NewsCategory.TECH -> stringResource(R.string.news_category_tech)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(50.dp))
                    .clickable { onSelect(category) }
                    .then(
                        if (isSelected) Modifier.background(
                            Brush.linearGradient(
                                listOf(AuroraViolet.copy(alpha = 0.25f), AuroraBlue.copy(alpha = 0.20f))
                            ),
                            RoundedCornerShape(50.dp)
                        ) else Modifier
                    )
            ) {
                Text(
                    text = label,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                )
            }
        }
    }
}
