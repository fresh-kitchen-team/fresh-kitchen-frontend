package com.freshkitchen.app.network

// ───────────────────────────────────────────
// 유저 프로필 Repository
// ───────────────────────────────────────────
class UserRepository(
    private val api: UserApiService,
) {
    suspend fun getProfile(): ApiResponse<UserProfileDto> = api.getProfile()

    suspend fun updateProfile(request: UserProfileUpdateRequest): ApiResponse<Void> =
        api.updateProfile(request)

    suspend fun deleteAccount(): ApiResponse<Void> = api.deleteAccount()

    suspend fun registerFcmToken(token: String): Result<Unit> = runCatching {
        api.registerFcmToken(FcmTokenRequest(tokenValue = token))
    }
}