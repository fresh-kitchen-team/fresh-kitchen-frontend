package com.freshkitchen.app.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

// ───────────────────────────────────────────
// 프로필 응답 DTO
// ───────────────────────────────────────────
data class UserProfileDto(
    val nickname: String?,
    val profileImageUrl: String?,
    val bio: String?,
    val preferredIngredients: List<String>?,
    val foodStyles: List<String>?,
    val allergies: List<String>?,
    val cookingTools: List<String>?
)

// ───────────────────────────────────────────
// 프로필 수정 요청 DTO
// ───────────────────────────────────────────
data class UserProfileUpdateRequest(
    val nickname: String? = null,
    val profileImageUrl: String? = null,
    val bio: String? = null,
    val preferredIngredients: List<String>? = null,
    val foodStyles: List<String>? = null,
    val allergies: List<String>? = null,
    val cookingTools: List<String>? = null
)

// ───────────────────────────────────────────
// 유저 API 인터페이스
// ───────────────────────────────────────────
interface UserApiService {

    // GET /api/v1/users/me/profile — 프로필 조회
    @GET("api/v1/users/me/profile")
    suspend fun getProfile(): ApiResponse<UserProfileDto>

    // PATCH /api/v1/users/me/profile — 프로필 수정
    @PATCH("api/v1/users/me/profile")
    suspend fun updateProfile(
        @Body request: UserProfileUpdateRequest
    ): ApiResponse<Void>

    // DELETE /api/v1/users/me — 회원 탈퇴 (소프트 삭제)
    @DELETE("api/v1/users/me")
    suspend fun deleteAccount(): ApiResponse<Void>

    // POST /api/v1/users/me/fcm-tokens — FCM 디바이스 토큰 등록/갱신
    @POST("api/v1/users/me/fcm-tokens")
    suspend fun registerFcmToken(
        @Body request: FcmTokenRequest
    ): ApiResponse<Void>
}

// FCM 토큰 등록 요청 DTO
data class FcmTokenRequest(
    val tokenValue: String,
    val deviceType: String = "ANDROID"
)