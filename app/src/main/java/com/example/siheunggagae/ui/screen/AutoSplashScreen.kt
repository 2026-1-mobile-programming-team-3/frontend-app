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
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFEDD4), Color(0xFFFEFEFE)),
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
                    color = Color(0xFF8A6E58),
                )
            }
        }
        LinearProgressIndicator(
            color = Color(0xFFF7A35B),
            trackColor = Color(0xFFFEE7EC),
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
