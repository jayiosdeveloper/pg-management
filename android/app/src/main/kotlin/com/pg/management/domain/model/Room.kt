package com.pg.management.domain.model

data class Bed(
    val id: String,
    val bedLabel: String,
    val status: String,   // "vacant" | "occupied"
)

data class RoomTenantUser(
    val fullName: String?,
    val userCode: String?,
)

data class RoomTenant(
    val id: String,
    val bedId: String?,
    val user: RoomTenantUser?,
)

data class Room(
    val id: String,
    val roomNumber: String,
    val floor: Int?,
    val capacity: Int,
    val monthlyRent: Double,
    val description: String?,
    val status: String,   // vacant | partial | occupied
    val beds: List<Bed>,
    val tenants: List<RoomTenant>,
) {
    val occupiedCount: Int get() = beds.count { it.status == "occupied" }
    val vacantCount: Int get() = beds.count { it.status == "vacant" }
}
