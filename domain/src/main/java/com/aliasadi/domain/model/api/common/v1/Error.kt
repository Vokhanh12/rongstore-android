package com.aliasadi.domain.model.api.common.v1

enum class ErrorCode {
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    VALIDATION_ERROR,
    CONFLICT,
    RATE_LIMITED,
    INTERNAL_ERROR,
    UNKNOWN
}

data class ErrorDetail(
    val field: String?,
    val message: String
)

data class Error(
    val code: ErrorCode,
    val message: String,
    val retryable: Boolean,
    val details: List<ErrorDetail> = emptyList()
)
