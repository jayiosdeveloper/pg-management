package com.pg.management.domain.repository

import com.pg.management.domain.model.AppNotification

interface NotificationRepository {
    suspend fun list(unreadOnly: Boolean = false): List<AppNotification>
    suspend fun markRead(id: String)
    suspend fun markAllRead()
    suspend fun send(userId: String?, allTenants: Boolean, title: String, body: String, type: String)
    suspend fun registerFcmToken(token: String)
}
