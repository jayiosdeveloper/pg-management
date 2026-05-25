package com.pg.management.core.network

class ApiException(
    val code: String,
    message: String,
    val httpStatus: Int? = null,
    val details: List<ApiFieldError>? = null,
) : Exception(message)

data class ApiFieldError(val path: String, val message: String)
