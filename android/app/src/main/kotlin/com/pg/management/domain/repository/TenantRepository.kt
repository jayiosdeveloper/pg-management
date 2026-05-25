package com.pg.management.domain.repository

import android.net.Uri
import com.pg.management.domain.model.Tenant
import com.pg.management.domain.model.TenantCredentials

interface TenantRepository {
    suspend fun list(query: String = "", status: String = "active", roomId: String? = null): List<Tenant>
    suspend fun get(id: String): Tenant
    suspend fun create(input: TenantInput): Pair<Tenant, TenantCredentials>
    suspend fun update(id: String, input: TenantInput): Tenant
    suspend fun delete(id: String)
    suspend fun uploadPhoto(id: String, uri: Uri): String
    suspend fun uploadAadhaarFront(id: String, uri: Uri): String
    suspend fun uploadAadhaarBack(id: String, uri: Uri): String
    suspend fun credentials(id: String): MemberCredentialsInfo
    suspend fun resetPassword(id: String, newPassword: String?): TenantCredentials
}

data class MemberCredentialsInfo(
    val tenantId: String,
    val userId: String,
    val userCode: String,
    val email: String?,
    val fullName: String,
    val phone: String?,
)

data class TenantInput(
    val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val occupation: String? = null,
    val idProofType: String? = null,
    val idProofNumber: String? = null,
    val roomId: String? = null,
    val bedId: String? = null,
    val joiningDate: String,
    val leavingDate: String? = null,
    val monthlyRent: Double? = null,
    val securityDeposit: Double? = null,
    val notes: String? = null,
    val status: String? = null,
)
