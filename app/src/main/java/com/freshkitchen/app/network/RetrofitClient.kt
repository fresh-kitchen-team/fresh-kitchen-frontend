package com.freshkitchen.app.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val okHttpClient = OkHttpClientFactory.authenticatedClient()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(OkHttpClientFactory.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

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

    val devApi: DevApiService by lazy {
        retrofit.create(DevApiService::class.java)
    }

    val appVersionApi: AppVersionApiService by lazy {
        retrofit.create(AppVersionApiService::class.java)
    }

    val legalApi: LegalApiService by lazy {
        retrofit.create(LegalApiService::class.java)
    }
}
