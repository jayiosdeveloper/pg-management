package com.pg.management.data.billing.remote

import com.pg.management.core.network.ApiEnvelope
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BillingApi {
    @GET("bills")
    suspend fun list(
        @Query("tenant_id") tenantId: String? = null,
        @Query("status") status: String? = "all",
        @Query("category") category: String? = "all",
        @Query("month") month: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
    ): ApiEnvelope<List<BillDto>>

    @GET("bills/{id}")
    suspend fun get(@Path("id") id: String): ApiEnvelope<BillDto>

    @POST("bills")
    suspend fun create(@Body req: CreateBillRequest): ApiEnvelope<BillDto>

    @DELETE("bills/{id}")
    suspend fun delete(@Path("id") id: String): ApiEnvelope<Unit>

    @POST("bills/{id}/payments")
    suspend fun recordPayment(@Path("id") id: String, @Body req: RecordPaymentRequest): ApiEnvelope<RecordPaymentResponse>

    @GET("bills/payments")
    suspend fun listPayments(
        @Query("tenant_id") tenantId: String? = null,
        @Query("bill_id") billId: String? = null,
    ): ApiEnvelope<List<PaymentDto>>

    @GET("bills/summary")
    suspend fun summary(): ApiEnvelope<BillSummaryDto>

    @POST("bills/bulk-generate")
    suspend fun bulkGenerate(@Body req: BulkGenerateRequest): ApiEnvelope<BulkGenerateResponse>

    @GET("invoices")
    suspend fun listInvoices(@Query("tenant_id") tenantId: String? = null): ApiEnvelope<List<InvoiceDto>>

    @POST("invoices/generate")
    suspend fun generateInvoice(@Body req: GenerateInvoiceRequest): ApiEnvelope<GenerateInvoiceResponse>

    @GET("bills/members-summary")
    suspend fun membersSummary(
        @Query("billing_month") billingMonth: String,
        @Query("category") category: String = "rent",
    ): ApiEnvelope<List<MembersSummaryRowDto>>

    @POST("bills/set-status")
    suspend fun setStatus(@Body req: SetStatusRequest): ApiEnvelope<MembersSummaryBill>
}
