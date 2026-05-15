package com.example.myfrigelocal

import com.example.myfrigelocal.BuildConfig
import com.example.myfrigelocal.logging.ApiLog

/**
 * In-memory access token for API calls (e.g. dev injection from [MainActivity]).
 * Replace with secure storage + login flow for production.
 */
object AuthTokenStore {
    @Volatile
    private var accessToken: String? = null

    fun setAccessToken(token: String) {
        accessToken = token.ifBlank { null }
        if (BuildConfig.DEBUG) {
            val len = accessToken?.length ?: 0
            ApiLog.i("Auth", "access token 저장됨(길이=$len, 값은 로그에 남기지 않음)")
        }
    }

    fun getAccessToken(): String? = accessToken

    fun clear() {
        accessToken = null
        if (BuildConfig.DEBUG) ApiLog.i("Auth", "access token cleared")
    }
}