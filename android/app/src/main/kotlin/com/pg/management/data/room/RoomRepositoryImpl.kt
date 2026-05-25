package com.pg.management.data.room

import com.pg.management.core.network.safeCall
import com.pg.management.data.room.remote.CreateRoomRequest
import com.pg.management.data.room.remote.BedInput
import com.pg.management.data.room.remote.RoomApi
import com.pg.management.data.room.remote.UpdateRoomRequest
import com.pg.management.domain.model.Room
import com.pg.management.domain.repository.RoomInput
import com.pg.management.domain.repository.RoomRepository
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepositoryImpl @Inject constructor(
    private val api: RoomApi,
    private val moshi: Moshi,
) : RoomRepository {
    override suspend fun list(status: String, query: String): List<Room> =
        safeCall(moshi) { api.list(status = status, q = query.ifBlank { null }) }.map { it.toDomain() }

    override suspend fun get(id: String): Room = safeCall(moshi) { api.get(id) }.toDomain()

    override suspend fun create(input: RoomInput): Room {
        val req = CreateRoomRequest(
            roomNumber = input.roomNumber,
            floor = input.floor,
            capacity = input.capacity,
            monthlyRent = input.monthlyRent,
            description = input.description,
        )
        return safeCall(moshi) { api.create(req) }.toDomain()
    }

    override suspend fun update(id: String, input: RoomInput): Room {
        val req = UpdateRoomRequest(
            roomNumber = input.roomNumber,
            floor = input.floor,
            capacity = input.capacity,
            monthlyRent = input.monthlyRent,
            description = input.description,
        )
        return safeCall(moshi) { api.update(id, req) }.toDomain()
    }

    override suspend fun delete(id: String) {
        safeCall(moshi) { api.delete(id) }
    }

    override suspend fun addBed(roomId: String, bedLabel: String): Room =
        safeCall(moshi) { api.addBed(roomId, BedInput(bedLabel)) }.toDomain()

    override suspend fun removeBed(roomId: String, bedId: String) {
        safeCall(moshi) { api.removeBed(roomId, bedId) }
    }
}
