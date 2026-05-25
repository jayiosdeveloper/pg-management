package com.pg.management.data.auth.remote

import com.pg.management.core.network.ApiEnvelope
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): ApiEnvelope<LoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body req: RefreshRequest): ApiEnvelope<RefreshResponse>

    @POST("auth/logout")
    suspend fun logout(@Body req: LogoutRequest): ApiEnvelope<Unit>

    @POST("auth/change-password")
    suspend fun changePassword(@Body req: ChangePasswordRequest): ApiEnvelope<Unit>
}
