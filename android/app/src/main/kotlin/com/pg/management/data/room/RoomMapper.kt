package com.pg.management.data.room

import com.pg.management.data.room.remote.BedDto
import com.pg.management.data.room.remote.RoomDto
import com.pg.management.data.room.remote.RoomTenantDto
import com.pg.management.domain.model.Bed
import com.pg.management.domain.model.Room
import com.pg.management.domain.model.RoomTenant
import com.pg.management.domain.model.RoomTenantUser

internal fun BedDto.toDomain() = Bed(id, bedLabel, status)

internal fun RoomTenantDto.toDomain() = RoomTenant(
    id = id,
    bedId = bedId,
    user = user?.let { RoomTenantUser(it.fullName, it.userCode) },
)

internal fun RoomDto.toDomain() = Room(
    id = id,
    roomNumber = roomNumber,
    floor = floor,
    capacity = capacity,
    monthlyRent = monthlyRent,
    description = description,
    status = status,
    beds = beds.map { it.toDomain() },
    tenants = tenants.map { it.toDomain() },
)
