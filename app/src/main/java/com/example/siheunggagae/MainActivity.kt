package com.example.siheunggagae

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsControllerCompat
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

class   MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // T5: status / navigation bar 를 라이트 모드(검정 아이콘) 로 강제.
        // 흰 배경 화면이 대부분이라 다크 아이콘이 자연스럽고, 지도 화면처럼
        // 카카오맵 위로 status bar 가 겹치는 케이스도 가독성 일관성 확보.
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        display?.supportedModes
            ?.maxByOrNull { it.refreshRate }
            ?.let { mode ->
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = mode.modeId
                }
            }

        // 포그라운드 FCM → FcmService가 "notification_link" 키로 설정
        // 백그라운드/종료 FCM → Firebase가 data 필드를 그대로 extras로 전달("link" 키)
        val initialLink = intent?.getStringExtra("notification_link")
            ?: intent?.getStringExtra("link")

        setContent {
            SiheungGagaeTheme {
                AppNavGraph(initialLink = initialLink)
            }
        }
    }

    /** 앱이 실행 중(SINGLE_TOP)일 때 알림을 탭하면 여기로 전달됨. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val link = intent.getStringExtra("notification_link")
            ?: intent.getStringExtra("link")
            ?: return
        (applicationContext as SiheungGagaeApp).pendingDeeplinkLink.value = link
    }
}

