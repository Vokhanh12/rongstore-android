package com.aliasadi.data.remote.http

import com.aliasadi.iam.v1.resources.StoreOwnerMutateRequest
import common.v1.BaseResponseOuterClass
import retrofit2.http.Body
import retrofit2.http.POST

interface StoreOwnerHttpApi {
    @POST("/v1/store-owners:mutate")
    suspend fun mutate(
        @Body request: StoreOwnerMutateRequest
    ): BaseResponseOuterClass.MutateResponse
}
