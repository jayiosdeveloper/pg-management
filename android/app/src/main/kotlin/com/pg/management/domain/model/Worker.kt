package com.pg.management.domain.model

data class WorkerUser(
    val id: String,
    val userCode: String,
    val fullName: String,
    val email: String?,
    val phone: String?,
)

data class Worker(
    val id: String,
    val user: WorkerUser,
    val roleTitle: String?,
    val monthlySalary: Double,
    val joiningDate: String,
    val leavingDate: String?,
    val photoUrl: String?,
    val aadhaarFrontUrl: String?,
    val aadhaarBackUrl: String?,
    val gender: String?,
    val address: String?,
    val city: String?,
    val state: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val idProofType: String?,
    val idProofNumber: String?,
    val status: String,
    val notes: String?,
)

data class WorkerCredentials(val userCode: String, val tempPassword: String)

data class WorkerCredentialsInfo(
    val workerId: String,
    val userId: String,
    val userCode: String,
    val email: String?,
    val fullName: String,
    val phone: String?,
)

data class SalaryPayment(
    val id: String,
    val workerId: String,
    val amount: Double,
    val payForMonth: String,
    val method: String,
    val paidAt: String,
    val reference: String?,
    val notes: String?,
)
