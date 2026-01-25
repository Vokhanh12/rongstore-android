package com.aliasadi.data.remote.http

import com.aliasadi.domain.model.StoreOwner

fun StoreOwner.toDomain(): StoreOwner =
    StoreOwner(
        id = id,
        lat = lat,
        lng = lng,
        tileX = tileX,
        tileY = tileY
    )