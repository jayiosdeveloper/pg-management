package com.pg.management.data.complaint.remote

import com.pg.management.core.network.ApiEnvelope
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class ComplaintUserBriefDto(
    val id: String? = null,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "user_code") val userCode: String? = null,
)

@JsonClass(generateAdapter = true)
data class ComplaintRoomBriefDto(
    val id: String? = null,
    @Json(name = "room_number") val roomNumber: String? = null,
)

@JsonClass(generateAdapter = true)
data class ComplaintTenantDto(
    val id: String,
    val user: ComplaintUserBriefDto? = null,
    val room: ComplaintRoomBriefDto? = null,
)

@JsonClass(generateAdapter = true)
data class ComplaintDto(
    val id: String,
    @Json(name = "tenant_id") val tenantId: String,
    val title: String,
    val description: String,
    val category: String? = null,
    val priority: String,
    val status: String,
    @Json(name = "admin_response") val adminResponse: String? = null,
    @Json(name = "resolved_at") val resolvedAt: String? = null,
    @Json(name = "created_at") val createdAt: String,
    val tenant: ComplaintTenantDto? = null,
)

@JsonClass(generateAdapter = true)
data class CreateComplaintRequest(
    val title: String,
    val description: String,
    val category: String? = null,
    val priority: String = "medium",
)

@JsonClass(generateAdapter = true)
data class UpdateComplaintRequest(
    val status: String? = null,
    @Json(name = "admin_response") val adminResponse: String? = null,
    val priority: String? = null,
)

interface ComplaintApi {
    @GET("complaints")
    suspend fun list(): ApiEnvelope<List<ComplaintDto>>

    @POST("complaints")
    suspend fun create(@Body req: CreateComplaintRequest): ApiEnvelope<ComplaintDto>

    @PATCH("complaints/{id}")
    suspend fun update(@Path("id") id: String, @Body req: UpdateComplaintRequest): ApiEnvelope<ComplaintDto>

    @DELETE("complaints/{id}")
    suspend fun delete(@Path("id") id: String): ApiEnvelope<Unit>
}
