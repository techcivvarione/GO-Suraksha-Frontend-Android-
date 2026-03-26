package com.gosuraksha.app.services

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.call.ScamCallChecker
import com.gosuraksha.app.ui.call.ScamOverlayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GosurakshaCallScreeningService : CallScreeningService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "CallScreeningService triggered")
        }

        val incomingNumber = callDetails.handle?.schemeSpecificPart ?: "unknown"

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Incoming number detected")
        }
        respondToCall(callDetails, CallResponse.Builder().build())

        serviceScope.launch {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Checking backend")
            }
            val result = ScamCallChecker.checkNumber(
                number = incomingNumber,
                context = applicationContext,
                useVerifyCall = true
            )
            if (result?.suspicionLevel == "high") {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Showing overlay")
                }
                ScamOverlayManager.showOverlay(
                    applicationContext,
                    incomingNumber,
                    result.report_count_24h,
                    result.category ?: "Scam Call"
                )
            }
        }
    }

    private companion object {
        private const val TAG = "CALL_SCAM"
    }
}
