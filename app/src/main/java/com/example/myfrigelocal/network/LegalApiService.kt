package com.example.myfrigelocal.network

import retrofit2.http.GET
import retrofit2.http.POST

interface LegalApiService {
    // 약관/개인정보처리방침 URL 조회
    @GET("api/v1/legal")
    suspend fun getLegal(): ApiResponse<LegalDto>

    // 동의 상태 조회
    @GET("api/v1/legal/agreement")
    suspend fun getAgreement(): ApiResponse<LegalAgreementDto>

    // 약관 동의 처리
    @POST("api/v1/legal/agreement")
    suspend fun postAgreement(): ApiResponse<LegalAgreementDto>
}
