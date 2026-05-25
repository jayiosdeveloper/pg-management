package com.pg.management.domain.repository

import com.pg.management.domain.model.Complaint

interface ComplaintRepository {
    suspend fun list(): List<Complaint>
    suspend fun create(title: String, description: String, category: String?, priority: String): Complaint
    suspend fun update(id: String, status: String? = null, adminResponse: String? = null, priority: String? = null): Complaint
    suspend fun delete(id: String)
}
