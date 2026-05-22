package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.MapViewWrapper
import com.example.siheunggagae.R

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import java.util.Calendar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.siheunggagae.data.local.FavoritesCache
import com.example.siheunggagae.data.model.FavoriteStoreCreateRequest
import com.example.siheunggagae.data.model.StoreDetailResponse
import com.example.siheunggagae.data.model.StoreReview
import com.example.siheunggagae.data.model.StoreReviewCreateRequest
import com.example.siheunggagae.data.network.RetrofitClient
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import com.kakao.vectormap.MapView
import kotlinx.coroutines.launch

// ─── Colors ────────────────────────────────────────────────────────────────────
private val TextBlackPL  = Color(0xFF1F130B)
private val Brown900PL   = Color(0xFF614B3A)
private val Brown700PL   = Color(0xFF8B6F59)
private val Brown400PL   = Color(0xFFC4A882)
private val BrownBorderPL = Color(0xFFE8D3C2)
private val Orange500PL  = Color(0xFFF7A45C)
private val Pink500PL    = Color(0xFFF14369)
private val Green500PL   = Color(0xFF00A63E)
private val GreenBgPL    = Color(0xFFDCFCE7)
private val StarYellowPL = Color(0xFFFDC700)
private val Gray100PL    = Color(0xFFF3F3F3)
private val BgPL         = Color(0xFFFEFEFE)
private val DividerPL    = Color(0xFFF3F4F6)
private val MapMintPL    = Color(0xFFD0FEE1)
private val MapSkyPL     = Color(0xFFE0F7FA)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    placeId: Int = 0,
    initialLat: Double = 0.0,
    initialLng: Double = 0.0,
    onBack: () -> Unit = {},
    onNavigateToMap: (lat: Double, lng: Double, storeId: Int) -> Unit = { _, _, _ -> },
    onEditRequestClick: (storeId: Int) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var store by remember { mutableStateOf<StoreDetailResponse?>(null) }
    var reviews by remember { mutableStateOf<List<StoreReview>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isFavorited by remember { mutableStateOf(false) }
    var showReviewSheet by remember { mutableStateOf(false) }
    var myNickname by remember { mutableStateOf<String?>(null) }
    var myUserId by remember { mutableStateOf<Int?>(null) }
    var displayCount by remember { mutableIntStateOf(3) }

    val mapView = remember { MapView(context) }
    val mapWrapper = remember { MapViewWrapper(mapView) }
    var mapReady by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        mapWrapper.init { mapReady = true }
    }

    LaunchedEffect(mapReady, store) {
        if (!mapReady || store == null) return@LaunchedEffect
        val lat = store?.latitude?.takeIf { it != 0.0 } ?: initialLat.takeIf { it != 0.0 } ?: return@LaunchedEffect
        val lng = store?.longitude?.takeIf { it != 0.0 } ?: initialLng.takeIf { it != 0.0 } ?: return@LaunchedEffect
        mapWrapper.addMarker(
            id = "detail_pin",
            lat = lat,
            lng = lng,
            markerColor = AndroidColor.parseColor("#F14369"),
        )
        mapWrapper.moveCamera(lat, lng, 16)
    }

    LaunchedEffect(placeId) {
        val me = runCatching { RetrofitClient.api.getMe().body() }.getOrNull()
        myNickname = me?.nickname
        myUserId = me?.id
        if (placeId > 0) {
            store = runCatching { RetrofitClient.api.getStoreDetail(placeId).body() }.getOrNull()
            reviews = runCatching {
                RetrofitClient.api.getStoreReviews(placeId).body()?.reviews ?: emptyList()
            }.getOrDefault(emptyList())
            // FavoritesCache 공유 상태로 즐겨찾기 여부 확인
            if (!FavoritesCache.isLoaded) {
                val items = runCatching {
                    RetrofitClient.api.getFavoriteStores().body()?.items
                }.getOrNull()
                val ids = items?.mapNotNull { it.storeId }?.toSet() ?: emptySet()
                FavoritesCache.init(ids)
            }
            isFavorited = FavoritesCache.contains(placeId)
        }
        isLoading = false
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("", text))
    }

    fun dialPhone(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    }

    fun shareStore() {
        val text = buildString {
            store?.name?.let { append(it).append("\n") }
            store?.address?.let { append(it) }
        }
        if (text.isNotBlank()) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, "공유하기"))
        }
    }

    fun toggleFavorite() {
        val newFavorited = !isFavorited
        isFavorited = newFavorited
        if (newFavorited) FavoritesCache.add(placeId) else FavoritesCache.remove(placeId)
        scope.launch {
            val ok = runCatching {
                val resp = if (newFavorited) RetrofitClient.api.addFavoriteStore(FavoriteStoreCreateRequest(placeId))
                           else RetrofitClient.api.deleteFavoriteStore(placeId)
                resp.isSuccessful
                    || (newFavorited && resp.code() == 409)
                    || (!newFavorited && resp.code() == 404)
            }.getOrDefault(false)
            if (!ok) {
                isFavorited = !newFavorited
                if (!newFavorited) FavoritesCache.add(placeId) else FavoritesCache.remove(placeId)
            }
        }
    }

    Scaffold(containerColor = BgPL) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Orange500PL)
            }
        } else if (store == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "정보를 불러올 수 없습니다", fontFamily = PretendardFamily, fontSize = 15.sp, color = Brown700PL)
            }
        } else {
            val s = store!!
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    bottom = innerPadding.calculateBottomPadding() + 16.dp,
                ),
            ) {
                // 상단 배너 — 상태바까지 연장된 그라디언트
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(MapMintPL, Color(0xFFB2DFBF), Color(0xFFD8F2DC)))),
                    ) {
                        Spacer(Modifier.statusBarsPadding())
                        Box(
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_location_on),
                                contentDescription = null,
                                tint = Pink500PL,
                                modifier = Modifier.size(48.dp),
                            )
                        }
                    }
                }

                // 장소 기본 정보
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = s.name ?: "",
                            fontFamily = PretendardFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 36.sp,
                            color = TextBlackPL,
                        )
                        if (s.ratingAvg != null) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(imageVector = Icons.Filled.Star, null, tint = StarYellowPL, modifier = Modifier.size(20.dp))
                                Text(
                                    text = "%.1f".format(s.ratingAvg),
                                    fontFamily = PretendardFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 24.sp,
                                    color = TextBlackPL,
                                )
                                Text(
                                    text = "(후기 ${reviews.size})",
                                    fontFamily = PretendardFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 24.sp,
                                    color = Brown700PL,
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }

                // 상세 정보
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        if (!s.operatingHours.isNullOrEmpty()) {
                            PlaceInfoRowPL(
                                iconRes = R.drawable.ic_schedule,
                                label = "영업 시간",
                                content = s.operatingHours,
                            ) {
                                val isOpen = isOpenNow(s.operatingHours)
                                if (isOpen != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(if (isOpen) GreenBgPL else Color(0xFFFFE4E6))
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                    ) {
                                        Text(
                                            text = if (isOpen) "영업 중" else "영업 마감",
                                            fontFamily = PretendardFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isOpen) Green500PL else Pink500PL,
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = DividerPL, thickness = 1.dp)
                        }
                        if (!s.address.isNullOrEmpty()) {
                            PlaceInfoRowPL(
                                iconRes = R.drawable.ic_location_on,
                                label = "주소",
                                content = s.address,
                            ) {
                                CopyButtonPL { copyToClipboard(s.address) }
                            }
                            HorizontalDivider(color = DividerPL, thickness = 1.dp)
                        }
                        if (!s.phone.isNullOrEmpty()) {
                            PlaceInfoRowPL(
                                iconRes = R.drawable.ic_call,
                                label = "전화",
                                content = s.phone,
                            ) {
                                CopyButtonPL { copyToClipboard(s.phone) }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, BrownBorderPL, RoundedCornerShape(12.dp))
                                    .clickable { dialPhone(s.phone) }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_call),
                                    contentDescription = null,
                                    tint = Brown700PL,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "전화하기",
                                    fontFamily = PretendardFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Brown700PL,
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }

                // 위치 카드
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "위치",
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp,
                            color = Brown700PL,
                        )
                        val mapLat = s.latitude?.takeIf { it != 0.0 } ?: initialLat.takeIf { it != 0.0 }
                        val mapLng = s.longitude?.takeIf { it != 0.0 } ?: initialLng.takeIf { it != 0.0 }
                        val hasLocation = mapLat != null && mapLng != null
                        if (hasLocation) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(24.dp)),
                            ) {
                                AndroidView(
                                    factory = { mapView },
                                    modifier = Modifier.fillMaxSize(),
                                )
                                // 모든 터치 소비 → 지도 조작 막고 탭만 화면 전환
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            onNavigateToMap(mapLat!!, mapLng!!, placeId)
                                        },
                                )
                                if (!s.address.isNullOrEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp)
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(Color.White)
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(painter = painterResource(R.drawable.ic_location_on), null, tint = Orange500PL, modifier = Modifier.size(12.dp))
                                        Text(
                                            text = s.address.take(10),
                                            fontFamily = PretendardFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 16.sp,
                                            color = TextBlackPL,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Brush.linearGradient(listOf(MapSkyPL, MapMintPL))),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("위치 정보 없음", fontFamily = PretendardFamily, fontSize = 13.sp, color = Brown700PL)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }

                // 정보 수정 요청 / 클레임 버튼
                if (myUserId != null && (s.isOwner || s.ownerUserId == null || s.ownerUserId != myUserId)) {
                    item {
                        val enabled = s.isOwner || s.ownerUserId == null
                        val label = when {
                            s.isOwner -> "내 매장 정보 수정"
                            s.ownerUserId == null -> "이 매장 클레임하기"
                            else -> "본인 명의 매장만 수정 요청 가능"
                        }
                        OutlinedButton(
                            onClick = { if (enabled) s.storeId?.let { onEditRequestClick(it) } },
                            enabled = enabled,
                            border = BorderStroke(1.dp, Brown900PL),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 8.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_pencil),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (enabled) Brown900PL else Brown700PL,
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontFamily = PretendardFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (enabled) Brown900PL else Brown700PL,
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }

                // 리뷰 헤더
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "리뷰",
                                fontFamily = PretendardFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 16.sp,
                                color = Brown700PL,
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Pink500PL)
                                    .clickable { showReviewSheet = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = "리뷰 쓰기",
                                    fontFamily = PretendardFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                )
                            }
                        }
                        if (s.ratingAvg != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "%.1f".format(s.ratingAvg),
                                    fontFamily = PretendardFamily,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 36.sp,
                                    color = TextBlackPL,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    val filled = s.ratingAvg?.let { Math.round(it).toInt() } ?: 0
                                    repeat(5) { i ->
                                        Icon(imageVector = Icons.Filled.Star, null, tint = if (i < filled) StarYellowPL else Gray100PL, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Text(
                                    text = "총 ${reviews.size}개의 리뷰",
                                    fontFamily = PretendardFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 16.sp,
                                    color = Brown700PL,
                                )
                            }
                        }
                    }
                }

                // 리뷰 목록 (displayCount만큼만 표시)
                items(reviews.take(displayCount)) { review ->
                    val uid = myUserId
                    val nick = myNickname
                    val isMyReview = when {
                        uid != null && review.userId != null -> review.userId == uid
                        nick != null && review.nickname != null -> review.nickname.trim() == nick.trim()
                        else -> false
                    }
                    PlaceReviewCardFromApi(
                        review = review,
                        isMyReview = isMyReview,
                        onDelete = {
                            val rid = review.reviewId ?: return@PlaceReviewCardFromApi
                            scope.launch {
                                val deleted = runCatching {
                                    RetrofitClient.api.deleteStoreReview(placeId, rid)
                                }.getOrNull()?.let { it.isSuccessful || it.code() == 404 } ?: false
                                if (deleted) {
                                    reviews = reviews.filter { it.reviewId != rid }
                                    runCatching { RetrofitClient.api.getStoreDetail(placeId).body() }
                                        .getOrNull()?.let { store = it }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
                    )
                }

                // 더 보기 버튼
                if (reviews.size > displayCount) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .border(1.dp, BrownBorderPL, RoundedCornerShape(50.dp))
                                .clickable { displayCount += 5 }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "리뷰 더 보기 (${reviews.size - displayCount}개 남음)",
                                fontFamily = PretendardFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Brown700PL,
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(60.dp)) }
            }
        }

        // 투명 TopBar — 그라디언트 위에 떠있음
        PlaceDetailTopBar(
            onBack = onBack,
            isFavorited = isFavorited,
            onFavoriteToggle = { toggleFavorite() },
            onShare = { shareStore() },
        )
        } // Box
    } // Scaffold

    if (showReviewSheet) {
        ReviewWriteSheet(
            storeId = placeId,
            onDismiss = { showReviewSheet = false },
            onSubmitted = {
                showReviewSheet = false
                scope.launch {
                    runCatching { RetrofitClient.api.getStoreReviews(placeId).body()?.reviews }
                        .getOrNull()?.let { reviews = it }
                    runCatching { RetrofitClient.api.getStoreDetail(placeId).body() }
                        .getOrNull()?.let { store = it }
                }
            },
        )
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun PlaceDetailTopBar(
    onBack: () -> Unit,
    isFavorited: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onShare: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .shadow(1.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .clickable { onBack() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "뒤로",
                tint = TextBlackPL,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .shadow(1.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .clickable { onFavoriteToggle() },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_favorite),
                    contentDescription = "즐겨찾기",
                    tint = if (isFavorited) Pink500PL else BrownBorderPL,
                    modifier = Modifier.size(16.dp),
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .shadow(1.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .clickable { onShare() },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_share),
                    contentDescription = "공유",
                    tint = TextBlackPL,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ─── 정보 Row ──────────────────────────────────────────────────────────────────

@Composable
private fun PlaceInfoRowPL(
    iconRes: Int,
    label: String,
    content: String,
    action: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, BrownBorderPL, RoundedCornerShape(10.dp)),
        ) {
            Icon(painter = painterResource(iconRes), contentDescription = null, tint = Brown700PL, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = Brown700PL,
            )
            Text(
                text = content,
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = TextBlackPL,
            )
        }
        action()
    }
}

// ─── 복사 버튼 ─────────────────────────────────────────────────────────────────

@Composable
private fun CopyButtonPL(onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Gray100PL)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = "복사",
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp,
            color = Brown700PL,
        )
    }
}

// ─── 리뷰 카드 (API) ───────────────────────────────────────────────────────────

private val avatarColors = listOf(
    Color(0xFFF04268), Color(0xFF7C3AED), Color(0xFFF7A45C),
    Color(0xFF00A63E), Color(0xFF388AF5),
)

@Composable
private fun PlaceReviewCardFromApi(
    review: StoreReview,
    isMyReview: Boolean = false,
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val avatarBg = avatarColors[(review.nickname?.length ?: 0) % avatarColors.size].copy(alpha = 0.15f)
    val avatarFg = avatarColors[(review.nickname?.length ?: 0) % avatarColors.size]
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, DividerPL, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarBg),
            ) {
                Text(
                    text = review.nickname?.firstOrNull()?.toString() ?: "?",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = avatarFg,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.nickname ?: "익명",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 20.sp,
                    color = TextBlackPL,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val rating = review.rating ?: 0
                    repeat(5) { i ->
                        Icon(imageVector = Icons.Filled.Star, null, tint = if (i < rating) StarYellowPL else Gray100PL, modifier = Modifier.size(12.dp))
                    }
                }
            }
            Text(
                text = review.createdAt?.take(10) ?: "",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = Brown400PL,
            )
            if (isMyReview) {
                Box {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "더보기",
                        tint = Brown400PL,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { showMenu = true },
                    )
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "삭제",
                                    fontFamily = PretendardFamily,
                                    fontSize = 14.sp,
                                    color = Pink500PL,
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                        )
                    }
                }
            }
        }
        if (!review.content.isNullOrEmpty()) {
            Text(
                text = review.content,
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = Brown700PL,
            )
        }
        if (review.isPetAllowed != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (review.isPetAllowed) GreenBgPL else Gray100PL)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = if (review.isPetAllowed) "반려동물 동반 가능" else "반려동물 동반 불가",
                    fontFamily = PretendardFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (review.isPetAllowed) Green500PL else Brown700PL,
                )
            }
        }
    }
}

// ─── 리뷰 작성 시트 ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewWriteSheet(
    storeId: Int,
    onDismiss: () -> Unit,
    onSubmitted: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var rating by remember { mutableIntStateOf(0) }
    var isPetAllowed by remember { mutableStateOf(true) }
    var content by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "리뷰 쓰기",
                fontFamily = PretendardFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlackPL,
            )

            // 별점
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "별점", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Brown700PL)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < rating) StarYellowPL else Gray100PL,
                            modifier = Modifier.size(32.dp).clickable { rating = index + 1 },
                        )
                    }
                }
            }

            // 반려동물 동반
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "반려동물 동반", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Brown700PL)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(true to "가능", false to "불가").forEach { (value, label) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isPetAllowed == value) Color(0xFF1A1A1A) else Gray100PL)
                                .clickable { isPetAllowed = value }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = label,
                                fontFamily = PretendardFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isPetAllowed == value) Color.White else Brown700PL,
                            )
                        }
                    }
                }
            }

            // 리뷰 내용
            OutlinedTextField(
                value = content,
                onValueChange = { if (it.length <= 300) content = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("리뷰 내용을 입력하세요.", fontFamily = PretendardFamily, fontSize = 14.sp, color = Color(0xFFC1AEA0)) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BrownBorderPL,
                    focusedBorderColor = Orange500PL,
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                maxLines = 5,
            )
            Text(
                text = "${content.length}/300",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                color = Brown400PL,
                modifier = Modifier.align(Alignment.End),
            )

            // 등록 버튼
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (rating > 0 && content.isNotBlank() && !isSubmitting) Pink500PL else Gray100PL)
                    .clickable(enabled = rating > 0 && content.isNotBlank() && !isSubmitting) {
                        isSubmitting = true
                        scope.launch {
                            val result = runCatching {
                                RetrofitClient.api.createStoreReview(
                                    storeId,
                                    StoreReviewCreateRequest(rating = rating, isPetAllowed = isPetAllowed, content = content),
                                )
                            }
                            val response = result.getOrNull()
                            val code = response?.code()
                            if (response?.isSuccessful == true) {
                                onSubmitted()
                            } else {
                                isSubmitting = false
                                val msg = when (code) {
                                    409 -> "이미 리뷰를 작성한 매장입니다."
                                    422 -> "입력 내용을 확인해 주세요."
                                    401, 403 -> "로그인이 필요합니다."
                                    null -> "네트워크 오류가 발생했습니다."
                                    else -> "등록에 실패했습니다. (HTTP $code)"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = "등록하기",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (rating > 0 && content.isNotBlank()) Color.White else Brown400PL,
                    )
                }
            }
        }
    }
}

// ─── 영업 시간 파싱 ────────────────────────────────────────────────────────────

private fun isOpenNow(operatingHours: String?): Boolean? {
    if (operatingHours.isNullOrBlank()) return null
    val pattern = Regex("""(\d{1,2}):(\d{2})\s*[-~]\s*(\d{1,2}):(\d{2})""")
    val match = pattern.find(operatingHours) ?: return null
    val (_, sh, sm, eh, em) = match.groupValues
    val cal = Calendar.getInstance()
    val now = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val start = sh.toInt() * 60 + sm.toInt()
    val end = eh.toInt() * 60 + em.toInt()
    return now in start until end
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PlaceDetailScreenPreview() {
    SiheungGagaeTheme { PlaceDetailScreen(placeId = 0) }
}
