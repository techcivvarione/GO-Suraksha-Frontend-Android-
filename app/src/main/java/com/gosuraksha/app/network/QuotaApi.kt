package com.gosuraksha.app.network

import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import retrofit2.http.GET

// =============================================================================
// QuotaApi.kt — /user/quota endpoint
// Returns the current user's scan usage vs plan limits for all scan types.
// =============================================================================

/**
 * Scan quota for the current billing period.
 * All fields default to 0 so the UI can handle partial responses gracefully.
 */
data class QuotaResponse(
    /** Total scans performed this period (text + QR combined). */
    val scan_used: Int = 0,
    /** Maximum scans allowed this period. */
    val scan_limit: Int = 50,
    /** AI image scans performed this period. */
    val image_scans_used: Int = 0,
    /** Maximum image scans allowed this period. */
    val image_scan_limit: Int = 5,
    /** Text / SMS / link scans this period. */
    val text_scans_used: Int = 0,
    /** Maximum text scans allowed. */
    val text_scan_limit: Int = 30,
    /** QR scans this period. */
    val qr_scans_used: Int = 0,
    /** Maximum QR scans allowed. */
    val qr_scan_limit: Int = 10,
    /** ISO-8601 date string when the quota resets. Nullable = unknown. */
    val reset_date: String? = null,
)

interface QuotaApi {
    @GET("/user/quota")
    suspend fun getQuota(): ApiResponse<QuotaResponse>
}
