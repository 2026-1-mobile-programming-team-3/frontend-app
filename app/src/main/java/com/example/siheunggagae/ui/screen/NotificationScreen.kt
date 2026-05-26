package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.data.model.NotificationCategory
import com.example.siheunggagae.data.model.NotificationItem
import com.example.siheunggagae.ui.viewmodel.NotiTab
import com.example.siheunggagae.ui.viewmodel.NotificationState
import com.example.siheunggagae.ui.viewmodel.NotificationViewModel

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.R
import com.example.siheunggagae.ui.component.EmptyStateView
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// 스펙 컬러
private val Brown900N    = Color(0xFF614B3A)
private val Brown700N    = Color(0xFF8A6E58)
private val Brown400N    = Color(0xFFC4A882)
private val BrownBorderN = Color(0xFFE8D3C2)
private val Orange500N   = Color(0xFFF7A35B)
private val Pink500N     = Color(0xFFF04268)
private val Green500N    = Color(0xFF00A63E)
private val PinkSurfaceN = Color(0xFFFEE7EC)
private val Background95 = Color(0xFFFEFEFE)
private val TextBlack    = Color(0xFF1E120A)
private val SelectBgN    = Color(0xFFF5EDE8)  // 선택된 항목 배경

private val NotificationCategory.iconColor
    get() = when (this) {
        NotificationCategory.MATCH    -> Pink500N
        NotificationCategory.VOLUNTEER,
        NotificationCategory.REVIEW   -> Orange500N
        NotificationCategory.NEWS,
        NotificationCategory.POLICY   -> Orange500N
        NotificationCategory.SYSTEM   -> Green500N
    }

private fun formatRelativeTime(createdAt: String): String {
    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).also { it.timeZone = TimeZone.getTimeZone("UTC") },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).also { it.timeZone = TimeZone.getTimeZone("UTC") },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
    )
    val date = formats.firstNotNullOfOrNull { runCatching { it.parse(createdAt) }.getOrNull() }
        ?: return createdAt
    val diffMin = (System.currentTimeMillis() - date.time) / 60_000
    return when {
        diffMin < 1    -> "방금 전"
        diffMin < 60   -> "${diffMin}분 전"
        diffMin < 1440 -> "${diffMin / 60}시간 전"
        diffMin < 43200 -> "${diffMin / 1440}일 전"
        else           -> "${diffMin / 43200}개월 전"
    }
}

// ─── 화면 ──────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel? = null,
    onBack: () -> Unit = {},
    onItemClick: (NotificationItem) -> Unit = {},
) {
    val state by remember(viewModel) {
        viewModel?.state ?: MutableStateFlow(NotificationState())
    }.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    // 삭제 완료 snackbar
    LaunchedEffect(state.deletedCount) {
        val count = state.deletedCount ?: return@LaunchedEffect
        snackbarHostState.showSnackbar("${count}개의 알림이 삭제되었습니다!")
        viewModel?.clearDeletedMessage()
    }

    // 하드웨어 뒤로가기 → 선택 모드 해제
    BackHandler(enabled = state.isSelectMode) {
        viewModel?.exitSelectMode()
    }

    val displayItems = remember(state.items, state.selectedTab) {
        when (state.selectedTab) {
            NotiTab.ALL      -> state.items
            NotiTab.MATCHING -> state.items.filter {
                it.category == NotificationCategory.VOLUNTEER ||
                it.category == NotificationCategory.MATCH ||
                it.category == NotificationCategory.REVIEW
            }
            NotiTab.SYSTEM   -> state.items.filter { it.category == NotificationCategory.SYSTEM }
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= total - 3 && total > 0
        }
            .distinctUntilChanged()
            .collect { nearEnd -> if (nearEnd) viewModel?.loadNextPage() }
    }

    Scaffold(
        containerColor = Background95,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            NotificationTopBar(
                isSelectMode = state.isSelectMode,
                selectedCount = state.selectedIds.size,
                onBack = {
                    if (state.isSelectMode) viewModel?.exitSelectMode() else onBack()
                },
                onMarkAllRead = { viewModel?.markAllRead() },
                onDelete = { viewModel?.deleteSelected() },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            AnimatedVisibility(visible = !state.isSelectMode, enter = fadeIn(), exit = fadeOut()) {
                NotiTabRow(
                    selected = state.selectedTab,
                    onSelect = { viewModel?.selectTab(it) },
                )
            }
            PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = { viewModel?.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.isLoading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Orange500N)
                    }
                    state.error != null && state.items.isEmpty() -> NotiErrorState(
                        message = state.error ?: "오류가 발생했습니다",
                        onRetry = { viewModel?.refresh() },
                    )
                    displayItems.isEmpty() -> EmptyNotification()
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(displayItems, key = { it.id }) { item ->
                                NotificationItemRow(
                                    item = item,
                                    isSelectMode = state.isSelectMode,
                                    isSelected = item.id in state.selectedIds,
                                    onClick = {
                                        if (state.isSelectMode) {
                                            viewModel?.toggleSelect(item.id)
                                        } else {
                                            if (!item.isRead) viewModel?.markRead(item.id)
                                            onItemClick(item)
                                        }
                                    },
                                    onLongClick = {
                                        if (!state.isSelectMode) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel?.enterSelectMode(item.id)
                                        }
                                    },
                                )
                            }
                            if (state.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(
                                            color = Orange500N,
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun NotificationTopBar(
    isSelectMode: Boolean,
    selectedCount: Int,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onBack() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = if (isSelectMode) "선택 취소" else "뒤로",
                tint = TextBlack,
                modifier = Modifier.size(22.dp),
            )
        }

        Text(
            text = if (isSelectMode) "${selectedCount}개 선택됨" else "알림",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlack,
            modifier = Modifier.align(Alignment.Center),
        )

        if (isSelectMode) {
            Text(
                text = "삭제",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp,
                color = if (selectedCount > 0) Pink500N else Brown400N,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(enabled = selectedCount > 0) { onDelete() }
                    .padding(horizontal = 4.dp, vertical = 4.dp),
            )
        } else {
            Text(
                text = "모두 읽음",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp,
                color = Brown700N,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { onMarkAllRead() }
                    .padding(horizontal = 4.dp, vertical = 4.dp),
            )
        }
    }
}

// ─── 탭 필터 Row ───────────────────────────────────────────────────────────────

@Composable
private fun NotiTabRow(selected: NotiTab, onSelect: (NotiTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NotiTab.entries.forEach { tab ->
            val isSel = tab == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isSel) Brown900N else Color.White)
                    .then(
                        if (!isSel) Modifier.border(1.dp, BrownBorderN, RoundedCornerShape(50.dp))
                        else Modifier
                    )
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = tab.label,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    color = if (isSel) Color.White else Brown700N,
                )
            }
        }
    }
}

// ─── 알림 아이템 ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotificationItemRow(
    item: NotificationItem,
    isSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val bg = when {
        isSelected           -> SelectBgN
        !item.isRead         -> PinkSurfaceN
        else                 -> Color.White
    }
    val isRounded = isSelected || !item.isRead

    if (isRounded) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(bg)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            NotificationItemContent(item = item, isSelectMode = isSelectMode, isSelected = isSelected)
        }
    } else {
        Column(
            modifier = Modifier
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                NotificationItemContent(item = item, isSelectMode = isSelectMode, isSelected = isSelected)
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFFF3F4F6),
            )
        }
    }
}

@Composable
private fun NotificationItemContent(
    item: NotificationItem,
    isSelectMode: Boolean,
    isSelected: Boolean,
) {
    val relativeTime = remember(item.createdAt) { formatRelativeTime(item.createdAt) }
    val timeColor = if (relativeTime == "방금 전") Pink500N else Brown400N

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 선택 모드: 체크박스 표시
        if (isSelectMode) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
                    .clip(CircleShape)
                    .background(if (isSelected) Brown900N else Color.White)
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) Brown900N else Color(0xFFD1D5DB),
                        shape = CircleShape,
                    ),
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE5E7EB), CircleShape),
        ) {
            Icon(
                imageVector = Icons.Default.PriorityHigh,
                contentDescription = null,
                tint = item.category.iconColor,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(Pink500N),
                        )
                    }
                    Text(
                        text = item.title,
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = if (!item.isRead) FontWeight.ExtraBold else FontWeight.Medium,
                        lineHeight = 24.sp,
                        color = TextBlack,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = relativeTime,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = timeColor,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.body,
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = Brown700N,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ─── 빈 / 오류 상태 ────────────────────────────────────────────────────────────

@Composable
private fun EmptyNotification() {
    EmptyStateView(
        title = "아직 알림이 없어요",
        subtitle = "새로운 활동이 있으면 여기에 표시됩니다",
        iconRes = R.drawable.ic_notifications,
        iconTint = Color(0xFF8A6E58),
        iconBackground = Color(0xFFE8D3C2),
    )
}

@Composable
private fun NotiErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = message,
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                color = Brown700N,
                textAlign = TextAlign.Center,
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Orange500N)
                    .clickable { onRetry() }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text("다시 시도", fontFamily = PretendardFamily, fontSize = 14.sp, color = Color.White)
            }
        }
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NotificationScreenPreview() {
    SiheungGagaeTheme { NotificationScreen() }
}
