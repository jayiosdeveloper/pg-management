package com.pg.management.data.tenant.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TenantUserDto(
    val id: String,
    @Json(name = "user_code") val userCode: String,
    @Json(name = "full_name") val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    @Json(name = "is_active") val isActive: Boolean = true,
)

@JsonClass(generateAdapter = true)
data class TenantRoomDto(
    val id: String,
    @Json(name = "room_number") val roomNumber: String,
    val floor: Int? = null,
    @Json(name = "monthly_rent") val monthlyRent: Double? = null,
    val status: String? = null,
)

@JsonClass(generateAdapter = true)
data class TenantBedDto(
    val id: String,
    @Json(name = "bed_label") val bedLabel: String,
    val status: String? = null,
)

@JsonClass(generateAdapter = true)
data class TenantDto(
    val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "room_id") val roomId: String? = null,
    @Json(name = "bed_id") val bedId: String? = null,
    @Json(name = "date_of_birth") val dateOfBirth: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    @Json(name = "emergency_contact_name") val emergencyContactName: String? = null,
    @Json(name = "emergency_contact_phone") val emergencyContactPhone: String? = null,
    val occupation: String? = null,
    @Json(name = "id_proof_type") val idProofType: String? = null,
    @Json(name = "id_proof_number") val idProofNumber: String? = null,
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "aadhaar_front_url") val aadhaarFrontUrl: String? = null,
    @Json(name = "aadhaar_back_url") val aadhaarBackUrl: String? = null,
    @Json(name = "joining_date") val joiningDate: String,
    @Json(name = "leaving_date") val leavingDate: String? = null,
    @Json(name = "monthly_rent") val monthlyRent: Double? = null,
    @Json(name = "security_deposit") val securityDeposit: Double? = null,
    val status: String,
    val notes: String? = null,
    val user: TenantUserDto? = null,
    val room: TenantRoomDto? = null,
    val bed: TenantBedDto? = null,
)

@JsonClass(generateAdapter = true)
data class CreateTenantRequest(
    @Json(name = "full_name") val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    @Json(name = "date_of_birth") val dateOfBirth: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    @Json(name = "emergency_contact_name") val emergencyContactName: String? = null,
    @Json(name = "emergency_contact_phone") val emergencyContactPhone: String? = null,
    val occupation: String? = null,
    @Json(name = "id_proof_type") val idProofType: String? = null,
    @Json(name = "id_proof_number") val idProofNumber: String? = null,
    @Json(name = "room_id") val roomId: String? = null,
    @Json(name = "bed_id") val bedId: String? = null,
    @Json(name = "joining_date") val joiningDate: String,
    @Json(name = "leaving_date") val leavingDate: String? = null,
    @Json(name = "monthly_rent") val monthlyRent: Double? = null,
    @Json(name = "security_deposit") val securityDeposit: Double? = 0.0,
    val notes: String? = null,
)

@JsonClass(generateAdapter = true)
data class UpdateTenantRequest(
    @Json(name = "full_name") val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @Json(name = "date_of_birth") val dateOfBirth: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    @Json(name = "emergency_contact_name") val emergencyContactName: String? = null,
    @Json(name = "emergency_contact_phone") val emergencyContactPhone: String? = null,
    val occupation: String? = null,
    @Json(name = "id_proof_type") val idProofType: String? = null,
    @Json(name = "id_proof_number") val idProofNumber: String? = null,
    @Json(name = "room_id") val roomId: String? = null,
    @Json(name = "bed_id") val bedId: String? = null,
    @Json(name = "joining_date") val joiningDate: String? = null,
    @Json(name = "leaving_date") val leavingDate: String? = null,
    @Json(name = "monthly_rent") val monthlyRent: Double? = null,
    @Json(name = "security_deposit") val securityDeposit: Double? = null,
    val status: String? = null,
    val notes: String? = null,
)

@JsonClass(generateAdapter = true)
data class CreateTenantResponse(
    val tenant: TenantDto,
    val credentials: TenantCredentialsDto,
)

@JsonClass(generateAdapter = true)
data class TenantCredentialsDto(
    @Json(name = "user_code") val userCode: String,
    @Json(name = "temp_password") val tempPassword: String,
)

@JsonClass(generateAdapter = true)
data class TenantUploadResponse(
    val url: String,
    @Json(name = "public_id") val publicId: String,
)

@JsonClass(generateAdapter = true)
data class TenantCredentialsResponse(
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "user_code") val userCode: String,
    val email: String?,
    @Json(name = "full_name") val fullName: String,
    val phone: String?,
)

@JsonClass(generateAdapter = true)
data class ResetPasswordRequest(@Json(name = "new_password") val newPassword: String?)

@JsonClass(generateAdapter = true)
data class ResetPasswordResponse(
    @Json(name = "user_code") val userCode: String,
    val email: String?,
    @Json(name = "new_password") val newPassword: String,
)
