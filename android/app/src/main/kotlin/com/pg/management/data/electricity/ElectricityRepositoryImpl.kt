package com.pg.management.data.electricity

import com.pg.management.core.network.safeCall
import com.pg.management.data.electricity.remote.CreateReadingRequest
import com.pg.management.data.electricity.remote.ElectricityApi
import com.pg.management.data.electricity.remote.ElectricityReadingDto
import com.pg.management.domain.model.ElectricityReading
import com.pg.management.domain.repository.ElectricityRepository
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

internal fun ElectricityReadingDto.toDomain() = ElectricityReading(
    id = id,
    roomId = roomId,
    roomNumber = room?.roomNumber,
    billingMonth = billingMonth.take(10),
    startReading = startReading,
    endReading = endReading,
    ratePerUnit = ratePerUnit,
    unitsUsed = unitsUsed,
    totalAmount = totalAmount,
    perMemberAmount = perMemberAmount,
    memberCount = memberCount,
    notes = notes,
    createdAt = createdAt,
)

@Singleton
class ElectricityRepositoryImpl @Inject constructor(
    private val api: ElectricityApi,
    private val moshi: Moshi,
) : ElectricityRepository {

    override suspend fun list(billingMonth: String): List<ElectricityReading> =
        safeCall(moshi) { api.list(billingMonth) }.map { it.toDomain() }

    override suspend fun create(roomId: String, billingMonth: String, startReading: Double, endReading: Double, ratePerUnit: Double, notes: String?): Pair<ElectricityReading, Int> {
        val req = CreateReadingRequest(roomId, billingMonth, startReading, endReading, ratePerUnit, notes)
        val r = safeCall(moshi) { api.create(req) }
        return r.reading.toDomain() to r.billsCreated
    }

    override suspend fun delete(id: String) { safeCall(moshi) { api.delete(id) } }
}
