package com.pg.management.core.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val code: String? = null,
    val details: List<ApiFieldError>? = null,
)
