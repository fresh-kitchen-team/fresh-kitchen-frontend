package com.example.myfrigelocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.myfrigelocal.data.auth.AuthTokenStore
import com.example.myfrigelocal.ui.MainScaffold
import com.example.myfrigelocal.ui.theme.MyFrigeLocalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 임시 테스트용 accessToken 설정
        // 실제 토큰을 커밋하면 안 됨. 테스트 끝나면 반드시 삭제.
        AuthTokenStore.setAccessToken("eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjIsInJvbGUiOiJVU0VSIiwidG9rZW5UeXBlIjoiYWNjZXNzIiwiaWF0IjoxNzc4NzU4Nzk5LCJleHAiOjE3Nzg3NjA1OTl9.dcWNWGK8PZZrPYHojpLY_zzMBTgW4DyOP9v9B6Gu3f4")

        setContent {
            MyFridgeApp()
        }
    }
}

@Composable
fun MyFridgeApp() {
    MyFrigeLocalTheme {
        MainScaffold()
    }
}
