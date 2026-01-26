package com.aliasadi.data.mapper.common.v1

import com.aliasadi.domain.model.api.common.v1.MutationFailure
import com.aliasadi.domain.model.api.common.v1.MutationResult
import com.aliasadi.domain.model.api.common.v1.MutationSuccess

import common.v1.MutateResultOuterClass.MutateResult

fun MutateResult.toDomain(): MutationResult =
    if (success) {
        MutationSuccess(
            opId = opId,
            resourceId = resourceId
        )
    } else {
        MutationFailure(
            opId = opId,
            reason = error.toDomainReason()
        )
    }
