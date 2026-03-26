package com.gosuraksha.app.data.repository

import com.gosuraksha.app.network.TrustedContactsApi
import com.gosuraksha.app.trusted.model.AddTrustedContactRequest
import com.gosuraksha.app.trusted.model.AddTrustedContactResponse
import com.gosuraksha.app.trusted.model.DeleteTrustedContactResponse
import com.gosuraksha.app.trusted.model.TrustedAlertsResponse
import com.gosuraksha.app.trusted.model.TrustedContactsListResponse

class TrustedContactsRepository(
    private val api: TrustedContactsApi
) {
    suspend fun listTrustedContacts(): TrustedContactsListResponse = api.listTrustedContacts()

    suspend fun addTrustedContact(request: AddTrustedContactRequest): AddTrustedContactResponse {
        return api.addTrustedContact(request)
    }

    suspend fun deleteTrustedContact(id: String): DeleteTrustedContactResponse {
        return api.deleteTrustedContact(id)
    }

    suspend fun getTrustedAlerts(limit: Int = 20, offset: Int = 0): TrustedAlertsResponse {
        return api.getTrustedAlerts(limit, offset)
    }
}
