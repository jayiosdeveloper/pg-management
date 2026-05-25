package com.pg.management.ui.screens.admin.tenants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.core.events.RefreshEvents
import com.pg.management.domain.model.Tenant
import com.pg.management.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TenantsListUi(
    val loading: Boolean = false,
    val query: String = "",
    val statusFilter: String = "active",
    val tenants: List<Tenant> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class TenantsListViewModel @Inject constructor(
    private val repo: TenantRepository,
    refreshEvents: RefreshEvents,
) : ViewModel() {
    private val _state = MutableStateFlow(TenantsListUi())
    val state: StateFlow<TenantsListUi> = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch { refreshEvents.tenantsChanged.collect { refresh() } }
    }

    fun onQueryChange(q: String) { _state.update { it.copy(query = q) } }
    fun onStatusFilter(f: String) { _state.update { it.copy(statusFilter = f) }; refresh() }

    fun refresh() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val list = repo.list(query = _state.value.query, status = _state.value.statusFilter)
                _state.update { it.copy(loading = false, tenants = list) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = t.message ?: "Failed to load") }
            }
        }
    }
}
