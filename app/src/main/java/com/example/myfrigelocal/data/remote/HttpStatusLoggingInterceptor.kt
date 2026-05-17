package com.example.myfrigelocal.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Logs Chat AI API calls: full URL, HTTP status, and error body peek on failure.
 *
 * Success response JSON (전체 body)는 DEBUG 빌드에서 OkHttp [HttpLoggingInterceptor.Level.BODY]로 출력됩니다.
 *
 * Logcat 필터: `FreshKitchenChat` 또는 Android Studio에서 tag:FreshKitchenChat
 */
class HttpStatusLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val isChat = path.contains("chat", ignoreCase = true)
        if (!isChat) return chain.proceed(request)

        val response = chain.proceed(request)
        val code = response.code
        val line = "${request.method} ${request.url} -> HTTP $code"
        when (code) {
            in 200..299 -> Log.i(TAG, line)
            401 -> Log.w(TAG, "$line (Unauthorized)")
            403 -> Log.w(TAG, "$line (Forbidden)")
            404 -> Log.w(TAG, "$line (Not Found)")
            in 500..599 -> Log.e(TAG, "$line (Server Error)")
            else -> Log.w(TAG, line)
        }
        if (!response.isSuccessful) {
            try {
                val peek = response.peekBody(8192).string()
                if (peek.isNotBlank()) {
                    Log.w(TAG, "[$API_LABEL] error body (peek): $peek")
                }
            } catch (e: Exception) {
                Log.w(TAG, "[$API_LABEL] could not peek error body: ${e.message}")
            }
        }
        return response
    }

    companion object {
        private const val TAG = "FreshKitchenChat"
        private const val API_LABEL = "AiChat"
    }
}
