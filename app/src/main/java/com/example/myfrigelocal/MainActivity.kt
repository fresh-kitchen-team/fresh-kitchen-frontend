package com.example.myfrigelocal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import com.example.myfrigelocal.data.auth.AuthTokenStore
import com.example.myfrigelocal.data.auth.TokenDataStore
import com.example.myfrigelocal.ui.MainScaffold
import com.example.myfrigelocal.ui.theme.MyFrigeLocalTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    // 알림 권한 요청 런처 (Android 13+)
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // 허용/거부 결과 — 별도 처리 불필요
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DataStore에서 저장된 토큰 읽기 (동기)
        val savedToken = runBlocking {
            TokenDataStore.getAccessToken(this@MainActivity).first()
        }

        if (savedToken != null) {
            // AuthTokenStore 하나로 통합 (RetrofitClient도 여기서 읽음)
            AuthTokenStore.setAccessToken(savedToken)
        }

        // 알림 권한 요청 (Android 13+)
        requestNotificationPermission()

        setContent {
            MyFridgeApp(isLoggedIn = savedToken != null)
        }
    }

    // Android 13 이상에서만 권한 팝업 표시, 이미 허용된 경우 스킵
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun MyFridgeApp(isLoggedIn: Boolean = false) {
    MyFrigeLocalTheme {
        MainScaffold(isLoggedIn = isLoggedIn)
    }
}