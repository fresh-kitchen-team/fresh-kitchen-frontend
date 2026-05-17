package com.example.myfrigelocal.data.remote

import android.util.Log
import com.example.myfrigelocal.BuildConfig
import com.example.myfrigelocal.data.TokenProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ChatRetrofitProvider {

    /** Swagger / product candidate — trailing slash required for relative @GET paths. */
    private const val DEFAULT_BASE_URL = "http://api.app-fresh.com/"

    /**
     * Do not use [GsonBuilder.serializeNulls] here: empty JSON arrays for optional lists
     * (e.g. `ingredients: []`) often fail backend validation with HTTP 400 "Invalid input".
     * Null fields are omitted so the server can apply its own defaults.
     */
    fun gson(): Gson = GsonBuilder().create()

    fun okHttpClient(tokenProvider: TokenProvider): OkHttpClient {
        // DEBUG: request/response JSON 전체 — Logcat 필터 `FreshKitchenChat`
        val logging = HttpLoggingInterceptor { message ->
            Log.d("FreshKitchenChat", message)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(HttpStatusLoggingInterceptor())
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun retrofit(
        tokenProvider: TokenProvider,
        baseUrl: String = DEFAULT_BASE_URL,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient(tokenProvider))
        .addConverterFactory(GsonConverterFactory.create(gson()))
        .build()

    fun chatApi(tokenProvider: TokenProvider): ChatApiService =
        retrofit(tokenProvider).create(ChatApiService::class.java)
}
