package com.example.siheunggagae

import android.app.Application
import com.example.siheunggagae.data.local.TokenManager
import com.example.siheunggagae.data.network.RetrofitClient

class SiheungGagaeApp : Application() {

    lateinit var tokenManager: TokenManager
        private set

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(applicationContext)
        RetrofitClient.init(tokenManager)
    }
}
