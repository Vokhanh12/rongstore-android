package com.aliasadi.data.mapper.common.v1

import com.aliasadi.domain.model.api.common.v1.*
import com.aliasadi.iam.client.dto.V1Error
import com.aliasadi.iam.client.dto.V1MutateResult

fun V1MutateResult.toDomainFromHttp(): MutationResult {
    val opIdSafe = opId ?: "unknown-op"

    return if (success == true) {
        MutationSuccess(
            opId = opIdSafe,
            resourceId = resourceId
                ?: error("MutationSuccess nhưng resourceId = null (opId=$opIdSafe)")
        )
    } else {
        MutationFailure(
            opId = opIdSafe,
            reason = error.toDomainFailureReason()
        )
    }
}

/* ================= Failure mapping ================= */

fun V1Error?.toDomainFailureReason(): MutationFailureReason =
    when (this?.code) {
        "PERMISSION_DENIED" ->
            MutationFailureReason.PermissionDenied

        "NOT_FOUND" ->
            MutationFailureReason.NotFound

        else ->
            MutationFailureReason.BusinessError(
                error = Error(
                    code = toDomainErrorCode(),
                    message = this?.message ?: "Unknown error",
                    retryable = false,
                    details = emptyList()
                )
            )
    }

private fun V1Error?.toDomainErrorCode(): ErrorCode =
    when (this?.code) {
        "PERMISSION_DENIED" -> ErrorCode.FORBIDDEN
        "NOT_FOUND" -> ErrorCode.NOT_FOUND
        "VALIDATION_ERROR" -> ErrorCode.VALIDATION_ERROR
        "CONFLICT" -> ErrorCode.CONFLICT
        else -> ErrorCode.UNKNOWN
    }
