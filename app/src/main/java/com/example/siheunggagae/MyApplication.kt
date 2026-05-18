package com.example.siheunggagae

import android.app.Application
import com.example.siheunggagae.BuildConfig  // ← 이걸로
import com.kakao.vectormap.KakaoMapSdk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoMapSdk.init(this, BuildConfig.KAKAO_APP_KEY)
    }
}