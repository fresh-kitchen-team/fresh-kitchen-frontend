package com.example.myfrigelocal.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Logs each Chat API response HTTP status so you can confirm 401 vs 403 in Logcat.
 *
 * Filter: `adb logcat -s FreshKitchenChat`
 */
class HttpStatusLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val path = request.url.encodedPath
        if (path.contains("chat")) {
            Log.i(
                TAG,
                "${request.method} ${request.url} -> HTTP ${response.code}",
            )
        }
        return response
    }

    companion object {
        private const val TAG = "FreshKitchenChat"
    }
}
