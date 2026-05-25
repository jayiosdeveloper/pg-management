package com.pg.management.domain.auth

import com.pg.management.domain.model.UserRole

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val userCode: String,
    val fullName: String,
    val email: String?,
    val role: UserRole,
)
