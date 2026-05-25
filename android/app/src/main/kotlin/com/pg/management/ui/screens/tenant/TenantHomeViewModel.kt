package com.pg.management.ui.screens.tenant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.management.domain.auth.AuthRepository
import com.pg.management.domain.auth.AuthSession
import com.pg.management.domain.model.AppNotification
import com.pg.management.domain.model.Bill
import com.pg.management.domain.model.BillSummary
import com.pg.management.domain.model.Complaint
import com.pg.management.domain.model.Invoice
import com.pg.management.domain.model.Payment
import com.pg.management.domain.repository.BillingRepository
import com.pg.management.domain.repository.ComplaintRepository
import com.pg.management.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TenantHomeUi(
    val loading: Boolean = true,
    val session: AuthSession? = null,
    val summary: BillSummary = BillSummary(0.0, 0.0, 0.0, 0),
    val bills: List<Bill> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val invoices: List<Invoice> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val complaints: List<Complaint> = emptyList(),
    val loggingOut: Boolean = false,
    val loggedOut: Boolean = false,
    val error: String? = null,
    val submittingComplaint: Boolean = false,
    val complaintSubmitted: Boolean = false,
)

@HiltViewModel
class TenantHomeViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val bills: BillingRepository,
    private val notifications: NotificationRepository,
    private val complaints: ComplaintRepository,
) : ViewModel() {

    private val local = MutableStateFlow(TenantHomeUi())

    val state: StateFlow<TenantHomeUi> = combine(local, auth.sessionFlow) { ui, s -> ui.copy(session = s) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, TenantHomeUi())

    init { refresh() }

    fun refresh() {
        local.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                coroutineScope {
                    val summaryDef = async { this@TenantHomeViewModel.bills.summary() }
                    val billsDef = async { this@TenantHomeViewModel.bills.list(status = "all") }
                    val paymentsDef = async { this@TenantHomeViewModel.bills.listPayments() }
                    val invoicesDef = async { this@TenantHomeViewModel.bills.listInvoices() }
                    val notifsDef = async { this@TenantHomeViewModel.notifications.list() }
                    val compsDef = async { this@TenantHomeViewModel.complaints.list() }
                    val sum = summaryDef.await()
                    val bs = billsDef.await()
                    val ps = paymentsDef.await()
                    val invs = invoicesDef.await()
                    val ns = notifsDef.await()
                    val cs = compsDef.await()
                    local.update { it.copy(loading = false, summary = sum, bills = bs, payments = ps, invoices = invs, notifications = ns, complaints = cs) }
                }
            } catch (e: Throwable) {
                local.update { it.copy(loading = false, error = e.message ?: "Failed to load") }
            }
        }
    }

    fun logout() {
        local.update { it.copy(loggingOut = true) }
        viewModelScope.launch {
            runCatching { auth.logout() }
            local.update { it.copy(loggingOut = false, loggedOut = true) }
        }
    }
    fun consumeLogout() = local.update { it.copy(loggedOut = false) }

    fun markNotifRead(id: String) {
        viewModelScope.launch {
            runCatching { notifications.markRead(id) }
            local.update {
                it.copy(notifications = it.notifications.map { n -> if (n.id == id) n.copy(isRead = true) else n })
            }
        }
    }

    fun markAllNotifsRead() {
        viewModelScope.launch {
            runCatching { notifications.markAllRead() }
            local.update { it.copy(notifications = it.notifications.map { n -> n.copy(isRead = true) }) }
        }
    }

    fun submitComplaint(title: String, description: String, priority: String) {
        local.update { it.copy(submittingComplaint = true, error = null) }
        viewModelScope.launch {
            try {
                complaints.create(title, description, null, priority)
                local.update { it.copy(submittingComplaint = false, complaintSubmitted = true) }
                refresh()
            } catch (e: Throwable) {
                local.update { it.copy(submittingComplaint = false, error = e.message ?: "Failed") }
            }
        }
    }

    fun consumeComplaintSubmitted() = local.update { it.copy(complaintSubmitted = false) }
}

