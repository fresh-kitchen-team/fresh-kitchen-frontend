package com.example.myfrigelocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.myfrigelocal.ui.MainScaffold
import com.example.myfrigelocal.ui.theme.MyFrigeLocalTheme
import com.example.myfrigelocal.data.auth.AuthTokenStore


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthTokenStore.setAccessToken("eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjMsInJvbGUiOiJVU0VSIiwidG9rZW5UeXBlIjoiYWNjZXNzIiwiaWF0IjoxNzc4ODIzNjYwLCJleHAiOjE3Nzk0Mjg0NjB9.0QBh8OOfbM_72scHyFPw2qNXPKEQKYAOphhrMZZLCGY")
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
