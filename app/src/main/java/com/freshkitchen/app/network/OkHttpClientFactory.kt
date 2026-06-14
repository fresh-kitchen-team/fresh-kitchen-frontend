package com.freshkitchen.app.network

import android.util.Log
import com.freshkitchen.app.BuildConfig
import com.freshkitchen.app.data.auth.AuthTokenStore
import com.freshkitchen.app.data.remote.TokenRefreshInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/** Shared OkHttp setup for all Retrofit clients (main, chat, scan). */
object OkHttpClientFactory {

    const val BASE_URL = "https://api.app-fresh.com/"

    fun authenticatedClient(
        connectTimeoutSec: Long = 15,
        readTimeoutSec: Long = 15,
        writeTimeoutSec: Long = 15,
        tokenSupplier: () -> String? = { AuthTokenStore.getAccessToken() },
        debugLogTag: String? = null,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                // 로그인/refresh 요청에는 기존 Bearer를 붙이지 않음 (만료 토큰이 있으면 로그인 자체가 실패할 수 있음)
                if (request.url.encodedPath.contains("/auth/", ignoreCase = true)) {
                    return@addInterceptor chain.proceed(request)
                }
                val token = tokenSupplier()?.trim()?.takeIf { it.isNotEmpty() }
                val authedRequest = if (token != null) {
                    request.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    request
                }
                chain.proceed(authedRequest)
            }
            .addInterceptor(TokenRefreshInterceptor())
            .connectTimeout(connectTimeoutSec, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSec, TimeUnit.SECONDS)
            .writeTimeout(writeTimeoutSec, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor { message ->
                Log.d(debugLogTag ?: "FreshKitchenHttp", message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
        }

        return builder.build()
    }
}
