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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
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
import android.content.Intent
import android.net.Uri
import com.example.siheunggagae.data.model.StoreCategory
import com.example.siheunggagae.data.model.StoreDetailResponse
import com.example.siheunggagae.data.model.StoreResponse
import com.example.siheunggagae.data.model.StoreSearchResult
import com.example.siheunggagae.data.model.StoreViewportItem
import com.example.siheunggagae.data.model.UserRole
import com.example.siheunggagae.data.model.VolunteerMarkerDto
import com.example.siheunggagae.data.model.toStoreResponse
import com.kakao.vectormap.KakaoMap
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
    focusLat: Double = 0.0,
    focusLng: Double = 0.0,
    focusStoreId: Int = 0,
) {
    val context = LocalContext.current
    val app = context.applicationContext as SiheungGagaeApp
    val viewModel: MapViewModel = viewModel(
        factory = MapViewModel.Factory(
            api = RetrofitClient.api,
            locationProvider = LocationProvider(context),
            filterStore = MapFilterStore(context),
            initialVolunteerMode = startVolunteerMode,
            focusLat = focusLat,
            focusLng = focusLng,
            focusStoreId = focusStoreId,
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val locationPermission = rememberLocationPermissionState { granted ->
        if (granted) viewModel.moveToCurrentLocation()
    }

    val snackbarHostState = remember { SnackbarHostState() }
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

    // 지도 초기화 + camera idle 리스너 등록
    LaunchedEffect(Unit) {
        mapWrapper.onMapDestroyed = { mapReady = false }
        mapWrapper.init { kakaoMap: KakaoMap ->
            kakaoMap.setOnCameraMoveEndListener { map, position, _ ->
                val viewport = map.getViewport()
                val sw = map.fromScreenPoint(viewport.left, viewport.bottom)
                val ne = map.fromScreenPoint(viewport.right, viewport.top)
                if (sw != null && ne != null) {
                    viewModel.onViewportChange(
                        sw.latitude, sw.longitude,
                        ne.latitude, ne.longitude,
                        position.zoomLevel,
                    )
                }
            }
            mapReady = true
        }
        if (!locationPermission.hasPermission) locationPermission.request()
    }

    // 세션 무효화(PlaceDetailScreen 등 다른 KakaoMap 인스턴스 초기화 시) 후 재초기화
    LaunchedEffect(mapReady) {
        if (!mapReady && mapWrapper.hasBeenInitialized) {
            delay(100)
            mapWrapper.reinit()
        }
    }

    // truncated 시 스낵바 안내
    LaunchedEffect(uiState.truncated) {
        if (uiState.truncated) {
            snackbarHostState.showSnackbar("더 확대해 주세요 — 표시 가능한 매장 수를 초과했습니다.")
        }
    }

    // cameraSerial이 바뀔 때마다 cameraTarget으로 이동 (초기 로드 / 내 위치 버튼 / 포커스 좌표 모두 처리)
    // 프로그래매틱 moveCamera 는 OnCameraMoveEndListener 를 발화하지 않을 수 있으므로
    // 애니메이션 완료 후 직접 viewport 로드를 트리거한다.
    LaunchedEffect(mapReady, uiState.cameraSerial) {
        if (!mapReady) return@LaunchedEffect
        val (lat, lng) = uiState.cameraTarget ?: return@LaunchedEffect
        val zoomLevel = if (focusLat != 0.0 && focusLng != 0.0 && uiState.cameraSerial == 1) 17 else 15
        mapWrapper.moveCamera(lat, lng, zoomLevel)
        // 카메라 애니메이션 완료 대기 후 초기 viewport 로드
        delay(500)
        val bounds = mapWrapper.getVisibleBounds() ?: return@LaunchedEffect
        viewModel.onViewportChange(
            bounds.first.latitude, bounds.first.longitude,
            bounds.second.latitude, bounds.second.longitude,
            mapWrapper.getCurrentZoom(),
        )
    }

    // viewport 마커 동기화 — zoom 11+ 에서 bbox 기반 데이터, 줌에 따라 클러스터링
    LaunchedEffect(mapReady, uiState.viewportStores, uiState.visibleCategories, uiState.currentZoom) {
        if (!mapReady) return@LaunchedEffect
        mapWrapper.clearMarkersWithPrefix("store_")
        mapWrapper.clearMarkersWithPrefix("cluster_")
        val filtered = uiState.viewportStores.filter { it.category in uiState.visibleCategories }
        computeViewportMarkers(filtered, uiState.currentZoom).forEach { marker ->
            if (marker.singleStore != null) {
                val color = categoryColors[marker.singleStore.category] ?: defaultMarkerColor
                mapWrapper.addMarker(
                    id = marker.id,
                    lat = marker.lat,
                    lng = marker.lng,
                    markerColor = color,
                    onTap = { viewModel.selectStore(marker.singleStore.toStoreResponse()) },
                )
            } else {
                mapWrapper.addClusterMarker(
                    id = marker.id,
                    lat = marker.lat,
                    lng = marker.lng,
                    count = marker.count,
                )
            }
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
            skipHiddenState = false,
        )
    )

    // 매장 선택 시 카메라 이동 + 목록 시트 숨기기 / 해제 시 다시 표시
    LaunchedEffect(uiState.selectedStore) {
        val store = uiState.selectedStore
        if (store != null) {
            if (mapReady) mapWrapper.moveCamera(store.latitude, store.longitude)
            sheetState.bottomSheetState.hide()
        } else {
            if (sheetState.bottomSheetState.currentValue == SheetValue.Hidden) {
                sheetState.bottomSheetState.partialExpand()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                MapBottomSheetContent(
                    stores = uiState.stores,
                    totalCount = uiState.totalCount,
                    onStoreClick = { store -> viewModel.selectStore(store) },
                    onFavoriteToggle = { store -> viewModel.toggleFavorite(store) },
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

    // 매장 선택 시 상세 모달
    val selectedStore = uiState.selectedStore
    if (selectedStore != null) {
        StoreDetailSheet(
            store = selectedStore,
            detail = uiState.selectedStoreDetail,
            isDetailLoading = uiState.isDetailLoading,
            onDismiss = { viewModel.selectStore(null) },
            onFavoriteToggle = { viewModel.toggleFavorite(selectedStore) },
            onNavigateToDetail = {
                viewModel.selectStore(null)
                onNavigate(Screen.PlaceDetail.createRoute(selectedStore.resolvedId, selectedStore.latitude, selectedStore.longitude))
            },
        )
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
    stores: List<StoreResponse>,
    totalCount: Int,
    onStoreClick: (StoreResponse) -> Unit,
    onFavoriteToggle: (StoreResponse) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        run {
            val displayCount = if (totalCount > 0) totalCount else stores.size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "주변 매장 ${displayCount}개",
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
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(stores, key = { it.resolvedId }) { store ->
                    MapPlaceItem(
                        place = store,
                        onClick = { onStoreClick(store) },
                        onFavoriteToggle = { onFavoriteToggle(store) },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = Color(0xFFF3F4F6),
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ─── 매장 상세 모달 ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreDetailSheet(
    store: StoreResponse,
    detail: StoreDetailResponse?,
    isDetailLoading: Boolean,
    onDismiss: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onNavigateToDetail: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        scrimColor = Color.Transparent,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = { MapDragHandle() },
    ) {
        // 그라디언트 배너
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(Brush.linearGradient(listOf(Color(0xFFD0FEE1), Color(0xFFE0F7FA)))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location_on),
                contentDescription = null,
                tint = Pink500Mp,
                modifier = Modifier.size(36.dp),
            )
        }

        // 이름 + 즐겨찾기
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = store.name,
                    fontFamily = PretendardFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = TextBlack,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(store.category, fontFamily = PretendardFamily, fontSize = 13.sp, color = Brown700Mp)
                    store.distanceM?.let { d ->
                        Text("·", fontFamily = PretendardFamily, fontSize = 13.sp, color = Brown700Mp)
                        val distText = if (d < 1000) "${"%.0f".format(d)}m" else "${"%.1f".format(d / 1000)}km"
                        Text(distText, fontFamily = PretendardFamily, fontSize = 13.sp, color = Brown700Mp)
                    }
                    store.ratingAvg?.let { r ->
                        Text("·", fontFamily = PretendardFamily, fontSize = 13.sp, color = Brown700Mp)
                        Icon(painterResource(R.drawable.ic_star), null, tint = StarYellow, modifier = Modifier.size(12.dp))
                        Text("%.1f".format(r), fontFamily = PretendardFamily, fontSize = 13.sp, color = Brown700Mp)
                    }
                }
            }
            IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_favorite),
                    contentDescription = "즐겨찾기",
                    tint = if (store.isFavorited) Pink500Mp else Brown400Mp,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        HorizontalDivider(color = Color(0xFFF3F4F6))

        // 상세 정보 (로딩 or 실제 내용)
        if (isDetailLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(72.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Orange500Mp, modifier = Modifier.size(24.dp))
            }
        } else if (detail != null) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                detail.address?.let { addr ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(painterResource(R.drawable.ic_location_on), null, tint = Brown700Mp, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                        Text(addr, fontFamily = PretendardFamily, fontSize = 14.sp, lineHeight = 20.sp, color = TextBlack)
                    }
                }
                detail.operatingHours?.let { hours ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(painterResource(R.drawable.ic_schedule), null, tint = Brown700Mp, modifier = Modifier.size(16.dp))
                        Text(hours, fontFamily = PretendardFamily, fontSize = 14.sp, lineHeight = 20.sp, color = TextBlack)
                    }
                }
                detail.phone?.let { phone ->
                    Row(
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                        },
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(painterResource(R.drawable.ic_call), null, tint = Brown700Mp, modifier = Modifier.size(16.dp))
                        Text(phone, fontFamily = PretendardFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF388AF5))
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFF3F4F6))
        }

        // 상세 페이지 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brown900Mp)
                .clickable { onNavigateToDetail() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "상세 정보 보기",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp,
                color = Color.White,
            )
        }
    }
}

// ─── 매장 카드 아이템 ─────────────────────────────────────────────────────────

@Composable
private fun MapPlaceItem(
    place: StoreResponse,
    onClick: () -> Unit = {},
    onFavoriteToggle: () -> Unit = {},
) {
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
        IconButton(onClick = { onFavoriteToggle() }, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_favorite),
                contentDescription = "즐겨찾기",
                tint = if (place.isFavorited) Pink500Mp else BrownBorderP,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ─── Viewport 마커 클러스터링 ──────────────────────────────────────────────────

private data class ViewportMarker(
    val id: String,
    val lat: Double,
    val lng: Double,
    val count: Int,
    val singleStore: StoreViewportItem?,  // null = 클러스터
)

/**
 * zoom >= 14: 개별 마커
 * zoom 11~13: 소수점 자리 수 기반 그리드 클러스터링
 *   zoom 11~12 → 1자리(≈11km 격자), zoom 13 → 2자리(≈1km 격자)
 */
private fun computeViewportMarkers(stores: List<StoreViewportItem>, zoom: Int): List<ViewportMarker> {
    if (zoom >= 14) {
        return stores.map { ViewportMarker("store_${it.storeId}", it.latitude, it.longitude, 1, it) }
    }
    val decimals = if (zoom <= 12) 1 else 2
    return stores
        .groupBy { "${"%.${decimals}f".format(it.latitude)},${"%.${decimals}f".format(it.longitude)}" }
        .map { (key, group) ->
            val centerLat = group.sumOf { it.latitude } / group.size
            val centerLng = group.sumOf { it.longitude } / group.size
            ViewportMarker(
                id = "cluster_$key",
                lat = centerLat,
                lng = centerLng,
                count = group.size,
                singleStore = if (group.size == 1) group.first() else null,
            )
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
