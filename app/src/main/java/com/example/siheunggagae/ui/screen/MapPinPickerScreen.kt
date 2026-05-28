package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.MapViewWrapper
import com.example.siheunggagae.R
import com.example.siheunggagae.ui.viewmodel.MapPinPickerViewModel
import com.example.siheunggagae.ui.viewmodel.PinPickerUi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.MapView
import kotlinx.coroutines.delay

// ─── 색상 ─────────────────────────────────────────────────────────────────────

private val Brown900M  = Color(0xFF614B3A)
private val Brown700M  = Color(0xFF8A6E58)
private val TextBlackM = Color(0xFF1E120A)
private val OrangeSandM = Color(0xFFFFEDD4)
private val Orange500M = Color(0xFFF7A35B)

// ─── 화면 ─────────────────────────────────────────────────────────────────────

@Composable
fun MapPinPickerScreen(
    viewModel: MapPinPickerViewModel,
    onBack: () -> Unit,
    onConfirm: (lat: Double, lng: Double, address: String?) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember { MapView(context) }
    val mapWrapper = remember { MapViewWrapper(mapView) }
    var mapReady by remember { mutableStateOf(false) }

    // MapView 라이프사이클 연동.
    // 카카오 MapView 는 start() 호출 후 KakaoMapReadyCallback 가 발화하기 전에 pause/resume 을 부르면
    // 내부 IMapSurfaceView 가 null 이라 NPE — 빠른 popBackStack 직후 onPause 가 그 사이에 끼는 경우가 있다.
    // mapReady 플래그로 gate 한다.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> if (mapReady) mapWrapper.resume()
                Lifecycle.Event.ON_PAUSE  -> if (mapReady) mapWrapper.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.finish()
        }
    }

    // 지도 초기화 + camera idle 리스너 등록
    LaunchedEffect(Unit) {
        mapWrapper.onMapDestroyed = { mapReady = false }
        mapWrapper.init { kakaoMap: KakaoMap ->
            kakaoMap.setOnCameraMoveEndListener { _, position, _ ->
                val latLng = position.position
                viewModel.onCameraIdle(latLng.latitude, latLng.longitude)
            }
            mapReady = true
        }
    }

    // 세션 무효화 후 재초기화
    LaunchedEffect(mapReady) {
        if (!mapReady && mapWrapper.hasBeenInitialized) {
            delay(100)
            mapWrapper.reinit()
        }
    }

    // 지도 준비 완료 후 초기 위치로 카메라 이동
    LaunchedEffect(mapReady) {
        if (mapReady) {
            mapWrapper.moveCamera(state.lat, state.lng, 17)
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
        )

        // 중앙 핀 오버레이 (지도와 함께 움직이지 않음)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_map_pin),
                contentDescription = null,
                tint = Orange500M,
                modifier = Modifier.size(40.dp),
            )
        }

        // TopBar (투명 오버레이)
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onBack() },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_left),
                        contentDescription = "뒤로가기",
                        tint = TextBlackM,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                "위치 선택",
                color = TextBlackM,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(36.dp))
        }

        // 하단 시트 (주소 + CTA)
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    "선택된 위치",
                    color = Brown700M,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(OrangeSandM),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_map_pin),
                            contentDescription = null,
                            tint = Orange500M,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            state.address ?: if (state.resolving) "주소 확인 중..." else "주소를 가져올 수 없어요",
                            color = TextBlackM,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "%.5f, %.5f".format(state.lat, state.lng),
                            color = Brown700M,
                            fontSize = 12.sp,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                val isCoordValid = state.lat != 0.0 && state.lng != 0.0 && !state.resolving
                Button(
                    onClick = { if (isCoordValid) onConfirm(state.lat, state.lng, state.address) },
                    enabled = isCoordValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brown900M,
                        disabledContainerColor = Brown900M.copy(alpha = 0.4f),
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text(
                        "이 위치로 설정",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun MapPinPickerScreenPreview() {
    // Preview용 더미 상태 렌더링 (실제 지도는 생략)
    val dummyState = PinPickerUi(
        lat = 37.3799,
        lng = 126.8030,
        address = "경기도 시흥시 시청로 14",
        resolving = false,
    )

    Box(Modifier.fillMaxSize()) {
        // 지도 플레이스홀더
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFD0FEE1)),
        )

        // 중앙 핀 오버레이
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_map_pin),
                contentDescription = null,
                tint = Orange500M,
                modifier = Modifier.size(40.dp),
            )
        }

        // TopBar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.size(36.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_left),
                        contentDescription = "뒤로가기",
                        tint = TextBlackM,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                "위치 선택",
                color = TextBlackM,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(36.dp))
        }

        // 하단 시트
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    "선택된 위치",
                    color = Brown700M,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(OrangeSandM),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_map_pin),
                            contentDescription = null,
                            tint = Orange500M,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            dummyState.address ?: "주소를 가져올 수 없어요",
                            color = TextBlackM,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "%.5f, %.5f".format(dummyState.lat, dummyState.lng),
                            color = Brown700M,
                            fontSize = 12.sp,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Brown900M),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text(
                        "이 위치로 설정",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
