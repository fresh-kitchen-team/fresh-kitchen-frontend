package com.example.myfrigelocal.network

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * 문의 / 문제 신고 API — Swagger `POST /api/v1/inquiries`
 *
 * 서버는 항상 `multipart/form-data` 를 요구합니다 (plain POST 시 415).
 * 이미지 없을 때는 빈 [image] 파트로 전송합니다.
 */
interface InquiryApiService {

    @Multipart
    @POST("api/v1/inquiries")
    suspend fun sendInquiry(
        @Query("type") type: String,
        @Query("category") category: String,
        @Query("content") content: String,
        @Part image: MultipartBody.Part,
    ): ApiResponse<Unit?>
}
