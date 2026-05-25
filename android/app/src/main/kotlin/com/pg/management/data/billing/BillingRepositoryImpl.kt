package com.pg.management.data.billing

import com.pg.management.core.network.safeCall
import com.pg.management.data.billing.remote.BillDto
import com.pg.management.data.billing.remote.BillingApi
import com.pg.management.data.billing.remote.BulkGenerateRequest
import com.pg.management.data.billing.remote.CreateBillRequest
import com.pg.management.data.billing.remote.GenerateInvoiceRequest
import com.pg.management.data.billing.remote.InvoiceDto
import com.pg.management.data.billing.remote.PaymentDto
import com.pg.management.data.billing.remote.RecordPaymentRequest
import com.pg.management.domain.model.Bill
import com.pg.management.domain.model.BillSummary
import com.pg.management.domain.model.Invoice
import com.pg.management.domain.model.Payment
import com.pg.management.domain.repository.BillInput
import com.pg.management.domain.repository.BillingRepository
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

internal fun BillDto.toDomain() = Bill(
    id = id,
    tenantId = tenantId,
    tenantName = tenant?.user?.fullName,
    tenantUserCode = tenant?.user?.userCode,
    roomNumber = tenant?.room?.roomNumber,
    category = category,
    amount = amount,
    amountPaid = amountPaid,
    billingMonth = billingMonth.take(10),
    dueDate = dueDate,
    description = description,
    status = status,
)

internal fun PaymentDto.toDomain() = Payment(
    id = id, billId = billId, tenantId = tenantId, amount = amount,
    method = method, paidAt = paidAt, reference = reference, notes = notes,
)

internal fun InvoiceDto.toDomain() = Invoice(
    id, tenantId, invoiceNumber, billingMonth.take(10),
    totalAmount, paidAmount, pendingAmount, pdfUrl, createdAt,
)

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val api: BillingApi,
    private val moshi: Moshi,
) : BillingRepository {

    override suspend fun list(tenantId: String?, status: String, category: String, month: String?): List<Bill> =
        safeCall(moshi) { api.list(tenantId, status, category, month) }.map { it.toDomain() }

    override suspend fun get(id: String): Bill = safeCall(moshi) { api.get(id) }.toDomain()

    override suspend fun create(input: BillInput): Bill {
        val req = CreateBillRequest(
            tenantId = input.tenantId, category = input.category, amount = input.amount,
            billingMonth = input.billingMonth, dueDate = input.dueDate, description = input.description,
        )
        return safeCall(moshi) { api.create(req) }.toDomain()
    }

    override suspend fun delete(id: String) { safeCall(moshi) { api.delete(id) } }

    override suspend fun recordPayment(billId: String, amount: Double, method: String, reference: String?, notes: String?): Pair<Bill, Payment> {
        val r = safeCall(moshi) { api.recordPayment(billId, RecordPaymentRequest(amount, method, null, reference, notes)) }
        return r.bill.toDomain() to r.payment.toDomain()
    }

    override suspend fun listPayments(tenantId: String?): List<Payment> =
        safeCall(moshi) { api.listPayments(tenantId) }.map { it.toDomain() }

    override suspend fun summary(): BillSummary {
        val s = safeCall(moshi) { api.summary() }
        return BillSummary(s.totalDue, s.overdue, s.paidThisMonth, s.countBills)
    }

    override suspend fun bulkGenerate(tenantIds: List<String>?, allActive: Boolean, category: String, billingMonth: String, dueDay: Int, amount: Double, description: String?): Pair<Int, Int> {
        val r = safeCall(moshi) { api.bulkGenerate(BulkGenerateRequest(tenantIds, allActive, category, billingMonth, dueDay, amount, description)) }
        return r.created to r.skipped
    }

    override suspend fun listInvoices(tenantId: String?): List<Invoice> =
        safeCall(moshi) { api.listInvoices(tenantId) }.map { it.toDomain() }

    override suspend fun generateInvoice(tenantId: String, billingMonth: String): String? =
        safeCall(moshi) { api.generateInvoice(GenerateInvoiceRequest(tenantId, billingMonth)) }.pdfUrl
}
