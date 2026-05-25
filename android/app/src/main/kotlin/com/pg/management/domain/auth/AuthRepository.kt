package com.pg.management.domain.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val sessionFlow: Flow<AuthSession?>
    suspend fun currentSession(): AuthSession?
    suspend fun login(identifier: String, password: String, remember: Boolean): AuthSession
    suspend fun logout()
    suspend fun changePassword(current: String, new: String)
}
