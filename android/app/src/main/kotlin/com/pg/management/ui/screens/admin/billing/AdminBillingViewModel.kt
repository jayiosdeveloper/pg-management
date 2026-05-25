package com.pg.management.ui.screens.admin.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.core.events.RefreshEvents
import com.pg.management.domain.model.Bill
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
    val bills: List<Bill> = emptyList(),
    // Month filter: YYYY-MM. Default = current month.
    val month: String = LocalDate.now().toString().take(7),
    val showGenerateDialog: Boolean = false,
    val generating: Boolean = false,
    val message: String? = null,
    val error: String? = null,
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
    }

    fun setMonth(month: String) { _s.update { it.copy(month = month) }; refresh() }
    fun showGenerate(show: Boolean) = _s.update { it.copy(showGenerateDialog = show) }
    fun consumeMessage() = _s.update { it.copy(message = null) }

    fun refresh() {
        _s.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val rows = repo.list(status = "all", month = _s.value.month)
                _s.update { it.copy(loading = false, bills = rows) }
            } catch (e: Throwable) {
                _s.update { it.copy(loading = false, error = e.message ?: "Failed") }
            }
        }
    }

    /**
     * Generates a rent bill for every active member for the selected month
     * using each member's monthly_rent (or their room's). One tap.
     */
    fun generateRentForMonth(dueDay: Int = 10) {
        _s.update { it.copy(generating = true, error = null) }
        viewModelScope.launch {
            try {
                val (created, skipped) = repo.bulkGenerate(
                    tenantIds = null,
                    allActive = true,
                    category = "rent",
                    billingMonth = _s.value.month,
                    dueDay = dueDay,
                    amount = 0.0,           // server falls back to tenant.monthly_rent
                    description = null,
                )
                refreshEvents.notifyBillsChanged()
                _s.update {
                    it.copy(
                        generating = false,
                        showGenerateDialog = false,
                        message = if (created > 0) "Created $created bill(s). Skipped $skipped." else "All members already have a bill for this month.",
                    )
                }
                refresh()
            } catch (e: Throwable) {
                _s.update { it.copy(generating = false, error = e.message ?: "Failed") }
            }
        }
    }
}
