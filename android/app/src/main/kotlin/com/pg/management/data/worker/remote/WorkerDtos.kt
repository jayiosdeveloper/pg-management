package com.pg.management.data.worker.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WorkerUserDto(
    val id: String,
    @Json(name = "user_code") val userCode: String,
    @Json(name = "full_name") val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    @Json(name = "is_active") val isActive: Boolean = true,
)

@JsonClass(generateAdapter = true)
data class WorkerDto(
    val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "role_title") val roleTitle: String? = null,
    @Json(name = "monthly_salary") val monthlySalary: Double,
    @Json(name = "joining_date") val joiningDate: String,
    @Json(name = "leaving_date") val leavingDate: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    @Json(name = "emergency_contact_name") val emergencyContactName: String? = null,
    @Json(name = "emergency_contact_phone") val emergencyContactPhone: String? = null,
    @Json(name = "id_proof_type") val idProofType: String? = null,
    @Json(name = "id_proof_number") val idProofNumber: String? = null,
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "aadhaar_front_url") val aadhaarFrontUrl: String? = null,
    @Json(name = "aadhaar_back_url") val aadhaarBackUrl: String? = null,
    val status: String,
    val notes: String? = null,
    val user: WorkerUserDto? = null,
)

@JsonClass(generateAdapter = true)
data class CreateWorkerRequest(
    @Json(name = "full_name") val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    @Json(name = "role_title") val roleTitle: String? = null,
    @Json(name = "monthly_salary") val monthlySalary: Double = 0.0,
    @Json(name = "joining_date") val joiningDate: String,
    val gender: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    @Json(name = "emergency_contact_name") val emergencyContactName: String? = null,
    @Json(name = "emergency_contact_phone") val emergencyContactPhone: String? = null,
    @Json(name = "id_proof_type") val idProofType: String? = null,
    @Json(name = "id_proof_number") val idProofNumber: String? = null,
    val notes: String? = null,
)

@JsonClass(generateAdapter = true)
data class UpdateWorkerRequest(
    @Json(name = "full_name") val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @Json(name = "role_title") val roleTitle: String? = null,
    @Json(name = "monthly_salary") val monthlySalary: Double? = null,
    @Json(name = "joining_date") val joiningDate: String? = null,
    @Json(name = "leaving_date") val leavingDate: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    @Json(name = "emergency_contact_name") val emergencyContactName: String? = null,
    @Json(name = "emergency_contact_phone") val emergencyContactPhone: String? = null,
    @Json(name = "id_proof_type") val idProofType: String? = null,
    @Json(name = "id_proof_number") val idProofNumber: String? = null,
    val status: String? = null,
    val notes: String? = null,
)

@JsonClass(generateAdapter = true)
data class CreateWorkerResponse(
    val worker: WorkerDto,
    val credentials: WorkerCredentialsDto,
)

@JsonClass(generateAdapter = true)
data class WorkerCredentialsDto(
    @Json(name = "user_code") val userCode: String,
    @Json(name = "temp_password") val tempPassword: String,
)

@JsonClass(generateAdapter = true)
data class WorkerCredentialsResponse(
    @Json(name = "worker_id") val workerId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "user_code") val userCode: String,
    val email: String?,
    @Json(name = "full_name") val fullName: String,
    val phone: String?,
)

@JsonClass(generateAdapter = true)
data class RecordSalaryRequest(
    val amount: Double,
    @Json(name = "pay_for_month") val payForMonth: String,
    val method: String = "cash",
    @Json(name = "paid_at") val paidAt: String? = null,
    val reference: String? = null,
    val notes: String? = null,
)

@JsonClass(generateAdapter = true)
data class SalaryPaymentDto(
    val id: String,
    @Json(name = "worker_id") val workerId: String,
    val amount: Double,
    @Json(name = "pay_for_month") val payForMonth: String,
    val method: String,
    @Json(name = "paid_at") val paidAt: String,
    val reference: String? = null,
    val notes: String? = null,
)

@JsonClass(generateAdapter = true)
data class WorkerUploadResponse(
    val url: String,
    @Json(name = "public_id") val publicId: String,
)
