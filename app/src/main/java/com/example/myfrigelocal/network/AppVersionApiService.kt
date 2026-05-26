package com.example.myfrigelocal.network

import retrofit2.http.GET

interface AppVersionApiService {
    @GET("api/v1/app/version")
    suspend fun getAppVersion(): ApiResponse<AppVersionDto>
}
