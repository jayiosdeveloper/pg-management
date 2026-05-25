package com.pg.management.domain.repository

import com.pg.management.domain.model.ElectricityReading

interface ElectricityRepository {
    suspend fun list(billingMonth: String): List<ElectricityReading>
    suspend fun create(
        roomId: String,
        billingMonth: String,
        startReading: Double,
        endReading: Double,
        ratePerUnit: Double,
        notes: String? = null,
    ): Pair<ElectricityReading, Int>
    suspend fun delete(id: String)
}
