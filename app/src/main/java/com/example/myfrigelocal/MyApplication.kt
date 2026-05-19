package com.example.myfrigelocal

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "8c80a8aa4bedabb2e8dc0bdbfd03c4d8")
    }
}
