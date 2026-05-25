package com.pg.management.data.billing.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BillTenantUserDto(
    val id: String? = null,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "user_code") val userCode: String? = null,
    val phone: String? = null,
    val email: String? = null,
)

@JsonClass(generateAdapter = true)
data class BillTenantRoomDto(
    val id: String? = null,
    @Json(name = "room_number") val roomNumber: String? = null,
)

@JsonClass(generateAdapter = true)
data class BillTenantDto(
    val id: String,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "room_id") val roomId: String? = null,
    val user: BillTenantUserDto? = null,
    val room: BillTenantRoomDto? = null,
)

@JsonClass(generateAdapter = true)
data class BillDto(
    val id: String,
    @Json(name = "tenant_id") val tenantId: String,
    val category: String,
    val amount: Double,
    @Json(name = "amount_paid") val amountPaid: Double,
    @Json(name = "billing_month") val billingMonth: String,
    @Json(name = "due_date") val dueDate: String,
    val description: String? = null,
    val status: String,
    val tenant: BillTenantDto? = null,
)

@JsonClass(generateAdapter = true)
data class CreateBillRequest(
    @Json(name = "tenant_id") val tenantId: String,
    val category: String,
    val amount: Double,
    @Json(name = "billing_month") val billingMonth: String,
    @Json(name = "due_date") val dueDate: String,
    val description: String? = null,
)

@JsonClass(generateAdapter = true)
data class RecordPaymentRequest(
    val amount: Double,
    val method: String = "cash",
    @Json(name = "paid_at") val paidAt: String? = null,
    val reference: String? = null,
    val notes: String? = null,
)

@JsonClass(generateAdapter = true)
data class PaymentDto(
    val id: String,
    @Json(name = "bill_id") val billId: String,
    @Json(name = "tenant_id") val tenantId: String,
    val amount: Double,
    val method: String,
    @Json(name = "paid_at") val paidAt: String,
    val reference: String? = null,
    val notes: String? = null,
)

@JsonClass(generateAdapter = true)
data class RecordPaymentResponse(val bill: BillDto, val payment: PaymentDto)

@JsonClass(generateAdapter = true)
data class BillSummaryDto(
    @Json(name = "total_due") val totalDue: Double,
    val overdue: Double,
    @Json(name = "paid_this_month") val paidThisMonth: Double,
    @Json(name = "count_bills") val countBills: Int,
)

@JsonClass(generateAdapter = true)
data class BulkGenerateRequest(
    @Json(name = "tenant_ids") val tenantIds: List<String>? = null,
    @Json(name = "generate_for_all_active") val generateForAllActive: Boolean = false,
    val category: String,
    @Json(name = "billing_month") val billingMonth: String,
    @Json(name = "due_day") val dueDay: Int = 10,
    val amount: Double,
    val description: String? = null,
)

@JsonClass(generateAdapter = true)
data class BulkGenerateResponse(val created: Int, val skipped: Int)

@JsonClass(generateAdapter = true)
data class InvoiceDto(
    val id: String,
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "invoice_number") val invoiceNumber: String,
    @Json(name = "billing_month") val billingMonth: String,
    @Json(name = "total_amount") val totalAmount: Double,
    @Json(name = "paid_amount") val paidAmount: Double,
    @Json(name = "pending_amount") val pendingAmount: Double,
    @Json(name = "pdf_url") val pdfUrl: String? = null,
    @Json(name = "created_at") val createdAt: String,
)

@JsonClass(generateAdapter = true)
data class GenerateInvoiceRequest(
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "billing_month") val billingMonth: String,
)

@JsonClass(generateAdapter = true)
data class GenerateInvoiceResponse(
    @Json(name = "invoice_number") val invoiceNumber: String,
    @Json(name = "pdf_url") val pdfUrl: String? = null,
)

// Bills overhaul — member-summary list ---------------------------------
@JsonClass(generateAdapter = true)
data class MembersSummaryUser(
    val id: String? = null,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "user_code") val userCode: String? = null,
    val email: String? = null,
    val phone: String? = null,
)

@JsonClass(generateAdapter = true)
data class MembersSummaryRoom(
    val id: String? = null,
    @Json(name = "room_number") val roomNumber: String? = null,
)

@JsonClass(generateAdapter = true)
data class MembersSummaryBed(
    val id: String? = null,
    @Json(name = "bed_label") val bedLabel: String? = null,
)

@JsonClass(generateAdapter = true)
data class MembersSummaryBill(
    val id: String,
    val amount: Double,
    @Json(name = "amount_paid") val amountPaid: Double,
    val status: String,
)

@JsonClass(generateAdapter = true)
data class MembersSummaryBillBreakdown(
    val id: String,
    val category: String,
    val amount: Double,
    @Json(name = "amount_paid") val amountPaid: Double,
    val status: String,
    val description: String? = null,
)

@JsonClass(generateAdapter = true)
data class MembersSummaryRowDto(
    @Json(name = "tenant_id") val tenantId: String,
    val user: MembersSummaryUser?,
    val room: MembersSummaryRoom?,
    val bed: MembersSummaryBed?,
    @Json(name = "monthly_rent") val monthlyRent: Double,
    val bills: List<MembersSummaryBillBreakdown> = emptyList(),
    @Json(name = "total_amount") val totalAmount: Double = 0.0,
    @Json(name = "total_paid") val totalPaid: Double = 0.0,
    @Json(name = "total_pending") val totalPending: Double = 0.0,
    @Json(name = "expected_total") val expectedTotal: Double = 0.0,
    val status: String,
)

@JsonClass(generateAdapter = true)
data class SetStatusRequest(
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "billing_month") val billingMonth: String,
    val status: String,           // "paid" | "partial" | "unpaid"
    val category: String = "rent",
    val amount: Double? = null,
    @Json(name = "paid_amount") val paidAmount: Double? = null,
)
