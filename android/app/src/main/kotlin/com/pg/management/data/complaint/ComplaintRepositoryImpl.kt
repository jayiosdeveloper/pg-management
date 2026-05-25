package com.pg.management.data.complaint

import com.pg.management.core.network.safeCall
import com.pg.management.data.complaint.remote.ComplaintApi
import com.pg.management.data.complaint.remote.ComplaintDto
import com.pg.management.data.complaint.remote.CreateComplaintRequest
import com.pg.management.data.complaint.remote.UpdateComplaintRequest
import com.pg.management.domain.model.Complaint
import com.pg.management.domain.repository.ComplaintRepository
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

internal fun ComplaintDto.toDomain() = Complaint(
    id = id,
    tenantId = tenantId,
    tenantName = tenant?.user?.fullName,
    tenantUserCode = tenant?.user?.userCode,
    roomNumber = tenant?.room?.roomNumber,
    title = title,
    description = description,
    category = category,
    priority = priority,
    status = status,
    adminResponse = adminResponse,
    resolvedAt = resolvedAt,
    createdAt = createdAt,
)

@Singleton
class ComplaintRepositoryImpl @Inject constructor(
    private val api: ComplaintApi,
    private val moshi: Moshi,
) : ComplaintRepository {
    override suspend fun list(): List<Complaint> = safeCall(moshi) { api.list() }.map { it.toDomain() }

    override suspend fun create(title: String, description: String, category: String?, priority: String): Complaint =
        safeCall(moshi) { api.create(CreateComplaintRequest(title, description, category, priority)) }.toDomain()

    override suspend fun update(id: String, status: String?, adminResponse: String?, priority: String?): Complaint =
        safeCall(moshi) { api.update(id, UpdateComplaintRequest(status, adminResponse, priority)) }.toDomain()

    override suspend fun delete(id: String) { safeCall(moshi) { api.delete(id) } }
}
