package com.example.myfrigelocal.network

import com.example.myfrigelocal.data.auth.AuthTokenStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://api.app-fresh.com/"

    // TODO: 로그인 연동 후 DataStore에서 읽어온 토큰으로 교체
    var accessToken: String = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjMsInJvbGUiOiJVU0VSIiwidG9rZW5UeXBlIjoiYWNjZXNzIiwiaWF0IjoxNzc4ODIzNjYwLCJleHAiOjE3Nzk0Mjg0NjB9.0QBh8OOfbM_72scHyFPw2qNXPKEQKYAOphhrMZZLCGY"
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val raw =
                AuthTokenStore.getAccessToken()?.trim().orEmpty().ifEmpty { accessToken.trim() }
            val authHeader =
                when {
                    raw.isEmpty() -> null
                    raw.startsWith("Bearer ", ignoreCase = true) -> raw
                    else -> "Bearer $raw"
                }
            val request =
                if (authHeader != null) {
                    chain.request().newBuilder()
                        .header("Authorization", authHeader)
                        .build()
                } else {
                    chain.request()
                }
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