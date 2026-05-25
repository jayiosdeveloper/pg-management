package com.pg.management.domain.model

data class ElectricityReading(
    val id: String,
    val roomId: String,
    val roomNumber: String?,
    val billingMonth: String,
    val startReading: Double,
    val endReading: Double,
    val ratePerUnit: Double,
    val unitsUsed: Double,
    val totalAmount: Double,
    val perMemberAmount: Double,
    val memberCount: Int,
    val notes: String?,
    val createdAt: String,
)
