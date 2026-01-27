package com.aliasadi.data.mapper.common.v1

import com.aliasadi.domain.model.api.common.v1.MutationFailure
import com.aliasadi.domain.model.api.common.v1.MutationFailureReason
import com.aliasadi.domain.model.api.common.v1.MutationResult
import com.aliasadi.domain.model.api.common.v1.MutationSuccess
import com.aliasadi.iam.client.dto.V1MutateResult

fun V1MutateResult.toDomain(): MutationResult {
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