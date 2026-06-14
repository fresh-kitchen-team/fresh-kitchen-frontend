package com.freshkitchen.app.notification

import android.util.Log
import com.freshkitchen.app.data.auth.SettingsDataStore
import com.freshkitchen.app.network.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// ───────────────────────────────────────────
// FCM 메시지 수신 서비스
//   - onNewToken : FCM 토큰 갱신 시 서버에 재등록
//   - onMessageReceived : 백엔드가 보낸 알림 수신 → 로컬 알림 표시
// ───────────────────────────────────────────
@AndroidEntryPoint
class FreshKitchenMessagingService : FirebaseMessagingService() {

    @Inject lateinit var userRepository: UserRepository

    companion object {
        private const val TAG = "FCMService"
    }

    // ───────────────────────────────────────────
    // FCM 토큰이 새로 발급되거나 갱신될 때 호출
    // 서버에 새 토큰을 등록해야 알림이 정상 수신됨
    // ───────────────────────────────────────────
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM 토큰 갱신: ${token.take(20)}...")
        registerTokenToServer(token)
    }

    // ───────────────────────────────────────────
    // 백엔드에서 FCM 메시지 수신 시 호출
    // data["type"] 으로 메시지 종류 구분:
    //   "INQUIRY_REPLY" → 문의 답변 알림
    //   그 외           → 유통기한 알림
    // ───────────────────────────────────────────
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM 메시지 수신 from=${message.from}")

        val type = message.data["type"] ?: ""

        // 알람 설정 확인 (DataStore는 코루틴 기반이므로 runBlocking으로 동기 처리)
        val shouldShow = runBlocking {
            if (type == "INQUIRY_REPLY") {
                SettingsDataStore.getInquiryAlarmEnabled(applicationContext).first()
            } else {
                SettingsDataStore.getExpiryAlarmEnabled(applicationContext).first()
            }
        }

        if (!shouldShow) {
            Log.d(TAG, "알림 설정 꺼짐 (type=$type) — 표시 생략")
            return
        }

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "FreshKitchen 알림"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: if (type == "INQUIRY_REPLY") "문의하신 내용에 답변이 등록되었어요." else "냉장고를 확인해보세요!"

        Log.d(TAG, "알림 표시: type=$type title=$title")
        NotificationHelper.sendExpiryNotification(this, title, body)
    }

    // ───────────────────────────────────────────
    // 서버에 FCM 토큰 등록 (백그라운드에서 실행)
    // ───────────────────────────────────────────
    private fun registerTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.registerFcmToken(token)
                if (result.isSuccess) {
                    Log.d(TAG, "FCM 토큰 서버 등록 성공")
                } else {
                    Log.w(TAG, "FCM 토큰 서버 등록 실패: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "FCM 토큰 서버 등록 예외: ${e.message}")
            }
        }
    }
}
