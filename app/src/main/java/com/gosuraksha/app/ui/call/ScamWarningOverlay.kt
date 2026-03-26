package com.gosuraksha.app.ui.call

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.TelecomManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.MainActivity
import com.gosuraksha.app.design.components.AppButton
import com.gosuraksha.app.design.components.AppCard
import com.gosuraksha.app.design.components.AppOutlinedButton
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.TypographyTokens
import com.gosuraksha.app.navigation.Screen
import com.gosuraksha.app.scam.ScamAlertNavigationStore
import com.gosuraksha.app.ui.theme.GOSurakshaTheme

data class ScamWarningData(
    val phoneNumber: String,
    val reportCount: Int,
    val category: String
)

object ScamWarningOverlayController {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlayView: View? = null

    fun show(context: Context, data: ScamWarningData) {
        mainHandler.post {
            dismiss(context)
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val composeView = ComposeView(context).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setContent {
                    GOSurakshaTheme {
                        ScamWarningOverlay(
                            data = data,
                            onIgnore = { dismiss(context) },
                            onReportScam = {
                                openReportScam(context)
                                dismiss(context)
                            },
                            onBlockNumber = {
                                openBlockedNumbers(context)
                                dismiss(context)
                            }
                        )
                    }
                }
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP
                x = 0
                y = 80
            }

            overlayView = composeView
            windowManager.addView(composeView, params)
        }
    }

    fun dismiss(context: Context) {
        mainHandler.post {
            val view = overlayView ?: return@post
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            runCatching { windowManager.removeView(view) }
            overlayView = null
        }
    }

    private fun openReportScam(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(ScamAlertNavigationStore.EXTRA_SCAM_ROUTE, Screen.ReportScam.route)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }

    private fun openBlockedNumbers(context: Context) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        val intent = telecomManager?.createManageBlockedNumbersIntent() ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

@Composable
fun ScamWarningOverlay(
    data: ScamWarningData,
    onIgnore: () -> Unit,
    onReportScam: () -> Unit,
    onBlockNumber: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4F2)),
            border = null
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = Color(0xFFC62828)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Possible Scam Call",
                        style = TypographyTokens.titleLarge,
                        color = Color(0xFF8E1B1B)
                    )
                }
                Text(
                    text = data.phoneNumber,
                    style = TypographyTokens.titleMedium,
                    color = ColorTokens.textPrimary()
                )
                Text(
                    text = "Reported by ${data.reportCount} users as suspicious",
                    style = TypographyTokens.bodyMedium,
                    color = ColorTokens.textPrimary()
                )
                Text(
                    text = "Category: ${data.category}",
                    style = TypographyTokens.bodySmall,
                    color = ColorTokens.textSecondary()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppOutlinedButton(
                        onClick = onIgnore,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ignore")
                    }
                    AppButton(
                        onClick = onReportScam,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Report Scam")
                    }
                    AppOutlinedButton(
                        onClick = onBlockNumber,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Block Number")
                    }
                }
            }
        }
    }
}
