package com.gosuraksha.app.call

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.scam.model.CheckNumberRequest
import com.gosuraksha.app.scam.model.CheckNumberResponse
import com.gosuraksha.app.scam.location.ScamLookupLocationProvider
import com.gosuraksha.app.ui.call.ScamWarningData
import com.gosuraksha.app.ui.call.ScamWarningOverlayController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object ScamCallChecker {
    private const val TAG = "CALL_SCAM"
    private const val DEBOUNCE_MS = 10_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lastCheckedAt = ConcurrentHashMap<String, Long>()

    suspend fun checkNumber(
        number: String,
        context: Context? = null,
        useVerifyCall: Boolean = false
    ): CheckNumberResponse? {
        val normalized = number.trim()
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Checking backend")
        }
        val location = context?.let { ScamLookupLocationProvider.getLastKnownLocation(it.applicationContext) }
        return runCatching {
            val request = CheckNumberRequest(
                phone_number = normalized,
                lat = location?.lat,
                lng = location?.lng,
                city = location?.city,
                state = location?.state,
                country = location?.country
            )
            if (useVerifyCall) {
                ApiClient.scamNetworkApi.verifyCall(request)
            } else {
                ApiClient.scamNetworkApi.checkNumber(request)
            }
        }.onFailure { error ->
            Log.e(TAG, "Failed to check incoming call number", error)
        }.getOrNull()
    }

    fun check(context: Context, number: String) {
        val normalized = number.trim()
        val now = System.currentTimeMillis()
        val lastChecked = lastCheckedAt[normalized]
        if (lastChecked != null && now - lastChecked < DEBOUNCE_MS) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Skipping duplicate incoming call lookup")
            }
            return
        }

        lastCheckedAt[normalized] = now
        scope.launch {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Checking number")
            }
            val response = checkNumber(normalized, context.applicationContext, useVerifyCall = true)
            if (response != null) {
                if (response.suspicion_level.equals("high", ignoreCase = true)) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Warning triggered")
                    }
                    if (!Settings.canDrawOverlays(context)) {
                        Log.w(TAG, "SYSTEM_ALERT_WINDOW not granted; overlay not shown")
                        return@launch
                    }
                    ScamWarningOverlayController.show(
                        context = context.applicationContext,
                        data = ScamWarningData(
                            phoneNumber = response.phone_number ?: normalized,
                            reportCount = response.report_count_24h,
                            category = response.category ?: "OTP Fraud"
                        )
                    )
                }
            }
        }
    }
}
