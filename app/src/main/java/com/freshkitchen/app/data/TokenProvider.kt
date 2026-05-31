package com.freshkitchen.app.data

import com.freshkitchen.app.data.auth.AuthTokenStore

/**
 * Supplies the Bearer token for [com.freshkitchen.app.data.remote.AuthInterceptor].
 *
 * Use [SessionTokenProvider] for production wiring (reads [AuthTokenStore]).
 * [NoOpTokenProvider] is only for tests or callers that must not send auth.
 */
fun interface TokenProvider {
    fun getAccessToken(): String?
}

/** Reads the token set by login (or debug) via [AuthTokenStore]. */
object SessionTokenProvider : TokenProvider {
    override fun getAccessToken(): String? = AuthTokenStore.getAccessToken()
}

/** Never sends Authorization (use in unit tests only). */
object NoOpTokenProvider : TokenProvider {
    override fun getAccessToken(): String? = null
}
