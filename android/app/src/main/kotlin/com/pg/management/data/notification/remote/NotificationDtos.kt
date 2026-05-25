package com.pg.management.data.notification.remote

import com.pg.management.core.network.ApiEnvelope
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class NotificationDto(
    val id: String,
    @Json(name = "user_id") val userId: String? = null,
    val title: String,
    val body: String,
    val type: String,
    @Json(name = "is_read") val isRead: Boolean,
    @Json(name = "sent_at") val sentAt: String,
)

@JsonClass(generateAdapter = true)
data class SendNotificationRequest(
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "all_tenants") val allTenants: Boolean = false,
    val title: String,
    val body: String,
    val type: String = "announcement",
)

@JsonClass(generateAdapter = true)
data class FcmTokenRequest(val token: String)

interface NotificationApi {
    @GET("notifications")
    suspend fun list(@Query("unread_only") unreadOnly: Boolean? = null): ApiEnvelope<List<NotificationDto>>

    @POST("notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String): ApiEnvelope<Unit>

    @POST("notifications/mark-all-read")
    suspend fun markAllRead(): ApiEnvelope<Unit>

    @POST("notifications")
    suspend fun send(@Body req: SendNotificationRequest): ApiEnvelope<Unit>

    @POST("notifications/fcm-token")
    suspend fun registerFcmToken(@Body req: FcmTokenRequest): ApiEnvelope<Unit>
}
