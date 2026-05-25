package com.pg.management.data.worker

import android.content.Context
import android.net.Uri
import com.pg.management.core.network.safeCall
import com.pg.management.data.tenant.remote.ResetPasswordRequest
import com.pg.management.data.worker.remote.CreateWorkerRequest
import com.pg.management.data.worker.remote.RecordSalaryRequest
import com.pg.management.data.worker.remote.SalaryPaymentDto
import com.pg.management.data.worker.remote.UpdateWorkerRequest
import com.pg.management.data.worker.remote.WorkerApi
import com.pg.management.data.worker.remote.WorkerDto
import com.pg.management.domain.model.SalaryPayment
import com.pg.management.domain.model.Worker
import com.pg.management.domain.model.WorkerCredentials
import com.pg.management.domain.model.WorkerCredentialsInfo
import com.pg.management.domain.model.WorkerUser
import com.pg.management.domain.repository.WorkerInput
import com.pg.management.domain.repository.WorkerRepository
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

internal fun WorkerDto.toDomain(): Worker {
    val u = user ?: error("Worker DTO missing user")
    return Worker(
        id = id,
        user = WorkerUser(u.id, u.userCode, u.fullName, u.email, u.phone),
        roleTitle = roleTitle,
        monthlySalary = monthlySalary,
        joiningDate = joiningDate,
        leavingDate = leavingDate,
        photoUrl = photoUrl,
        aadhaarFrontUrl = aadhaarFrontUrl,
        aadhaarBackUrl = aadhaarBackUrl,
        gender = gender,
        address = address,
        city = city,
        state = state,
        emergencyContactName = emergencyContactName,
        emergencyContactPhone = emergencyContactPhone,
        idProofType = idProofType,
        idProofNumber = idProofNumber,
        status = status,
        notes = notes,
    )
}

internal fun SalaryPaymentDto.toDomain() = SalaryPayment(
    id = id, workerId = workerId, amount = amount, payForMonth = payForMonth.take(10),
    method = method, paidAt = paidAt, reference = reference, notes = notes,
)

@Singleton
class WorkerRepositoryImpl @Inject constructor(
    private val api: WorkerApi,
    private val moshi: Moshi,
    @ApplicationContext private val context: Context,
) : WorkerRepository {

    override suspend fun list(query: String, status: String): List<Worker> =
        safeCall(moshi) { api.list(query.ifBlank { null }, status) }.map { it.toDomain() }

    override suspend fun get(id: String): Worker = safeCall(moshi) { api.get(id) }.toDomain()

    override suspend fun create(input: WorkerInput): Pair<Worker, WorkerCredentials> {
        val req = CreateWorkerRequest(
            fullName = input.fullName,
            email = input.email,
            phone = input.phone,
            roleTitle = input.roleTitle,
            monthlySalary = input.monthlySalary,
            joiningDate = input.joiningDate,
            gender = input.gender,
            address = input.address,
            city = input.city,
            state = input.state,
            emergencyContactName = input.emergencyContactName,
            emergencyContactPhone = input.emergencyContactPhone,
            idProofType = input.idProofType,
            idProofNumber = input.idProofNumber,
            notes = input.notes,
        )
        val r = safeCall(moshi) { api.create(req) }
        return r.worker.toDomain() to WorkerCredentials(r.credentials.userCode, r.credentials.tempPassword)
    }

    override suspend fun update(id: String, input: WorkerInput): Worker {
        val req = UpdateWorkerRequest(
            fullName = input.fullName,
            email = input.email,
            phone = input.phone,
            roleTitle = input.roleTitle,
            monthlySalary = input.monthlySalary,
            joiningDate = input.joiningDate,
            leavingDate = input.leavingDate,
            gender = input.gender,
            address = input.address,
            city = input.city,
            state = input.state,
            emergencyContactName = input.emergencyContactName,
            emergencyContactPhone = input.emergencyContactPhone,
            idProofType = input.idProofType,
            idProofNumber = input.idProofNumber,
            status = input.status,
            notes = input.notes,
        )
        return safeCall(moshi) { api.update(id, req) }.toDomain()
    }

    override suspend fun delete(id: String) { safeCall(moshi) { api.delete(id) } }

    override suspend fun uploadPhoto(id: String, uri: Uri) =
        upload(uri) { part -> safeCall(moshi) { api.uploadPhoto(id, part) }.url }

    override suspend fun uploadAadhaarFront(id: String, uri: Uri) =
        upload(uri) { part -> safeCall(moshi) { api.uploadAadhaarFront(id, part) }.url }

    override suspend fun uploadAadhaarBack(id: String, uri: Uri) =
        upload(uri) { part -> safeCall(moshi) { api.uploadAadhaarBack(id, part) }.url }

    override suspend fun credentials(id: String): WorkerCredentialsInfo {
        val r = safeCall(moshi) { api.credentials(id) }
        return WorkerCredentialsInfo(r.workerId, r.userId, r.userCode, r.email, r.fullName, r.phone)
    }

    override suspend fun resetPassword(id: String, newPassword: String?): WorkerCredentials {
        val r = safeCall(moshi) { api.resetPassword(id, ResetPasswordRequest(newPassword?.takeIf { it.isNotBlank() })) }
        return WorkerCredentials(r.userCode, r.newPassword)
    }

    override suspend fun recordSalary(id: String, amount: Double, payForMonth: String, method: String, reference: String?, notes: String?): SalaryPayment {
        val r = safeCall(moshi) {
            api.recordSalary(id, RecordSalaryRequest(amount, payForMonth, method, null, reference, notes))
        }
        return r.toDomain()
    }

    override suspend fun listSalary(workerId: String?, month: String?): List<SalaryPayment> =
        safeCall(moshi) { api.listSalary(workerId, month) }.map { it.toDomain() }

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
