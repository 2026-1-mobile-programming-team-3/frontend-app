package com.example.siheunggagae

import android.app.Application
import com.example.siheunggagae.data.local.LocalNotificationStore
import com.example.siheunggagae.data.local.TokenManager
import com.kakao.vectormap.KakaoMapSdk
import com.example.siheunggagae.data.location.LocationProvider
import com.example.siheunggagae.data.network.FcmTokenManager
import com.example.siheunggagae.data.network.RetrofitClient
import com.example.siheunggagae.data.repository.GeoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class SiheungGagaeApp : Application() {

    lateinit var tokenManager: TokenManager
        private set

    lateinit var fcmTokenManager: FcmTokenManager
        private set

    var geoRepository: GeoRepository? = null
        private set

    lateinit var localNotificationStore: LocalNotificationStore
        private set

    val sessionExpiredChannel = Channel<Unit>(Channel.CONFLATED)

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        runCatching { KakaoMapSdk.init(this, BuildConfig.KAKAO_APP_KEY) }
        tokenManager = TokenManager(applicationContext)
        localNotificationStore = LocalNotificationStore(applicationContext)
        RetrofitClient.init(tokenManager) {
            sessionExpiredChannel.trySend(Unit)
        }
        fcmTokenManager = FcmTokenManager(RetrofitClient.api, tokenManager)
        runCatching {
            geoRepository = GeoRepository(RetrofitClient.api, LocationProvider(applicationContext))
        }
        appScope.launch {
            runCatching { fcmTokenManager.registerCurrentDevice() }
        }
    }
}
