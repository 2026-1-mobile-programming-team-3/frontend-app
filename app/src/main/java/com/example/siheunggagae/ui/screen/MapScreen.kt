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
import androidx.compose.ui.window.Dialog
import com.example.siheunggagae.data.local.SiheungRegions
import com.example.siheunggagae.data.location.EffectiveCenter
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.siheunggagae.map.MarkerSpec
import com.example.siheunggagae.map.computeMarkerSpecs
import com.example.siheunggagae.ui.component.EmptyStateView
import com.kakao.vectormap.KakaoMap
import com.example.siheunggagae.data.network.RetrofitClient
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.util.CategoryVisual
import com.example.siheunggagae.ui.util.appleSpec
import com.example.siheunggagae.ui.util.appleTapScale
import com.example.siheunggagae.ui.util.matchStatusToKorean
import com.example.siheunggagae.ui.util.rememberAppleInteractionSource
import com.example.siheunggagae.ui.util.rememberLocationPermissionState
import com.example.siheunggagae.ui.viewmodel.MapViewModel
import com.kakao.vectormap.MapView
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch

// ─── 상수 ────────────────────────────────────────────────────────────────────

private val MapSheetPeekHeight = 220.dp

// ─── 색상 ────────────────────────────────────────────────────────────────────

private val Brown900Mp   = Color(0xFF614B3A)
private val Brown700Mp   = Color(0xFF8A6E58)
private val Brown400Mp   = Color(0xFFC4A882)
private val BrownBorderP = Color(0xFFE8D3C2)
private val Orange500Mp  = Color(0xFFF7A35B)
private val Pink500Mp    = Color(0xFFF04268)
private val TextBlack    = Color(0xFF1E120A)
private val StarYellow   = Color(0xFFFDC700)

private val volunteerMarkerColor = 0xFF2196F3.toInt()

private fun storeCategoryToKorean(category: String?): String =
    StoreCategory.entries.find { it.apiValue == category }?.label ?: category ?: ""

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
    var showDongPicker by remember { mutableStateOf(false) }
    var mapReady by remember { mutableStateOf(false) }
    val lastMarkerTapMs = remember { mutableLongStateOf(0L) }

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

    // 내 위치 파란 점 — 권한 있고 ViewModel 이 위치 잡고 있을 때만.
    LaunchedEffect(mapReady, uiState.location, locationPermission.hasPermission) {
        val loc = uiState.location
        if (mapReady && loc != null && locationPermission.hasPermission) {
            mapWrapper.updateMyLocation(loc.latitude, loc.longitude)
        } else if (mapReady) {
            mapWrapper.removeMyLocation()
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

    // viewport 마커 동기화 — picture-on-update 가 아니라 spec diff 기반 sync
    LaunchedEffect(
        mapReady,
        uiState.viewportStores,
        uiState.visibleCategories,
        uiState.currentZoom,
        uiState.selectedStore?.resolvedId,
    ) {
        if (!mapReady) return@LaunchedEffect
        val projector = mapWrapper.screenProjector() ?: return@LaunchedEffect
        val filtered = uiState.viewportStores.filter { it.category in uiState.visibleCategories }
        val byId = filtered.associateBy { it.storeId }
        val specs = computeMarkerSpecs(filtered, projector, uiState.currentZoom, uiState.selectedStore?.resolvedId)
            .map { spec ->
                when (spec) {
                    is MarkerSpec.Single -> {
                        val storeIdInt = spec.id.removePrefix("store_").toIntOrNull()
                        spec.copy(onTap = {
                            val now = System.currentTimeMillis()
                            if (now - lastMarkerTapMs.longValue >= 400) {
                                lastMarkerTapMs.longValue = now
                                byId[storeIdInt]?.toStoreResponse()?.let { viewModel.selectStore(it) }
                            }
                        })
                    }
                    is MarkerSpec.Cluster -> spec.copy(onTap = {
                        val now = System.currentTimeMillis()
                        if (now - lastMarkerTapMs.longValue >= 400) {
                            lastMarkerTapMs.longValue = now
                            mapWrapper.fitMapPoints(spec.memberCoords, paddingPx = 120)
                        }
                    })
                }
            }
        mapWrapper.syncMarkers("store", specs)
    }

    // 봉사요청 마커 동기화 (같은 픽셀 거리 클러스터링 사용)
    LaunchedEffect(mapReady, uiState.isVolunteerMode, uiState.volunteerMarkers, uiState.currentZoom) {
        if (!mapReady) return@LaunchedEffect
        if (!uiState.isVolunteerMode) {
            mapWrapper.syncMarkers("vol", emptyList())
            return@LaunchedEffect
        }
        val projector = mapWrapper.screenProjector() ?: return@LaunchedEffect
        // 봉사요청을 StoreViewportItem 형태로 어댑팅 — 카테고리 "VOLUNTEER" 단일
        val asItems = uiState.volunteerMarkers.map { v ->
            StoreViewportItem(
                storeId = v.requestId,
                name = v.title ?: "",
                latitude = v.latitude,
                longitude = v.longitude,
                category = "VOLUNTEER",
            )
        }
        val specs = computeMarkerSpecs(asItems, projector, uiState.currentZoom, selectedId = null)
            .map { spec ->
                when (spec) {
                    is MarkerSpec.Single -> {
                        val volId = spec.id.removePrefix("store_").toIntOrNull() ?: return@map spec
                        spec.copy(
                            id = "vol_$volId",
                            color = volunteerMarkerColor,
                            onTap = { onNavigate(Screen.MatchingPublicDetail.createRoute(volId)) },
                        )
                    }
                    is MarkerSpec.Cluster -> spec.copy(
                        id = "volcluster_" + spec.id.removePrefix("cluster_"),
                        onTap = { mapWrapper.fitMapPoints(spec.memberCoords, paddingPx = 120) },
                    )
                }
            }
        mapWrapper.syncMarkers("vol", specs)
    }

    // skipHiddenState=true 로 시트가 완전히 숨겨지지 않게 — 사용자가 한 번 접으면 다시 펼 방법이 없는 버그 방지.
    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true,
        )
    )

    // 매장 선택 시 카메라 이동 + 시트는 peek 으로 — StoreDetailSheet 모달이 위에 뜨므로 시트를 hide 할 필요 없음.
    LaunchedEffect(uiState.selectedStore) {
        val store = uiState.selectedStore
        if (store != null && mapReady) {
            val z = mapWrapper.getCurrentZoom().coerceAtLeast(16)
            mapWrapper.animateCamera(store.latitude, store.longitude, z, durationMs = 350)
            sheetState.bottomSheetState.partialExpand()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
    ) { navPadding ->
        BottomSheetScaffold(
            modifier = Modifier.padding(navPadding),
            scaffoldState = sheetState,
            sheetPeekHeight = MapSheetPeekHeight,
            sheetContainerColor = Color.White,
            sheetTonalElevation = 0.dp,
            sheetShadowElevation = 8.dp,
            sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            sheetDragHandle = {
                MapDragHandle(
                    isExpanded = sheetState.bottomSheetState.currentValue == SheetValue.Expanded,
                    onCollapseClick = {
                        scope.launch { sheetState.bottomSheetState.partialExpand() }
                    },
                )
            },
            sheetContent = {
                if (uiState.isVolunteerMode) {
                    VolunteerBottomSheetContent(
                        volunteers = uiState.volunteerMarkers,
                        onItemClick = { vol ->
                            mapWrapper.moveCamera(vol.latitude, vol.longitude, 16)
                        },
                    )
                } else {
                    MapBottomSheetContent(
                        stores = uiState.stores,
                        totalCount = uiState.totalCount,
                        truncated = uiState.truncated,
                        onStoreClick = { store -> viewModel.selectStore(store) },
                        onFavoriteToggle = { store -> viewModel.toggleFavorite(store) },
                    )
                }
            },
            containerColor = Color.Transparent,
        ) { _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                // 실제 카카오 MapView
                AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

                // 상단 검색바 + 카테고리 칩 + 펫호텔 비교 배너
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                ) {
                    // GPS 가 시흥 밖일 때 fallback 안내 배너 — design.md §5 색 의미론: 위치=Mint
                    AnimatedVisibility(
                        visible = uiState.centerFallback != null,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    ) {
                        uiState.centerFallback?.let { cf ->
                            FallbackCenterBanner(
                                center = cf,
                                onChangeClick = { showDongPicker = true },
                                onDismiss = { viewModel.dismissFallbackBanner() },
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                    }
                    MapSearchCard(onClick = { showSearch = true })
                    Spacer(Modifier.height(8.dp))
                    MapCategoryChipRow(
                        selected = uiState.selectedCategory,
                        onSelect = { viewModel.selectCategory(it) },
                    )
                    // PET_HOTEL 단독 선택 시 비교 배너
                    val isPetHotelOnly = uiState.selectedCategory == StoreCategory.PET_HOTEL
                    val petHotelCount = if (isPetHotelOnly) {
                        uiState.viewportStores.count { it.category == "PET_HOTEL" }
                    } else 0
                    AnimatedVisibility(
                        visible = isPetHotelOnly && petHotelCount > 0,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    ) {
                        PetHotelCompareBanner(
                            count = petHotelCount,
                            onClick = {
                                val center = uiState.cameraTarget
                                val lat = center?.first ?: 37.3799
                                val lng = center?.second ?: 126.8030
                                // /maps/pet-hotels 는 원형 radius 만 지원하므로 viewport 대각선의 절반을
                                // radius 로 넘겨 일단 viewport 를 외접하도록 가져온 뒤, 비교 화면에서
                                // viewport bbox 로 다시 클리핑해 배너 카운트와 결과 개수를 일치시킨다.
                                val radius = radiusFromViewport(uiState.viewportBounds)
                                onNavigate(
                                    com.example.siheunggagae.Screen.PetHotelCompare.createRoute(
                                        lat = lat,
                                        lng = lng,
                                        radius = radius,
                                        viewportBounds = uiState.viewportBounds,
                                    ),
                                )
                            },
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }

                // 우측 플로팅 버튼 — peek 시트 위에 위치하도록 BottomEnd 기준. iOS 스타일 tap haptic.
                val haptic = LocalHapticFeedback.current
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = MapSheetPeekHeight + 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MapIconFab(R.drawable.ic_my_location, "내 위치") {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.moveToCurrentLocation()
                    }
                    MapIconFab(R.drawable.ic_layers, "레이어") {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showFilterSheet = true
                    }
                    MapIconFab(R.drawable.ic_refresh, "새로고침") {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.refresh()
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

    if (showDongPicker) {
        DongPickerDialog(
            currentDong = uiState.centerFallback?.regionDong,
            onDismiss = { showDongPicker = false },
            onSelect = { dong ->
                viewModel.moveToDong(dong)
                showDongPicker = false
            },
        )
    }
}

// ─── Fallback 배너 (GPS 가 시흥 밖일 때) ──────────────────────────────────────

@Composable
private fun FallbackCenterBanner(
    center: EffectiveCenter,
    onChangeClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val regionLabel = center.regionDong ?: "시흥시청"
    val sourceLabel = when (center.source) {
        EffectiveCenter.Source.USER_PROFILE -> "내 등록 동네"
        EffectiveCenter.Source.DEFAULT -> "시흥 기본 위치"
        EffectiveCenter.Source.GPS -> "현재 위치"
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEAFBF1),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location_on),
                contentDescription = null,
                tint = Color(0xFF00A63E),
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "현재 위치가 시흥 밖이라 ${regionLabel} 기준으로 보고 있어요",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack,
                    lineHeight = 16.sp,
                )
                Text(
                    text = sourceLabel,
                    fontFamily = PretendardFamily,
                    fontSize = 10.sp,
                    color = Brown700Mp,
                    lineHeight = 14.sp,
                )
            }
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White)
                    .clickable { onChangeClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "변경",
                    fontFamily = PretendardFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00A63E),
                )
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Brown400Mp,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

// ─── 동네 선택 다이얼로그 ──────────────────────────────────────────────────────

@Composable
private fun DongPickerDialog(
    currentDong: String?,
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
                color = TextBlack,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            HorizontalDivider(color = Color(0xFFF3F4F6))
            LazyColumn(modifier = Modifier.height(360.dp)) {
                items(SiheungRegions.dongCoordinates.keys.toList()) { dong ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(dong) }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            dong,
                            fontFamily = PretendardFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp,
                            color = TextBlack,
                        )
                        if (dong == currentDong) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = Pink500Mp,
                                modifier = Modifier.size(20.dp),
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
            tint = Brown700Mp,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = "매장 · 병원 · 공원 검색",
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = Brown700Mp,
        )
    }
}

// ─── 펫호텔 비교 배너 ─────────────────────────────────────────────────────────

/**
 * viewport bbox [swLat, swLng, neLat, neLng] 의 대각선 절반(meter) 을 radius 로 추정.
 * 1km ~ 50km 로 clamp. bbox 가 null 이면 5km 디폴트.
 */
private fun radiusFromViewport(bbox: DoubleArray?): Int {
    if (bbox == null || bbox.size < 4) return 5000
    val swLat = bbox[0]
    val swLng = bbox[1]
    val neLat = bbox[2]
    val neLng = bbox[3]
    val avgLat = (swLat + neLat) / 2.0
    val latM = (neLat - swLat) * 111_000.0
    val lngM = (neLng - swLng) * 111_000.0 * kotlin.math.cos(Math.toRadians(avgLat))
    val diagonal = kotlin.math.sqrt(latM * latM + lngM * lngM)
    return (diagonal / 2.0).toInt().coerceIn(1000, 50000)
}

@Composable
private fun PetHotelCompareBanner(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "주변 펫호텔 ${count}곳",
                    fontFamily = PretendardFamily,
                    color = Color(0xFF1E120A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "가격순 보기",
                    fontFamily = PretendardFamily,
                    color = Color(0xFF8A6E58),
                    fontSize = 10.sp,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF614B3A))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Text(
                    text = "가격 비교 →",
                    fontFamily = PretendardFamily,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
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
            // bg/fg 색을 AppleEaseOut 으로 보간 — 선택 시 hard cut 대신 부드러운 morph.
            val bg by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF1A1A1A) else Color.White,
                animationSpec = appleSpec(),
                label = "mapChipBg",
            )
            val fg by animateColorAsState(
                targetValue = if (isSelected) Color.White else Brown700Mp,
                animationSpec = appleSpec(),
                label = "mapChipFg",
            )
            val interaction = rememberAppleInteractionSource()
            Box(
                modifier = Modifier
                    .appleTapScale(interaction)
                    .shadow(2.dp, RoundedCornerShape(50.dp))
                    .clip(RoundedCornerShape(50.dp))
                    .background(bg)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                    ) { onSelect(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = category.label,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    color = fg,
                )
            }
        }
    }
}

// ─── 플로팅 버튼 ──────────────────────────────────────────────────────────────

@Composable
private fun MapIconFab(iconRes: Int, contentDescription: String, onClick: () -> Unit = {}) {
    val interaction = rememberAppleInteractionSource()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .appleTapScale(interaction)
            .size(40.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(interactionSource = interaction, indication = null) { onClick() },
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
private fun MapDragHandle(
    isExpanded: Boolean = false,
    onCollapseClick: (() -> Unit)? = null,
) {
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
        if (isExpanded && onCollapseClick != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F4F6))
                    .clickable { onCollapseClick() },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_down),
                    contentDescription = "접기",
                    tint = Brown700Mp,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ─── 하단 시트 콘텐츠 ─────────────────────────────────────────────────────────

@Composable
private fun MapBottomSheetContent(
    stores: List<StoreResponse>,
    totalCount: Int,
    truncated: Boolean,
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "주변 매장 ${displayCount}개",
                        fontFamily = PretendardFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        color = TextBlack,
                    )
                    // 표시 한도 초과 시 영구 배지 — 스낵바 4 초 안내 대체.
                    if (truncated) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = "더 확대해 주세요",
                                fontFamily = PretendardFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFCA8A04),
                            )
                        }
                    }
                }
                // 정렬: API 가 거리순으로 반환하므로 정적 라벨. (추후 정렬 옵션 추가 시 클릭 가능 칩으로 전환)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_swap_vert),
                        contentDescription = null,
                        tint = Brown700Mp,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "가까운 순",
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 17.sp,
                        color = Brown700Mp,
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFF3F4F6))
            if (stores.isEmpty()) {
                EmptyStateView(
                    title = "이 영역에 매장이 없어요",
                    subtitle = "지도를 옮기거나 확대해 보세요",
                    iconRes = R.drawable.ic_map,
                )
            } else {
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
}

// ─── 봉사활동 바텀시트 ────────────────────────────────────────────────────────

@Composable
private fun VolunteerBottomSheetContent(
    volunteers: List<VolunteerMarkerDto>,
    onItemClick: (VolunteerMarkerDto) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "봉사활동 요청 ${volunteers.size}건",
                fontFamily = PretendardFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = TextBlack,
            )
        }
        HorizontalDivider(color = Color(0xFFF3F4F6))
        if (volunteers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "주변 봉사 요청이 없습니다",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    color = Brown700Mp,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(volunteers, key = { it.requestId }) { vol ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(vol) }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F4FD)),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_handshake),
                                contentDescription = null,
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = vol.title ?: "봉사 요청",
                                fontFamily = PretendardFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 22.sp,
                                color = TextBlack,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                            Text(
                                text = vol.status?.let { matchStatusToKorean(it) } ?: "",
                                fontFamily = PretendardFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Brown700Mp,
                            )
                        }
                        Icon(
                            painter = painterResource(R.drawable.ic_location_on),
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(16.dp),
                        )
                    }
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
        val visual = CategoryVisual.forCategory(store.category)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Brush.linearGradient(visual.gradient)),
            contentAlignment = Alignment.Center,
        ) {
            // 워터마크 (반투명 큰 아이콘)
            Icon(
                painter = painterResource(visual.drawableRes),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.18f),
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 8.dp),
            )
            // 전경 카테고리 아이콘 (선명)
            Icon(
                painter = painterResource(visual.drawableRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(56.dp),
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
                    Text(storeCategoryToKorean(store.category), fontFamily = PretendardFamily, fontSize = 13.sp, color = Brown700Mp)
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
                        append(storeCategoryToKorean(place.category))
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
        val haptic = LocalHapticFeedback.current
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onFavoriteToggle()
            },
            modifier = Modifier.size(36.dp),
        ) {
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
                    items(results, key = { it.resolvedId ?: it.hashCode() }) { result ->
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
                                    text = storeCategoryToKorean(result.category),
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
