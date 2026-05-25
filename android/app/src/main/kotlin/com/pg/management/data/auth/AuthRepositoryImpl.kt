package com.pg.management.data.auth

import com.pg.management.core.network.safeCall
import com.pg.management.core.storage.AuthStorage
import com.pg.management.data.auth.remote.AuthApi
import com.pg.management.data.auth.remote.ChangePasswordRequest
import com.pg.management.data.auth.remote.LoginRequest
import com.pg.management.data.auth.remote.LogoutRequest
import com.pg.management.domain.auth.AuthRepository
import com.pg.management.domain.auth.AuthSession
import com.pg.management.domain.model.UserRole
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val storage: AuthStorage,
    private val moshi: Moshi,
) : AuthRepository {

    override val sessionFlow: Flow<AuthSession?> = storage.sessionFlow

    override suspend fun currentSession(): AuthSession? = storage.currentSession()

    override suspend fun login(identifier: String, password: String, remember: Boolean): AuthSession {
        val data = safeCall(moshi) { api.login(LoginRequest(identifier, password)) }
        val role = UserRole.fromString(data.user.role)
            ?: throw IllegalStateException("Unknown role: ${data.user.role}")
        val session = AuthSession(
            accessToken = data.accessToken,
            refreshToken = data.refreshToken,
            userId = data.user.id,
            userCode = data.user.userCode,
            fullName = data.user.fullName,
            email = data.user.email,
            role = role,
        )
        storage.save(session, remember)
        return session
    }

    override suspend fun logout() {
        val refresh = storage.currentSession()?.refreshToken
        runCatching { safeCall(moshi) { api.logout(LogoutRequest(refresh)) } }
        storage.clear()
    }

    override suspend fun changePassword(current: String, new: String) {
        safeCall(moshi) { api.changePassword(ChangePasswordRequest(current, new)) }
    }
}
