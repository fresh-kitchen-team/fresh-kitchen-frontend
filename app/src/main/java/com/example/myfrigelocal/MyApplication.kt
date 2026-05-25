package com.example.myfrigelocal

import android.app.Application
import android.util.Log
import com.example.myfrigelocal.network.UserRepository
import com.example.myfrigelocal.notification.NotificationHelper
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.common.KakaoSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 카카오 SDK 초기화
        KakaoSdk.init(this, "8c80a8aa4bedabb2e8dc0bdbfd03c4d8")

        // 알림 채널 생성 (Android 8.0+ 필수, 중복 생성해도 무방)
        NotificationHelper.createNotificationChannel(this)

        // FCM 토큰 발급 → 서버에 등록
        registerFcmToken()
    }

    // ───────────────────────────────────────────
    // Firebase에서 FCM 토큰 발급 → 서버에 등록
    // 비로그인 상태일 경우 실패해도 무시
    // (로그인 후 토큰 갱신 시 onNewToken에서 재시도)
    // ───────────────────────────────────────────
    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("MyApplication", "FCM 토큰 발급: ${token.take(20)}...")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        UserRepository().registerFcmToken(token)
                        Log.d("MyApplication", "FCM 토큰 서버 등록 완료")
                    } catch (e: Exception) {
                        Log.w("MyApplication", "FCM 토큰 등록 실패 (비로그인 상태일 수 있음): ${e.message}")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("MyApplication", "FCM 토큰 발급 실패: ${e.message}")
            }
    }
}
