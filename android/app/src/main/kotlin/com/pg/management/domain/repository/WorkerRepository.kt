package com.pg.management.domain.repository

import android.net.Uri
import com.pg.management.domain.model.SalaryPayment
import com.pg.management.domain.model.Worker
import com.pg.management.domain.model.WorkerCredentials
import com.pg.management.domain.model.WorkerCredentialsInfo

interface WorkerRepository {
    suspend fun list(query: String = "", status: String = "active"): List<Worker>
    suspend fun get(id: String): Worker
    suspend fun create(input: WorkerInput): Pair<Worker, WorkerCredentials>
    suspend fun update(id: String, input: WorkerInput): Worker
    suspend fun delete(id: String)
    suspend fun uploadPhoto(id: String, uri: Uri): String
    suspend fun uploadAadhaarFront(id: String, uri: Uri): String
    suspend fun uploadAadhaarBack(id: String, uri: Uri): String
    suspend fun credentials(id: String): WorkerCredentialsInfo
    suspend fun resetPassword(id: String, newPassword: String?): WorkerCredentials
    suspend fun recordSalary(id: String, amount: Double, payForMonth: String, method: String, reference: String?, notes: String?): SalaryPayment
    suspend fun listSalary(workerId: String? = null, month: String? = null): List<SalaryPayment>
}

data class WorkerInput(
    val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    val roleTitle: String? = null,
    val monthlySalary: Double = 0.0,
    val joiningDate: String,
    val leavingDate: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val idProofType: String? = null,
    val idProofNumber: String? = null,
    val notes: String? = null,
    val status: String? = null,
)
