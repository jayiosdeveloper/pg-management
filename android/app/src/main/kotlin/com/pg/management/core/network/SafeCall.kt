package com.pg.management.core.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import retrofit2.HttpException
import java.io.IOException

/**
 * Wraps a Retrofit suspend call returning ApiEnvelope<T> and turns it into a normal value
 * or an ApiException with a structured code/message.
 */
suspend inline fun <reified T> safeCall(moshi: Moshi, crossinline block: suspend () -> ApiEnvelope<T>): T {
    return try {
        val env = block()
        if (env.success && env.data != null) {
            env.data
        } else {
            throw ApiException(
                code = env.code ?: "UNKNOWN",
                message = env.message ?: "Unknown error",
                details = env.details,
            )
        }
    } catch (e: HttpException) {
        val body = e.response()?.errorBody()?.string()
        val parsed = parseErrorBody(moshi, body)
        throw ApiException(
            code = parsed?.code ?: "HTTP_${e.code()}",
            message = parsed?.message ?: e.message(),
            httpStatus = e.code(),
            details = parsed?.details,
        )
    } catch (e: IOException) {
        throw ApiException(code = "NETWORK", message = e.message ?: "Network error")
    }
}

@PublishedApi
internal fun parseErrorBody(moshi: Moshi, body: String?): ApiEnvelope<Any>? {
    if (body.isNullOrBlank()) return null
    return try {
        val type = Types.newParameterizedType(ApiEnvelope::class.java, Any::class.java)
        val adapter: JsonAdapter<ApiEnvelope<Any>> = moshi.adapter(type)
        adapter.fromJson(body)
    } catch (_: Throwable) { null }
}
