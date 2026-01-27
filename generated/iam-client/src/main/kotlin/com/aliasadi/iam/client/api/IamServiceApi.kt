package com.aliasadi.iam.client.api

import com.aliasadi.iam.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.aliasadi.iam.client.dto.RpcStatus
import com.aliasadi.iam.client.dto.V1BaseResponse
import com.aliasadi.iam.client.dto.V1HandshakeRequest
import com.aliasadi.iam.client.dto.V1LoginRequest
import com.aliasadi.iam.client.dto.V1MutateResponse
import com.aliasadi.iam.client.dto.V1StoreOwnerMutateRequest
import com.aliasadi.iam.client.dto.V1StoreOwnerSearchByTilesRequest
import com.aliasadi.iam.client.dto.V1StoreOwnerSearchRequest

interface IamServiceApi {
    /**
     * POST v1/handshake
     * 
     * 
     * Responses:
     *  - 200: A successful response.
     *  - 0: An unexpected error response.
     *
     * @param body 
     * @return [V1BaseResponse]
     */
    @POST("v1/handshake")
    suspend fun iamServiceHandshake(@Body body: V1HandshakeRequest): Response<V1BaseResponse>

    /**
     * POST v1/login
     * 
     * 
     * Responses:
     *  - 200: A successful response.
     *  - 0: An unexpected error response.
     *
     * @param body 
     * @return [V1BaseResponse]
     */
    @POST("v1/login")
    suspend fun iamServiceLogin(@Body body: V1LoginRequest): Response<V1BaseResponse>

    /**
     * POST v1/store-owners:mutate
     * 
     * 
     * Responses:
     *  - 200: A successful response.
     *  - 0: An unexpected error response.
     *
     * @param body 
     * @return [V1MutateResponse]
     */
    @POST("v1/store-owners:mutate")
    suspend fun iamServiceStoreOwnerMutate(@Body body: V1StoreOwnerMutateRequest): Response<V1MutateResponse>

    /**
     * POST v1/store-owners:search
     * 
     * 
     * Responses:
     *  - 200: A successful response.
     *  - 0: An unexpected error response.
     *
     * @param body 
     * @return [V1BaseResponse]
     */
    @POST("v1/store-owners:search")
    suspend fun iamServiceStoreOwnerSearch(@Body body: V1StoreOwnerSearchRequest): Response<V1BaseResponse>

    /**
     * POST v1/store-owners:search-by-tiles
     * 
     * 
     * Responses:
     *  - 200: A successful response.
     *  - 0: An unexpected error response.
     *
     * @param body 
     * @return [V1BaseResponse]
     */
    @POST("v1/store-owners:search-by-tiles")
    suspend fun iamServiceStoreOwnerSearchByTiles(@Body body: V1StoreOwnerSearchByTilesRequest): Response<V1BaseResponse>

}
