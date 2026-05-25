package com.pg.management.data.common

import com.pg.management.core.storage.AuthStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Attaches the Bearer access token (if any) to every outgoing request. */
class AuthInterceptor @Inject constructor(
    private val storage: AuthStorage,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        // Skip auth-less endpoints to avoid recursion
        val path = req.url.encodedPath
        if (path.endsWith("/auth/login") || path.endsWith("/auth/refresh")) {
            return chain.proceed(req)
        }
        val token = runBlocking { storage.currentSession()?.accessToken }
        val updated = if (token.isNullOrBlank()) req
        else req.newBuilder().header("Authorization", "Bearer $token").build()
        return chain.proceed(updated)
    }
}
