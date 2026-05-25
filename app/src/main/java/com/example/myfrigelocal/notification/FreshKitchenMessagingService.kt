package com.example.myfrigelocal.notification

import android.util.Log
import com.example.myfrigelocal.network.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ───────────────────────────────────────────
// FCM 메시지 수신 서비스
//   - onNewToken : FCM 토큰 갱신 시 서버에 재등록
//   - onMessageReceived : 백엔드가 보낸 알림 수신 → 로컬 알림 표시
// ───────────────────────────────────────────
class FreshKitchenMessagingService : FirebaseMessagingService() {

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
    // notification 필드 or data 필드로 내용 전달됨
    // ───────────────────────────────────────────
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM 메시지 수신 from=${message.from}")

        // notification 필드 (백엔드가 title/body를 설정한 경우)
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "FreshKitchen 알림"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "냉장고를 확인해보세요!"

        Log.d(TAG, "알림 표시: title=$title body=$body")
        NotificationHelper.sendExpiryNotification(this, title, body)
    }

    // ───────────────────────────────────────────
    // 서버에 FCM 토큰 등록 (백그라운드에서 실행)
    // ───────────────────────────────────────────
    private fun registerTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = UserRepository().registerFcmToken(token)
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
