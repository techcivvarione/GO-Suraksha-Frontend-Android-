package com.gosuraksha.app.scam.model

import com.google.gson.annotations.SerializedName

data class ReportScamRequest(
    val phoneNumber: String? = null,
    val phishingLink: String? = null,
    val paymentId: String? = null,
    val categories: List<String>,
    val description: String,
    val screenshotUri: String? = null
)

data class ReportScamResponse(
    val status: String? = null,
    val reportId: String? = null,
    val message: String? = null
)

data class CheckNumberRequest(
    @SerializedName("phone_number")
    val phone_number: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null
) {
    val phoneNumber: String
        get() = phone_number
}

data class CheckNumberResponse(
    @SerializedName("phone_number")
    val phone_number: String? = null,
    @SerializedName("suspicion_level")
    val suspicion_level: String? = null,
    @SerializedName("report_count_24h")
    val report_count_24h: Int = 0,
    val lastReportedLabel: String? = null,
    val category: String? = null
) {
    val suspicionLevel: String
        get() = suspicion_level ?: "unknown"

    val phoneNumber: String?
        get() = phone_number

    val reportCount: Int
        get() = report_count_24h

    val suspicious: Boolean
        get() = suspicion_level.equals("high", ignoreCase = true)
}

data class ScamAlertCampaign(
    val id: String,
    val scamType: String,
    val reportCount: Int,
    val regionsAffected: List<String> = emptyList(),
    val explanation: String,
    val preventionTips: List<String> = emptyList(),
    val category: String? = null,
    val recentActivityTimeline: List<ScamActivityPoint> = emptyList()
)

data class ScamActivityPoint(
    val label: String,
    val value: Int
)

data class ScamAlertPageResponse(
    val items: List<ScamAlertCampaign> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = false
)

enum class ScamCategory(val label: String) {
    ScamCall("Scam Call"),
    PhishingLink("Phishing Link"),
    ScamSms("Scam SMS"),
    FraudPaymentRequest("Fraud Payment Request")
}
