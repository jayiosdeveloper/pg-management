package com.pg.management.domain.model

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val isRead: Boolean,
    val sentAt: String,
)

data class Complaint(
    val id: String,
    val tenantId: String,
    val tenantName: String?,
    val tenantUserCode: String?,
    val roomNumber: String?,
    val title: String,
    val description: String,
    val category: String?,
    val priority: String,
    val status: String,
    val adminResponse: String?,
    val resolvedAt: String?,
    val createdAt: String,
)
