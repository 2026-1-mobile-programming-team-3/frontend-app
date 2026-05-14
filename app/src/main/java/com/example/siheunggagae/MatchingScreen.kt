package com.example.siheunggagae

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.Brown40
import com.example.siheunggagae.ui.theme.Gray10
import com.example.siheunggagae.ui.theme.Gray40
import com.example.siheunggagae.ui.theme.Gray80
import com.example.siheunggagae.ui.theme.Gray90
import com.example.siheunggagae.ui.theme.Gray95
import com.example.siheunggagae.ui.theme.Orange40
import com.example.siheunggagae.ui.theme.Pink90
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import com.example.siheunggagae.ui.theme.WalkGood
import com.example.siheunggagae.ui.theme.WalkNormal

// ─── 데이터 ────────────────────────────────────────────────────────────────────

enum class MatchingStatus(val label: String) {
    OPEN("모집중"), REVIEWING("검토중"), IN_PROGRESS("진행중"), COMPLETED("완료")
}

private val MatchingStatus.color get() = when (this) {
    MatchingStatus.OPEN        -> WalkGood
    MatchingStatus.REVIEWING   -> Orange40
    MatchingStatus.IN_PROGRESS -> WalkNormal
    MatchingStatus.COMPLETED   -> Gray40
}

private val MatchingStatus.bgColor get() = when (this) {
    MatchingStatus.OPEN        -> Color(0xFFECFDF5)
    MatchingStatus.REVIEWING   -> Color(0xFFFFF3E0)
    MatchingStatus.IN_PROGRESS -> Color(0xFFEFF6FF)
    MatchingStatus.COMPLETED   -> Gray90
}

enum class MatchingTab(val label: String, val count: Int?) {
    ALL("전체", 8), OPEN("모집중", 5), REVIEWING("검토중", 2),
    IN_PROGRESS("진행중", 1), COMPLETED("완료", null)
}

data class MatchingRequest(
    val id: Int,
    val status: MatchingStatus,
    val dDay: String?,
    val title: String,
    val location: String? = null,
    val distance: String? = null,
    val date: String? = null,
    val petTypes: List<String> = emptyList(),
    val applicationCount: Int? = null,
)

private val sampleRequests = listOf(
    MatchingRequest(
        id = 1, status = MatchingStatus.OPEN, dDay = "D-2",
        title = "정왕동 실외견 병원 이동 부탁드립니다",
        location = "정왕동", distance = "0.8km", date = "5월 10일",
        petTypes = listOf("강아지"), applicationCount = 2
    ),
    MatchingRequest(
        id = 2, status = MatchingStatus.OPEN, dDay = "D-4",
        title = "배곧 동물병원 중성화 수술 이동 도와주세요",
        location = "배곧신도시", distance = "2.3km", date = "5월 12일",
        petTypes = listOf("고양이"), applicationCount = 2
    ),
    MatchingRequest(
        id = 3, status = MatchingStatus.REVIEWING, dDay = null,
        title = "목감동 > 시흥시청 병원 이동 지원 요청"
    ),
    MatchingRequest(
        id = 4, status = MatchingStatus.IN_PROGRESS, dDay = null,
        title = "정왕동 실외견 검진 이동",
        location = "정왕동", distance = "1.2km", date = "5월 6일",
        petTypes = listOf("강아지")
    ),
)

// ─── 화면 ──────────────────────────────────────────────────────────────────────

private val FabColor = Color(0xFF3E2A1A)

@Composable
fun MatchingScreen(
    onMyRequests: () -> Unit = {},
    onRequestFlowClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf(MatchingTab.ALL) }

    val filtered = when (selectedTab) {
        MatchingTab.ALL         -> sampleRequests
        MatchingTab.OPEN        -> sampleRequests.filter { it.status == MatchingStatus.OPEN }
        MatchingTab.REVIEWING   -> sampleRequests.filter { it.status == MatchingStatus.REVIEWING }
        MatchingTab.IN_PROGRESS -> sampleRequests.filter { it.status == MatchingStatus.IN_PROGRESS }
        MatchingTab.COMPLETED   -> sampleRequests.filter { it.status == MatchingStatus.COMPLETED }
    }

    Scaffold(
        containerColor = Gray95,
        topBar = { MatchingTopBar(onMyRequests = onMyRequests) },
        bottomBar = { AppBottomBar(currentRoute = Screen.Matching.route, onNavigate = onNavigate) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRequestFlowClick,
                containerColor = FabColor,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "새 요청", modifier = Modifier.size(24.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item { SummaryCards() }
            item {
                MatchingTabRow(selected = selectedTab, onSelect = { selectedTab = it })
            }
            item {
                RequestListHeader()
                HorizontalDivider(color = Gray90)
            }
            items(filtered, key = { it.id }) { request ->
                MatchingRequestCard(request = request)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Gray90
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun MatchingTopBar(onMyRequests: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "매칭",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Brown40
        )
        IconButton(onClick = onMyRequests, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Assignment,
                contentDescription = "내 봉사 요청 목록",
                tint = Gray40,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ─── 요약 카드 2열 ─────────────────────────────────────────────────────────────

@Composable
private fun SummaryCards() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            bgColor = Pink90,
            icon = Icons.AutoMirrored.Filled.Assignment,
            iconColor = Color(0xFFE91E63),
            label = "내 요청",
            value = "2건 검토 중"
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            bgColor = Color(0xFFECFDF5),
            icon = Icons.Default.Favorite,
            iconColor = WalkGood,
            label = "봉사 활동",
            value = "1건 진행 중"
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    bgColor: Color,
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp, color = Gray40)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Gray10)
    }
}

// ─── 탭 Row ────────────────────────────────────────────────────────────────────

@Composable
private fun MatchingTabRow(selected: MatchingTab, onSelect: (MatchingTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
    ) {
        MatchingTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Column(
                modifier = Modifier
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = tab.label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Gray10 else Gray40
                    )
                    if (tab.count != null) {
                        Text(
                            text = "${tab.count}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Gray10 else Gray80
                        )
                    }
                }
                // 언더라인
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(if (isSelected) Gray10 else Color.Transparent)
                )
            }
        }
    }
    HorizontalDivider(color = Gray90)
}

// ─── 리스트 헤더 ───────────────────────────────────────────────────────────────

@Composable
private fun RequestListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "이동 지원 요청",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Gray10
        )
        Text(
            text = "🗺 지도 보기",
            fontSize = 14.sp,
            color = Orange40,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { }
        )
    }
}

// ─── 요청 카드 ─────────────────────────────────────────────────────────────────

@Composable
private fun MatchingRequestCard(request: MatchingRequest) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 상태 chip + D-day
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip(status = request.status)
            if (request.dDay != null) {
                Text(
                    text = "#${request.dDay}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE91E63)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 제목
        Text(
            text = request.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Gray10,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 22.sp
        )

        // 서브 정보 (지역·거리·날짜)
        val subInfo = listOfNotNull(request.location, request.distance, request.date)
            .joinToString(" · ")
        if (subInfo.isNotEmpty()) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = subInfo, fontSize = 13.sp, color = Gray40)
        }

        // 하단: 반려동물 chip + 신청 수
        if (request.petTypes.isNotEmpty() || request.applicationCount != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                request.petTypes.forEach { PetTypeChip(it) }
                if (request.applicationCount != null) {
                    Text(
                        text = "신청 ${request.applicationCount}건",
                        fontSize = 12.sp,
                        color = Gray40
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: MatchingStatus) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(status.bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = status.color
        )
    }
}

@Composable
private fun PetTypeChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Gray90)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = Gray40)
    }
}


// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MatchingScreenPreview() {
    SiheungGagaeTheme { MatchingScreen() }
}
