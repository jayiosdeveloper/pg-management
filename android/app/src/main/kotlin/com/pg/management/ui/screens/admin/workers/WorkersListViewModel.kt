package com.pg.management.ui.screens.admin.workers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.core.events.RefreshEvents
import com.pg.management.domain.model.Worker
import com.pg.management.domain.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkersListUi(
    val loading: Boolean = false,
    val query: String = "",
    val statusFilter: String = "active",
    val workers: List<Worker> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class WorkersListViewModel @Inject constructor(
    private val repo: WorkerRepository,
    refreshEvents: RefreshEvents,
) : ViewModel() {
    private val _s = MutableStateFlow(WorkersListUi())
    val state: StateFlow<WorkersListUi> = _s.asStateFlow()

    init {
        refresh()
        viewModelScope.launch { refreshEvents.workersChanged.collect { refresh() } }
    }

    fun onQueryChange(q: String) { _s.update { it.copy(query = q) } }
    fun onStatusFilter(f: String) { _s.update { it.copy(statusFilter = f) }; refresh() }

    fun refresh() {
        _s.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                _s.update { it.copy(loading = false, workers = repo.list(_s.value.query, _s.value.statusFilter)) }
            } catch (e: Throwable) {
                _s.update { it.copy(loading = false, error = e.message ?: "Failed") }
            }
        }
    }
}
