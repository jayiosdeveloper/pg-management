package com.pg.management.data.room.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BedDto(
    val id: String,
    @Json(name = "bed_label") val bedLabel: String,
    val status: String,
)

@JsonClass(generateAdapter = true)
data class RoomTenantUserDto(
    val id: String? = null,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "user_code") val userCode: String? = null,
    val email: String? = null,
    val phone: String? = null,
)

@JsonClass(generateAdapter = true)
data class RoomTenantDto(
    val id: String,
    @Json(name = "bed_id") val bedId: String? = null,
    val user: RoomTenantUserDto? = null,
)

@JsonClass(generateAdapter = true)
data class RoomDto(
    val id: String,
    @Json(name = "room_number") val roomNumber: String,
    val floor: Int? = null,
    val capacity: Int,
    @Json(name = "monthly_rent") val monthlyRent: Double,
    val description: String? = null,
    val status: String,
    val beds: List<BedDto> = emptyList(),
    val tenants: List<RoomTenantDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class BedInput(@Json(name = "bed_label") val bedLabel: String)

@JsonClass(generateAdapter = true)
data class CreateRoomRequest(
    @Json(name = "room_number") val roomNumber: String,
    val floor: Int? = null,
    val capacity: Int = 1,
    @Json(name = "monthly_rent") val monthlyRent: Double = 0.0,
    val description: String? = null,
    val beds: List<BedInput>? = null,
)

@JsonClass(generateAdapter = true)
data class UpdateRoomRequest(
    @Json(name = "room_number") val roomNumber: String? = null,
    val floor: Int? = null,
    val capacity: Int? = null,
    @Json(name = "monthly_rent") val monthlyRent: Double? = null,
    val description: String? = null,
)
