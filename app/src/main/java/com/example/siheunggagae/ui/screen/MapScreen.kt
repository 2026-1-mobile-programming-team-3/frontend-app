package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.AppBottomBar
import com.example.siheunggagae.MapViewWrapper
import com.example.siheunggagae.R
import com.example.siheunggagae.Screen
import com.example.siheunggagae.SiheungGagaeApp

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.siheunggagae.data.local.MapFilterStore
import com.example.siheunggagae.data.location.LocationProvider
import com.example.siheunggagae.data.model.StoreCategory
import com.example.siheunggagae.data.model.StoreResponse
import com.example.siheunggagae.data.model.StoreSearchResult
import com.example.siheunggagae.data.model.UserRole
import com.example.siheunggagae.data.model.VolunteerMarkerDto
import com.example.siheunggagae.data.network.RetrofitClient
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.util.rememberLocationPermissionState
import com.example.siheunggagae.ui.viewmodel.MapViewModel
import com.kakao.vectormap.MapView
import kotlinx.coroutines.launch

// ─── 색상 ────────────────────────────────────────────────────────────────────

private val Brown900Mp   = Color(0xFF614B3A)
private val Brown700Mp   = Color(0xFF8A6E58)
private val Brown400Mp   = Color(0xFFC4A882)
private val BrownBorderP = Color(0xFFE8D3C2)
private val Orange500Mp  = Color(0xFFF7A35B)
private val Pink500Mp    = Color(0xFFF04268)
private val TextBlack    = Color(0xFF1E120A)
private val StarYellow   = Color(0xFFFDC700)

private val categoryColors = mapOf(
    "CAFE"       to 0xFF8A6E58.toInt(),
    "PARK"       to 0xFF4CAF50.toInt(),
    "HOSPITAL"   to 0xFFF04268.toInt(),
    "GROOMING"   to 0xFF9C27B0.toInt(),
    "RESTAURANT" to 0xFFF7A35B.toInt(),
)
private val defaultMarkerColor = 0xFF614B3A.toInt()
private val volunteerMarkerColor = 0xFF2196F3.toInt()

// ─── 메인 화면 ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigate: (String) -> Unit = {},
    startVolunteerMode: Boolean = false,
) {
    val context = LocalContext.current
    val app = context.applicationContext as SiheungGagaeApp
    val viewModel: MapViewModel = viewModel(
        factory = MapViewModel.Factory(
            api = RetrofitClient.api,
            locationProvider = LocationProvider(context),
            filterStore = MapFilterStore(context),
            initialVolunteerMode = startVolunteerMode,
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val locationPermission = rememberLocationPermissionState { granted ->
        if (granted) viewModel.moveToCurrentLocation()
    }

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var mapReady by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }
    val mapWrapper = remember { MapViewWrapper(mapView) }

    // MapView 라이프사이클 연동
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapWrapper.resume()
                Lifecycle.Event.ON_PAUSE  -> mapWrapper.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.finish()
        }
    }

    // 지도 초기화
    LaunchedEffect(Unit) {
        mapWrapper.init { mapReady = true }
        if (!locationPermission.hasPermission) locationPermission.request()
    }

    // 맵 준비되면 무조건 시흥시로 시작 (에뮬레이터 GPS 위치 무시)
    LaunchedEffect(mapReady) {
        if (!mapReady) return@LaunchedEffect
        mapWrapper.moveCamera(37.3795, 126.8025)
    }

    // 내 위치 버튼 → cameraSerial이 바뀔 때마다 항상 이동 (같은 좌표여도 재실행)
    LaunchedEffect(mapReady, uiState.cameraSerial) {
        if (!mapReady) return@LaunchedEffect
        val (lat, lng) = uiState.cameraTarget ?: return@LaunchedEffect
        mapWrapper.moveCamera(lat, lng)
    }

    // 매장 마커 동기화 (카테고리 필터 반영)
    LaunchedEffect(mapReady, uiState.stores, uiState.visibleCategories) {
        if (!mapReady) return@LaunchedEffect
        mapWrapper.clearMarkersWithPrefix("store_")
        uiState.stores
            .filter { it.category in uiState.visibleCategories }
            .forEach { store ->
                val color = categoryColors[store.category] ?: defaultMarkerColor
                mapWrapper.addMarker(
                    id = "store_${store.storeId}",
                    lat = store.latitude,
                    lng = store.longitude,
                    markerColor = color,
                    onTap = { viewModel.selectStore(store) },
                )
            }
    }

    // 봉사요청 마커 동기화
    LaunchedEffect(mapReady, uiState.isVolunteerMode, uiState.volunteerMarkers) {
        if (!mapReady) return@LaunchedEffect
        mapWrapper.clearMarkersWithPrefix("vol_")
        if (uiState.isVolunteerMode) {
            uiState.volunteerMarkers.forEach { vol ->
                mapWrapper.addMarker(
                    id = "vol_${vol.requestId}",
                    lat = vol.latitude,
                    lng = vol.longitude,
                    markerColor = volunteerMarkerColor,
                    onTap = { onNavigate(Screen.MatchingPublicDetail.createRoute(vol.requestId)) },
                )
            }
        }
    }

    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true,
        )
    )

    // 마커 탭 시 시트 살짝 올리기
    LaunchedEffect(uiState.selectedStore) {
        if (uiState.selectedStore != null) {
            scope.launch { sheetState.bottomSheetState.partialExpand() }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { AppBottomBar(currentRoute = Screen.Map.route, onNavigate = onNavigate) },
    ) { navPadding ->
        BottomSheetScaffold(
            modifier = Modifier.padding(navPadding),
            scaffoldState = sheetState,
            sheetPeekHeight = 280.dp,
            sheetContainerColor = Color.White,
            sheetTonalElevation = 0.dp,
            sheetShadowElevation = 8.dp,
            sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            sheetDragHandle = { MapDragHandle() },
            sheetContent = {
                val isExpanded = sheetState.bottomSheetState.currentValue == SheetValue.Expanded
                MapBottomSheetContent(
                    selectedStore = uiState.selectedStore,
                    stores = uiState.stores,
                    totalCount = uiState.totalCount,
                    isExpanded = isExpanded,
                    onStoreClick = { store -> onNavigate(Screen.PlaceDetail.createRoute(store.storeId)) },
                    onClearSelection = { viewModel.selectStore(null) },
                )
            },
            containerColor = Color.Transparent,
        ) { _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                // 실제 카카오 MapView
                AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

                // 상단 검색바 + 카테고리 칩
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                ) {
                    MapSearchCard(onClick = { showSearch = true })
                    Spacer(Modifier.height(8.dp))
                    MapCategoryChipRow(
                        selected = uiState.selectedCategory,
                        onSelect = { viewModel.selectCategory(it) },
                    )
                }

                // 우측 플로팅 버튼
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MapIconFab(R.drawable.ic_my_location, "내 위치") {
                        viewModel.moveToCurrentLocation()
                    }
                    MapIconFab(R.drawable.ic_layers, "레이어") { showFilterSheet = true }
                    MapIconFab(R.drawable.ic_refresh, "새로고침") {
                        val loc = uiState.location
                        if (loc != null) viewModel.refresh(loc.latitude, loc.longitude)
                    }
                }

                // 봉사요청 마커 토글 (VOLUNTEER 전용)
                if (uiState.userRole == UserRole.VOLUNTEER) {
                    VolunteerToggleChip(
                        isOn = uiState.isVolunteerMode,
                        onClick = { viewModel.toggleVolunteerMode() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 16.dp, top = 120.dp),
                    )
                }
            }
        }
    }

    if (showFilterSheet) {
        MapFilterBottomSheet(
            initialCategories = uiState.visibleCategories,
            onDismiss = { showFilterSheet = false },
            onApply = { entries ->
                val apiCategories = entries
                    .filter { it.isSelected }
                    .mapNotNull { MapFilterStore.NAME_TO_API[it.name] }
                    .toSet()
                viewModel.applyFilter(apiCategories)
                showFilterSheet = false
            },
        )
    }

    if (showSearch) {
        MapSearchOverlay(
            onDismiss = { showSearch = false },
            onResultClick = { storeId ->
                showSearch = false
                onNavigate(Screen.PlaceDetail.createRoute(storeId))
            },
        )
    }
}

// ─── 검색바 ─────────────────────────────────────────────────────────────────

@Composable
private fun MapSearchCard(onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(4.dp, RoundedCornerShape(50.dp))
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            tint = Brown400Mp,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = "매장 · 병원 · 공원 검색",
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = Brown400Mp,
        )
    }
}

// ─── 카테고리 칩 ──────────────────────────────────────────────────────────────

@Composable
private fun MapCategoryChipRow(selected: StoreCategory, onSelect: (StoreCategory) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StoreCategory.entries.forEach { category ->
            val isSelected = category == selected
            Box(
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(50.dp))
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isSelected) Color(0xFF1A1A1A) else Color.White)
                    .clickable { onSelect(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = category.label,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    color = if (isSelected) Color.White else Brown700Mp,
                )
            }
        }
    }
}

// ─── 플로팅 버튼 ──────────────────────────────────────────────────────────────

@Composable
private fun MapIconFab(iconRes: Int, contentDescription: String, onClick: () -> Unit = {}) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick() },
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = Brown700Mp,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ─── 봉사요청 토글 칩 ─────────────────────────────────────────────────────────

@Composable
private fun VolunteerToggleChip(isOn: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(50.dp))
            .clip(RoundedCornerShape(50.dp))
            .background(if (isOn) Color(0xFF2196F3) else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = "봉사요청 보기",
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 16.sp,
            color = if (isOn) Color.White else Brown700Mp,
        )
    }
}

// ─── 드래그 핸들 ─────────────────────────────────────────────────────────────

@Composable
private fun MapDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFE8E8E8)),
        )
    }
}

// ─── 하단 시트 콘텐츠 ─────────────────────────────────────────────────────────

@Composable
private fun MapBottomSheetContent(
    selectedStore: StoreResponse?,
    stores: List<StoreResponse>,
    totalCount: Int,
    isExpanded: Boolean,
    onStoreClick: (StoreResponse) -> Unit,
    onClearSelection: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (selectedStore != null) {
            // 마커 선택 상태: 단일 매장 카드
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "선택된 매장",
                    fontFamily = PretendardFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = TextBlack,
                )
                IconButton(onClick = onClearSelection, modifier = Modifier.size(28.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_refresh),
                        contentDescription = "선택 해제",
                        tint = Brown700Mp,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFF3F4F6))
            MapPlaceItem(place = selectedStore, onClick = { onStoreClick(selectedStore) })
        } else {
            // 기본 상태: 주변 매장 목록
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (totalCount > 0) "주변 매장 $totalCount 곳" else "주변 매장",
                    fontFamily = PretendardFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = TextBlack,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_swap_vert),
                        contentDescription = null,
                        tint = Brown700Mp,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "거리순",
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 17.sp,
                        color = Brown700Mp,
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFF3F4F6))
            if (isExpanded) {
                // 풀스크린 모드: 전체 리스트
                LazyColumn {
                    items(stores, key = { it.storeId }) { store ->
                        MapPlaceItem(place = store, onClick = { onStoreClick(store) })
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = Color(0xFFF3F4F6),
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            } else {
                // 기본 모드: 첫 번째 카드만
                stores.firstOrNull()?.let { store ->
                    MapPlaceItem(place = store, onClick = { onStoreClick(store) })
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = Color(0xFFF3F4F6),
                    )
                }
            }
        }
    }
}

// ─── 매장 카드 아이템 ─────────────────────────────────────────────────────────

@Composable
private fun MapPlaceItem(place: StoreResponse, onClick: () -> Unit = {}) {
    val distanceText = place.distanceM?.let {
        if (it < 1000) "${"%.0f".format(it)}m" else "${"%.1f".format(it / 1000)}km"
    } ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brown900Mp),
        ) {
            Text(
                text = place.name.take(1),
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = Color.White,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.name,
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = TextBlack,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = buildString {
                        append(place.category)
                        if (distanceText.isNotEmpty()) append(" · $distanceText")
                    },
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    color = Brown700Mp,
                )
                if (place.ratingAvg != null) {
                    Text("·", fontFamily = PretendardFamily, fontSize = 14.sp, color = Brown700Mp)
                    Icon(
                        painter = painterResource(R.drawable.ic_star),
                        contentDescription = null,
                        tint = StarYellow,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = "%.1f".format(place.ratingAvg),
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        color = Brown700Mp,
                    )
                }
            }
        }
        IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_favorite),
                contentDescription = "즐겨찾기",
                tint = if (place.isFavorited) Pink500Mp else BrownBorderP,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ─── 검색 오버레이 (병화-4) ───────────────────────────────────────────────────

@Composable
private fun MapSearchOverlay(
    onDismiss: () -> Unit,
    onResultClick: (storeId: Int) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<StoreSearchResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
            return@LaunchedEffect
        }
        delay(300)
        isLoading = true
        results = runCatching {
            RetrofitClient.api.searchStores(query).body()?.results ?: emptyList()
        }.getOrDefault(emptyList())
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // 검색 입력창
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .clickable { onDismiss() },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "닫기",
                        tint = Brown700Mp,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .shadow(2.dp, RoundedCornerShape(50.dp))
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "매장 · 병원 · 공원 검색",
                            fontFamily = PretendardFamily,
                            fontSize = 15.sp,
                            color = Brown400Mp,
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = PretendardFamily,
                            fontSize = 15.sp,
                            color = TextBlack,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF3F4F6))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Orange500Mp, modifier = Modifier.size(32.dp))
                }
            } else if (results.isEmpty() && query.isNotBlank()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "검색 결과가 없습니다",
                        fontFamily = PretendardFamily,
                        fontSize = 15.sp,
                        color = Brown700Mp,
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(results) { result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { result.resolvedId?.let { onResultClick(it) } }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF3F4F6)),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_location_on),
                                    contentDescription = null,
                                    tint = Brown700Mp,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = result.name ?: "",
                                    fontFamily = PretendardFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 20.sp,
                                    color = TextBlack,
                                )
                                if (!result.address.isNullOrEmpty()) {
                                    Text(
                                        text = result.address,
                                        fontFamily = PretendardFamily,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = Brown700Mp,
                                    )
                                }
                            }
                            if (!result.category.isNullOrEmpty()) {
                                Text(
                                    text = result.category,
                                    fontFamily = PretendardFamily,
                                    fontSize = 12.sp,
                                    color = Brown400Mp,
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = Color(0xFFF3F4F6),
                        )
                    }
                }
            }
        }
    }
}
