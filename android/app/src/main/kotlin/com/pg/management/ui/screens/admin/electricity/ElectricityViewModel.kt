package com.pg.management.ui.screens.admin.electricity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.core.events.RefreshEvents
import com.pg.management.domain.model.ElectricityReading
import com.pg.management.domain.model.Room
import com.pg.management.domain.repository.ElectricityRepository
import com.pg.management.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ElectricityUi(
    val loading: Boolean = false,
    val month: String = LocalDate.now().toString().take(7),
    val readings: List<ElectricityReading> = emptyList(),
    val rooms: List<Room> = emptyList(),

    // Add reading form
    val showForm: Boolean = false,
    val formRoomId: String? = null,
    val formStartReading: String = "",
    val formEndReading: String = "",
    val formRatePerUnit: String = "10",
    val formNotes: String = "",
    val submitting: Boolean = false,
    val deletingId: String? = null,
    val error: String? = null,
    val message: String? = null,
) {
    // Live preview based on form values
    val previewUnits: Double get() {
        val s = formStartReading.toDoubleOrNull() ?: 0.0
        val e = formEndReading.toDoubleOrNull() ?: 0.0
        return (e - s).coerceAtLeast(0.0)
    }
    val previewTotal: Double get() = previewUnits * (formRatePerUnit.toDoubleOrNull() ?: 0.0)
    val previewMemberCount: Int get() {
        val r = rooms.firstOrNull { it.id == formRoomId } ?: return 0
        return r.occupiedCount.coerceAtLeast(1)
    }
    val previewPerMember: Double get() =
        if (previewMemberCount > 0) previewTotal / previewMemberCount else 0.0

    val canSubmit: Boolean get() =
        !submitting && formRoomId != null &&
        previewUnits > 0 && (formRatePerUnit.toDoubleOrNull() ?: 0.0) > 0
}

@HiltViewModel
class ElectricityViewModel @Inject constructor(
    private val electricity: ElectricityRepository,
    private val rooms: RoomRepository,
    private val refreshEvents: RefreshEvents,
) : ViewModel() {
    private val _s = MutableStateFlow(ElectricityUi())
    val state: StateFlow<ElectricityUi> = _s.asStateFlow()

    init {
        load()
        viewModelScope.launch { refreshEvents.billsChanged.collect { load() } }
        viewModelScope.launch { refreshEvents.tenantsChanged.collect { load() } }
        viewModelScope.launch { refreshEvents.roomsChanged.collect { load() } }
    }

    fun setMonth(m: String) { _s.update { it.copy(month = m) }; load() }
    fun openForm(open: Boolean) = _s.update { it.copy(showForm = open, error = null) }
    fun consumeMessage() = _s.update { it.copy(message = null) }
    fun update(transform: (ElectricityUi) -> ElectricityUi) = _s.update(transform)

    fun load() {
        _s.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val rs = electricity.list(_s.value.month)
                val allRooms = runCatching { rooms.list(status = "all") }.getOrDefault(emptyList())
                _s.update { it.copy(loading = false, readings = rs, rooms = allRooms) }
            } catch (e: Throwable) {
                _s.update { it.copy(loading = false, error = e.message ?: "Failed") }
            }
        }
    }

    fun submitReading() {
        val s = _s.value
        if (!s.canSubmit) return
        _s.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            try {
                val (_, billsCreated) = electricity.create(
                    roomId = s.formRoomId!!,
                    billingMonth = s.month,
                    startReading = s.formStartReading.toDouble(),
                    endReading = s.formEndReading.toDouble(),
                    ratePerUnit = s.formRatePerUnit.toDouble(),
                    notes = s.formNotes.trim().ifBlank { null },
                )
                refreshEvents.notifyBillsChanged()
                _s.update {
                    it.copy(
                        submitting = false, showForm = false,
                        formRoomId = null, formStartReading = "", formEndReading = "", formRatePerUnit = "10", formNotes = "",
                        message = "Created. $billsCreated electricity bill(s) added.",
                    )
                }
                load()
            } catch (e: Throwable) {
                _s.update { it.copy(submitting = false, error = e.message ?: "Failed") }
            }
        }
    }

    fun deleteReading(id: String) {
        _s.update { it.copy(deletingId = id, error = null) }
        viewModelScope.launch {
            try {
                electricity.delete(id)
                refreshEvents.notifyBillsChanged()
                _s.update { it.copy(deletingId = null) }
                load()
            } catch (e: Throwable) {
                _s.update { it.copy(deletingId = null, error = e.message ?: "Failed") }
            }
        }
    }
}
