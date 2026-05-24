package com.example.siheunggagae.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.siheunggagae.SiheungGagaeApp
import com.example.siheunggagae.data.repository.AuthRepository
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

/**
 * 재호-1 앱 시작 스플래시
 *
 * 최소 1.5초 로고를 표시하면서 동시에 토큰 상태를 확인한다.
 *  - 토큰 없음              → onStartScreen (시작 화면)
 *  - 토큰 있고 유효함        → onHome
 *  - 토큰 만료 → 갱신 성공  → onHome
 *  - 토큰 만료 → 갱신 실패  → onStartScreen
 */
@Composable
fun AutoSplashScreen(
    onHome: () -> Unit,
    onStartScreen: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as SiheungGagaeApp

    LaunchedEffect(Unit) {
        val authRepository = AuthRepository(app.tokenManager, app.fcmTokenManager)

        // 토큰 확인과 1.5초 최소 표시 시간을 병렬로 실행
        val goHomeDeferred = async {
            val tokenManager = app.tokenManager
            when {
                tokenManager.accessToken == null          -> false   // 재호-1.4: 토큰 없음
                !tokenManager.isAccessTokenExpired()      -> true    // 재호-1.2: 유효한 토큰
                else                                      -> authRepository.refresh()  // 재호-1.3: 만료 → 갱신
            }
        }

        delay(800L)                         // 최소 표시 시간
        if (goHomeDeferred.await()) onHome() else onStartScreen()
    }

    SplashLogo()
}

@Composable
private fun SplashLogo() {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/logo.svg")
                .build(),
            contentDescription = "시흥가개 로고",
            modifier = Modifier.size(200.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AutoSplashScreenPreview() {
    SiheungGagaeTheme { SplashLogo() }
}
