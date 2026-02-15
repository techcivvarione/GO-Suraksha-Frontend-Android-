package com.gosuraksha.app.data.remote.dto

data class CyberSosRequest(
    val scam_type: String,
    val incident_date: String,
    val description: String,
    val loss_amount: String? = null,
    val source: String? = null
)

data class CyberSosResponse(
    val status: String,
    val emergency_contact: EmergencyContact,
    val complaint_copy: String,
    val next_steps: List<String>
)

data class EmergencyContact(
    val india_helpline: String,
    val portal: String
)

data class ApiError(
    val error: String?,
    val message: String?
)
