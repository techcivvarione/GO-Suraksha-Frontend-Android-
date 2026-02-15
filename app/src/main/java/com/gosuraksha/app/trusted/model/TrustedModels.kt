package com.gosuraksha.app.trusted.model

data class TrustedContact(
    val id: String?,
    val contact_name: String?,
    val contact_email: String?,
    val contact_phone: String?,
    val status: String?,
    val created_at: String?
)

data class TrustedContactsListResponse(
    val count: Int?,
    val data: List<TrustedContact>?
)

data class AddTrustedContactRequest(
    val contact_name: String,
    val contact_email: String?,
    val contact_phone: String?
)

data class AddTrustedContactResponse(
    val status: String?
)

data class DeleteTrustedContactResponse(
    val status: String?
)

data class TrustedAlertsResponse(
    val count: Int?,
    val alerts: List<TrustedAlert>?
)

data class TrustedAlert(
    val id: String?,
    val message: String?,
    val created_at: String?
)
