package com.freshkitchen.app.network

import retrofit2.http.Body
import retrofit2.http.POST

// ───────────────────────────────────────────
// 로그인 요청 / 응답 DTO
// ───────────────────────────────────────────
data class GoogleLoginRequest(val idToken: String)
data class KakaoLoginRequest(val idToken: String)
data class RefreshTokenRequest(val refreshToken: String)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val newUser: Boolean
)

// ───────────────────────────────────────────
// 인증 API 인터페이스
// ───────────────────────────────────────────
interface AuthApiService {

    // POST /api/v1/auth/google — 구글 소셜 로그인
    @POST("api/v1/auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ): ApiResponse<AuthResponse>

    // POST /api/v1/auth/kakao — 카카오 소셜 로그인
    @POST("api/v1/auth/kakao")
    suspend fun loginWithKakao(
        @Body request: KakaoLoginRequest
    ): ApiResponse<AuthResponse>

    // POST /api/v1/auth/refresh — 토큰 갱신
    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): ApiResponse<AuthResponse>

    // POST /api/v1/auth/logout — 로그아웃 (서버 토큰 블랙리스트 처리)
    @POST("api/v1/auth/logout")
    suspend fun logout(): ApiResponse<Void>
}