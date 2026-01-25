package com.aliasadi.domain.model.commands

import com.aliasadi.domain.model.valueobjects.Location
import com.aliasadi.domain.model.valueobjects.Tile

sealed class StoreOwnerMutateCommand {
    data class Create(
        val location: Location,
        val tile: Tile,
        val createBy: Int
    ) : StoreOwnerMutateCommand()

    data class Update(
        val id: String,
        val location: Location,
        val tile: Tile,
        val updateBy: Int
    ) : StoreOwnerMutateCommand()

    data class Delete(
        val id: String
    ) : StoreOwnerMutateCommand()
}