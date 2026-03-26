package com.gosuraksha.app.data.mapper

import com.gosuraksha.app.data.remote.dto.auth.UserResponse
import com.gosuraksha.app.domain.model.Plan
import com.gosuraksha.app.domain.model.User

fun UserResponse.toDomain(): User {
    val userId = safeString(id, fallback = "")
        .takeIf { it.isNotBlank() }
        ?: throw IllegalStateException("User ID missing from backend")

    val planValue = when (safeString(plan, fallback = "FREE").uppercase()) {
        "GO_ULTRA" -> Plan.GO_ULTRA
        "GO_PRO", "PAID" -> Plan.GO_PRO
        else -> Plan.FREE
    }
    return User(
        id = userId,
        name = safeString(name, fallback = "Guest"),
        email = safeString(email, fallback = "unknown@example.com"),
        phone = phone?.trim()?.takeIf { it.isNotEmpty() },
        profileImageUrl = when (val value = profile_image_url) {
            is String -> value.trim().takeIf { it.isNotEmpty() }
            is Map<*, *> -> (value["url"] as? String)?.trim()?.takeIf { it.isNotEmpty() }
            else -> null
        },
        role = safeString(role, fallback = "user"),
        plan = planValue
    )
}
