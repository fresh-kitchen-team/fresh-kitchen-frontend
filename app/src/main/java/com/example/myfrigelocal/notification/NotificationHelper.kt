package com.example.myfrigelocal.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myfrigelocal.MainActivity
import com.example.myfrigelocal.R

// ───────────────────────────────────────────
// 알림 채널 생성 + 유통기한 알림 발송 유틸
// ───────────────────────────────────────────
object NotificationHelper {

    private const val CHANNEL_ID   = "expiry_alarm_channel"
    private const val CHANNEL_NAME = "유통기한 알림"
    private const val NOTIF_ID     = 1001

    // ───────────────────────────────────────────
    // 앱 시작 시 한 번만 호출 (채널 등록)
    // ───────────────────────────────────────────
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "냉장고 속 유통기한이 임박한 식재료를 알려드려요"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // ───────────────────────────────────────────
    // 유통기한 임박 알림 발송
    //   title   : 알림 제목
    //   message : 알림 본문 (식재료 목록)
    // ───────────────────────────────────────────
    fun sendExpiryNotification(context: Context, title: String, message: String) {
        // 알림 탭 시 앱 메인 화면으로 이동
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)   // 앱 아이콘 사용
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))  // 긴 텍스트 펼쳐서 표시
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)  // 탭하면 알림 자동 삭제
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS 권한 없을 때 조용히 무시
        }
    }
}
