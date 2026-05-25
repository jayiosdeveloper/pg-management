package com.pg.management.data.auth.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val identifier: String,
    val password: String,
    @Json(name = "fcm_token") val fcmToken: String? = null,
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val user: AuthUserDto,
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
)

@JsonClass(generateAdapter = true)
data class AuthUserDto(
    val id: String,
    @Json(name = "user_code") val userCode: String,
    @Json(name = "full_name") val fullName: String,
    val email: String?,
    val role: String,
)

@JsonClass(generateAdapter = true)
data class RefreshRequest(@Json(name = "refresh_token") val refreshToken: String)

@JsonClass(generateAdapter = true)
data class RefreshResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
)

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "current_password") val current: String,
    @Json(name = "new_password") val new: String,
)

@JsonClass(generateAdapter = true)
data class LogoutRequest(@Json(name = "refresh_token") val refreshToken: String? = null)
