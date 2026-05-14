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
        AuthTokenStore.setAccessToken("eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjIsInJvbGUiOiJVU0VSIiwidG9rZW5UeXBlIjoiYWNjZXNzIiwiaWF0IjoxNzc4NzcyMjIyLCJleHAiOjE3Nzg3NzQwMjJ9.9xxSg31LvOQQYT0KQiUBvG5Z3HNILGk-XEB1IvxcqNk")
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
