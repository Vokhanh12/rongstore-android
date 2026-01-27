package com.aliasadi.data.auth

class LocalTokenProvider @Inject constructor(
    private val dataStore: TokenDataStore
) : ITokenProvider {

    override fun getAccessToken(): String? {
        return dataStore.accessToken
    }
}