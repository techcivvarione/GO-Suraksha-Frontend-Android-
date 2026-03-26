package com.gosuraksha.app.network

import com.gosuraksha.app.trusted.model.*
import retrofit2.http.*

interface TrustedContactsApi {

    @GET("contacts/trusted/")
    suspend fun listTrustedContacts(): TrustedContactsListResponse

    @POST("contacts/trusted/")
    suspend fun addTrustedContact(
        @Body request: AddTrustedContactRequest
    ): AddTrustedContactResponse

    @DELETE("contacts/trusted/{contact_id}")
    suspend fun deleteTrustedContact(
        @Path("contact_id") contactId: String
    ): DeleteTrustedContactResponse

    @GET("trusted/alerts")
    suspend fun getTrustedAlerts(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): TrustedAlertsResponse
}
