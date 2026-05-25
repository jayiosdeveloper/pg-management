package com.pg.management.ui.screens.admin.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.core.events.RefreshEvents
import com.pg.management.domain.model.MemberMonthStatus
import com.pg.management.domain.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AdminBillingUi(
    val loading: Boolean = false,
    val rows: List<MemberMonthStatus> = emptyList(),
    val month: String = LocalDate.now().toString().take(7),
    val updatingTenantId: String? = null,
    val partialFor: MemberMonthStatus? = null,
    val error: String? = null,
    val message: String? = null,
)

@HiltViewModel
class AdminBillingViewModel @Inject constructor(
    private val repo: BillingRepository,
    private val refreshEvents: RefreshEvents,
) : ViewModel() {
    private val _s = MutableStateFlow(AdminBillingUi())
    val state: StateFlow<AdminBillingUi> = _s.asStateFlow()

    init {
        refresh()
        viewModelScope.launch { refreshEvents.billsChanged.collect { refresh() } }
        viewModelScope.launch { refreshEvents.tenantsChanged.collect { refresh() } }
    }

    fun setMonth(month: String) { _s.update { it.copy(month = month) }; refresh() }
    fun openPartial(row: MemberMonthStatus?) = _s.update { it.copy(partialFor = row) }
    fun consumeMessage() = _s.update { it.copy(message = null) }

    fun refresh() {
        _s.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                _s.update { it.copy(loading = false, rows = repo.membersSummary(_s.value.month)) }
            } catch (e: Throwable) {
                _s.update { it.copy(loading = false, error = e.message ?: "Failed to load") }
            }
        }
    }

    fun mark(row: MemberMonthStatus, status: String, paidAmount: Double? = null) {
        _s.update { it.copy(updatingTenantId = row.tenantId, error = null) }
        viewModelScope.launch {
            try {
                repo.setStatus(row.tenantId, _s.value.month, status, category = "all", paidAmount = paidAmount)
                refreshEvents.notifyBillsChanged()
                _s.update { it.copy(updatingTenantId = null, partialFor = null) }
                refresh()
            } catch (e: Throwable) {
                _s.update { it.copy(updatingTenantId = null, error = e.message ?: "Failed") }
            }
        }
    }
}
