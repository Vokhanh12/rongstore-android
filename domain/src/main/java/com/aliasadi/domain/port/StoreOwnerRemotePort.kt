package com.aliasadi.domain.port

import com.aliasadi.domain.model.commands.StoreOwnerMutateCommand
import com.aliasadi.domain.model.api.common.v1.MutationResult

interface StoreOwnerRemotePort {
    suspend fun mutate(
        commands: List<StoreOwnerMutateCommand>
    ): List<MutationResult>
}
