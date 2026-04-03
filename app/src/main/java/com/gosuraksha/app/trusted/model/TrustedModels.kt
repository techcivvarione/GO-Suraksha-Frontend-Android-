package com.gosuraksha.app.trusted.model

data class TrustedContact(
    val id: String?,
    val name: String?,
    val email: String?,
    val phone: String?,
    val relationship: String?,
    val family_link_enabled: Boolean? = null,
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

data class InviteTrustedContactRequest(
    val name: String,
    val phone: String,
    val relationship: String? = null,
    val add_to_family: Boolean = true
)

data class InviteTrustedContactResponse(
    val status: String? = null,
    val invite_id: String? = null,
    val receiver_phone: String? = null
)

data class PendingInvite(
    val id: String,
    val status: String? = null,
    val created_at: String? = null,
    val contact_name: String? = null,
    val relationship: String? = null,
    val add_to_family: Boolean? = null,
    val sender_user_id: String? = null,
    val sender_name: String? = null,
    val sender_phone: String? = null
)

data class PendingInvitesResponse(
    val count: Int = 0,
    val invites: List<PendingInvite> = emptyList()
)

data class InviteActionRequest(
    val invite_id: String,
    val action: String,
    val add_to_family: Boolean? = null
)

data class InviteActionResponse(
    val status: String? = null,
    val invite_id: String? = null,
    val family_link_enabled: Boolean? = null
)

data class AddTrustedContactResponse(
    val status: String?
)

data class DeleteTrustedContactResponse(
    val status: String?
)

data class SetPrimaryContactResponse(
    val status: String? = null
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

data class FamilyProtectionCapabilities(
    val plan: String? = null,
    val trusted_contacts_enabled: Boolean = false,
    val trusted_contacts_limit: Int = 0,
    val alerts_enabled: Boolean = false,
    val manual_alerts_enabled: Boolean = false,
    val auto_alerts_enabled: Boolean = false,
    val family_dashboard_enabled: Boolean = false,
    val secure_now_enabled: Boolean = false,
    val cyber_sos_enabled: Boolean = false,
    val family_mode: String? = null
)

data class FamilyRiskSummary(
    val high: Int = 0,
    val medium: Int = 0,
    val low: Int = 0
)

data class FamilyMemberDashboardItem(
    val user_id: String,
    val name: String? = null,
    val email: String? = null,
    val phone_number: String? = null,
    val security_score: Int = 0,
    val risk_summary: FamilyRiskSummary = FamilyRiskSummary(),
    val total_scans: Int = 0,
    val pending_secure_now: Int = 0,
    val last_scan_at: String? = null,
    val recent_alerts: List<TrustedAlert> = emptyList()
)

data class FamilyHeadSummary(
    val user_id: String,
    val name: String? = null
)

data class FamilyDashboardResponse(
    val capabilities: FamilyProtectionCapabilities? = null,
    val family_head: FamilyHeadSummary? = null,
    val members_count: Int = 0,
    val members: List<FamilyMemberDashboardItem> = emptyList(),
    val pending_invites_count: Int = 0,
    val mode: String? = null
)

data class SecureNowItem(
    val id: String,
    val type: String? = null,
    val title: String,
    val description: String,
    val status: String? = null,
    val risk_level: String? = null,
    val created_at: String? = null,
    val completed_at: String? = null,
    val read_only: Boolean = false,
    val owner_name: String? = null,
    val owner_user_id: String? = null
)

data class SecureNowCounts(
    val own_pending: Int = 0,
    val family_pending: Int = 0
)

data class SecureNowResponse(
    val capabilities: FamilyProtectionCapabilities? = null,
    val own_items: List<SecureNowItem> = emptyList(),
    val family_items: List<SecureNowItem> = emptyList(),
    val counts: SecureNowCounts = SecureNowCounts()
)

data class CompleteSecureNowResponse(
    val status: String? = null,
    val item_id: String? = null
)

data class ManualAlertRequest(
    val title: String = "Manual Protection Alert",
    val message: String = "A high-risk cyber event needs attention.",
    val risk_score: Int = 90,
    val alert_type: String = "MANUAL_HIGH_RISK_ALERT"
)

data class ManualAlertResponse(
    val status: String? = null,
    val message: String? = null
)

data class NotificationInvite(
    val id: String,
    val status: String? = null,
    val created_at: String? = null,
    val contact_name: String? = null,
    val relationship: String? = null,
    val sender_name: String? = null,
    val sender_phone: String? = null
)

data class NotificationAlert(
    val id: String,
    val source: String? = null,
    val alert_type: String? = null,
    val risk_level: String? = null,
    val risk_score: Int = 0,
    val created_at: String? = null,
    val member_name: String? = null,
    val message: String? = null
)

data class NotificationSystemEvent(
    val id: String,
    val type: String? = null,
    val title: String,
    val description: String,
    val status: String? = null,
    val risk_level: String? = null,
    val created_at: String? = null
)

data class NotificationFeedResponse(
    val invites: List<NotificationInvite> = emptyList(),
    val alerts: List<NotificationAlert> = emptyList(),
    val system_events: List<NotificationSystemEvent> = emptyList(),
    val unread_count: Int = 0
)
