package com.pg.management.ui.screens.admin.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.core.events.RefreshEvents
import com.pg.management.domain.model.Room
import com.pg.management.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomsListUi(
    val loading: Boolean = false,
    val rooms: List<Room> = emptyList(),
    val filter: String = "all",
    val error: String? = null,
)

@HiltViewModel
class RoomsListViewModel @Inject constructor(
    private val repo: RoomRepository,
    refreshEvents: RefreshEvents,
) : ViewModel() {
    private val _state = MutableStateFlow(RoomsListUi())
    val state: StateFlow<RoomsListUi> = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch { refreshEvents.roomsChanged.collect { refresh() } }
    }

    fun onFilter(f: String) { _state.update { it.copy(filter = f) }; refresh() }

    fun refresh() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val rooms = repo.list(status = _state.value.filter)
                _state.update { it.copy(loading = false, rooms = rooms) }
            } catch (e: Throwable) {
                _state.update { it.copy(loading = false, error = e.message ?: "Failed") }
            }
        }
    }
}
