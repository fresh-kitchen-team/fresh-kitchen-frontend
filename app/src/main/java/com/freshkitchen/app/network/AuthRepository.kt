package com.freshkitchen.app.network

// ───────────────────────────────────────────
// 인증 레포지토리
// ───────────────────────────────────────────
class AuthRepository(
    private val api: AuthApiService = RetrofitClient.authApi
) {
    suspend fun loginWithGoogle(idToken: String): ApiResponse<AuthResponse> {
        return api.loginWithGoogle(GoogleLoginRequest(idToken))
    }

    suspend fun loginWithKakao(idToken: String): ApiResponse<AuthResponse> {
        return api.loginWithKakao(KakaoLoginRequest(idToken))
    }

    suspend fun logout(): ApiResponse<Void> = api.logout()
}