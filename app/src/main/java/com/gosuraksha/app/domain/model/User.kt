package com.gosuraksha.app.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val role: String,
    val plan: Plan
) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(name.isNotBlank()) { "name must not be blank" }
        require(email.isNotBlank()) { "email must not be blank" }
        require(role.isNotBlank()) { "role must not be blank" }
    }
}

enum class Plan {
    FREE,
    GO_PRO,
    GO_ULTRA
}
