package com.example.myfrigelocal.data.remote

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

    fun gson(): Gson = GsonBuilder()
        .serializeNulls()
        .create()

    fun okHttpClient(tokenProvider: TokenProvider): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
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
