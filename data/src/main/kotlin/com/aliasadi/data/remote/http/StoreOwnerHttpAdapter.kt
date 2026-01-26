package com.aliasadi.data.remote.http

import android.util.Log
import com.aliasadi.data.mapper.iam.v1.toProto
import com.aliasadi.data.mapper.common.v1.toDomain
import com.aliasadi.domain.port.StoreOwnerRemotePort
import com.aliasadi.domain.model.*
import com.aliasadi.domain.model.api.common.v1.MutationResult
import com.aliasadi.domain.model.commands.StoreOwnerMutateCommand
import com.aliasadi.iam.v1.resources.StoreOwnerMutateRequest
import java.io.IOException

class StoreOwnerHttpAdapter(
    private val api: StoreOwnerHttpApi
) : StoreOwnerRemotePort {

    override suspend fun mutate(
        commands: List<StoreOwnerMutateCommand>
    ): List<MutationResult> {

        val request = StoreOwnerMutateRequest.newBuilder()
            .addAllMutations(
                commands.mapIndexed { index, command ->
                    command.toProto(opId = "op-$index")
                }
            )
            .build()

        val response = try {
            api.mutate(request)
        } catch (e: IOException) {
            throw RemoteException.Network(e)
        }

        if (!response.isSuccessful) {
            throw mapHttpError(response.code())
        }

        val body = response.body()
            ?: throw RemoteException.EmptyBody

        return body.mutateResultsList
            .map { it.toDomain() }
    }
}

sealed class RemoteException : RuntimeException() {
    class Network(cause: Throwable) : RemoteException()
    class NotFound : RemoteException()
    class Unauthorized : RemoteException()
    class ServerError : RemoteException()
    object EmptyBody : RemoteException()
}

private fun mapHttpError(code: Int): RemoteException =
    when (code) {
        401 -> RemoteException.Unauthorized()
        404 -> RemoteException.NotFound()
        in 500..599 -> RemoteException.ServerError()
        else -> RemoteException.ServerError()
    }
