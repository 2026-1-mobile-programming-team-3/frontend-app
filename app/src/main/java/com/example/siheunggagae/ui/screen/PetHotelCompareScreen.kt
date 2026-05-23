package com.example.siheunggagae.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.siheunggagae.R
import com.example.siheunggagae.data.model.PetHotelResponse
import com.example.siheunggagae.ui.viewmodel.CompareSortAxis
import com.example.siheunggagae.ui.viewmodel.PetHotelCompareUi
import com.example.siheunggagae.ui.viewmodel.PetHotelCompareViewModel
import com.example.siheunggagae.ui.viewmodel.PetSize

private val BackgroundP    = Color(0xFFFEFEFE)
private val TextBlackP     = Color(0xFF1E120A)
private val Brown700P      = Color(0xFF8A6E58)
private val Brown900P      = Color(0xFF614B3A)
private val BorderBeigeP   = Color(0xFFE8D3C2)
private val OrangeSandP    = Color(0xFFFFEDD4)
private val Orange500P     = Color(0xFFF7A35B)
private val PinkSurfaceP   = Color(0xFFFEE7EC)
private val Pink500P       = Color(0xFFF04268)
@Suppress("unused")
private val Gray300P       = Color(0xFFE8E8E8)
private val GrayTrackP     = Color(0xFFF4F4F4)
private val StarYellowP    = Color(0xFFFDC700)
@Suppress("unused")
private val PlaceholderP   = Color(0xFFC1AEA0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetHotelCompareScreen(
    viewModel: PetHotelCompareViewModel? = null,
    onBack: () -> Unit = {},
    onStoreClick: (storeId: Int) -> Unit = {},
    onCompareClick: () -> Unit = {},
) {
    val state by (viewModel?.state?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(PetHotelCompareUi.Loading) })

    Scaffold(
        containerColor = BackgroundP,
        topBar = {
            CompareTopBar(
                state = state,
                onBack = onBack,
                onCompareClick = onCompareClick,
                canCompare = ((state as? PetHotelCompareUi.Success)?.items?.size ?: 0) >= 2,
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SortChipsRow(
                selected = (state as? PetHotelCompareUi.Success)?.sort ?: CompareSortAxis.PRICE,
                onSelect = { viewModel?.setSort(it) },
            )
            SizeChipsRow(
                selected = (state as? PetHotelCompareUi.Success)?.size ?: PetSize.ALL,
                onSelect = { viewModel?.setSize(it) },
            )
            when (val s = state) {
                PetHotelCompareUi.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = Orange500P) }

                is PetHotelCompareUi.Error -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(s.message, color = Brown700P, fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel?.retry() }) { Text("다시 시도") }
                    }
                }

                is PetHotelCompareUi.Success -> {
                    if (s.items.isEmpty()) {
                        EmptyState(onExpand = { viewModel?.expandRadius() })
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                        ) {
                            items(s.items, key = { it.storeId }) { hotel ->
                                PetHotelCard(
                                    hotel = hotel,
                                    absMin = s.absMinPrice,
                                    absMax = s.absMaxPrice,
                                    isLowest = s.items.firstOrNull()?.storeId == hotel.storeId &&
                                        s.sort == CompareSortAxis.PRICE,
                                    onClick = { onStoreClick(hotel.storeId) },
                                    onFavoriteClick = { viewModel?.toggleFavorite(hotel.storeId) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareTopBar(
    state: PetHotelCompareUi,
    onBack: () -> Unit,
    onCompareClick: () -> Unit = {},
    canCompare: Boolean = false,
) {
    val count = (state as? PetHotelCompareUi.Success)?.items?.size ?: 0
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier
                .size(40.dp)
                .clickable { onBack() },
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "뒤로가기",
                    tint = TextBlackP,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            if (count > 0) "주변 펫호텔 ${count}곳" else "주변 펫호텔",
            color = TextBlackP, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier
                .size(40.dp)
                .alpha(if (canCompare) 1f else 0.4f)
                .clickable(enabled = canCompare) { onCompareClick() },
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_sliders),
                    contentDescription = "비교",
                    tint = TextBlackP,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun SortChipsRow(
    selected: CompareSortAxis,
    onSelect: (CompareSortAxis) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        item { ChipP("최저가", selected == CompareSortAxis.PRICE) { onSelect(CompareSortAxis.PRICE) } }
        item { ChipP("거리", selected == CompareSortAxis.DISTANCE) { onSelect(CompareSortAxis.DISTANCE) } }
        item { ChipP("별점", selected == CompareSortAxis.RATING) { onSelect(CompareSortAxis.RATING) } }
    }
}

@Composable
private fun SizeChipsRow(
    selected: PetSize,
    onSelect: (PetSize) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(bottom = 14.dp),
    ) {
        item { ChipP("전체", selected == PetSize.ALL, small = true) { onSelect(PetSize.ALL) } }
        item { ChipP("소형", selected == PetSize.SMALL, small = true) { onSelect(PetSize.SMALL) } }
        item { ChipP("중형", selected == PetSize.MEDIUM, small = true) { onSelect(PetSize.MEDIUM) } }
        item { ChipP("대형", selected == PetSize.LARGE, small = true) { onSelect(PetSize.LARGE) } }
    }
}

@Composable
private fun ChipP(
    label: String,
    isSelected: Boolean,
    small: Boolean = false,
    onClick: () -> Unit,
) {
    val bg = if (isSelected) Color(0xFF1A1A1A) else Color.White
    val fg = if (isSelected) Color.White else Brown700P
    val ph = if (small) 10.dp else 16.dp
    val pv = if (small) 4.dp else 8.dp
    val font = if (small) 11.sp else 14.sp
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .then(
                if (!isSelected) Modifier.border(1.dp, BorderBeigeP, RoundedCornerShape(50.dp))
                else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = ph, vertical = pv),
    ) { Text(label, color = fg, fontSize = font, fontWeight = FontWeight.Medium) }
}

@Composable
private fun PetHotelCard(
    hotel: PetHotelResponse,
    absMin: Int,
    absMax: Int,
    isLowest: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp)
            .clickable { onClick() },
    ) {
        Column {
            // 상단 가로 썸네일 140dp
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                if (!hotel.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = hotel.thumbnailUrl,
                        contentDescription = hotel.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(OrangeSandP, PinkSurfaceP),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_hotel),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
                // 좌상단 "최저가" 뱃지 (최저가 카드만)
                if (isLowest) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brown900P)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            "최저가",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                // 우상단 ♥
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable { onFavoriteClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_favorite),
                        contentDescription = "즐겨찾기",
                        tint = if (hotel.isFavorited) Pink500P else BorderBeigeP,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // 정보 영역
            Column(modifier = Modifier.padding(12.dp, 12.dp, 14.dp, 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        hotel.name,
                        color = TextBlackP,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f),
                    )
                    if (hotel.ratingAvg != null) {
                        Text(
                            "★ ${"%.1f".format(hotel.ratingAvg)}",
                            color = StarYellowP,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "(${hotel.ratingCount})",
                            color = Brown700P,
                            fontSize = 12.sp,
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "${shortAddress(hotel.address)} · ${formatDistance(hotel.distanceM)}",
                    color = Brown700P,
                    fontSize = 11.sp,
                )

                // PriceRangeBar
                if (hotel.minPriceKrw != null && hotel.maxPriceKrw != null && absMax > absMin) {
                    Spacer(Modifier.height(10.dp))
                    PriceRangeBar(
                        storeMin = hotel.minPriceKrw,
                        storeMax = hotel.maxPriceKrw,
                        absMin = absMin,
                        absMax = absMax,
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(OrangeSandP)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            "${hotel.planCount}옵션",
                            color = Brown900P,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (hotel.minPriceKrw != null) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("최저가 ", color = Brown700P, fontSize = 11.sp)
                            Text(
                                "%,d".format(hotel.minPriceKrw),
                                color = Brown900P,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Text(
                                "원~",
                                color = Brown700P,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun shortAddress(address: String): String {
    val parts = address.split(" ").filter { it.isNotBlank() }
    if (parts.size <= 2) return address
    return parts.drop(1).take(2).joinToString(" ")
}

private fun formatDistance(meters: Double?): String {
    if (meters == null) return "거리 -"
    return if (meters < 1000) "${meters.toInt()}m" else "${"%.1f".format(meters / 1000.0)}km"
}

@Composable
private fun EmptyState(onExpand: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(OrangeSandP),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_hotel),
                    contentDescription = null,
                    tint = Orange500P,
                    modifier = Modifier.size(40.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "주변에 펫호텔이 없어요",
                color = TextBlackP,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "반경을 넓혀 검색해보세요",
                color = Brown700P,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onExpand,
                colors = ButtonDefaults.buttonColors(containerColor = Brown900P),
                shape = RoundedCornerShape(12.dp),
            ) { Text("반경 늘리기", color = Color.White) }
        }
    }
}

@Composable
internal fun PriceRangeBar(
    storeMin: Int,
    storeMax: Int,
    absMin: Int,
    absMax: Int,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.horizontalGradient(listOf(OrangeSandP, Orange500P))
    val dotColor = Brown900P
    val track = GrayTrackP
    val range = (absMax - absMin).coerceAtLeast(1)
    val startFrac = ((storeMin - absMin).toFloat() / range).coerceIn(0f, 1f)
    val endFrac = ((storeMax - absMin).toFloat() / range).coerceIn(0f, 1f)

    Canvas(modifier = modifier.height(8.dp).fillMaxWidth()) {
        val h = size.height
        val w = size.width
        val cr = h / 2f
        drawRoundRect(
            color = track,
            cornerRadius = CornerRadius(cr, cr),
            size = Size(w, h),
        )
        val barLeft = startFrac * w
        val barWidth = (endFrac - startFrac) * w
        if (barWidth > 0f) {
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(barLeft, 0f),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(cr, cr),
            )
        }
        val dotR = h * 0.85f
        drawCircle(
            color = Color.White,
            radius = dotR + 1.5.dp.toPx(),
            center = Offset(barLeft, h / 2f),
        )
        drawCircle(
            color = dotColor,
            radius = dotR,
            center = Offset(barLeft, h / 2f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PetHotelCompareScreenPreview() {
    PetHotelCompareScreen()
}
