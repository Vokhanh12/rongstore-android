package com.aliasadi.data.remote.http

import com.aliasadi.data.mapper.common.v1.toDomain
import com.aliasadi.data.mapper.iam.v1.toHttpDto
import com.aliasadi.domain.model.api.common.v1.MutationResult
import com.aliasadi.domain.model.commands.StoreOwnerMutateCommand
import com.aliasadi.domain.port.IamRemotePort
import com.aliasadi.iam.client.api.IamServiceApi
import com.aliasadi.iam.client.dto.V1StoreOwnerMutateRequest
import java.io.IOException


    class IamHttpAdapter(
        private val api: IamServiceApi
    ) : IamRemotePort {

        override suspend fun mutate(
            commands: List<StoreOwnerMutateCommand>
        ): List<MutationResult> {

            val request = buildRequest(commands)

            val response = try {
                api.iamServiceStoreOwnerMutate(request)
            } catch (e: IOException) {
                throw RemoteException.Network(e)
            }

            if (!response.isSuccessful) {
                throw RemoteException.fromHttpCode(response.code())
            }

            val body = response.body()
                ?: throw RemoteException.EmptyBody

            return body.mutateResults
                ?.map { it.toDomain() }
                ?: emptyList()
        }

        private fun buildRequest(
            commands: List<StoreOwnerMutateCommand>
        ): V1StoreOwnerMutateRequest =
            V1StoreOwnerMutateRequest(
                mutations = commands.mapIndexed { index, command ->
                    command.toHttpDto(opId = "op-$index")
                }
            )
    }

sealed class RemoteException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    class Network(cause: Throwable) :
        RemoteException("Network error", cause)

    object Unauthorized :
        RemoteException("Unauthorized")

    object NotFound :
        RemoteException("Not found")

    object ServerError :
        RemoteException("Server error")

    object EmptyBody :
        RemoteException("Response body is empty")

    companion object {
        fun fromHttpCode(code: Int): RemoteException =
            when (code) {
                401 -> Unauthorized
                404 -> NotFound
                in 500..599 -> ServerError
                else -> ServerError
            }
    }
}