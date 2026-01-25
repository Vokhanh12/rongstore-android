package com.aliasadi.domain.port

import com.aliasadi.domain.model.commands.StoreOwnerMutateCommand
import com.aliasadi.domain.model.MutationResult

interface StoreOwnerRemotePort {

    suspend fun mutate(
        commands: List<StoreOwnerMutateCommand>
    ): List<MutationResult>
}
