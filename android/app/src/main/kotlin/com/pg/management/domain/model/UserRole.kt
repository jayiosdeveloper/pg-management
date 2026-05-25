package com.pg.management.domain.model

enum class UserRole(val value: String) {
    Admin("admin"),
    Tenant("tenant");

    companion object {
        fun fromString(v: String): UserRole? = entries.firstOrNull { it.value.equals(v, ignoreCase = true) }
    }
}
