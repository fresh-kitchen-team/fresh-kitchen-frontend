package com.example.myfrigelocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.myfrigelocal.data.auth.AuthTokenStore
import com.example.myfrigelocal.data.auth.TokenDataStore
import com.example.myfrigelocal.ui.MainScaffold
import com.example.myfrigelocal.ui.theme.MyFrigeLocalTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
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

        setContent {
            MyFridgeApp(isLoggedIn = savedToken != null)
        }
    }
}

@Composable
fun MyFridgeApp(isLoggedIn: Boolean = false) {
    MyFrigeLocalTheme {
        MainScaffold(isLoggedIn = isLoggedIn)
    }
}