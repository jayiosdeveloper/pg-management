package com.pg.management.data.tenant

import com.pg.management.data.tenant.remote.TenantBedDto
import com.pg.management.data.tenant.remote.TenantDto
import com.pg.management.data.tenant.remote.TenantRoomDto
import com.pg.management.data.tenant.remote.TenantUserDto
import com.pg.management.domain.model.BedBrief
import com.pg.management.domain.model.RoomBrief
import com.pg.management.domain.model.Tenant
import com.pg.management.domain.model.TenantUser

internal fun TenantUserDto.toDomain() = TenantUser(
    id = id,
    userCode = userCode,
    fullName = fullName,
    email = email,
    phone = phone,
    isActive = isActive,
)

internal fun TenantRoomDto.toDomain() = RoomBrief(id, roomNumber, floor, monthlyRent, status)

internal fun TenantBedDto.toDomain() = BedBrief(id, bedLabel, status)

internal fun TenantDto.toDomain(): Tenant {
    val u = user ?: error("Tenant DTO missing user payload")
    return Tenant(
        id = id,
        user = u.toDomain(),
        room = room?.toDomain(),
        bed = bed?.toDomain(),
        dateOfBirth = dateOfBirth,
        gender = gender,
        address = address,
        city = city,
        state = state,
        emergencyContactName = emergencyContactName,
        emergencyContactPhone = emergencyContactPhone,
        occupation = occupation,
        idProofType = idProofType,
        idProofNumber = idProofNumber,
        photoUrl = photoUrl,
        aadhaarFrontUrl = aadhaarFrontUrl,
        aadhaarBackUrl = aadhaarBackUrl,
        joiningDate = joiningDate,
        leavingDate = leavingDate,
        monthlyRent = monthlyRent,
        securityDeposit = securityDeposit,
        status = status,
        notes = notes,
    )
}
