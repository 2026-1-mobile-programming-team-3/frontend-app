package com.example.siheunggagae

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

class   MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

