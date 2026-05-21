package com.example.siheunggagae.ui.screen

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.AppBottomBar
import com.example.siheunggagae.R
import com.example.siheunggagae.Screen
import com.example.siheunggagae.data.model.MatchListItem
import com.example.siheunggagae.data.model.MatchStatus
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import com.example.siheunggagae.ui.util.bgColor
import com.example.siheunggagae.ui.util.textColor
import com.example.siheunggagae.ui.viewmodel.MatchingUiState
import com.example.siheunggagae.ui.viewmodel.MatchingViewModel

// 스펙 컬러
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

// ─── 데이터 ────────────────────────────────────────────────────────────────────

// 하드코딩된 숫자(count) 제거, API 상태값과 매칭
enum class MatchingTab(val label: String, val status: MatchStatus?) {
    ALL("전체", null),
    WAITING("모집중", MatchStatus.WAITING),
    MATCHING("검토중", MatchStatus.MATCHING),
    PROGRESS("진행중", MatchStatus.PROGRESS),
    DONE("완료", MatchStatus.DONE),
}

// ─── 화면 ──────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingScreen(
    viewModel: MatchingViewModel,
    onMyRequests: () -> Unit = {},
    onRequestFlowClick: () -> Unit = {},
    onCardClick: (requestId: Int) -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(MatchingTab.ALL) }

    // 팀원이 추가한 액션 시트 상태 (타입을 MatchListItem으로 변경)
    var showActionsSheet by remember { mutableStateOf(false) }
    var longPressedRequest by remember { mutableStateOf<MatchListItem?>(null) }
    val actionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 화면이 나타날 때마다 서버에서 최신 목록을 새로고침
    LaunchedEffect(Unit) {
        viewModel.fetchMatches(selectedTab.status?.name)
    }

    // 요청 건수 계산 로직
    val requestCount = if (uiState is MatchingUiState.Success) {
        (uiState as MatchingUiState.Success).matches.size
    } else 0

    Scaffold(
        containerColor = Background95,
        topBar = { MatchingTopBar(onMyRequests = onMyRequests) },
        bottomBar = { AppBottomBar(currentRoute = Screen.Matching.route, onNavigate = onNavigate) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRequestFlowClick,
                containerColor = FABBrown,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(painter = painterResource(R.drawable.ic_add), contentDescription = "새 요청", modifier = Modifier.size(24.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            item { SummaryCards(onMyRequests = onMyRequests, requestCount = requestCount) }

            item {
                MatchingTabRow(selected = selectedTab, onSelect = { tab ->
                    selectedTab = tab
                    viewModel.fetchMatches(tab.status?.name)
                })
            }
            item { RequestListHeader(onMapViewClick = { onNavigate("map?volunteerMode=true") }) }
            item { Spacer(Modifier.height(12.dp)) }

            // 🔥 서버 데이터 상태에 따라 화면 그리기 (질문자님 기능)
            when (val state = uiState) {
                is MatchingUiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Orange500M)
                        }
                    }
                }
                is MatchingUiState.Error -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(text = state.message, color = Pink500M, fontFamily = PretendardFamily)
                        }
                    }
                }
                is MatchingUiState.Success -> {
                    if (state.matches.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(text = "조건에 맞는 봉사 요청이 없습니다.", color = Brown700M, fontFamily = PretendardFamily)
                            }
                        }
                    } else {
                        items(state.matches, key = { it.matchId ?: it.hashCode() }) { request ->
                            MatchingRequestCard(
                                request = request,
                                onCardClick = { request.matchId?.let { onCardClick(it) } },
                                // 팀원의 롱클릭 기능 연동
                                onCardLongClick = {
                                    longPressedRequest = request
                                    showActionsSheet = true
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) } // 하단 여백 추가
        }
    }

    // 팀원이 추가한 액션 시트
    if (showActionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionsSheet = false },
            sheetState = actionsSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            CardActionsSheetContent(
                request = longPressedRequest,
                onDismiss = { showActionsSheet = false },
            )
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────
@Composable
private fun MatchingTopBar(onMyRequests: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "매칭", fontFamily = PretendardFamily, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp, color = TextBlack)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).clickable { onMyRequests() }
        ) {
            Icon(painter = painterResource(R.drawable.ic_assignment), contentDescription = "내 봉사 요청 목록", tint = Brown700M, modifier = Modifier.size(22.dp))
        }
    }
}

// ─── 요약 카드 2열 ─────────────────────────────────────────────────────────────
@Composable
private fun SummaryCards(onMyRequests: () -> Unit, requestCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f).clickable { onMyRequests() },
            bgColor = PinkSurfaceM,
            iconRes = R.drawable.ic_assignment,
            iconColor = Pink500M,
            label = "내 요청",
            value = "${requestCount}건 검토 중"
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            bgColor = Color(0xFFF0FDF4),
            iconRes = R.drawable.ic_favorite,
            iconColor = Green500M,
            label = "봉사 활동",
            value = "1건 진행 중"
        )
    }
}

@Composable
private fun SummaryCard(modifier: Modifier, bgColor: Color, iconRes: Int, iconColor: Color, label: String, value: String) {
    Column(modifier = modifier.clip(RoundedCornerShape(14.dp)).background(bgColor).padding(horizontal = 16.dp, vertical = 14.dp)) {
        Icon(painter = painterResource(iconRes), contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(8.dp))
        Text(text = label, fontFamily = PretendardFamily, fontSize = 12.sp, lineHeight = 16.sp, color = Brown700M)
        Spacer(Modifier.height(2.dp))
        Text(text = value, fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp, color = TextBlack)
    }
}

// ─── 탭 Row ────────────────────────────────────────────────────────────────────
@Composable
private fun MatchingTabRow(selected: MatchingTab, onSelect: (MatchingTab) -> Unit) {
    val tabs = MatchingTab.entries
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        tabs.forEachIndexed { idx, tab ->
            val isSel = tab == selected
            Row(
                modifier = Modifier.clickable { onSelect(tab) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(text = tab.label, fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, lineHeight = 20.sp, color = if (isSel) TextBlack else Brown400M)
            }
            if (idx < tabs.lastIndex) {
                Text(text = "  ·  ", fontFamily = PretendardFamily, fontSize = 14.sp, color = Brown400M)
            }
        }
    }
}

// ─── 리스트 헤더 ───────────────────────────────────────────────────────────────
@Composable
private fun RequestListHeader(onMapViewClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "이동 지원 요청", fontFamily = PretendardFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp, color = TextBlack)
        Row(
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.clickable { onMapViewClick() }
        ) {
            Icon(painter = painterResource(R.drawable.ic_map), contentDescription = null, tint = Color(0xFFF7A35B), modifier = Modifier.size(16.dp))
            Text(text = "지도 보기", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF7A35B))
        }
    }
}

// ─── 요청 카드 ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MatchingRequestCard(
    request: MatchListItem,
    onCardClick: () -> Unit = {},
    onCardLongClick: () -> Unit = {}
) {
    // 서버에서 온 String 상태값을 앱 내 Enum 상태값으로 매핑
    val matchStatus = MatchStatus.entries.find { it.name == request.status } ?: MatchStatus.WAITING

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .combinedClickable(
                onClick = { onCardClick() },
                onLongClick = { onCardLongClick() }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            // 1행: 상태 칩 + 생성일(또는 희망일)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChipM(status = matchStatus)
                if (request.createdAt != null) {
                    Text(text = request.createdAt.take(10), fontFamily = PretendardFamily, fontSize = 14.sp, color = Brown700M)
                }
            }
            Spacer(Modifier.height(8.dp))
            // 2행: 제목
            Text(
                text = request.title ?: "제목 없음",
                fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp, color = TextBlack,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            // 3행: 지역·희망일
            val subInfo = listOfNotNull(request.address?.split(" ")?.getOrNull(2) ?: request.address, request.desiredDate?.take(10)).joinToString(" · ")
            if (subInfo.isNotEmpty()) {
                Spacer(Modifier.height(5.dp))
                Text(text = subInfo, fontFamily = PretendardFamily, fontSize = 13.sp, lineHeight = 18.sp, color = Brown700M)
            }
            // 4행: 신청수
            if (request.applicationsCount != null && request.applicationsCount > 0) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "신청 ${request.applicationsCount}건", fontFamily = PretendardFamily, fontSize = 12.sp, lineHeight = 16.sp, color = Brown700M)
                }
            }
        }
    }
}

@Composable
private fun StatusChipM(status: MatchStatus) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(status.bgColor).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = status.label, fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp, color = status.textColor)
    }
}

@Composable
private fun PetTypeChipM(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .border(1.dp, BrownBorderM, RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Brown700M
        )
    }
}

// ─── 카드 액션 시트 ────────────────────────────────────────────────────────────

@Composable
private fun CardActionsSheetContent(
    request: MatchListItem?,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
    ) {
        if (request != null) {
            Text(
                text = request.title ?: "제목 없음",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = Brown700M,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }
        HorizontalDivider(color = Color(0xFFE8E8E8))

        CardActionRow(
            icon = Icons.Default.BookmarkBorder,
            iconBg = Color(0xFFFFF3E0),
            iconTint = Orange500M,
            label = "북마크 추가",
            labelColor = TextBlack,
            onClick = onDismiss,
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFE8E8E8))

        CardActionRow(
            icon = Icons.Default.Share,
            iconBg = Color(0xFFEFF6FF),
            iconTint = Color(0xFF388AF5),
            label = "공유하기",
            labelColor = TextBlack,
            onClick = onDismiss,
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFE8E8E8))

        CardActionRow(
            icon = Icons.Default.Flag,
            iconBg = Color(0xFFFEE7EC),
            iconTint = Pink500M,
            label = "신고하기",
            labelColor = Pink500M,
            onClick = onDismiss,
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFE8E8E8))

        CardActionRow(
            icon = Icons.Default.VisibilityOff,
            iconBg = Color(0xFFF3F4F6),
            iconTint = Color(0xFF6B7280),
            label = "게시글 숨기기",
            labelColor = TextBlack,
            onClick = onDismiss,
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFE8E8E8))

        CardActionRow(
            icon = Icons.Default.Block,
            iconBg = Color(0xFFFEE7EC),
            iconTint = Pink500M,
            label = "작성자 차단",
            labelColor = Pink500M,
            onClick = onDismiss,
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFE8E8E8))

        CardActionRow(
            icon = Icons.Default.Block,
            iconBg = Color(0xFFFEE7EC),
            iconTint = Pink500M,
            label = "게시글 숨기기 + 작성자 차단",
            labelColor = Pink500M,
            onClick = onDismiss,
        )
    }
}

@Composable
private fun CardActionRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    labelColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 24.sp,
            color = labelColor,
        )
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MatchingScreenPreview() {
    SiheungGagaeTheme {
        // Preview용 컴포저블은 ViewModel이 필요하여 주석처리 혹은 Mock 데이터를 넘기도록 작성해야 합니다.
        // MatchingScreen(viewModel = ...)
    }
}