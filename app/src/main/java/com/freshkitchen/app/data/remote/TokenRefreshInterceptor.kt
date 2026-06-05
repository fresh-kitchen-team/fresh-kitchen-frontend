package com.freshkitchen.app.data.remote

import android.util.Log
import com.freshkitchen.app.MyApplication
import com.freshkitchen.app.data.auth.AuthTokenStore
import com.freshkitchen.app.data.auth.TokenDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 * 401 응답 시 refresh token으로 access token을 자동 갱신하고 원래 요청을 재시도한다.
 *
 * - /auth/ 경로(로그인, refresh 등)는 인터셉트하지 않아 무한 루프를 방지한다.
 * - refresh 실패 시 토큰을 전부 삭제해 다음 앱 실행 시 로그인 화면으로 이동하게 한다.
 */
class TokenRefreshInterceptor : okhttp3.Interceptor {

    private val gson = Gson()

    override fun intercept(chain: okhttp3.Interceptor.Chain): Response {
        val request = chain.request()

        // /auth/ 엔드포인트는 그냥 통과 (무한 루프 방지)
        if (request.url.encodedPath.contains("/auth/", ignoreCase = true)) {
            return chain.proceed(request)
        }

        val response = chain.proceed(request)
        if (response.code != 401) return response

        // 401 → refresh 시도
        response.close()
        Log.d(TAG, "401 수신 — refresh 시도: ${request.url}")

        val refreshToken = AuthTokenStore.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Log.w(TAG, "refresh token 없음 — 로그인 필요")
            return chain.proceed(request)
        }

        val newTokens = callRefreshSync(refreshToken)
        if (newTokens == null) {
            Log.w(TAG, "refresh 실패 — 토큰 삭제")
            AuthTokenStore.clear()
            runBlocking {
                runCatching {
                    TokenDataStore.clearTokens(MyApplication.appContext)
                }
            }
            return chain.proceed(request)
        }

        // 새 토큰 저장
        AuthTokenStore.setAccessToken(newTokens.accessToken)
        AuthTokenStore.setRefreshToken(newTokens.refreshToken)
        runBlocking {
            runCatching {
                val provider = TokenDataStore.getLoginProvider(MyApplication.appContext).first() ?: "GOOGLE"
                TokenDataStore.saveTokens(
                    MyApplication.appContext,
                    newTokens.accessToken,
                    newTokens.refreshToken,
                    provider
                )
            }
        }

        Log.d(TAG, "refresh 성공 — 원래 요청 재시도")

        // 새 토큰으로 원래 요청 재시도
        val retryRequest = request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()
        return chain.proceed(retryRequest)
    }

    /**
     * Retrofit을 거치지 않고 OkHttp로 직접 refresh 호출 (동기).
     * Retrofit 클라이언트와 순환 참조를 피하기 위해 별도 클라이언트 사용.
     */
    private fun callRefreshSync(refreshToken: String): TokenPair? {
        return try {
            val client = OkHttpClient()
            val body = """{"refreshToken":"$refreshToken"}"""
                .toRequestBody("application/json".toMediaType())
            val req = Request.Builder()
                .url("https://api.app-fresh.com/api/v1/auth/refresh")
                .post(body)
                .build()
            val res = client.newCall(req).execute()
            if (!res.isSuccessful) {
                Log.w(TAG, "refresh HTTP 실패: ${res.code}")
                return null
            }
            val json = res.body?.string() ?: return null
            // {"status":...,"code":"COMMON-200","data":{"accessToken":"...","refreshToken":"..."}}
            val envelope = gson.fromJson(json, RefreshEnvelope::class.java)
            if (envelope?.data == null) {
                Log.w(TAG, "refresh 응답 data 없음: $json")
                return null
            }
            TokenPair(envelope.data.accessToken, envelope.data.refreshToken)
        } catch (e: Exception) {
            Log.e(TAG, "refresh 호출 예외: ${e.message}")
            null
        }
    }

    private data class RefreshEnvelope(
        val status: Int = 0,
        val code: String? = null,
        val data: TokenPair? = null
    )

    private data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )

    companion object {
        private const val TAG = "TokenRefresh"
    }
}
