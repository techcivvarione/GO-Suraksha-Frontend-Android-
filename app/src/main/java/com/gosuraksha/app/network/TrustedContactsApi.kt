package com.gosuraksha.app.network

import com.gosuraksha.app.trusted.model.*
import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import retrofit2.http.*

interface TrustedContactsApi {

    @GET("contacts/trusted/")
    suspend fun listTrustedContacts(): ApiResponse<TrustedContactsListResponse>

    @POST("contacts/trusted/")
    suspend fun addTrustedContact(
        @Body request: AddTrustedContactRequest
    ): ApiResponse<AddTrustedContactResponse>

    @POST("contacts/invite")
    suspend fun inviteTrustedContact(
        @Body request: InviteTrustedContactRequest
    ): ApiResponse<InviteTrustedContactResponse>

    @GET("contacts/pending")
    suspend fun getPendingInvites(): ApiResponse<PendingInvitesResponse>

    @POST("contacts/accept")
    suspend fun respondToInvite(
        @Body request: InviteActionRequest
    ): ApiResponse<InviteActionResponse>

    @DELETE("contacts/trusted/{contact_id}")
    suspend fun deleteTrustedContact(
        @Path("contact_id") contactId: String
    ): ApiResponse<DeleteTrustedContactResponse>

    @PATCH("contacts/trusted/{contact_id}/set-primary")
    suspend fun setPrimaryContact(
        @Path("contact_id") contactId: String
    ): ApiResponse<SetPrimaryContactResponse>

    @GET("trusted/alerts")
    suspend fun getTrustedAlerts(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ApiResponse<TrustedAlertsResponse>

    @GET("family/dashboard")
    suspend fun getFamilyDashboard(): ApiResponse<FamilyDashboardResponse>

    @GET("secure-now")
    suspend fun getSecureNow(): ApiResponse<SecureNowResponse>

    @POST("secure-now/{item_id}/complete")
    suspend fun completeSecureNow(
        @Path("item_id") itemId: String
    ): ApiResponse<CompleteSecureNowResponse>

    @POST("alerts/manual-trigger")
    suspend fun triggerManualAlert(
        @Body request: ManualAlertRequest
    ): ApiResponse<ManualAlertResponse>

    @GET("notifications")
    suspend fun getNotifications(): ApiResponse<NotificationFeedResponse>
}
