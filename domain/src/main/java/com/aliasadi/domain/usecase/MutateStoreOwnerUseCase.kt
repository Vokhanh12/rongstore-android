package com.aliasadi.domain.usecase

import com.aliasadi.domain.model.api.common.v1.MutationResult
import com.aliasadi.domain.model.commands.StoreOwnerMutateCommand
import com.aliasadi.domain.port.StoreOwnerRemotePort
import javax.inject.Inject

class MutateStoreOwnerUseCase @Inject constructor(
    private val remotePort: StoreOwnerRemotePort
) {

    suspend operator fun invoke(
        commands: List<StoreOwnerMutateCommand>
    ): List<MutationResult> {

        if (commands.isEmpty()) return emptyList()

        return remotePort.mutate(commands)
    }
}
