package com.pg.management.data.electricity.remote

import com.pg.management.core.network.ApiEnvelope
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class ElectricityRoomDto(
    val id: String,
    @Json(name = "room_number") val roomNumber: String,
    val floor: Int? = null,
)

@JsonClass(generateAdapter = true)
data class ElectricityReadingDto(
    val id: String,
    @Json(name = "room_id") val roomId: String,
    @Json(name = "billing_month") val billingMonth: String,
    @Json(name = "start_reading") val startReading: Double,
    @Json(name = "end_reading") val endReading: Double,
    @Json(name = "rate_per_unit") val ratePerUnit: Double,
    @Json(name = "units_used") val unitsUsed: Double,
    @Json(name = "total_amount") val totalAmount: Double,
    @Json(name = "per_member_amount") val perMemberAmount: Double,
    @Json(name = "member_count_at_creation") val memberCount: Int,
    val notes: String?,
    @Json(name = "created_at") val createdAt: String,
    val room: ElectricityRoomDto?,
)

@JsonClass(generateAdapter = true)
data class CreateReadingRequest(
    @Json(name = "room_id") val roomId: String,
    @Json(name = "billing_month") val billingMonth: String,
    @Json(name = "start_reading") val startReading: Double,
    @Json(name = "end_reading") val endReading: Double,
    @Json(name = "rate_per_unit") val ratePerUnit: Double,
    val notes: String? = null,
)

@JsonClass(generateAdapter = true)
data class CreateReadingResponse(
    val reading: ElectricityReadingDto,
    @Json(name = "bills_created") val billsCreated: Int,
)

interface ElectricityApi {
    @GET("electricity")
    suspend fun list(@Query("billing_month") billingMonth: String): ApiEnvelope<List<ElectricityReadingDto>>

    @POST("electricity")
    suspend fun create(@Body req: CreateReadingRequest): ApiEnvelope<CreateReadingResponse>

    @DELETE("electricity/{id}")
    suspend fun delete(@Path("id") id: String): ApiEnvelope<Unit>
}
