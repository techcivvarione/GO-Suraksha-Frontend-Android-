package com.gosuraksha.app.data.repository

import com.gosuraksha.app.network.TrustedContactsApi
import com.gosuraksha.app.trusted.model.AddTrustedContactRequest
import com.gosuraksha.app.trusted.model.AddTrustedContactResponse
import com.gosuraksha.app.trusted.model.CompleteSecureNowResponse
import com.gosuraksha.app.trusted.model.DeleteTrustedContactResponse
import com.gosuraksha.app.trusted.model.FamilyDashboardResponse
import com.gosuraksha.app.trusted.model.InviteActionRequest
import com.gosuraksha.app.trusted.model.InviteActionResponse
import com.gosuraksha.app.trusted.model.InviteTrustedContactRequest
import com.gosuraksha.app.trusted.model.InviteTrustedContactResponse
import com.gosuraksha.app.trusted.model.ManualAlertRequest
import com.gosuraksha.app.trusted.model.ManualAlertResponse
import com.gosuraksha.app.trusted.model.NotificationFeedResponse
import com.gosuraksha.app.trusted.model.PendingInvitesResponse
import com.gosuraksha.app.trusted.model.SecureNowResponse
import com.gosuraksha.app.trusted.model.SetPrimaryContactResponse
import com.gosuraksha.app.trusted.model.TrustedAlertsResponse
import com.gosuraksha.app.trusted.model.TrustedContactsListResponse

class TrustedContactsRepository(
    private val api: TrustedContactsApi
) {
    suspend fun listTrustedContacts(): TrustedContactsListResponse = api.listTrustedContacts().data

    suspend fun addTrustedContact(request: AddTrustedContactRequest): AddTrustedContactResponse {
        return api.addTrustedContact(request).data
    }

    suspend fun inviteTrustedContact(request: InviteTrustedContactRequest): InviteTrustedContactResponse {
        return api.inviteTrustedContact(request).data
    }

    suspend fun getPendingInvites(): PendingInvitesResponse = api.getPendingInvites().data

    suspend fun respondToInvite(request: InviteActionRequest): InviteActionResponse {
        return api.respondToInvite(request).data
    }

    suspend fun deleteTrustedContact(id: String): DeleteTrustedContactResponse {
        return api.deleteTrustedContact(id).data
    }

    suspend fun getTrustedAlerts(limit: Int = 20, offset: Int = 0): TrustedAlertsResponse {
        return api.getTrustedAlerts(limit, offset).data
    }

    suspend fun setPrimaryContact(id: String): SetPrimaryContactResponse = api.setPrimaryContact(id).data

    suspend fun getFamilyDashboard(): FamilyDashboardResponse = api.getFamilyDashboard().data

    suspend fun getSecureNow(): SecureNowResponse = api.getSecureNow().data

    suspend fun completeSecureNow(itemId: String): CompleteSecureNowResponse {
        return api.completeSecureNow(itemId).data
    }

    suspend fun triggerManualAlert(request: ManualAlertRequest): ManualAlertResponse {
        return api.triggerManualAlert(request).data
    }

    suspend fun getNotifications(): NotificationFeedResponse = api.getNotifications().data
}
