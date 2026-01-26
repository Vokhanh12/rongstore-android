package com.aliasadi.data.mapper.iam.v1

import com.aliasadi.domain.model.commands.StoreOwnerMutateCommand
import com.aliasadi.iam.v1.resources.StoreOwnerMutation

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
