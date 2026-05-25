package com.pg.management.domain.model

data class TenantUser(
    val id: String,
    val userCode: String,
    val fullName: String,
    val email: String?,
    val phone: String?,
    val isActive: Boolean,
)

data class RoomBrief(
    val id: String,
    val roomNumber: String,
    val floor: Int?,
    val monthlyRent: Double?,
    val status: String?,
)

data class BedBrief(
    val id: String,
    val bedLabel: String,
    val status: String?,
)

data class Tenant(
    val id: String,
    val user: TenantUser,
    val room: RoomBrief?,
    val bed: BedBrief?,
    val dateOfBirth: String?,
    val gender: String?,
    val address: String?,
    val city: String?,
    val state: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val occupation: String?,
    val idProofType: String?,
    val idProofNumber: String?,
    val photoUrl: String?,
    val aadhaarFrontUrl: String?,
    val aadhaarBackUrl: String?,
    val joiningDate: String,
    val leavingDate: String?,
    val monthlyRent: Double?,
    val securityDeposit: Double?,
    val status: String,
    val notes: String?,
)

data class TenantCredentials(
    val userCode: String,
    val tempPassword: String,
)
