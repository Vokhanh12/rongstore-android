package com.aliasadi.data.remote.http

import com.aliasadi.domain.port.StoreOwnerRemotePort
import com.aliasadi.domain.model.*
import com.aliasadi.domain.model.commands.StoreOwnerMutateCommand
import com.aliasadi.domain.model.queries.StoreOwnerSearchQuery
import com.aliasadi.iam.v1.resources.StoreOwnerMutateRequest
import common.v1.MutateResultOuterClass

class StoreOwnerHttpAdapter(
    private val api: StoreOwnerHttpApi
) : StoreOwnerRemotePort {
    override suspend fun mutate(commands: List<StoreOwnerMutationCommand>): List<MutationResult> {
        TODO("Not yet implemented")
    }

    override suspend fun search(query: StoreOwnerSearchQuery): Page<StoreOwner> {
        TODO("Not yet implemented")
    }


}

override suspend fun mutate(
    commands: List<StoreOwnerMutateCommand>
): List<MutateResultOuterClass> {

    val request = StoreOwnerMutateRequest(
        mutations = commands.map { it.toProto() }
    )

    val response = api.mutate(request)
    return response.toDomain()
}