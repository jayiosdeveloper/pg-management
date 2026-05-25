package com.pg.management.ui.screens.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.core.events.RefreshEvents
import com.pg.management.domain.repository.RoomRepository
import com.pg.management.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardStats(
    val totalTenants: Int = 0,
    val activeTenants: Int = 0,
    val totalRooms: Int = 0,
    val occupiedRooms: Int = 0,
    val vacantBeds: Int = 0,
    val occupiedBeds: Int = 0,
)

data class DashboardUiState(
    val loading: Boolean = false,
    val stats: DashboardStats = DashboardStats(),
    val error: String? = null,
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val tenants: TenantRepository,
    private val rooms: RoomRepository,
    refreshEvents: RefreshEvents,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch { refreshEvents.tenantsChanged.collect { refresh() } }
        viewModelScope.launch { refreshEvents.roomsChanged.collect { refresh() } }
        viewModelScope.launch { refreshEvents.workersChanged.collect { refresh() } }
        viewModelScope.launch { refreshEvents.billsChanged.collect { refresh() } }
    }

    fun refresh() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val (allTenants, allRooms) = coroutineScope {
                    val t = async { tenants.list(status = "all") }
                    val r = async { rooms.list(status = "all") }
                    t.await() to r.await()
                }
                val occupiedRooms = allRooms.count { it.status != "vacant" }
                val vacantBeds = allRooms.sumOf { it.vacantCount }
                val occupiedBeds = allRooms.sumOf { it.occupiedCount }
                _state.update {
                    it.copy(
                        loading = false,
                        stats = DashboardStats(
                            totalTenants = allTenants.size,
                            activeTenants = allTenants.count { t -> t.status == "active" },
                            totalRooms = allRooms.size,
                            occupiedRooms = occupiedRooms,
                            vacantBeds = vacantBeds,
                            occupiedBeds = occupiedBeds,
                        ),
                    )
                }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = t.message ?: "Failed to load dashboard") }
            }
        }
    }
}
