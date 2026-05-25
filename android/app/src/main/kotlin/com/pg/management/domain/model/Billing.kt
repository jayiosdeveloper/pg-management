package com.pg.management.domain.model

data class Bill(
    val id: String,
    val tenantId: String,
    val tenantName: String?,
    val tenantUserCode: String?,
    val roomNumber: String?,
    val category: String,
    val amount: Double,
    val amountPaid: Double,
    val billingMonth: String,
    val dueDate: String,
    val description: String?,
    val status: String,
) {
    val pending: Double get() = (amount - amountPaid).coerceAtLeast(0.0)
}

data class Payment(
    val id: String,
    val billId: String,
    val tenantId: String,
    val amount: Double,
    val method: String,
    val paidAt: String,
    val reference: String?,
    val notes: String?,
)

data class BillSummary(
    val totalDue: Double,
    val overdue: Double,
    val paidThisMonth: Double,
    val countBills: Int,
)

data class Invoice(
    val id: String,
    val tenantId: String,
    val invoiceNumber: String,
    val billingMonth: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val pdfUrl: String?,
    val createdAt: String,
)
