package com.pg.management.ui.screens.admin.rooms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.domain.repository.RoomInput
import com.pg.management.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomFormUi(
    val roomId: String? = null,
    val roomNumber: String = "",
    val floor: String = "",
    val capacity: String = "1",
    val monthlyRent: String = "",
    val description: String = "",
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
) {
    val canSubmit get() = roomNumber.isNotBlank() && capacity.toIntOrNull() != null && capacity.toInt() > 0 && !saving
}

@HiltViewModel
class RoomFormViewModel @Inject constructor(
    private val repo: RoomRepository,
    savedState: SavedStateHandle,
) : ViewModel() {
    // Coerce empty-string nav arg to null so the "Add Room" flow is not mistaken for "Edit"
    private val _state = MutableStateFlow(
        RoomFormUi(roomId = (savedState.get<String>("roomId"))?.takeIf { it.isNotBlank() })
    )
    val state: StateFlow<RoomFormUi> = _state.asStateFlow()

    init { _state.value.roomId?.let { load(it) } }

    fun update(transform: (RoomFormUi) -> RoomFormUi) = _state.update(transform)

    private fun load(id: String) {
        viewModelScope.launch {
            try {
                val r = repo.get(id)
                _state.update {
                    it.copy(
                        roomNumber = r.roomNumber,
                        floor = r.floor?.toString().orEmpty(),
                        capacity = r.capacity.toString(),
                        monthlyRent = r.monthlyRent.toString(),
                        description = r.description.orEmpty(),
                    )
                }
            } catch (e: Throwable) {
                _state.update { it.copy(error = e.message ?: "Failed to load room") }
            }
        }
    }

    fun submit() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                val input = RoomInput(
                    roomNumber = s.roomNumber.trim(),
                    floor = s.floor.toIntOrNull(),
                    capacity = s.capacity.toInt(),
                    monthlyRent = s.monthlyRent.toDoubleOrNull() ?: 0.0,
                    description = s.description.trim().ifBlank { null },
                )
                if (s.roomId == null) repo.create(input) else repo.update(s.roomId, input)
                _state.update { it.copy(saving = false, saved = true) }
            } catch (e: Throwable) {
                _state.update { it.copy(saving = false, error = e.message ?: "Save failed") }
            }
        }
    }
}
