package com.gosuraksha.app.ui.call

import android.content.Context
import android.util.Log
import com.gosuraksha.app.BuildConfig

object ScamOverlayManager {
    private const val TAG = "CALL_SCAM"

    fun showOverlay(
        context: Context,
        phoneNumber: String,
        reportCount: Int = 0,
        category: String = "Scam Call"
    ) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Showing overlay")
        }
        show(context, phoneNumber, reportCount, category)
    }

    fun show(
        context: Context,
        phoneNumber: String,
        reportCount: Int,
        category: String
    ) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Overlay shown")
        }
        ScamWarningOverlayController.show(
            context = context,
            data = ScamWarningData(
                phoneNumber = phoneNumber,
                reportCount = reportCount,
                category = category
            )
        )
    }

    fun dismiss(context: Context) {
        ScamWarningOverlayController.dismiss(context)
    }
}
