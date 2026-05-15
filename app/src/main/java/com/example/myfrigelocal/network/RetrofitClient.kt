package com.example.myfrigelocal.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://api.app-fresh.com/"

    // TODO: 로그인 연동 후 DataStore에서 읽어온 토큰으로 교체
    var accessToken: String = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInJvbGUiOiJVU0VSIiwidG9rZW5UeXBlIjoiYWNjZXNzIiwiaWF0IjoxNzc4ODE2MTAzLCJleHAiOjE3Nzg4MTc5MDN9.VHQVmKy4YOV9RfHN-7gVM0zhsxMqsqSCLfCscmT63rM"
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder().apply {
                if (accessToken.isNotEmpty()) {
                    addHeader("Authorization", "Bearer $accessToken")
                }
            }.build()
            chain.proceed(request)
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // API 서비스 인스턴스
    val homeApi: HomeApiService by lazy {
        retrofit.create(HomeApiService::class.java)
    }

    val ingredientApi: IngredientApiService by lazy {
        retrofit.create(IngredientApiService::class.java)
    }
}