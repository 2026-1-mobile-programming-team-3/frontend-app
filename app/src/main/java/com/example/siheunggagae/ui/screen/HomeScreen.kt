package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.AppBottomBar
import com.example.siheunggagae.MapViewWrapper
import com.example.siheunggagae.R
import com.example.siheunggagae.Screen
import com.example.siheunggagae.SiheungGagaeApp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.siheunggagae.data.location.LocationProvider
import com.example.siheunggagae.data.model.NewsItem
import com.example.siheunggagae.data.model.StoreCategory
import com.example.siheunggagae.data.model.StoreResponse
import com.example.siheunggagae.data.network.RetrofitClient
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.viewmodel.HomeViewModel
import com.kakao.vectormap.MapView

// ─── 색상 ────────────────────────────────────────────────────────────────────

private val Brown900H     = Color(0xFF614B3A)
private val Brown700H     = Color(0xFF8A6E58)
private val BrownBorderH  = Color(0xFFE8D3C2)
private val Orange500H    = Color(0xFFF7A35B)
private val Pink500H      = Color(0xFFF04268)
private val Green500H     = Color(0xFF00A63E)
private val Blue400H      = Color(0xFF388AF5)
private val Orange600H    = Color(0xFFEE6A46)
private val PinkSurfaceH  = Color(0xFFFEE7EC)
private val BackgroundH   = Color(0xFFFEFEFE)
private val GrayTextH     = Color(0xFF6B7280)
private val TextBlackH    = Color(0xFF1E120A)
private val StarYellowH   = Color(0xFFFDC700)

private val miniMarkerColors = mapOf(
    "CAFE"       to 0xFF8A6E58.toInt(),
    "PARK"       to 0xFF4CAF50.toInt(),
    "HOSPITAL"   to 0xFFF04268.toInt(),
    "GROOMING"   to 0xFF9C27B0.toInt(),
    "RESTAURANT" to 0xFFF7A35B.toInt(),
)

private val siheungDongs = listOf(
    "정왕동", "배곧동", "목감동", "시화동", "신천동",
    "연성동", "은행동", "장곡동", "능곡동", "조남동",
    "군자동", "방산동", "월곶동",
)

// ─── 메인 화면 ────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onNotificationClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onPlaceDetailClick: (Int) -> Unit = {},
    onNewsDetailClick: (Int) -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            api = RetrofitClient.api,
            locationProvider = LocationProvider(context),
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    var showDongModal by remember { mutableStateOf(false) }

    // 미니 맵
    val lifecycleOwner = LocalLifecycleOwner.current
    val miniMapView = remember { MapView(context) }
    val miniMapWrapper = remember { MapViewWrapper(miniMapView) }
    var miniMapReady by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> miniMapWrapper.resume()
                Lifecycle.Event.ON_PAUSE  -> miniMapWrapper.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            miniMapView.finish()
        }
    }

    LaunchedEffect(Unit) { miniMapWrapper.init { miniMapReady = true } }

    LaunchedEffect(miniMapReady, uiState.location) {
        val loc = uiState.location ?: return@LaunchedEffect
        if (miniMapReady) miniMapWrapper.moveCamera(loc.latitude, loc.longitude, 14)
    }

    LaunchedEffect(miniMapReady, uiState.allStores) {
        if (!miniMapReady) return@LaunchedEffect
        miniMapWrapper.clearMarkersWithPrefix("mini_")
        uiState.allStores.take(4).forEach { store ->
            val color = miniMarkerColors[store.category] ?: 0xFF614B3A.toInt()
            miniMapWrapper.addMarker("mini_${store.id}", store.lat, store.lng, color)
        }
    }

    Scaffold(
        containerColor = BackgroundH,
        bottomBar = { AppBottomBar(currentRoute = Screen.Home.route, onNavigate = onNavigate) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            HomeTopBar(
                nickname = uiState.nickname.ifEmpty { "사용자" },
                regionDong = uiState.regionDong,
                onNotificationClick = onNotificationClick,
                onDongClick = { showDongModal = true },
            )
            Spacer(Modifier.height(8.dp))
            WalkIndexSection(
                walkScore = uiState.walkScore,
                weather = uiState.weather,
                temperatureCelsius = uiState.temperatureCelsius,
                airQuality = uiState.airQuality,
                pendingMatchCount = uiState.pendingMatchCount,
                nearestDDay = uiState.nearestDDay,
                onBannerClick = { onNavigate(Screen.MyRequests.route) },
            )
            Spacer(Modifier.height(8.dp))
            NearbyStoresSection(
                regionDong = uiState.regionDong,
                nearbyStoreCount = uiState.nearbyStoreCount,
                selectedCategory = uiState.selectedCategory,
                stores = uiState.displayedStores,
                miniMapView = miniMapView,
                onCategorySelected = { viewModel.selectCategory(it) },
                onMapClick = { onNavigate(Screen.Map.route) },
                onPlaceDetailClick = onPlaceDetailClick,
                onFavoriteClick = { store -> viewModel.toggleFavorite(store) },
            )
            Spacer(Modifier.height(8.dp))
            PetNewsSection(
                news = uiState.news,
                onAllClick = { onNavigate(Screen.News.route) },
                onNewsClick = onNewsDetailClick,
                onVolunteerApplyClick = { onNavigate(Screen.VolunteerApply.route) },
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDongModal) {
        DongChangeModal(
            currentDong = uiState.regionDong,
            onDismiss = { showDongModal = false },
            onSelect = { dong ->
                viewModel.updateRegionDong(dong)
                showDongModal = false
            },
        )
    }
}

// ─── TopBar ──────────────────────────────────────────────────────────────────

@Composable
fun HomeTopBar(
    nickname: String = "사용자",
    regionDong: String = "정왕동",
    onNotificationClick: () -> Unit = {},
    onDongClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "안녕하세요, ${nickname}님",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = Brown700H,
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFF101828))) { append("시흥") }
                    withStyle(SpanStyle(color = StarYellowH)) { append("가개") }
                },
                fontFamily = PretendardFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 32.sp,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .border(1.dp, BrownBorderH, RoundedCornerShape(50.dp))
                    .clickable { onDongClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(painterResource(R.drawable.ic_location_on), null, tint = Orange500H, modifier = Modifier.size(14.dp))
                Text(
                    text = regionDong,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    color = Brown700H,
                )
            }
            IconButton(onClick = onNotificationClick, modifier = Modifier.size(36.dp)) {
                Icon(painterResource(R.drawable.ic_notifications), "알림", tint = Brown700H, modifier = Modifier.size(22.dp))
            }
        }
    }
}

// ─── 산책지수 섹션 ────────────────────────────────────────────────────────────

@Composable
fun WalkIndexSection(
    walkScore: Int = 0,
    weather: String = "",
    temperatureCelsius: Int? = null,
    airQuality: String = "",
    pendingMatchCount: Int = 0,
    nearestDDay: Int? = null,
    onBannerClick: () -> Unit = {},
) {
    val scoreColor = when {
        walkScore >= 90 -> Green500H
        walkScore >= 60 -> Blue400H
        else            -> Orange600H
    }
    val scoreComment = when {
        walkScore >= 90 -> "좋아요"
        walkScore >= 60 -> "보통이에요"
        else            -> "나빠요"
    }
    val weatherText = buildString {
        if (weather.isNotEmpty()) append(weather)
        if (temperatureCelsius != null) append(" · ${temperatureCelsius}°")
        if (airQuality.isNotEmpty()) append(" · 미세먼지 $airQuality")
    }.ifEmpty { "날씨 정보 없음" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Text(
            text = "오늘 산책지수",
            fontFamily = PretendardFamily,
            color = TextBlackH,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = buildAnnotatedString {
                if (walkScore > 0) {
                    withStyle(SpanStyle(fontFamily = PretendardFamily, color = scoreColor, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)) {
                        append("${walkScore}점")
                    }
                    withStyle(SpanStyle(fontFamily = PretendardFamily, color = TextBlackH, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)) {
                        append("으로 $scoreComment")
                    }
                } else {
                    withStyle(SpanStyle(fontFamily = PretendardFamily, color = GrayTextH, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)) {
                        append("로딩 중...")
                    }
                }
            },
            lineHeight = 32.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = weatherText,
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = GrayTextH,
        )
        if (pendingMatchCount > 0 || nearestDDay != null) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(PinkSurfaceH)
                    .clickable { onBannerClick() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = buildAnnotatedString {
                        if (nearestDDay != null) {
                            withStyle(SpanStyle(fontFamily = PretendardFamily, color = Pink500H, fontWeight = FontWeight.Bold, fontSize = 12.sp)) {
                                append("[D-${nearestDDay}]  ")
                            }
                        }
                        withStyle(SpanStyle(fontFamily = PretendardFamily, color = Color(0xFF374151), fontWeight = FontWeight.Medium, fontSize = 14.sp)) {
                            append("신청 ${pendingMatchCount}건 검토하기  ")
                        }
                    },
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Brown700H, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ─── 주변 매장 섹션 ───────────────────────────────────────────────────────────

@Composable
fun NearbyStoresSection(
    regionDong: String = "정왕동",
    nearbyStoreCount: Int = 0,
    selectedCategory: StoreCategory = StoreCategory.ALL,
    stores: List<StoreResponse> = emptyList(),
    miniMapView: MapView,
    onCategorySelected: (StoreCategory) -> Unit = {},
    onMapClick: () -> Unit = {},
    onPlaceDetailClick: (Int) -> Unit = {},
    onFavoriteClick: (StoreResponse) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 20.dp),
    ) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("주변 매장", fontFamily = PretendardFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp, color = TextBlackH)
                if (nearbyStoreCount > 0) {
                    Text("${nearbyStoreCount}곳", fontFamily = PretendardFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp, color = Pink500H)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { onMapClick() },
            ) {
                Icon(painterResource(R.drawable.ic_map), null, tint = Orange500H, modifier = Modifier.size(16.dp))
                Text("지도 보기", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Orange500H)
            }
        }
        Spacer(Modifier.height(12.dp))

        // 미니 지도
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            AndroidView(factory = { miniMapView }, modifier = Modifier.fillMaxSize())

            // 동 이름 칩
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(painterResource(R.drawable.ic_location_on), null, tint = Orange500H, modifier = Modifier.size(12.dp))
                Text(regionDong, fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
            }

            // 탭 시 전체 지도로 이동
            Box(modifier = Modifier.fillMaxSize().clickable { onMapClick() })
        }

        Spacer(Modifier.height(12.dp))

        // 카테고리 칩
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StoreCategory.entries.forEach { cat ->
                val isSel = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isSel) Color(0xFF1A1A1A) else Color.White)
                        .then(if (!isSel) Modifier.border(1.dp, BrownBorderH, RoundedCornerShape(50.dp)) else Modifier)
                        .clickable { onCategorySelected(cat) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = cat.label,
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                        color = if (isSel) Color.White else Brown700H,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        // 매장 카드 (최대 3개)
        stores.take(3).forEachIndexed { idx, store ->
            HomeStoreItem(
                number = idx + 1,
                store = store,
                onPlaceDetailClick = onPlaceDetailClick,
                onFavoriteClick = onFavoriteClick,
            )
            if (idx < minOf(stores.size, 3) - 1) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF3F4F6))
            }
        }
    }
}

@Composable
private fun HomeStoreItem(
    number: Int,
    store: StoreResponse,
    onPlaceDetailClick: (Int) -> Unit,
    onFavoriteClick: (StoreResponse) -> Unit,
) {
    val distanceText = store.distanceM?.let {
        if (it < 1000) "${"%.0f".format(it)}m" else "${"%.1f".format(it / 1000)}km"
    } ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlaceDetailClick(store.id) }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(32.dp).clip(CircleShape)
                .background(if (number == 1) Pink500H else Brown900H),
        ) {
            Text("$number", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = if (number == 1) FontWeight.Medium else FontWeight.Normal, color = Color.White)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(store.name, fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, lineHeight = 20.sp, color = TextBlackH)
            Text(
                text = buildString {
                    append(store.category)
                    if (distanceText.isNotEmpty()) append(" · $distanceText")
                    if (store.rating != null) append(" · ⭐ ${"%.1f".format(store.rating)}")
                },
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp,
                color = Brown700H,
            )
        }
        IconButton(onClick = { onFavoriteClick(store) }, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_favorite),
                contentDescription = "즐겨찾기",
                tint = if (store.isFavorite) Pink500H else BrownBorderH,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ─── 반려동물 소식 섹션 ───────────────────────────────────────────────────────

@Composable
fun PetNewsSection(
    news: List<NewsItem> = emptyList(),
    onAllClick: () -> Unit = {},
    onNewsClick: (Int) -> Unit = {},
    onVolunteerApplyClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 20.dp),
    ) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("반려동물 소식", fontFamily = PretendardFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp, color = TextBlackH)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { onAllClick() },
            ) {
                Icon(painterResource(R.drawable.ic_newsmode), null, tint = Orange500H, modifier = Modifier.size(16.dp))
                Text("전체 보기", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Orange500H)
            }
        }
        Spacer(Modifier.height(12.dp))

        val mainNews = news.firstOrNull()
        val subNews = news.drop(1).take(3)

        // 메인 뉴스 카드
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF9F9F9))
                .clickable { mainNews?.let { onNewsClick(it.id) } }
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_home_news_thumb),
                contentDescription = "뉴스 썸네일",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                if (mainNews != null) {
                    val (tColor, tBg) = newsTagColors(mainNews.category)
                    HomeTagChip(text = mainNews.category, textColor = tColor, bgColor = tBg)
                    Spacer(Modifier.height(5.dp))
                    Text(mainNews.title, fontFamily = PretendardFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 28.sp, color = TextBlackH, maxLines = 2)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            append(mainNews.publishedAt)
                            if (!mainNews.source.isNullOrEmpty()) append(" · ${mainNews.source}")
                        },
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        color = GrayTextH,
                    )
                } else {
                    HomeTagChip("지원", Color(0xFF0284C7), Color(0xFFE0F2FE))
                    Spacer(Modifier.height(5.dp))
                    Text("소식을 불러오는 중...", fontFamily = PretendardFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 28.sp, color = GrayTextH)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF3F4F6))

        // 서브 뉴스 리스트
        subNews.forEach { item ->
            val (tColor, tBg) = newsTagColors(item.category)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNewsClick(item.id) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HomeTagChip(item.category, tColor, tBg)
                Text(item.title, fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, lineHeight = 20.sp, color = TextBlackH, modifier = Modifier.weight(1f))
                Text(item.publishedAt, fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp, color = GrayTextH)
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF3F4F6))
        }

        Spacer(Modifier.height(12.dp))

        // 봉사자 배너
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PinkSurfaceH)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.ic_handshake), null, tint = Orange500H, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            Text("이동 지원 봉사자가 되어 보세요", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, lineHeight = 20.sp, color = TextBlackH, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White)
                    .clickable { onVolunteerApplyClick() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text("신청 →", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Brown700H)
            }
        }
    }
}

// ─── 동 변경 모달 ──────────────────────────────────────────────────────────────

@Composable
private fun DongChangeModal(
    currentDong: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 8.dp),
        ) {
            Text(
                text = "동네 변경",
                fontFamily = PretendardFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = TextBlackH,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            HorizontalDivider(color = Color(0xFFF3F4F6))
            LazyColumn(modifier = Modifier.height(320.dp)) {
                items(siheungDongs) { dong ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(dong) }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(dong, fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, color = TextBlackH)
                        if (dong == currentDong) {
                            Icon(painterResource(R.drawable.ic_location_on), null, tint = Pink500H, modifier = Modifier.size(16.dp))
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF3F4F6))
                }
            }
        }
    }
}

// ─── 유틸 ────────────────────────────────────────────────────────────────────

@Composable
private fun HomeTagChip(text: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    ) {
        Text(text, fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp, color = textColor)
    }
}

private fun newsTagColors(category: String): Pair<Color, Color> = when (category) {
    "지원" -> Pair(Color(0xFF0284C7), Color(0xFFE0F2FE))
    "행사" -> Pair(Color(0xFF7C3AED), Color(0xFFF5F3FF))
    "봉사" -> Pair(Color(0xFF059669), Color(0xFFD1FAE5))
    "정책" -> Pair(Color(0xFFDC2626), Color(0xFFFEF2F2))
    else   -> Pair(Color(0xFF6B7280), Color(0xFFF3F4F6))
}
