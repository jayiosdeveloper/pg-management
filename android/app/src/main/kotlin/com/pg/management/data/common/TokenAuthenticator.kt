package com.pg.management.data.common

import com.pg.management.core.storage.AuthStorage
import com.pg.management.data.auth.remote.AuthApi
import com.pg.management.data.auth.remote.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

/**
 * When the server returns 401 we attempt one refresh-token exchange and retry the request.
 * If the refresh fails the session is cleared so the UI is forced back to the login screen.
 */
class TokenAuthenticator @Inject constructor(
    private val storage: AuthStorage,
    private val authApiProvider: Provider<AuthApi>,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite retry loops
        if (responseCount(response) >= 2) return null

        val currentRefresh = runBlocking { storage.currentSession()?.refreshToken } ?: return null

        val newAccess = runBlocking {
            try {
                val env = authApiProvider.get().refresh(RefreshRequest(currentRefresh))
                val data = env.data ?: return@runBlocking null
                storage.updateTokens(data.accessToken, data.refreshToken)
                data.accessToken
            } catch (_: Throwable) {
                storage.clear()
                null
            }
        } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var c = 1
        var prior = response.priorResponse
        while (prior != null) {
            c++
            prior = prior.priorResponse
        }
        return c
    }
}
