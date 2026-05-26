package com.example.siheunggagae.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.PaddingValues
import com.example.siheunggagae.ui.viewmodel.DistanceFilter
import com.example.siheunggagae.ui.viewmodel.MatchSort
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.siheunggagae.ui.viewmodel.MatchingUi
import com.example.siheunggagae.ui.viewmodel.computeImminence
import com.example.siheunggagae.ui.viewmodel.walkingMinutes
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.R
import com.example.siheunggagae.Screen
import com.example.siheunggagae.data.location.EffectiveCenter
import com.example.siheunggagae.data.model.MatchCategory
import com.example.siheunggagae.data.model.MatchListItem
import com.example.siheunggagae.data.model.requiresVolunteerRole
import com.example.siheunggagae.ui.component.ShimmerBox
import com.example.siheunggagae.ui.component.SiheungSnackbarHost
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import com.example.siheunggagae.ui.util.appleSpec
import com.example.siheunggagae.ui.util.appleTapScale
import com.example.siheunggagae.ui.util.rememberAppleInteractionSource
import com.example.siheunggagae.ui.viewmodel.Imminence
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.siheunggagae.ui.viewmodel.MatchingViewModel
import com.example.siheunggagae.ui.util.matchStatusToKorean
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle

private val Brown900M     = Color(0xFF614B3A)
private val Brown700M     = Color(0xFF8A6E58)
private val Brown400M     = Color(0xFFC4A882)
private val BrownBorderM  = Color(0xFFE8D3C2)
private val Orange500M    = Color(0xFFF7A35B)
private val Pink500M      = Color(0xFFF04268)
private val Green500M     = Color(0xFF00A63E)
private val PinkSurfaceM  = Color(0xFFFEE7EC)
private val Background95  = Color(0xFFFEFEFE)
private val TextBlack     = Color(0xFF1E120A)
private val FABBrown      = Color(0xFF9A7B5E)

// R4 token aliases (reuse values already defined above)
private val TextBlackM   = TextBlack
private val PlaceholderM = Color(0xFFC1AEA0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingScreen(
    viewModel: MatchingViewModel,
    onBack: () -> Unit = {},
    onMyRequestsClick: () -> Unit = {},
    onRequestFlowClick: () -> Unit = {},
    onCardClick: (matchId: Int, isMine: Boolean) -> Unit = { _, _ -> },
    onVolunteerApplyClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    locationAvailable: Boolean = false,
    centerFallback: EffectiveCenter? = null,
    currentUserId: Int? = null,
    currentUserNickname: String? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val isSearchMode = (state as? MatchingUi.Success)?.isSearchMode == true
    val searchQuery = (state as? MatchingUi.Success)?.searchQuery ?: ""
    val keyboardController = LocalSoftwareKeyboardController.current

    BackHandler(enabled = isSearchMode) {
        keyboardController?.hide()
        viewModel.exitSearch()
    }

    var showSortSheet by remember { mutableStateOf(false) }
    var showDistanceSheet by remember { mutableStateOf(false) }
    var moreSheetFor by remember { mutableStateOf<Int?>(null) }  // matchId — 본인 글일 때만
    val sheetHaptic = LocalHapticFeedback.current

    // 무한 스크롤 trigger
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastIndex ->
                val success = state as? MatchingUi.Success ?: return@collect
                if (lastIndex != null && lastIndex >= success.items.size - 3 && success.hasMore) {
                    viewModel.loadMore()
                }
            }
    }

    Scaffold(containerColor = Color(0xFFFEFEFE)) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            PullToRefreshBox(
                isRefreshing = (state as? MatchingUi.Success)?.isRefreshing == true,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(Modifier.fillMaxSize()) {
                    MatchTopBar(
                        isSearchMode = isSearchMode,
                        onSearchToggle = {
                            if (isSearchMode) { keyboardController?.hide(); viewModel.exitSearch() }
                            else viewModel.enterSearch()
                        },
                        onMyRequestsClick = onMyRequestsClick,
                    )
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSearchMode,
                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut(),
                    ) {
                        MatchSearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.updateSearch(it) },
                        )
                    }
                    val success = state as? MatchingUi.Success
                    if (centerFallback != null) {
                        MatchingFallbackBanner(center = centerFallback)
                    }
                    StatusTabRow(
                        selected = success?.selectedStatus,
                        counts = success?.statusTabCounts ?: emptyMap(),
                        onSelect = { viewModel.setStatus(it) },
                    )
                    SortDistanceRow(
                        sort = success?.sort ?: MatchSort.IMMINENT,
                        distance = success?.distance ?: DistanceFilter.KM5,
                        locationAvailable = locationAvailable,
                        onSortClick = {
                            sheetHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showSortSheet = true
                        },
                        onDistanceClick = {
                            sheetHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showDistanceSheet = true
                        },
                    )
                    CategoryChipsRow(
                        selected = success?.selectedCategory,
                        onSelect = { viewModel.setCategory(it) },
                    )
                    if (success?.showVolunteerWarning == true) {
                        VolunteerWarningBanner(onApplyClick = onVolunteerApplyClick)
                    }
                    val newCountInFlow = success?.newCount ?: 0
                    androidx.compose.animation.AnimatedVisibility(
                        visible = newCountInFlow > 0,
                        enter = androidx.compose.animation.slideInVertically(initialOffsetY = { -it }) + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }) + androidx.compose.animation.fadeOut(),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            NewCountPill(
                                count = newCountInFlow,
                                onClick = { viewModel.dismissNewCount() },
                            )
                        }
                    }

                    when (val s = state) {
                        MatchingUi.Loading -> SkeletonCards()
                        is MatchingUi.Error -> ErrorBlock(s.message) { viewModel.refresh() }
                        is MatchingUi.Success -> {
                            if (s.items.isEmpty()) {
                                EmptyState(
                                    onExpandDistance = { viewModel.setDistance(DistanceFilter.ALL) },
                                    onCreate = onRequestFlowClick,
                                )
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 96.dp),
                                ) {
                                    itemsIndexed(s.items, key = { idx, item -> item.matchId ?: idx }) { _, item ->
                                        val isMine = when {
                                            currentUserId != null && item.authorUserId != null ->
                                                item.authorUserId == currentUserId
                                            !currentUserNickname.isNullOrBlank() &&
                                                !item.authorNickname.isNullOrBlank() ->
                                                item.authorNickname == currentUserNickname
                                            else -> false
                                        }
                                        val imm = computeImminence(item.desiredDate, item.desiredTime)
                                        val distanceLabel = buildMatchDistanceLabel(item)
                                        // animateItem(): 새 카드 추가·재정렬 시 부드러운 슬라이드/페이드 (Compose 1.7+).
                                        MatchCardR(
                                            modifier = Modifier.animateItem(),
                                            item = item,
                                            isMine = isMine,
                                            imminence = imm,
                                            distanceLabel = distanceLabel,
                                            onClick = {
                                                item.matchId?.let { onCardClick(it, isMine) }
                                            },
                                            onMoreClick = if (isMine) {
                                                { item.matchId?.let { moreSheetFor = it } }
                                            } else null,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // FAB — 검색 모드일 때는 숨김
            val haptic = LocalHapticFeedback.current
            if (!isSearchMode) FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRequestFlowClick()
                },
                containerColor = Color(0xFF9A7B5E),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 100.dp),
            ) {
                Icon(painter = painterResource(R.drawable.ic_add), contentDescription = "요청 작성")
            }
        }
    }

    if (showSortSheet) {
        SortSheet(
            current = (state as? MatchingUi.Success)?.sort ?: MatchSort.IMMINENT,
            locationAvailable = locationAvailable,
            onPick = { viewModel.setSort(it) },
            onDismiss = { showSortSheet = false },
        )
    }
    if (showDistanceSheet) {
        DistanceSheet(
            current = (state as? MatchingUi.Success)?.distance ?: DistanceFilter.KM5,
            onPick = { viewModel.setDistance(it) },
            onDismiss = { showDistanceSheet = false },
        )
    }
    moreSheetFor?.let { matchId ->
        CardMoreSheet(
            onEdit = { /* C4: 실제 연결 */ },
            onDelete = { /* C4: 실제 연결 */ },
            onDismiss = { moreSheetFor = null },
        )
    }
}

@Composable
private fun SkeletonCards() {
    Column {
        repeat(3) { i ->
            val a = 1f - (i * 0.2f)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
                    .alpha(a),
            ) {
                Row(Modifier.padding(14.dp)) {
                    ShimmerBox(Modifier.size(60.dp), shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        ShimmerBox(Modifier.height(14.dp).fillMaxWidth(0.5f), shape = RoundedCornerShape(6.dp))
                        Spacer(Modifier.height(8.dp))
                        ShimmerBox(Modifier.height(14.dp).fillMaxWidth(0.8f), shape = RoundedCornerShape(6.dp))
                        Spacer(Modifier.height(8.dp))
                        ShimmerBox(Modifier.height(10.dp).fillMaxWidth(0.6f), shape = RoundedCornerShape(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NewCountPill(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFF614B3A))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_refresh),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "새 요청 ${count}건 · 탭하여 보기",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = Brown700M, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onRetry) { Text("다시 시도") }
        }
    }
}

private fun buildMatchDistanceLabel(item: MatchListItem): String {
    val addressShort = shortAddressForMatch(item.address.orEmpty())
    val dist = item.distanceM
    return if (dist != null) {
        val unit = if (dist < 1000) "도보 ${walkingMinutes(dist)}분" else "%.1fkm".format(dist / 1000)
        "$addressShort · $unit"
    } else addressShort.ifBlank { "위치 미상" }
}

private fun shortAddressForMatch(address: String): String {
    val parts = address.split(" ").filter { it.isNotBlank() }
    if (parts.size <= 1) return address
    return parts.drop(1).take(2).joinToString(" ").ifBlank { address }
}

// ──────────────────────────────────────────────────────────────────────────────
// R4 — Card composable + supporting chips
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MatchCardR(
    item: MatchListItem,
    isMine: Boolean,
    imminence: Imminence?,
    distanceLabel: String,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val status = item.status ?: "WAITING"
    val cardAlpha = if (status == "DONE") 0.55f else 1f
    val interaction = rememberAppleInteractionSource()
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp)
            .alpha(cardAlpha)
            .appleTapScale(interaction)
            .clickable(interactionSource = interaction, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
    ) {
        Box {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                CardThumbnail(category = item.category)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f).padding(end = 28.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        StatusChip(status)
                        item.category?.let { cat ->
                            if (cat.requiresVolunteerRole()) VolunteerCatChip()
                        }
                        imminence?.let { ImminenceChip(it, item.desiredDate, item.desiredTime) }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.title.orEmpty(),
                        color = TextBlackM,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_location_on),
                            contentDescription = null,
                            tint = Brown700M,
                            modifier = Modifier.size(11.dp),
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(distanceLabel, color = Brown700M, fontSize = 11.sp, maxLines = 1)
                        if (isMine) {
                            Spacer(Modifier.width(6.dp))
                            Text("·", color = PlaceholderM, fontSize = 11.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "내가 작성",
                                color = Brown700M,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    val applicants = item.applicationsCount ?: 0
                    if (applicants > 0) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_users),
                                contentDescription = null,
                                tint = if (isMine) Pink500M else Brown700M,
                                modifier = Modifier.size(11.dp),
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = if (isMine) "신청 ${applicants}명" else "신청 $applicants",
                                color = if (isMine) Pink500M else Brown700M,
                                fontSize = 11.sp,
                                fontWeight = if (isMine) FontWeight.ExtraBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
            // top-right ⋮ — 본인 글일 때만 노출
            if (onMoreClick != null) {
                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = "더보기",
                        tint = Brown700M,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CardThumbnail(category: MatchCategory?) {
    val brush = when (category) {
        MatchCategory.WALK -> Brush.linearGradient(listOf(Color(0xFFFFEDD4), Color(0xFFF7A35B)))
        MatchCategory.VET -> Brush.linearGradient(listOf(Color(0xFFDCFCE7), Color(0xFF16A34A)))
        MatchCategory.SHOPPING -> Brush.linearGradient(listOf(Color(0xFFFEE7EC), Color(0xFFF04268)))
        MatchCategory.MOVE -> Brush.linearGradient(listOf(Color(0xFFDBEAFE), Color(0xFF388AF5)))
        MatchCategory.VOLUNTEER -> Brush.linearGradient(listOf(Color(0xFFD0FEE1), Color(0xFF00A63E)))
        MatchCategory.OTHER -> Brush.linearGradient(listOf(Color(0xFFF4F4F4), Color(0xFFC4A882)))
        null -> Brush.linearGradient(listOf(Color(0xFFF4F4F4), Color(0xFFE0E0E0)))
    }
    val iconRes = when (category) {
        MatchCategory.WALK -> R.drawable.ic_paw
        MatchCategory.VET -> R.drawable.ic_stethoscope
        MatchCategory.SHOPPING -> R.drawable.ic_shopping_cart
        MatchCategory.MOVE -> R.drawable.ic_car
        MatchCategory.VOLUNTEER -> R.drawable.ic_award
        MatchCategory.OTHER -> R.drawable.ic_users
        null -> R.drawable.ic_users
    }
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val (bg, fg, label) = when (status) {
        "WAITING" -> Triple(Color(0xFFFEE7EC), Color(0xFFE84B6A), "모집중")
        "MATCHING" -> Triple(Color(0xFFFEF3C7), Color(0xFFCA8A04), "검토중")
        "PROGRESS" -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), "진행중")
        "DONE" -> Triple(Color(0xFFF3F4F6), Color(0xFF6B7280), "완료")
        else -> Triple(Color(0xFFF3F4F6), Color(0xFF6B7280), matchStatusToKorean(status))
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) { Text(label, color = fg, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun VolunteerCatChip() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFF00A63E))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_award),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(10.dp),
        )
        Spacer(Modifier.width(3.dp))
        Text("봉사자", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ImminenceChip(imm: Imminence, date: String?, time: String?) {
    val (bg, fg, label) = when (imm) {
        Imminence.CRITICAL_6H -> Triple(
            Color(0xFFFEE7EC), Color(0xFFF04268),
            "마감 임박" + (computeRemainingShort(date, time)?.let { " · $it" } ?: ""),
        )
        Imminence.TODAY_24H -> Triple(
            Color(0xFFFFEDD4), Color(0xFFF7A35B),
            "오늘 " + (time?.take(5) ?: ""),
        )
        Imminence.TOMORROW_D1 -> Triple(
            Color(0xFFF2F2F2), Color(0xFF8A6E58),
            "내일 · D-1",
        )
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_clock),
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(10.dp),
        )
        Spacer(Modifier.width(3.dp))
        Text(label, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

private fun computeRemainingShort(date: String?, time: String?): String? {
    if (date.isNullOrBlank() || time.isNullOrBlank()) return null
    val d = runCatching { java.time.LocalDate.parse(date) }.getOrNull() ?: return null
    val t = runCatching { java.time.LocalTime.parse(time) }.getOrNull() ?: return null
    val target = java.time.LocalDateTime.of(d, t)
        .atZone(java.time.ZoneId.of("Asia/Seoul")).toInstant()
    val sec = target.epochSecond - java.time.Instant.now().epochSecond
    return when {
        sec <= 0 -> null
        sec < 3600 -> "${sec / 60}m"
        sec < 6 * 3600 -> "${sec / 3600}h"
        else -> null
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// R5 — TopBar / Pill 탭 / 정렬·거리 시트 / 카테고리 칩 / 봉사자 안내 배너
// ──────────────────────────────────────────────────────────────────────────────

@Composable
internal fun MatchTopBar(
    isSearchMode: Boolean,
    onSearchToggle: () -> Unit,
    onMyRequestsClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding()
            .padding(horizontal = 18.dp).padding(top = 4.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("매칭", color = TextBlackM, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.weight(1f))
        MatchTopBarIcon(
            iconRes = R.drawable.ic_search,
            tint = if (isSearchMode) Brown900M else Brown700M,
            onClick = onSearchToggle,
        )
        Spacer(Modifier.width(8.dp))
        MatchTopBarIcon(R.drawable.ic_assignment) { onMyRequestsClick() }
    }
}

@Composable
private fun MatchSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(bottom = 10.dp)
            .height(44.dp),
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = Brown700M,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                modifier = Modifier.weight(1f).focusRequester(focusRequester),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = TextBlackM,
                    fontFamily = PretendardFamily,
                ),
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                "제목, 닉네임, 주소 검색...",
                                fontSize = 14.sp,
                                color = PlaceholderM,
                                fontFamily = PretendardFamily,
                            )
                        }
                        inner()
                    }
                },
            )
            if (query.isNotEmpty()) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "지우기",
                    tint = Brown400M,
                    modifier = Modifier.size(16.dp).clickable { onQueryChange("") },
                )
            }
        }
    }
}

@Composable
private fun MatchTopBarIcon(iconRes: Int, tint: Color = Brown700M, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.size(40.dp).clickable { onClick() },
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
internal fun StatusTabRow(
    selected: String?,
    counts: Map<String?, Int>,
    onSelect: (String?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 12.dp),
    ) {
        item { PillTab("전체", selected == null, count = null) { onSelect(null) } }
        item { PillTab("모집중", selected == "WAITING", count = counts["WAITING"]) { onSelect("WAITING") } }
        item { PillTab("검토중", selected == "MATCHING", count = counts["MATCHING"]) { onSelect("MATCHING") } }
        item { PillTab("진행중", selected == "PROGRESS", count = counts["PROGRESS"]) { onSelect("PROGRESS") } }
        item { PillTab("완료", selected == "DONE", count = null) { onSelect("DONE") } }
    }
}

@Composable
private fun PillTab(label: String, on: Boolean, count: Int?, onClick: () -> Unit) {
    // bg/fg morph 로 선택 전환이 부드럽게.
    val bg by animateColorAsState(
        targetValue = if (on) Color(0xFF1A1A1A) else Color.White,
        animationSpec = appleSpec(),
        label = "pillBg",
    )
    val fg by animateColorAsState(
        targetValue = if (on) Color.White else Brown700M,
        animationSpec = appleSpec(),
        label = "pillFg",
    )
    val interaction = rememberAppleInteractionSource()
    Box(
        modifier = Modifier
            .appleTapScale(interaction)
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .then(if (!on) Modifier.border(1.dp, Color(0xFFE8D3C2), RoundedCornerShape(50.dp)) else Modifier)
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 16.dp, vertical = 7.dp),
    ) {
        val text = if (count != null && count > 0) "$label $count" else label
        Text(text, color = fg, fontSize = 13.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
internal fun SortDistanceRow(
    sort: MatchSort,
    distance: DistanceFilter,
    locationAvailable: Boolean,
    onSortClick: () -> Unit,
    onDistanceClick: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 18.dp).padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SortDistancePill(label = sort.label(), onClick = onSortClick, active = true, enabled = true)
        // 위치 미확보 시 거리 필터 비활성 — 회색 + 클릭 무시.
        SortDistancePill(
            label = if (locationAvailable) distance.label() else "위치 확인 중",
            onClick = onDistanceClick,
            active = locationAvailable && distance != DistanceFilter.ALL,
            enabled = locationAvailable,
        )
    }
}

internal fun MatchSort.label(): String = when (this) {
    MatchSort.IMMINENT -> "임박순"
    MatchSort.RECENT -> "최신순"
    MatchSort.NEAREST -> "가까운순"
}

internal fun DistanceFilter.label(): String = when (this) {
    DistanceFilter.KM1 -> "1km 이내"
    DistanceFilter.KM3 -> "3km 이내"
    DistanceFilter.KM5 -> "5km 이내"
    DistanceFilter.ALL -> "전체 거리"
}

@Composable
private fun SortDistancePill(label: String, onClick: () -> Unit, active: Boolean, enabled: Boolean = true) {
    Row(
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White)
            .border(
                1.dp,
                if (active) Brown900M else Color(0xFFE8D3C2),
                RoundedCornerShape(50.dp),
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            color = if (active) Brown900M else Brown700M,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            painter = painterResource(R.drawable.ic_chevron_down),
            contentDescription = null,
            tint = Brown700M,
            modifier = Modifier.size(12.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SortSheet(
    current: MatchSort,
    locationAvailable: Boolean,
    onPick: (MatchSort) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            Text("정렬", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextBlackM)
            Spacer(Modifier.height(14.dp))
            SortOptionRow("임박순", "출발 시각이 가까운 순", current == MatchSort.IMMINENT) {
                onPick(MatchSort.IMMINENT); onDismiss()
            }
            SortOptionRow("최신순", "최근 작성된 순", current == MatchSort.RECENT) {
                onPick(MatchSort.RECENT); onDismiss()
            }
            SortOptionRow(
                "가까운순",
                if (locationAvailable) "내 위치에서 가까운 순" else "위치 권한이 필요해요 (비활성)",
                current == MatchSort.NEAREST,
                enabled = locationAvailable,
            ) {
                if (locationAvailable) {
                    onPick(MatchSort.NEAREST); onDismiss()
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SortOptionRow(
    label: String,
    desc: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 10.dp)
            .alpha(if (enabled) 1f else 0.4f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = TextBlackM, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(desc, color = Brown700M, fontSize = 12.sp)
        }
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Brown900M,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DistanceSheet(
    current: DistanceFilter,
    onPick: (DistanceFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            Text("거리", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextBlackM)
            Spacer(Modifier.height(14.dp))
            SortOptionRow("1km 이내", "도보 15분 정도", current == DistanceFilter.KM1) { onPick(DistanceFilter.KM1); onDismiss() }
            SortOptionRow("3km 이내", "동네 한 바퀴", current == DistanceFilter.KM3) { onPick(DistanceFilter.KM3); onDismiss() }
            SortOptionRow("5km 이내", "근처 지역까지", current == DistanceFilter.KM5) { onPick(DistanceFilter.KM5); onDismiss() }
            SortOptionRow("전체 거리", "거리 제한 없이", current == DistanceFilter.ALL) { onPick(DistanceFilter.ALL); onDismiss() }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
internal fun CategoryChipsRow(
    selected: MatchCategory?,
    onSelect: (MatchCategory?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(bottom = 14.dp),
    ) {
        item { CategoryChip(null, "전체", null, selected == null) { onSelect(null) } }
        item { CategoryChip(MatchCategory.WALK, "산책동행", R.drawable.ic_paw, selected == MatchCategory.WALK) { onSelect(MatchCategory.WALK) } }
        item { CategoryChip(MatchCategory.VET, "병원동행", R.drawable.ic_stethoscope, selected == MatchCategory.VET) { onSelect(MatchCategory.VET) } }
        item { CategoryChip(MatchCategory.SHOPPING, "장보기", R.drawable.ic_shopping_cart, selected == MatchCategory.SHOPPING) { onSelect(MatchCategory.SHOPPING) } }
        item { CategoryChip(MatchCategory.MOVE, "이동", R.drawable.ic_car, selected == MatchCategory.MOVE) { onSelect(MatchCategory.MOVE) } }
        item { CategoryChip(MatchCategory.VOLUNTEER, "봉사", R.drawable.ic_award, selected == MatchCategory.VOLUNTEER) { onSelect(MatchCategory.VOLUNTEER) } }
        item { CategoryChip(MatchCategory.OTHER, "기타", R.drawable.ic_users, selected == MatchCategory.OTHER) { onSelect(MatchCategory.OTHER) } }
    }
}

@Composable
private fun CategoryChip(
    cat: MatchCategory?,
    label: String,
    iconRes: Int?,
    on: Boolean,
    onClick: () -> Unit,
) {
    val isVolunteer = cat?.requiresVolunteerRole() == true
    val bg by animateColorAsState(
        targetValue = when {
            on -> Color(0xFF1A1A1A)
            isVolunteer -> Color(0xFFDCFCE7)
            else -> Color.White
        },
        animationSpec = appleSpec(),
        label = "catChipBg",
    )
    val fg by animateColorAsState(
        targetValue = when {
            on -> Color.White
            isVolunteer -> Color(0xFF16A34A)
            else -> Brown700M
        },
        animationSpec = appleSpec(),
        label = "catChipFg",
    )
    val interaction = rememberAppleInteractionSource()
    Row(
        modifier = Modifier
            .appleTapScale(interaction)
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .then(
                if (!on) Modifier.border(
                    1.dp,
                    if (isVolunteer) Color(0xFF16A34A) else Color(0xFFE8D3C2),
                    RoundedCornerShape(50.dp),
                ) else Modifier
            )
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(label, color = fg, fontSize = 12.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun EmptyState(onExpandDistance: () -> Unit, onCreate: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEDD4)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_paw),
                    contentDescription = null,
                    tint = Color(0xFFF7A35B),
                    modifier = Modifier.size(40.dp),
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "근처에 요청이 없어요",
                color = TextBlackM,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "거리를 더 넓게 보거나\n직접 요청을 작성해 보세요",
                color = Brown700M,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onExpandDistance,
                    border = BorderStroke(1.dp, Brown900M),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("거리 넓히기", color = Brown900M, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onCreate,
                    colors = ButtonDefaults.buttonColors(containerColor = Brown900M),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("요청 작성", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardMoreSheet(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
            MoreRow("수정") { onEdit(); onDismiss() }
            MoreRow("삭제", danger = true) { onDelete(); onDismiss() }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MoreRow(label: String, danger: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 14.dp)
            .alpha(if (enabled) 1f else 0.4f),
    ) {
        Text(
            label,
            color = if (danger) Color(0xFFF04268) else TextBlackM,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** GPS 가 시흥 밖이라 사용자 등록 동네/시청 좌표 기준으로 매칭을 보여주는 중임을 안내. */
@Composable
private fun MatchingFallbackBanner(center: EffectiveCenter) {
    val regionLabel = center.regionDong ?: "시흥시청"
    Row(
        modifier = Modifier
            .padding(horizontal = 18.dp).padding(bottom = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEAFBF1))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_location_on),
            contentDescription = null,
            tint = Color(0xFF00A63E),
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "현재 위치가 시흥 밖이라 ${regionLabel} 기준으로 매칭을 표시 중이에요",
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextBlack,
            lineHeight = 16.sp,
        )
    }
}

@Composable
internal fun VolunteerWarningBanner(onApplyClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 18.dp).padding(bottom = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0FDF4))
            .border(1.dp, Color(0xFF00A63E), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_award),
            contentDescription = null,
            tint = Color(0xFF00A63E),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("봉사자 전용 카테고리", color = Color(0xFF00A63E), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(2.dp))
            Text(
                "이 카테고리는 봉사자 자격 보유자만 작성·신청할 수 있어요. 자격 신청을 먼저 해 주세요.",
                color = Brown700M,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF00A63E))
                .clickable { onApplyClick() }
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Text("자격 신청", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}