package com.pg.management.ui.screens.admin.billing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.core.events.RefreshEvents
import com.pg.management.domain.model.Bill
import com.pg.management.domain.model.Payment
import com.pg.management.domain.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillDetailUi(
    val loading: Boolean = true,
    val bill: Bill? = null,
    val payments: List<Payment> = emptyList(),
    val tenantPhone: String? = null,
    val recordOpen: Boolean = false,
    val recording: Boolean = false,
    val generatingPdf: Boolean = false,
    val pdfUrl: String? = null,
    val sharePromptVisible: Boolean = false,
    val deleting: Boolean = false,
    val deleted: Boolean = false,
    val error: String? = null,
    val message: String? = null,
)

@HiltViewModel
class BillDetailViewModel @Inject constructor(
    private val billing: BillingRepository,
    private val tenants: com.pg.management.domain.repository.TenantRepository,
    private val refreshEvents: RefreshEvents,
    savedState: SavedStateHandle,
) : ViewModel() {

    private val billId: String = checkNotNull(savedState["billId"]) { "billId arg missing" }
    private val _s = MutableStateFlow(BillDetailUi())
    val state: StateFlow<BillDetailUi> = _s.asStateFlow()

    init { refresh() }

    fun refresh() {
        _s.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val bill = billing.get(billId)
                val payments = billing.listPayments().filter { it.billId == billId }
                val phone = runCatching { tenants.get(bill.tenantId).user.phone }.getOrNull()
                _s.update { it.copy(loading = false, bill = bill, payments = payments, tenantPhone = phone) }
            } catch (e: Throwable) {
                _s.update { it.copy(loading = false, error = e.message ?: "Failed to load") }
            }
        }
    }

    fun openRecord(open: Boolean) = _s.update { it.copy(recordOpen = open) }

    fun recordPayment(amount: Double, method: String, reference: String?) {
        _s.update { it.copy(recording = true, error = null) }
        viewModelScope.launch {
            try {
                billing.recordPayment(billId, amount, method, reference, null)
                refreshEvents.notifyBillsChanged()
                _s.update { it.copy(recording = false, recordOpen = false, message = "Payment recorded") }
                refresh()
            } catch (e: Throwable) {
                _s.update { it.copy(recording = false, error = e.message ?: "Failed") }
            }
        }
    }

    /**
     * Generates a fresh invoice PDF for the bill's tenant + month and stores
     * the public URL. The UI can then offer WhatsApp share or browser open.
     */
    fun generatePdf() {
        val bill = _s.value.bill ?: return
        _s.update { it.copy(generatingPdf = true, error = null) }
        viewModelScope.launch {
            try {
                val url = billing.generateInvoice(bill.tenantId, bill.billingMonth.take(7))
                _s.update { it.copy(generatingPdf = false, pdfUrl = url, sharePromptVisible = url != null, message = if (url == null) "Invoice created, but no PDF URL (configure Cloudinary)" else null) }
            } catch (e: Throwable) {
                _s.update { it.copy(generatingPdf = false, error = e.message ?: "PDF failed") }
            }
        }
    }

    fun dismissSharePrompt() = _s.update { it.copy(sharePromptVisible = false) }
    fun consumeMessage() = _s.update { it.copy(message = null) }

    fun delete() {
        _s.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            try {
                billing.delete(billId)
                refreshEvents.notifyBillsChanged()
                _s.update { it.copy(deleting = false, deleted = true) }
            } catch (e: Throwable) {
                _s.update { it.copy(deleting = false, error = e.message ?: "Delete failed") }
            }
        }
    }
}
