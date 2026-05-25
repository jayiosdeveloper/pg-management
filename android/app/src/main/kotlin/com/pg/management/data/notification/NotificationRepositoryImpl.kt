package com.pg.management.data.notification

import com.pg.management.core.network.safeCall
import com.pg.management.data.notification.remote.FcmTokenRequest
import com.pg.management.data.notification.remote.NotificationApi
import com.pg.management.data.notification.remote.NotificationDto
import com.pg.management.data.notification.remote.SendNotificationRequest
import com.pg.management.domain.model.AppNotification
import com.pg.management.domain.repository.NotificationRepository
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

internal fun NotificationDto.toDomain() = AppNotification(id, title, body, type, isRead, sentAt)

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val api: NotificationApi,
    private val moshi: Moshi,
) : NotificationRepository {
    override suspend fun list(unreadOnly: Boolean): List<AppNotification> =
        safeCall(moshi) { api.list(if (unreadOnly) true else null) }.map { it.toDomain() }

    override suspend fun markRead(id: String) { safeCall(moshi) { api.markRead(id) } }

    override suspend fun markAllRead() { safeCall(moshi) { api.markAllRead() } }

    override suspend fun send(userId: String?, allTenants: Boolean, title: String, body: String, type: String) {
        safeCall(moshi) { api.send(SendNotificationRequest(userId, allTenants, title, body, type)) }
    }

    override suspend fun registerFcmToken(token: String) {
        safeCall(moshi) { api.registerFcmToken(FcmTokenRequest(token)) }
    }
}
