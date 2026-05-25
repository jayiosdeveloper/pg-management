package com.pg.management.domain.repository

import com.pg.management.domain.model.Room

interface RoomRepository {
    suspend fun list(status: String = "all", query: String = ""): List<Room>
    suspend fun get(id: String): Room
    suspend fun create(input: RoomInput): Room
    suspend fun update(id: String, input: RoomInput): Room
    suspend fun delete(id: String)
    suspend fun addBed(roomId: String, bedLabel: String): Room
    suspend fun removeBed(roomId: String, bedId: String)
}

data class RoomInput(
    val roomNumber: String,
    val floor: Int? = null,
    val capacity: Int = 1,
    val monthlyRent: Double = 0.0,
    val description: String? = null,
)
