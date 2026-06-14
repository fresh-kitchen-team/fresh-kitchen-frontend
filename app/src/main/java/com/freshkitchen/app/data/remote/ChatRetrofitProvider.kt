package com.freshkitchen.app.data.remote

import com.freshkitchen.app.data.TokenProvider
import com.freshkitchen.app.network.OkHttpClientFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ChatRetrofitProvider {

    /**
     * Do not use [GsonBuilder.serializeNulls] here: empty JSON arrays for optional lists
     * (e.g. `ingredients: []`) often fail backend validation with HTTP 400 "Invalid input".
     */
    fun gson(): Gson = GsonBuilder().create()

    fun okHttpClient(tokenProvider: TokenProvider): OkHttpClient {
        val client = OkHttpClientFactory.authenticatedClient(
            connectTimeoutSec = 30,
            readTimeoutSec = 60,
            writeTimeoutSec = 60,
            tokenSupplier = { tokenProvider.getAccessToken() },
            debugLogTag = "FreshKitchenChat",
        )
        return client.newBuilder()
            .addInterceptor(HttpStatusLoggingInterceptor())
            .build()
    }

    fun retrofit(
        tokenProvider: TokenProvider,
        baseUrl: String = OkHttpClientFactory.BASE_URL,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient(tokenProvider))
        .addConverterFactory(GsonConverterFactory.create(gson()))
        .build()

    fun chatApi(tokenProvider: TokenProvider): ChatApiService =
        retrofit(tokenProvider).create(ChatApiService::class.java)
}
