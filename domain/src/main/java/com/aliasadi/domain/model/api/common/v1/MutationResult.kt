package com.aliasadi.domain.model.api.common.v1

sealed class MutationResult {
    abstract val opId: String
}

data class MutationSuccess(
    override val opId: String,
    val resourceId: String
) : MutationResult()

data class MutationFailure(
    override val opId: String,
    val reason: MutationFailureReason
) : MutationResult()

sealed class MutationFailureReason {
    data class BusinessError(
        val error: Error
    ) : MutationFailureReason()

    data object PermissionDenied : MutationFailureReason()

    data object NotFound : MutationFailureReason()
}
