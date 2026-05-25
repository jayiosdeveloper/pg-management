package com.pg.management.domain.repository

import com.pg.management.domain.model.Bill
import com.pg.management.domain.model.BillSummary
import com.pg.management.domain.model.Invoice
import com.pg.management.domain.model.MemberMonthStatus
import com.pg.management.domain.model.Payment

interface BillingRepository {
    suspend fun list(tenantId: String? = null, status: String = "all", category: String = "all", month: String? = null): List<Bill>
    suspend fun get(id: String): Bill
    suspend fun create(input: BillInput): Bill
    suspend fun delete(id: String)
    suspend fun recordPayment(billId: String, amount: Double, method: String, reference: String?, notes: String?): Pair<Bill, Payment>
    suspend fun listPayments(tenantId: String? = null): List<Payment>
    suspend fun summary(): BillSummary
    suspend fun bulkGenerate(tenantIds: List<String>?, allActive: Boolean, category: String, billingMonth: String, dueDay: Int, amount: Double, description: String?): Pair<Int, Int>
    suspend fun listInvoices(tenantId: String? = null): List<Invoice>
    suspend fun generateInvoice(tenantId: String, billingMonth: String): String?

    suspend fun membersSummary(billingMonth: String): List<MemberMonthStatus>
    suspend fun setStatus(tenantId: String, billingMonth: String, status: String, amount: Double? = null, paidAmount: Double? = null)
}

data class BillInput(
    val tenantId: String,
    val category: String,
    val amount: Double,
    val billingMonth: String,
    val dueDate: String,
    val description: String? = null,
)
