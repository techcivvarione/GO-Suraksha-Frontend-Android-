package com.gosuraksha.app.trusted.model

data class TrustedContact(
    val id: String?,
    val name: String?,
    val email: String?,
    val phone: String?,
    val relationship: String?,
    val is_primary: Boolean?,
    val status: String?,
    val created_at: String?
)

data class TrustedContactsListResponse(
    val count: Int?,
    val data: List<TrustedContact>?
)

data class AddTrustedContactRequest(
    val name: String,
    val email: String?,
    val phone: String?
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
