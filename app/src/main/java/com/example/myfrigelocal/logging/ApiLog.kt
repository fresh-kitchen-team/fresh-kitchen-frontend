package com.example.myfrigelocal.logging

import android.util.Log
import com.example.myfrigelocal.BuildConfig

/**
 * Logcat 필터: `MyFridgeApi` 또는 메시지 검색 `api`
 * (예: 문의 상세 `api/InquiryDetail`, 첨부 이미지 `api/InquiryImage`)
 */
object ApiLog {
    const val TAG = "MyFridgeApi"

    fun d(sub: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, "[$sub] $message")
     }

    fun i(sub: String, message: String) {
        if (BuildConfig.DEBUG) Log.i(TAG, "[$sub] $message")
    }

    /** 릴리스에서도 실패 원인 확인용으로 남길 수 있음 */
    fun w(sub: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.w(TAG, "[$sub] $message", throwable)
        else Log.w(TAG, "[$sub] $message")
    }

    fun e(sub: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.e(TAG, "[$sub] $message", throwable)
        else Log.e(TAG, "[$sub] $message")
    }
}
