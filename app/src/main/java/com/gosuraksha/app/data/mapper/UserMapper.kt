package com.gosuraksha.app.data.mapper

import com.gosuraksha.app.data.remote.dto.auth.UserResponse
import com.gosuraksha.app.domain.model.Plan
import com.gosuraksha.app.domain.model.User

fun UserResponse.toDomain(): User {
    val planValue = when (plan.uppercase()) {
        "PAID" -> Plan.PAID
        else -> Plan.FREE
    }
    return User(
        id = id,
        name = name,
        email = email,
        role = role,
        plan = planValue
    )
}
