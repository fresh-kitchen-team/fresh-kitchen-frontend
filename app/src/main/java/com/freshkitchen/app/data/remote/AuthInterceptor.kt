package com.freshkitchen.app.data.remote

import android.util.Log
import com.freshkitchen.app.data.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val isChat = path.contains("chat", ignoreCase = true)
        val token = tokenProvider.getAccessToken()?.trim().orEmpty()
        if (isChat) {
            Log.i(
                TAG,
                "${request.method} ${request.url} | Authorization token present=${token.isNotEmpty()} (token value not logged)",
            )
        }
        if (token.isEmpty() && isChat) {
            Log.w(TAG, "No access token — Authorization header omitted for ${request.url}")
        }
        val authed = if (token.isNotEmpty()) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        return chain.proceed(authed)
    }

    companion object {
        private const val TAG = "FreshKitchenChat"
    }
}
