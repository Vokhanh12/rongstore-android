package com.aliasadi.data.mapper.iam.v1

import com.aliasadi.domain.model.commands.StoreOwnerMutateCommand

// ===== gRPC =====
import com.aliasadi.iam.v1.resources.StoreOwnerMutation

// ===== OpenAPI =====
import com.aliasadi.iam.client.dto.V1StoreOwnerMutation
import com.aliasadi.iam.client.dto.StoreOwnerMutationDeleteStoreOwner
import com.aliasadi.iam.client.dto.StoreOwnerMutationUpdateStoreOwner
import com.aliasadi.iam.client.dto.StoreOwnerMutationCreateStoreOwner


/* ============================================================
 * gRPC MAPPER (PROTO)
 * ============================================================ */

fun StoreOwnerMutateCommand.toProto(opId: String): StoreOwnerMutation {
    val builder = StoreOwnerMutation.newBuilder()
        .setOpId(opId)

    when (this) {

        is StoreOwnerMutateCommand.Create -> {
            builder.setCreate(
                StoreOwnerMutation.CreateStoreOwner.newBuilder()
                    .setLat(location.lat)
                    .setLng(location.lng)
                    .setTileX(tile.x)
                    .setTileY(tile.y)
                    .setCreateBy(createBy)
                    .build()
            )
        }

        is StoreOwnerMutateCommand.Update -> {
            builder.setUpdate(
                StoreOwnerMutation.UpdateStoreOwner.newBuilder()
                    .setId(id)
                    .setLat(location.lat)
                    .setLng(location.lng)
                    .setTileX(tile.x)
                    .setTileY(tile.y)
                    .setUpdateBy(updateBy)
                    .build()
            )
        }

        is StoreOwnerMutateCommand.Delete -> {
            builder.setDelete(
                StoreOwnerMutation.DeleteStoreOwner.newBuilder()
                    .setId(id)
                    .build()
            )
        }
    }

    return builder.build()
}

/* ============================================================
 * HTTP / OPENAPI MAPPER
 * ============================================================ */

fun StoreOwnerMutateCommand.toHttpDto(
    opId: String
): V1StoreOwnerMutation =
    when (this) {

        is StoreOwnerMutateCommand.Create ->
            V1StoreOwnerMutation(
                opId = opId,
                create = StoreOwnerMutationCreateStoreOwner(
                    lat = location.lat,
                    lng = location.lng,
                    tileX = tile.x,
                    tileY = tile.y,
                    createBy = createBy
                ),
                update = null,
                delete = null
            )

        is StoreOwnerMutateCommand.Update ->
            V1StoreOwnerMutation(
                opId = opId,
                create = null,
                update = StoreOwnerMutationUpdateStoreOwner(
                    id = id,
                    lat = location.lat,
                    lng = location.lng,
                    tileX = tile.x,
                    tileY = tile.y,
                    updateBy = updateBy
                ),
                delete = null
            )

        is StoreOwnerMutateCommand.Delete ->
            V1StoreOwnerMutation(
                opId = opId,
                create = null,
                update = null,
                delete = StoreOwnerMutationDeleteStoreOwner(
                    id = id
                )
            )
    }
