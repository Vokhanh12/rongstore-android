package com.aliasadi.data.api

import com.aliasadi.iam.v1.resources.StoreOwnerMutateRequest
import com.aliasadi.iam.v1.resources.StoreOwnerSearchResponse

interface StoreOwnerApi {

    suspend fun mutate(
        request: StoreOwnerMutateRequest
    ): StoreOwnerMutateResponse

    suspend fun search(
        request: StoreOwnerSearchRequest
    ): StoreOwnerSearchResponse
}
