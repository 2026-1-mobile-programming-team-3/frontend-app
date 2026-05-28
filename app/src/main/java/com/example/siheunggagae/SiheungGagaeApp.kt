package com.example.siheunggagae

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.crossfade
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SiheungGagaeApp : Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: coil3.PlatformContext): ImageLoader =
        ImageLoader.Builder(context).crossfade(200).build()

    lateinit var tokenManager: TokenManager
        private set

    lateinit var fcmTokenManager: FcmTokenManager
        private set

    var geoRepository: GeoRepository? = null
        private set

    lateinit var localNotificationStore: LocalNotificationStore
        private set

    val sessionExpiredChannel = Channel<Unit>(Channel.CONFLATED)

    /** FCM 알림 탭 시 이동할 deeplink URL. NavGraph 가 collect 해서 화면 이동 후 null 로 초기화. */
    val pendingDeeplinkLink = MutableStateFlow<String?>(null)

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        runCatching{KakaoMapSdk.init(this, BuildConfig.KAKAO_APP_KEY)}
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
