package com.example.myfrigelocal.network

import com.example.myfrigelocal.data.auth.AuthTokenStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://api.app-fresh.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder().apply {
                val token = AuthTokenStore.getAccessToken()
                if (!token.isNullOrEmpty()) {
                    addHeader("Authorization", "Bearer $token")
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

    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val userApi: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val tipsApi: TipsApiService by lazy {
        retrofit.create(TipsApiService::class.java)
    }

    val analyticsApi: AnalyticsApiService by lazy {
        retrofit.create(AnalyticsApiService::class.java)
    }

    val inquiriesApi: InquiryApiService by lazy {
        retrofit.create(InquiryApiService::class.java)
    }
}