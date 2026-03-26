package com.gosuraksha.app.ui.signup.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.ui.components.localizedUiMessage

@Composable
fun SignupStates(errorMessage: String?) {
    AnimatedVisibility(
        visible = errorMessage != null,
        enter = fadeIn(tween(250)) + expandVertically(tween(250)),
        exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEF4444).copy(alpha = 0.10f))
                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.Cancel, null, tint = Color(0xFFEF4444))
            Text(localizedUiMessage(errorMessage.orEmpty()), fontSize = 11.sp, color = Color(0xFFEF4444))
        }
    }
}
