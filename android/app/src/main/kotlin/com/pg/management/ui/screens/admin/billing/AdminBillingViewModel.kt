package com.pg.management.ui.screens.admin.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.domain.model.Bill
import com.pg.management.domain.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminBillingUi(
    val loading: Boolean = false,
    val bills: List<Bill> = emptyList(),
    val showBulkDialog: Boolean = false,
    val bulkSubmitting: Boolean = false,
    val recordingFor: Bill? = null,
    val recording: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

@HiltViewModel
class AdminBillingViewModel @Inject constructor(
    private val repo: BillingRepository,
) : ViewModel() {
    private val _s = MutableStateFlow(AdminBillingUi())
    val state: StateFlow<AdminBillingUi> = _s.asStateFlow()

    init { refresh() }

    fun refresh() {
        _s.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                _s.update { it.copy(loading = false, bills = repo.list(status = "all")) }
            } catch (e: Throwable) {
                _s.update { it.copy(loading = false, error = e.message ?: "Failed") }
            }
        }
    }

    fun showBulk(show: Boolean) = _s.update { it.copy(showBulkDialog = show) }
    fun openRecord(b: Bill?) = _s.update { it.copy(recordingFor = b) }
    fun consumeMessage() = _s.update { it.copy(message = null) }

    fun bulkGenerate(category: String, month: String, amount: Double, dueDay: Int, description: String?) {
        _s.update { it.copy(bulkSubmitting = true, error = null) }
        viewModelScope.launch {
            try {
                val (created, skipped) = repo.bulkGenerate(null, true, category, month, dueDay, amount, description)
                _s.update { it.copy(bulkSubmitting = false, showBulkDialog = false, message = "Created $created, skipped $skipped") }
                refresh()
            } catch (e: Throwable) {
                _s.update { it.copy(bulkSubmitting = false, error = e.message ?: "Failed") }
            }
        }
    }

    fun recordPayment(billId: String, amount: Double, method: String, reference: String?) {
        _s.update { it.copy(recording = true, error = null) }
        viewModelScope.launch {
            try {
                repo.recordPayment(billId, amount, method, reference, null)
                _s.update { it.copy(recording = false, recordingFor = null, message = "Payment recorded") }
                refresh()
            } catch (e: Throwable) {
                _s.update { it.copy(recording = false, error = e.message ?: "Failed") }
            }
        }
    }
}
