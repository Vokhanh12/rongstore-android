package com.aliasadi.data.auth

class InMemorySessionStore : ISessionStore {
    override var accessToken: String? = null
}