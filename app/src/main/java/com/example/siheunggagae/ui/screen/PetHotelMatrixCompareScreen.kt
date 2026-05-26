package com.example.siheunggagae.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.siheunggagae.R
import com.example.siheunggagae.data.model.PetHotelResponse
import com.example.siheunggagae.ui.viewmodel.CompareInsight
import com.example.siheunggagae.ui.viewmodel.CompareMode
import com.example.siheunggagae.ui.viewmodel.MatrixCompareUi
import com.example.siheunggagae.ui.viewmodel.PetHotelMatrixCompareViewModel

// ─── Color tokens (M suffix) ─────────────────────────────────────────────────
private val BgM           = Color(0xFFFEFEFE)
private val TextBlackM    = Color(0xFF1E120A)
private val Brown700M     = Color(0xFF8A6E58)
private val Brown900M     = Color(0xFF614B3A)
private val BorderBeigeM  = Color(0xFFE8D3C2)
private val OrangeSandM   = Color(0xFFFFEDD4)
private val Orange500M    = Color(0xFFF7A35B)
private val PinkSurfaceM  = Color(0xFFFEE7EC)
private val Pink500M      = Color(0xFFF04268)
@Suppress("unused")
private val PinkText      = Color(0xFF9F1239)
private val StarYellowM   = Color(0xFFFDC700)
private val TagGrayM      = Color(0xFFF2F2F2)
private val PlaceholderM  = Color(0xFFC1AEA0)

// ─── Entry point ─────────────────────────────────────────────────────────────

@Composable
fun PetHotelMatrixCompareScreen(
    viewModel: PetHotelMatrixCompareViewModel,
    onBack: () -> Unit,
    onStoreClick: (storeId: Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = BgM,
        topBar = { MatrixTopBar(onBack) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                MatrixCompareUi.Loading -> CenteredLoading()
                is MatrixCompareUi.Error -> CenteredError(s.message) { viewModel.retry() }
                is MatrixCompareUi.Success -> SuccessBody(s, viewModel, onStoreClick)
            }
        }
    }
}

// ─── TopBar ──────────────────────────────────────────────────────────────────

@Composable
private fun MatrixTopBar(onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(BgM)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .padding(top = 8.dp), // extra breathing room below status bar
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
                    tint = TextBlackM,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            "펫호텔 비교",
            color = TextBlackM,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.weight(1f))
        // Balance spacer so title is centred
        Spacer(Modifier.size(40.dp))
    }
}

// ─── Mode toggle ─────────────────────────────────────────────────────────────

@Composable
private fun ModeToggle(
    mode: CompareMode,
    enabled: Boolean,
    onSetMode: (CompareMode) -> Unit,
) {
    val alpha = if (enabled) 1f else 0.5f
    Row(
        Modifier
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(TagGrayM)
            .padding(4.dp)
            .alpha(alpha),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        ToggleOption(
            label = "1:1 비교",
            selected = mode == CompareMode.ONE_ON_ONE,
            onClick = { if (enabled) onSetMode(CompareMode.ONE_ON_ONE) },
        )
        ToggleOption(
            label = "여러 곳 비교",
            selected = mode == CompareMode.MULTI,
            onClick = { if (enabled) onSetMode(CompareMode.MULTI) },
        )
    }
}

@Composable
private fun ToggleOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) Color.White else Color.Transparent
    val fg = if (selected) Brown900M else Brown700M
    val fw = if (selected) FontWeight.SemiBold else FontWeight.Medium
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .then(
                if (selected) Modifier else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = fg, fontSize = 14.sp, fontWeight = fw)
    }
}

// ─── Store Picker BottomSheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StorePickerSheet(
    hotels: List<PetHotelResponse>,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Text(
            "매장 선택",
            color = TextBlackM,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
        )
        HorizontalDivider(color = BorderBeigeM, thickness = 1.dp)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            hotels.forEach { hotel ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(hotel.storeId) }
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    ) {
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
                                        Brush.linearGradient(listOf(OrangeSandM, PinkSurfaceM))
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_hotel),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            hotel.name,
                            color = TextBlackM,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (hotel.ratingAvg != null) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, modifier = Modifier.size(11.dp), tint = StarYellowM)
                                    Text("${"%.1f".format(hotel.ratingAvg)}", color = StarYellowM, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(fmtDistanceM(hotel.distanceM), color = Brown700M, fontSize = 11.sp)
                            if (hotel.minPriceKrw != null && hotel.minPriceKrw > 0) {
                                Text("%,d원/박".format(hotel.minPriceKrw), color = Brown700M, fontSize = 11.sp)
                            } else if (hotel.minPriceKrw == null || hotel.minPriceKrw == 0) {
                                Text("가격 정보 없음", color = Brown700M, fontSize = 11.sp)
                            }
                        }
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = Brown700M,
                        modifier = Modifier.size(16.dp),
                    )
                }
                HorizontalDivider(color = TagGrayM, thickness = 0.5.dp)
            }
        }
    }
}

// ─── Matrix Cell ──────────────────────────────────────────────────────────────

@Composable
private fun MatrixCell(
    value: String,
    isBest: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(if (isBest) OrangeSandM else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        if (isBest) {
            // Orange dot top-left
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Orange500M),
            )
        }
        Text(
            value,
            color = if (isBest) Brown900M else TextBlackM,
            fontSize = 12.sp,
            fontWeight = if (isBest) FontWeight.ExtraBold else FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ─── Star Rating Cell ─────────────────────────────────────────────────────────

@Composable
private fun StarRatingCell(
    rating: Double?,
    count: Int,
    isBest: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(if (isBest) OrangeSandM else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        if (isBest) {
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Orange500M),
            )
        }
        if (rating != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_star),
                        contentDescription = null,
                        tint = StarYellowM,
                        modifier = Modifier.size(11.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        "${"%.1f".format(rating)}",
                        color = if (isBest) Brown900M else TextBlackM,
                        fontSize = 12.sp,
                        fontWeight = if (isBest) FontWeight.ExtraBold else FontWeight.SemiBold,
                    )
                }
                Text(
                    "${count}개",
                    color = Brown700M,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
        } else {
            Text("정보 없음", color = Brown700M, fontSize = 11.sp)
        }
    }
}

// ─── Insight Card ─────────────────────────────────────────────────────────────

@Composable
private fun InsightCard(
    text: String,
    isMulti: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OrangeSandM)
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(
                if (isMulti) R.drawable.ic_award else R.drawable.ic_lightbulb
            ),
            contentDescription = null,
            tint = Orange500M,
            modifier = Modifier.size(20.dp).padding(top = 1.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = Brown900M,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 18.sp,
        )
    }
}

// ─── Detail Button ────────────────────────────────────────────────────────────

@Composable
private fun DetailBtn(
    label: String,
    isA: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg = if (isA) Orange500M else Pink500M
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ─── Row labels for matrix ───────────────────────────────────────────────────

private val matrixRows = listOf(
    "거리",
    "별점",
    "최저가",
    "옵션 수",
)

// ─── OneOnOne Block ───────────────────────────────────────────────────────────

@Composable
private fun OneOnOneBlock(
    state: MatrixCompareUi.Success,
    viewModel: PetHotelMatrixCompareViewModel,
    onStoreClick: (Int) -> Unit,
) {
    val items = state.items
    val aHotel = items.firstOrNull { it.storeId == state.selectedAId }
    val bHotel = items.firstOrNull { it.storeId == state.selectedBId }
    val insight = state.insight

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
    ) {
        // Dropdowns
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StoreDropdown(
                hotel = aHotel,
                isSlotA = true,
                allHotels = items,
                onPick = { viewModel.selectA(it) },
                modifier = Modifier.weight(1f),
            )
            StoreDropdown(
                hotel = bHotel,
                isSlotA = false,
                allHotels = items,
                onPick = { viewModel.selectB(it) },
                modifier = Modifier.weight(1f),
            )
        }

        if (aHotel != null && bHotel != null) {
            Spacer(Modifier.height(4.dp))

            // 2-col matrix
            TwoColMatrix(a = aHotel, b = bHotel, insight = insight)

            Spacer(Modifier.height(12.dp))

            // Insight card
            InsightCard(
                text = buildInsight1on1(aHotel, bHotel, insight),
                isMulti = false,
                modifier = Modifier.padding(horizontal = 18.dp),
            )

            Spacer(Modifier.height(14.dp))

            // Detail buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 18.dp),
            ) {
                DetailBtn(
                    label = "${aHotel.name} 상세",
                    isA = true,
                    modifier = Modifier.weight(1f),
                ) { onStoreClick(aHotel.storeId) }
                DetailBtn(
                    label = "${bHotel.name} 상세",
                    isA = false,
                    modifier = Modifier.weight(1f),
                ) { onStoreClick(bHotel.storeId) }
            }
        } else {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("매장 두 곳을 선택하세요", color = PlaceholderM, fontSize = 14.sp)
            }
        }
    }
}

// ─── Two-Column Matrix ────────────────────────────────────────────────────────

@Composable
private fun TwoColMatrix(
    a: PetHotelResponse,
    b: PetHotelResponse,
    insight: CompareInsight,
) {
    val labelW = 72.dp
    val colW = 100.dp

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
    ) {
        Column {
            // Header row
            Row(Modifier.fillMaxWidth().background(TagGrayM)) {
                Box(Modifier.width(labelW).height(36.dp))
                Box(
                    Modifier.width(colW).height(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(a.name, color = Orange500M, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(
                    Modifier.weight(1f).height(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(b.name, color = Pink500M, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            HorizontalDivider(color = BorderBeigeM, thickness = 0.5.dp)

            // Distance row
            MatrixRow(
                label = "거리",
                aContent = {
                    MatrixCell(
                        fmtDistanceM(a.distanceM),
                        isBest = insight.nearestId == a.storeId,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                bContent = {
                    MatrixCell(
                        fmtDistanceM(b.distanceM),
                        isBest = insight.nearestId == b.storeId,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                labelW = labelW,
                colW = colW,
            )
            HorizontalDivider(color = TagGrayM, thickness = 0.5.dp)

            // Rating row
            MatrixRow(
                label = "별점",
                aContent = {
                    StarRatingCell(a.ratingAvg, a.ratingCount, insight.highestRatedId == a.storeId, Modifier.fillMaxSize())
                },
                bContent = {
                    StarRatingCell(b.ratingAvg, b.ratingCount, insight.highestRatedId == b.storeId, Modifier.fillMaxSize())
                },
                labelW = labelW,
                colW = colW,
            )
            HorizontalDivider(color = TagGrayM, thickness = 0.5.dp)

            // Price row
            MatrixRow(
                label = "최저가",
                aContent = {
                    MatrixCell(
                        if (a.minPriceKrw != null && a.minPriceKrw > 0) "%,d원/박".format(a.minPriceKrw) else "가격 정보 없음",
                        isBest = insight.cheapestId == a.storeId,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                bContent = {
                    MatrixCell(
                        if (b.minPriceKrw != null && b.minPriceKrw > 0) "%,d원/박".format(b.minPriceKrw) else "가격 정보 없음",
                        isBest = insight.cheapestId == b.storeId,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                labelW = labelW,
                colW = colW,
            )
            HorizontalDivider(color = TagGrayM, thickness = 0.5.dp)

            // Options row
            MatrixRow(
                label = "옵션 수",
                aContent = {
                    MatrixCell(
                        "${a.planCount}개",
                        isBest = insight.mostOptionsId == a.storeId,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                bContent = {
                    MatrixCell(
                        "${b.planCount}개",
                        isBest = insight.mostOptionsId == b.storeId,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                labelW = labelW,
                colW = colW,
            )
        }
    }
}

@Composable
private fun MatrixRow(
    label: String,
    aContent: @Composable () -> Unit,
    bContent: @Composable () -> Unit,
    labelW: Dp,
    colW: Dp,
) {
    Row(Modifier.fillMaxWidth().height(44.dp)) {
        Box(
            Modifier
                .width(labelW)
                .fillMaxSize()
                .background(TagGrayM.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, color = Brown700M, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Box(Modifier.width(colW).fillMaxSize()) { aContent() }
        Box(Modifier.weight(1f).fillMaxSize()) { bContent() }
    }
}

// ─── Multi Block ──────────────────────────────────────────────────────────────

@Composable
private fun MultiBlock(
    state: MatrixCompareUi.Success,
    onStoreClick: (Int) -> Unit,
) {
    val items = state.items
    val insight = state.insight

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
    ) {
        // Subheader
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_sliders),
                contentDescription = null,
                tint = Brown700M,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "주변 펫호텔 ${items.size}곳 비교",
                color = TextBlackM,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        // Horizontally scrollable N-column matrix
        MultiColMatrix(items = items, insight = insight)

        // Swipe hint
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = null,
                    tint = PlaceholderM,
                    modifier = Modifier.size(12.dp),
                )
                Text("좌우로 스와이프", color = PlaceholderM, fontSize = 11.sp)
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = PlaceholderM,
                    modifier = Modifier.size(12.dp),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Insight card
        InsightCard(
            text = buildInsightMulti(items, insight),
            isMulti = true,
            modifier = Modifier.padding(horizontal = 18.dp),
        )

        Spacer(Modifier.height(14.dp))

        // Store detail buttons — horizontal scroll
        Text(
            "매장 상세 보기",
            color = Brown700M,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items) { hotel ->
                val isA = items.indexOf(hotel) % 2 == 0
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isA) OrangeSandM else PinkSurfaceM)
                        .border(1.dp, if (isA) Orange500M else Pink500M, RoundedCornerShape(10.dp))
                        .clickable { onStoreClick(hotel.storeId) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        hotel.name,
                        color = if (isA) Brown900M else Pink500M,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
            }
        }
    }
}

// ─── Multi-column matrix ──────────────────────────────────────────────────────

@Composable
private fun MultiColMatrix(
    items: List<PetHotelResponse>,
    insight: CompareInsight,
) {
    val labelW = 72.dp
    val colW = 72.dp

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
    ) {
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            // Label column (sticky-ish — leftmost)
            Column {
                // Header spacer
                Box(Modifier.width(labelW).height(36.dp).background(TagGrayM))
                HorizontalDivider(color = BorderBeigeM, thickness = 0.5.dp)
                matrixRows.forEachIndexed { i, label ->
                    Box(
                        modifier = Modifier
                            .width(labelW)
                            .height(44.dp)
                            .background(TagGrayM.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, color = Brown700M, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    if (i < matrixRows.size - 1) {
                        HorizontalDivider(color = TagGrayM, thickness = 0.5.dp)
                    }
                }
            }

            // Store columns
            items.forEachIndexed { idx, hotel ->
                Column {
                    // Store name header
                    Box(
                        modifier = Modifier
                            .width(colW)
                            .height(36.dp)
                            .background(TagGrayM),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            hotel.name,
                            color = if (idx % 2 == 0) Orange500M else Pink500M,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                    HorizontalDivider(color = BorderBeigeM, thickness = 0.5.dp)

                    // Distance
                    MatrixCell(
                        fmtDistanceM(hotel.distanceM),
                        isBest = insight.nearestId == hotel.storeId,
                        modifier = Modifier.width(colW),
                    )
                    HorizontalDivider(color = TagGrayM, thickness = 0.5.dp)

                    // Rating
                    StarRatingCell(
                        hotel.ratingAvg, hotel.ratingCount,
                        isBest = insight.highestRatedId == hotel.storeId,
                        modifier = Modifier.width(colW),
                    )
                    HorizontalDivider(color = TagGrayM, thickness = 0.5.dp)

                    // Price
                    MatrixCell(
                        if (hotel.minPriceKrw != null && hotel.minPriceKrw > 0) "%,d원/박".format(hotel.minPriceKrw) else "가격 정보 없음",
                        isBest = insight.cheapestId == hotel.storeId,
                        modifier = Modifier.width(colW),
                    )
                    HorizontalDivider(color = TagGrayM, thickness = 0.5.dp)

                    // Options
                    MatrixCell(
                        "${hotel.planCount}개",
                        isBest = insight.mostOptionsId == hotel.storeId,
                        modifier = Modifier.width(colW),
                    )
                }

                // Vertical divider between columns
                if (idx < items.size - 1) {
                    Box(
                        Modifier
                            .width(0.5.dp)
                            .height(36.dp + 44.dp * 4 + 0.5.dp * 5)
                            .background(TagGrayM),
                    )
                }
            }
        }
    }
}

// ─── Success body ─────────────────────────────────────────────────────────────

@Composable
private fun SuccessBody(
    state: MatrixCompareUi.Success,
    viewModel: PetHotelMatrixCompareViewModel,
    onStoreClick: (Int) -> Unit,
) {
    val toggleEnabled = state.items.size > 2
    Column(Modifier.fillMaxSize()) {
        ModeToggle(
            mode = state.mode,
            enabled = toggleEnabled,
            onSetMode = { viewModel.setMode(it) },
        )
        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("주변에 펫호텔이 없어요", color = PlaceholderM, fontSize = 14.sp)
            }
        } else {
            when (state.mode) {
                CompareMode.ONE_ON_ONE -> OneOnOneBlock(state, viewModel, onStoreClick)
                CompareMode.MULTI -> MultiBlock(state, onStoreClick)
            }
        }
    }
}

// ─── Loading / Error helpers ──────────────────────────────────────────────────

@Composable
private fun CenteredLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Orange500M)
    }
}

@Composable
private fun CenteredError(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = Brown700M, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onRetry) {
                Text("다시 시도", color = Brown900M, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─── Insight text builders ────────────────────────────────────────────────────

private fun buildInsight1on1(
    a: PetHotelResponse,
    b: PetHotelResponse,
    insight: CompareInsight,
): String {
    val aWins = mutableListOf<String>()
    val bWins = mutableListOf<String>()
    if (insight.cheapestId == a.storeId) aWins += "가격"
    else if (insight.cheapestId == b.storeId) bWins += "가격"
    if (insight.nearestId == a.storeId) aWins += "거리"
    else if (insight.nearestId == b.storeId) bWins += "거리"
    if (insight.highestRatedId == a.storeId) aWins += "별점"
    else if (insight.highestRatedId == b.storeId) bWins += "별점"
    if (insight.mostOptionsId == a.storeId) aWins += "옵션"
    else if (insight.mostOptionsId == b.storeId) bWins += "옵션"
    val parts = mutableListOf<String>()
    if (aWins.isNotEmpty()) parts += "${a.name}이(가) ${aWins.joinToString("·")}에서 유리"
    if (bWins.isNotEmpty()) parts += "${b.name}이(가) ${bWins.joinToString("·")}에서 유리"
    return if (parts.isEmpty()) "두 매장이 비슷한 조건이에요" else parts.joinToString(", ")
}

private fun buildInsightMulti(items: List<PetHotelResponse>, insight: CompareInsight): String {
    fun name(id: Int?) = items.firstOrNull { it.storeId == id }?.name ?: "-"
    return "최저가 ${name(insight.cheapestId)} · " +
        "별점 ${name(insight.highestRatedId)} · " +
        "옵션 ${name(insight.mostOptionsId)} · " +
        "가까움 ${name(insight.nearestId)}"
}

// ─── Distance helper ──────────────────────────────────────────────────────────

private fun fmtDistanceM(meters: Double?): String {
    if (meters == null) return "거리 -"
    return if (meters < 1000) "${meters.toInt()}m" else "${"%.1f".format(meters / 1000.0)}km"
}

// ─── Store Dropdown ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreDropdown(
    hotel: PetHotelResponse?,
    isSlotA: Boolean,
    allHotels: List<PetHotelResponse>,
    onPick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSheet by remember { mutableStateOf(false) }
    val borderColor = if (isSlotA) Orange500M else Pink500M
    val slotLabel = if (isSlotA) "A" else "B"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { showSheet = true }
            .padding(10.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(borderColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(slotLabel, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(6.dp))
                if (hotel != null) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp)),
                    ) {
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
                                    .background(Brush.linearGradient(listOf(OrangeSandM, PinkSurfaceM))),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_hotel),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        hotel.name,
                        color = TextBlackM,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Text(
                        "매장 선택",
                        color = PlaceholderM,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_down),
                    contentDescription = null,
                    tint = Brown700M,
                    modifier = Modifier.size(16.dp),
                )
            }
            if (hotel != null) {
                Spacer(Modifier.height(2.dp))
                Row(
                    Modifier.padding(start = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (hotel.ratingAvg != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, modifier = Modifier.size(10.dp), tint = StarYellowM)
                            Text("${"%.1f".format(hotel.ratingAvg)}", color = StarYellowM, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(fmtDistanceM(hotel.distanceM), color = Brown700M, fontSize = 10.sp)
                    if (hotel.minPriceKrw != null && hotel.minPriceKrw > 0) {
                        Text("%,d원/박".format(hotel.minPriceKrw), color = Brown700M, fontSize = 10.sp)
                    } else if (hotel.minPriceKrw == null || hotel.minPriceKrw == 0) {
                        Text("가격 정보 없음", color = Brown700M, fontSize = 10.sp)
                    }
                }
            }
        }
    }

    if (showSheet) {
        StorePickerSheet(
            hotels = allHotels,
            onPick = { onPick(it); showSheet = false },
            onDismiss = { showSheet = false },
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun PetHotelMatrixComparePreview1on1() {
    val dummyHotels = listOf(
        PetHotelResponse(
            storeId = 1, name = "강아지 호텔 A", address = "경기 시흥시 정왕동 1234",
            latitude = 37.35, longitude = 126.73, distanceM = 320.0,
            ratingAvg = 4.5, ratingCount = 23, planCount = 3,
            minPriceKrw = 25000, maxPriceKrw = 55000,
        ),
        PetHotelResponse(
            storeId = 2, name = "해피 펫 스테이", address = "경기 시흥시 배곧동 5678",
            latitude = 37.36, longitude = 126.74, distanceM = 800.0,
            ratingAvg = 4.8, ratingCount = 47, planCount = 5,
            minPriceKrw = 35000, maxPriceKrw = 75000,
        ),
    )
    val insight = com.example.siheunggagae.ui.viewmodel.computeInsight(dummyHotels)
    val state = MatrixCompareUi.Success(
        items = dummyHotels,
        mode = CompareMode.ONE_ON_ONE,
        selectedAId = 1,
        selectedBId = 2,
        insight = insight,
    )
    val a = dummyHotels[0]
    val b = dummyHotels[1]
    androidx.compose.material3.MaterialTheme {
        Box(Modifier.fillMaxSize().background(BgM)) {
            Column {
                MatrixTopBar(onBack = {})
                ModeToggle(mode = state.mode, enabled = false, onSetMode = {})
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp),
                ) {
                    TwoColMatrix(a = a, b = b, insight = insight)
                    Spacer(Modifier.height(12.dp))
                    InsightCard(
                        text = buildInsight1on1(a, b, insight),
                        isMulti = false,
                        modifier = Modifier.padding(horizontal = 18.dp),
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 18.dp),
                    ) {
                        DetailBtn("${a.name} 상세", isA = true, modifier = Modifier.weight(1f)) {}
                        DetailBtn("${b.name} 상세", isA = false, modifier = Modifier.weight(1f)) {}
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PetHotelMatrixComparePreviewMulti() {
    val dummyHotels = listOf(
        PetHotelResponse(
            storeId = 1, name = "강아지 호텔 A", address = "경기 시흥시 정왕동 1234",
            latitude = 37.35, longitude = 126.73, distanceM = 320.0,
            ratingAvg = 4.5, ratingCount = 23, planCount = 3,
            minPriceKrw = 25000, maxPriceKrw = 55000,
        ),
        PetHotelResponse(
            storeId = 2, name = "해피 펫 스테이", address = "경기 시흥시 배곧동 5678",
            latitude = 37.36, longitude = 126.74, distanceM = 800.0,
            ratingAvg = 4.8, ratingCount = 47, planCount = 5,
            minPriceKrw = 35000, maxPriceKrw = 75000,
        ),
        PetHotelResponse(
            storeId = 3, name = "포근한 멍멍이 숙소", address = "경기 시흥시 능곡동 910",
            latitude = 37.37, longitude = 126.75, distanceM = 1200.0,
            ratingAvg = 4.2, ratingCount = 12, planCount = 2,
            minPriceKrw = 20000, maxPriceKrw = 40000,
        ),
    )
    val insight = com.example.siheunggagae.ui.viewmodel.computeInsight(dummyHotels)
    val state = MatrixCompareUi.Success(
        items = dummyHotels,
        mode = CompareMode.MULTI,
        selectedAId = 1,
        selectedBId = 2,
        insight = insight,
    )
    androidx.compose.material3.MaterialTheme {
        Box(Modifier.fillMaxSize().background(BgM)) {
            Column {
                MatrixTopBar(onBack = {})
                ModeToggle(mode = state.mode, enabled = true, onSetMode = {})
                MultiBlock(state = state, onStoreClick = {})
            }
        }
    }
}
