package com.pg.management.data.worker.remote

import com.pg.management.core.network.ApiEnvelope
import com.pg.management.data.tenant.remote.ResetPasswordRequest
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkerApi {
    @GET("workers")
    suspend fun list(
        @Query("q") q: String? = null,
        @Query("status") status: String? = "active",
    ): ApiEnvelope<List<WorkerDto>>

    @GET("workers/{id}")
    suspend fun get(@Path("id") id: String): ApiEnvelope<WorkerDto>

    @POST("workers")
    suspend fun create(@Body req: CreateWorkerRequest): ApiEnvelope<CreateWorkerResponse>

    @PATCH("workers/{id}")
    suspend fun update(@Path("id") id: String, @Body req: UpdateWorkerRequest): ApiEnvelope<WorkerDto>

    @DELETE("workers/{id}")
    suspend fun delete(@Path("id") id: String): ApiEnvelope<Unit>

    @Multipart
    @POST("workers/{id}/photo")
    suspend fun uploadPhoto(@Path("id") id: String, @Part file: MultipartBody.Part): ApiEnvelope<WorkerUploadResponse>

    @Multipart
    @POST("workers/{id}/aadhaar-front")
    suspend fun uploadAadhaarFront(@Path("id") id: String, @Part file: MultipartBody.Part): ApiEnvelope<WorkerUploadResponse>

    @Multipart
    @POST("workers/{id}/aadhaar-back")
    suspend fun uploadAadhaarBack(@Path("id") id: String, @Part file: MultipartBody.Part): ApiEnvelope<WorkerUploadResponse>

    @GET("workers/{id}/credentials")
    suspend fun credentials(@Path("id") id: String): ApiEnvelope<WorkerCredentialsResponse>

    @POST("workers/{id}/reset-password")
    suspend fun resetPassword(@Path("id") id: String, @Body req: ResetPasswordRequest): ApiEnvelope<com.pg.management.data.tenant.remote.ResetPasswordResponse>

    @POST("workers/{id}/salary")
    suspend fun recordSalary(@Path("id") id: String, @Body req: RecordSalaryRequest): ApiEnvelope<SalaryPaymentDto>

    @GET("workers/salary-payments")
    suspend fun listSalary(
        @Query("worker_id") workerId: String? = null,
        @Query("month") month: String? = null,
    ): ApiEnvelope<List<SalaryPaymentDto>>
}
