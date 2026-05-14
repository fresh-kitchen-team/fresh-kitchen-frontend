package com.example.myfrigelocal.data.remote

import android.util.Log
import com.example.myfrigelocal.data.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenProvider.getAccessToken()?.trim().orEmpty()
        if (token.isEmpty() && request.url.encodedPath.contains("chat")) {
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
