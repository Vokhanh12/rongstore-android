package com.aliasadi.data.mapper.common.v1

import com.aliasadi.domain.model.api.common.v1.ErrorCode
import common.v1.ErrorOuterClass
import com.aliasadi.domain.model.api.common.v1.Error
import com.aliasadi.domain.model.api.common.v1.ErrorDetail
import com.aliasadi.domain.model.api.common.v1.MutationFailureReason

fun ErrorOuterClass.Error.toDomainError(): Error {
    return Error(
        code = toDomainCode(),
        message = message,
        retryable = retryable,
        details = detailsList.map {
            ErrorDetail(
                field = it.field.takeIf { f -> f.isNotBlank() },
                message = it.message
            )
        }
    )
}

private fun ErrorOuterClass.Error.toDomainCode(): ErrorCode =
    when (code) {
        "AUTH-HAND-001" -> ErrorCode.UNAUTHORIZED
        "AUTH-HAND-403" -> ErrorCode.FORBIDDEN
        "NOT_FOUND" -> ErrorCode.NOT_FOUND
        "VALIDATION_ERROR" -> ErrorCode.VALIDATION_ERROR
        "CONFLICT" -> ErrorCode.CONFLICT
        "RATE_LIMIT" -> ErrorCode.RATE_LIMITED
        else -> ErrorCode.UNKNOWN
    }


fun ErrorOuterClass.Error.toDomainReason(): MutationFailureReason =
    when (code) {
        "NOT_FOUND" ->
            MutationFailureReason.NotFound

        "AUTH-HAND-403" ->
            MutationFailureReason.PermissionDenied

        else ->
            MutationFailureReason.BusinessError(
                error = toDomainError()
            )
    }