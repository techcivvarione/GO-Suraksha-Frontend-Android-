package com.gosuraksha.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import com.gosuraksha.app.R

@Composable
fun GoogleAuthSection(
    isDark: Boolean,
    isLoading: Boolean,
    enabled: Boolean,
    onGoogleLogin: () -> Unit
) {
    OutlinedButton(
        onClick = onGoogleLogin,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(46.dp),
        shape = RoundedCornerShape(11.dp),
        border = BorderStroke(1.5.dp, if (isDark) DarkBorder else LightBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isDark) DarkSurfaceAlt else LightSurface,
            contentColor = if (isDark) DarkTextPri else LightTextPri,
            disabledContentColor = if (isDark) DarkTextTert else LightTextTert
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp, color = Green400)
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.login_google_loading), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        } else {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1E3D28) else Color(0xFFF0F0F0))
                    .border(1.dp, if (isDark) DarkBorder else Color(0xFFDDDDDD), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("G", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = if (isDark) Green400 else Color(0xFF555555))
            }
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.login_continue_with_google), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
