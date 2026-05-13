package com.example.myfrigelocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.myfrigelocal.ui.MainScaffold
import com.example.myfrigelocal.ui.theme.MyFrigeLocalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthTokenStore.setAccessToken("eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjIsInJvbGUiOiJVU0VSIiwidG9rZW5UeXBlIjoiYWNjZXNzIiwiaWF0IjoxNzc4NzY3MjUyLCJleHAiOjE3Nzg3NjkwNTJ9.U4bpUJ8yLkd6TAkfIKgNJT8A0liJLerK5B2WAe-9r1c")
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
