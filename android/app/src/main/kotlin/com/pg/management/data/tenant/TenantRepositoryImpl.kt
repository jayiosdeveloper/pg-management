package com.pg.management.data.tenant

import android.content.Context
import android.net.Uri
import com.pg.management.core.network.safeCall
import com.pg.management.data.tenant.remote.CreateTenantRequest
import com.pg.management.data.tenant.remote.TenantApi
import com.pg.management.data.tenant.remote.UpdateTenantRequest
import com.pg.management.domain.model.Tenant
import com.pg.management.domain.model.TenantCredentials
import com.pg.management.domain.repository.TenantInput
import com.pg.management.domain.repository.TenantRepository
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TenantRepositoryImpl @Inject constructor(
    private val api: TenantApi,
    private val moshi: Moshi,
    @ApplicationContext private val context: Context,
) : TenantRepository {

    override suspend fun list(query: String, status: String, roomId: String?): List<Tenant> {
        val data = safeCall(moshi) {
            api.list(q = query.ifBlank { null }, status = status, roomId = roomId)
        }
        return data.map { it.toDomain() }
    }

    override suspend fun get(id: String): Tenant =
        safeCall(moshi) { api.get(id) }.toDomain()

    override suspend fun create(input: TenantInput): Pair<Tenant, TenantCredentials> {
        val req = CreateTenantRequest(
            fullName = input.fullName,
            email = input.email,
            phone = input.phone,
            dateOfBirth = input.dateOfBirth,
            gender = input.gender,
            address = input.address,
            city = input.city,
            state = input.state,
            emergencyContactName = input.emergencyContactName,
            emergencyContactPhone = input.emergencyContactPhone,
            occupation = input.occupation,
            idProofType = input.idProofType,
            idProofNumber = input.idProofNumber,
            roomId = input.roomId,
            bedId = input.bedId,
            joiningDate = input.joiningDate,
            leavingDate = input.leavingDate,
            monthlyRent = input.monthlyRent,
            securityDeposit = input.securityDeposit ?: 0.0,
            notes = input.notes,
        )
        val data = safeCall(moshi) { api.create(req) }
        return data.tenant.toDomain() to TenantCredentials(data.credentials.userCode, data.credentials.tempPassword)
    }

    override suspend fun update(id: String, input: TenantInput): Tenant {
        val req = UpdateTenantRequest(
            fullName = input.fullName,
            email = input.email,
            phone = input.phone,
            dateOfBirth = input.dateOfBirth,
            gender = input.gender,
            address = input.address,
            city = input.city,
            state = input.state,
            emergencyContactName = input.emergencyContactName,
            emergencyContactPhone = input.emergencyContactPhone,
            occupation = input.occupation,
            idProofType = input.idProofType,
            idProofNumber = input.idProofNumber,
            roomId = input.roomId,
            bedId = input.bedId,
            joiningDate = input.joiningDate,
            leavingDate = input.leavingDate,
            monthlyRent = input.monthlyRent,
            securityDeposit = input.securityDeposit,
            notes = input.notes,
            status = input.status,
        )
        return safeCall(moshi) { api.update(id, req) }.toDomain()
    }

    override suspend fun delete(id: String) {
        safeCall(moshi) { api.delete(id) }
    }

    override suspend fun uploadPhoto(id: String, uri: Uri) =
        upload(uri) { part -> safeCall(moshi) { api.uploadPhoto(id, part) }.url }

    override suspend fun uploadAadhaarFront(id: String, uri: Uri) =
        upload(uri) { part -> safeCall(moshi) { api.uploadAadhaarFront(id, part) }.url }

    override suspend fun uploadAadhaarBack(id: String, uri: Uri) =
        upload(uri) { part -> safeCall(moshi) { api.uploadAadhaarBack(id, part) }.url }

    private suspend inline fun upload(uri: Uri, crossinline call: suspend (MultipartBody.Part) -> String): String =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Unable to read selected image")
            val mime = resolver.getType(uri) ?: "image/jpeg"
            val fileName = "upload.${mime.substringAfter('/').ifBlank { "jpg" }}"
            val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, body)
            call(part)
        }
}
