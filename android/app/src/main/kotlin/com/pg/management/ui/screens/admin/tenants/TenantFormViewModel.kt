package com.pg.management.ui.screens.admin.tenants

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.domain.model.Room
import com.pg.management.domain.model.TenantCredentials
import com.pg.management.domain.repository.RoomRepository
import com.pg.management.domain.repository.TenantInput
import com.pg.management.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TenantFormUi(
    val tenantId: String? = null,        // null = create
    val loading: Boolean = false,
    val saving: Boolean = false,
    val rooms: List<Room> = emptyList(),
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",
    val city: String = "",
    val state: String = "",
    val address: String = "",
    val emergencyName: String = "",
    val emergencyPhone: String = "",
    val occupation: String = "",
    val idProofType: String = "aadhaar",
    val idProofNumber: String = "",
    val roomId: String? = null,
    val bedId: String? = null,
    val joiningDate: String = LocalDate.now().toString(),
    val monthlyRent: String = "",
    val securityDeposit: String = "",
    val notes: String = "",
    val error: String? = null,
    val savedCredentials: TenantCredentials? = null,
    val savedTenantId: String? = null,
) {
    val canSubmit: Boolean get() = fullName.isNotBlank() && joiningDate.isNotBlank() && !saving
}

@HiltViewModel
class TenantFormViewModel @Inject constructor(
    private val tenants: TenantRepository,
    private val rooms: RoomRepository,
    savedState: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(TenantFormUi(tenantId = savedState["tenantId"]))
    val state: StateFlow<TenantFormUi> = _state.asStateFlow()

    init {
        loadRooms()
        _state.value.tenantId?.let { load(it) }
    }

    fun update(transform: (TenantFormUi) -> TenantFormUi) = _state.update(transform)

    private fun loadRooms() {
        viewModelScope.launch {
            try {
                val r = rooms.list(status = "all")
                _state.update { it.copy(rooms = r) }
            } catch (_: Throwable) { /* tolerate missing rooms */ }
        }
    }

    private fun load(id: String) {
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            try {
                val t = tenants.get(id)
                _state.update {
                    it.copy(
                        loading = false,
                        fullName = t.user.fullName,
                        email = t.user.email.orEmpty(),
                        phone = t.user.phone.orEmpty(),
                        gender = t.gender.orEmpty(),
                        city = t.city.orEmpty(),
                        state = t.state.orEmpty(),
                        address = t.address.orEmpty(),
                        emergencyName = t.emergencyContactName.orEmpty(),
                        emergencyPhone = t.emergencyContactPhone.orEmpty(),
                        occupation = t.occupation.orEmpty(),
                        idProofType = t.idProofType ?: "aadhaar",
                        idProofNumber = t.idProofNumber.orEmpty(),
                        roomId = t.room?.id,
                        bedId = t.bed?.id,
                        joiningDate = t.joiningDate,
                        monthlyRent = t.monthlyRent?.toString().orEmpty(),
                        securityDeposit = t.securityDeposit?.toString().orEmpty(),
                        notes = t.notes.orEmpty(),
                    )
                }
            } catch (e: Throwable) {
                _state.update { it.copy(loading = false, error = e.message ?: "Failed to load tenant") }
            }
        }
    }

    fun submit() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                val input = TenantInput(
                    fullName = s.fullName.trim(),
                    email = s.email.trim().ifBlank { null },
                    phone = s.phone.trim().ifBlank { null },
                    gender = s.gender.trim().ifBlank { null },
                    city = s.city.trim().ifBlank { null },
                    state = s.state.trim().ifBlank { null },
                    address = s.address.trim().ifBlank { null },
                    emergencyContactName = s.emergencyName.trim().ifBlank { null },
                    emergencyContactPhone = s.emergencyPhone.trim().ifBlank { null },
                    occupation = s.occupation.trim().ifBlank { null },
                    idProofType = s.idProofType.trim().ifBlank { null },
                    idProofNumber = s.idProofNumber.trim().ifBlank { null },
                    roomId = s.roomId,
                    bedId = s.bedId,
                    joiningDate = s.joiningDate,
                    monthlyRent = s.monthlyRent.toDoubleOrNull(),
                    securityDeposit = s.securityDeposit.toDoubleOrNull(),
                    notes = s.notes.trim().ifBlank { null },
                )
                if (s.tenantId == null) {
                    val (tenant, creds) = tenants.create(input)
                    _state.update { it.copy(saving = false, savedCredentials = creds, savedTenantId = tenant.id) }
                } else {
                    val updated = tenants.update(s.tenantId, input)
                    _state.update { it.copy(saving = false, savedTenantId = updated.id) }
                }
            } catch (e: Throwable) {
                _state.update { it.copy(saving = false, error = e.message ?: "Save failed") }
            }
        }
    }

    fun acknowledgeSave() = _state.update { it.copy(savedCredentials = null, savedTenantId = null) }
}
