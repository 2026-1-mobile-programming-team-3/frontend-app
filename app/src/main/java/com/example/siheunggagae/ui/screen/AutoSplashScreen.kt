package com.example.siheunggagae.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.siheunggagae.SiheungGagaeApp
import com.example.siheunggagae.data.repository.AuthRepository
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 재호-1 앱 시작 스플래시
 *
 * 최소 800ms 로고를 표시하면서 동시에 서버에 토큰 유효성을 확인한다.
 *  - 토큰 없음                         → onStartScreen
 *  - 토큰 있고 서버 검증 성공          → onHome
 *  - 토큰 있고 서버 검증 401           → TokenAuthenticator 가 refresh 자동 시도,
 *                                        성공 시 onHome, 실패 시 onStartScreen
 *  - 네트워크 오류로 3초 내 응답 없음  → onStartScreen (재로그인 유도)
 *
 * 기존에는 클라이언트 측 만료 여부만 보고 onHome 으로 보냈는데, 서버에서 강제 로그아웃된
 * 케이스에서는 곧장 401 → sessionExpired → Login 으로 한 번 더 튕겨, 그 사이 Home 이
 * 0.5~1초 정도 깜빡 보이고 사라지는 현상이 있었다. /users/me 한 번으로 서버 측 실제
 * 세션 상태를 미리 검증해 onHome 호출 자체를 미루는 식으로 깜빡임을 제거한다.
 */
@Composable
fun AutoSplashScreen(
    onHome: () -> Unit,
    onStartScreen: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as SiheungGagaeApp

    // 스플래시 중 시스템 back으로 앱이 즉시 종료되는 것을 막는다.
    BackHandler(enabled = true) { /* no-op */ }

    LaunchedEffect(Unit) {
        val authRepository = AuthRepository(app.tokenManager, app.fcmTokenManager)

        // 토큰 확인과 800ms 최소 표시 시간을 병렬로 실행
        val goHomeDeferred = async {
            withTimeoutOrNull(3_000L) { authRepository.ensureSession() } ?: false
        }

        delay(800L)                         // 최소 표시 시간
        if (goHomeDeferred.await()) onHome() else onStartScreen()
    }

    SplashLogo()
}

private val OrangeSandSplash  = Color(0xFFFFEDD4)
private val BackgroundSplash  = Color(0xFFFEFEFE)
private val Brown700Splash    = Color(0xFF8A6E58)
private val Orange500Splash   = Color(0xFFF7A35B)
private val PinkSurfaceSplash = Color(0xFFFEE7EC)

@Composable
private fun SplashLogo() {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(OrangeSandSplash, BackgroundSplash),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        val visible = remember { MutableTransitionState(false).apply { targetState = true } }
        AnimatedVisibility(
            visibleState = visible,
            enter = fadeIn() + scaleIn(initialScale = 0.7f),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data("file:///android_asset/logo.svg")
                        .build(),
                    contentDescription = "시흥가개 로고",
                    modifier = Modifier.size(140.dp),
                )
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "시흥의 모든 댕댕이를 위해 🐾",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Brown700Splash,
                )
            }
        }
        LinearProgressIndicator(
            color = Orange500Splash,
            trackColor = PinkSurfaceSplash,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .width(120.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AutoSplashScreenPreview() {
    SiheungGagaeTheme { SplashLogo() }
}
