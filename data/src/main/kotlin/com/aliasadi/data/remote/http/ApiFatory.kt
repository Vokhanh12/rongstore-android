package com.aliasadi.data.remote.http

import com.aliasadi.data.auth.ITokenProvider
import com.aliasadi.iam.client.api.IamServiceApi
import com.aliasadi.iam.client.infrastructure.ApiClient
import okhttp3.OkHttpClient

object ApiFactory {

    fun iamApi(
        baseUrl: String,
        tokenProvider: ITokenProvider
    ): IamServiceApi {

        val okHttp = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .build()

        val apiClient = ApiClient()
            .setBasePath(baseUrl)
            .setHttpClient(okHttp)

        return IamServiceApi(apiClient)
    }
}