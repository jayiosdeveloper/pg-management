package com.pg.management.core.result

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Error(val code: String, val message: String, val httpStatus: Int? = null) : ApiResult<Nothing>
    data object Loading : ApiResult<Nothing>
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(value))
    is ApiResult.Error -> this
    ApiResult.Loading -> ApiResult.Loading
}
