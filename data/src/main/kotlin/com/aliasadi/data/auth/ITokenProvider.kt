package com.aliasadi.data.auth

interface ITokenProvider {
    fun getAccessToken(): String?
}