package com.pg.management.data.tenant.remote

import com.pg.management.core.network.ApiEnvelope
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TenantApi {
    @GET("tenants")
    suspend fun list(
        @Query("q") q: String? = null,
        @Query("status") status: String? = "active",
        @Query("room_id") roomId: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50,
    ): ApiEnvelope<List<TenantDto>>

    @GET("tenants/{id}")
    suspend fun get(@Path("id") id: String): ApiEnvelope<TenantDto>

    @POST("tenants")
    suspend fun create(@Body req: CreateTenantRequest): ApiEnvelope<CreateTenantResponse>

    @PATCH("tenants/{id}")
    suspend fun update(@Path("id") id: String, @Body req: UpdateTenantRequest): ApiEnvelope<TenantDto>

    @DELETE("tenants/{id}")
    suspend fun delete(@Path("id") id: String): ApiEnvelope<Unit>

    @Multipart
    @POST("tenants/{id}/photo")
    suspend fun uploadPhoto(@Path("id") id: String, @Part file: MultipartBody.Part): ApiEnvelope<TenantUploadResponse>

    @Multipart
    @POST("tenants/{id}/aadhaar-front")
    suspend fun uploadAadhaarFront(@Path("id") id: String, @Part file: MultipartBody.Part): ApiEnvelope<TenantUploadResponse>

    @Multipart
    @POST("tenants/{id}/aadhaar-back")
    suspend fun uploadAadhaarBack(@Path("id") id: String, @Part file: MultipartBody.Part): ApiEnvelope<TenantUploadResponse>

    @GET("tenants/{id}/credentials")
    suspend fun credentials(@Path("id") id: String): ApiEnvelope<TenantCredentialsResponse>

    @POST("tenants/{id}/reset-password")
    suspend fun resetPassword(@Path("id") id: String, @Body req: ResetPasswordRequest): ApiEnvelope<ResetPasswordResponse>
}
