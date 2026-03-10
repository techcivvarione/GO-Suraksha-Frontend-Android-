package com.gosuraksha.app.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val plan: Plan
)

enum class Plan {
    FREE,
    PAID
}
