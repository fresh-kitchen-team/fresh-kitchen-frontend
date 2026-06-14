package com.freshkitchen.app.di

import android.content.Context
import com.freshkitchen.app.data.SessionTokenProvider
import com.freshkitchen.app.data.TokenProvider
import com.freshkitchen.app.data.remote.ChatApiService
import com.freshkitchen.app.data.remote.ChatRetrofitProvider
import com.freshkitchen.app.data.repository.ChatRepository
import com.freshkitchen.app.data.scan.ScanRepository
import com.freshkitchen.app.network.AnalyticsApiService
import com.freshkitchen.app.network.AnalyticsRepository
import com.freshkitchen.app.network.AuthApiService
import com.freshkitchen.app.network.AuthRepository
import com.freshkitchen.app.network.HomeApiService
import com.freshkitchen.app.network.HomeRepository
import com.freshkitchen.app.network.IngredientApiService
import com.freshkitchen.app.network.IngredientRepository
import com.freshkitchen.app.network.InquiryApiService
import com.freshkitchen.app.network.InquiryRepository
import com.freshkitchen.app.network.RetrofitClient
import com.freshkitchen.app.network.TipsApiService
import com.freshkitchen.app.network.TipsRepository
import com.freshkitchen.app.network.UserApiService
import com.freshkitchen.app.network.UserRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideTokenProvider(): TokenProvider = SessionTokenProvider

    @Provides @Singleton
    fun provideChatGson(): Gson = ChatRetrofitProvider.gson()

    @Provides @Singleton
    fun provideChatApi(tokenProvider: TokenProvider): ChatApiService =
        ChatRetrofitProvider.chatApi(tokenProvider)

    @Provides @Singleton fun provideHomeApi(): HomeApiService = RetrofitClient.homeApi
    @Provides @Singleton fun provideIngredientApi(): IngredientApiService = RetrofitClient.ingredientApi
    @Provides @Singleton fun provideAuthApi(): AuthApiService = RetrofitClient.authApi
    @Provides @Singleton fun provideUserApi(): UserApiService = RetrofitClient.userApi
    @Provides @Singleton fun provideTipsApi(): TipsApiService = RetrofitClient.tipsApi
    @Provides @Singleton fun provideAnalyticsApi(): AnalyticsApiService = RetrofitClient.analyticsApi
    @Provides @Singleton fun provideInquiryApi(): InquiryApiService = RetrofitClient.inquiriesApi
    @Provides @Singleton fun provideLegalApi(): com.freshkitchen.app.network.LegalApiService = RetrofitClient.legalApi
    @Provides @Singleton fun provideAppVersionApi(): com.freshkitchen.app.network.AppVersionApiService = RetrofitClient.appVersionApi

    @Provides @Singleton
    fun provideHomeRepository(api: HomeApiService): HomeRepository = HomeRepository(api)

    @Provides @Singleton
    fun provideIngredientRepository(api: IngredientApiService): IngredientRepository =
        IngredientRepository(api)

    @Provides @Singleton
    fun provideAuthRepository(api: AuthApiService): AuthRepository = AuthRepository(api)

    @Provides @Singleton
    fun provideUserRepository(api: UserApiService): UserRepository = UserRepository(api)

    @Provides @Singleton
    fun provideTipsRepository(api: TipsApiService): TipsRepository = TipsRepository(api)

    @Provides @Singleton
    fun provideAnalyticsRepository(api: AnalyticsApiService): AnalyticsRepository =
        AnalyticsRepository(api)

    @Provides @Singleton
    fun provideInquiryRepository(api: InquiryApiService): InquiryRepository =
        InquiryRepository(api)

    @Provides @Singleton
    fun provideChatRepository(api: ChatApiService): ChatRepository = ChatRepository(api)

    @Provides @Singleton
    fun provideScanRepository(@ApplicationContext context: Context): ScanRepository =
        ScanRepository(context)
}
