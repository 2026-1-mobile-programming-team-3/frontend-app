package com.example.siheunggagae

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

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

// ─── 데이터 ────────────────────────────────────────────────────────────────────

enum class NotiCategory(val label: String) {
    ALL("전체"), MATCHING("매칭"), NEWS("소식"), SYSTEM("시스템")
}

private val NotiCategory.iconColor get() = when (this) {
    NotiCategory.MATCHING -> Pink500N
    NotiCategory.NEWS     -> Orange500N
    NotiCategory.SYSTEM   -> Green500N
    NotiCategory.ALL      -> Brown700N
}

data class NotificationItem(
    val id: Int,
    val title: String,
    val content: String,
    val time: String,
    val isRead: Boolean,
    val category: NotiCategory,
)

private val sampleNotifications = listOf(
    NotificationItem(
        id = 1,
        title = "매칭 신청 수락",
        content = "'정왕동 실외견 검진 이동' 요청이 수락되었습니다.",
        time = "방금 전",
        isRead = false,
        category = NotiCategory.MATCHING,
    ),
    NotificationItem(
        id = 2,
        title = "봉사 완료 인증",
        content = "봉사 활동이 인증되어 새로운 뱃지를 획득했습니다.",
        time = "2시간 전",
        isRead = true,
        category = NotiCategory.SYSTEM,
    ),
    NotificationItem(
        id = 3,
        title = "새로운 소식 등록",
        content = "'2026년 실외 사육견 중성화 수술비 지원'을 확인해 보세요.",
        time = "5시간 전",
        isRead = true,
        category = NotiCategory.NEWS,
    ),
    NotificationItem(
        id = 4,
        title = "파댕이 등록!",
        content = "새로운 반려동물을 추가하셨습니다.",
        time = "1일 전",
        isRead = false,
        category = NotiCategory.SYSTEM,
    ),
    NotificationItem(
        id = 5,
        title = "매칭 신청 수락",
        content = "'중성화 수술 이동 도와주세요' 요청이 수락되었습니다.",
        time = "3일 전",
        isRead = true,
        category = NotiCategory.MATCHING,
    ),
)

// ─── 화면 ──────────────────────────────────────────────────────────────────────

@Composable
fun NotificationScreen(onBack: () -> Unit = {}) {
    var selectedCategory by remember { mutableStateOf(NotiCategory.ALL) }
    val notifications = remember { mutableStateListOf(*sampleNotifications.toTypedArray()) }

    val filtered = if (selectedCategory == NotiCategory.ALL) notifications.toList()
    else notifications.filter { it.category == selectedCategory }

    Scaffold(
        containerColor = Background95,
        topBar = {
            NotificationTopBar(
                onBack = onBack,
                onMarkAllRead = {
                    val updated = notifications.map { it.copy(isRead = true) }
                    notifications.clear()
                    notifications.addAll(updated)
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            NotiFilterChipRow(selected = selectedCategory, onSelect = { selectedCategory = it })
            if (filtered.isEmpty()) {
                EmptyNotification()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(filtered, key = { it.id }) { item ->
                        NotificationItemRow(
                            item = item,
                            onClick = {
                                val idx = notifications.indexOfFirst { it.id == item.id }
                                if (idx >= 0) notifications[idx] = notifications[idx].copy(isRead = true)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun NotificationTopBar(onBack: () -> Unit, onMarkAllRead: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onBack() }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "뒤로",
                tint = TextBlack,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = "알림",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlack,
            modifier = Modifier.align(Alignment.Center)
        )
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
                .padding(horizontal = 4.dp, vertical = 4.dp)
        )
    }
}

// ─── 필터 Chip Row ─────────────────────────────────────────────────────────────

@Composable
private fun NotiFilterChipRow(selected: NotiCategory, onSelect: (NotiCategory) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NotiCategory.entries.forEach { category ->
            val isSel = category == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isSel) Color(0xFF1A1A1A) else Color.White)
                    .then(if (!isSel) Modifier.border(1.dp, BrownBorderN, RoundedCornerShape(50.dp)) else Modifier)
                    .clickable { onSelect(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category.label,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    color = if (isSel) Color.White else Brown700N
                )
            }
        }
    }
}

// ─── 알림 아이템 ───────────────────────────────────────────────────────────────

@Composable
private fun NotificationItemRow(item: NotificationItem, onClick: () -> Unit) {
    if (!item.isRead) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PinkSurfaceN)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            NotificationItemContent(item = item)
        }
    } else {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable { onClick() }
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                NotificationItemContent(item = item)
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = Color(0xFFF3F4F6)
            )
        }
    }
}

@Composable
private fun NotificationItemContent(item: NotificationItem) {
    val iconColor = item.category.iconColor
    val timeColor = if (item.time == "방금 전") Pink500N else Brown400N

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE5E7EB), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.PriorityHigh,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.title,
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = TextBlack,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.time,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = timeColor
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.content,
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

// ─── 빈 상태 ───────────────────────────────────────────────────────────────────

@Composable
private fun EmptyNotification() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PriorityHigh,
                contentDescription = null,
                tint = Color(0xFFD1D5DB),
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "알림이 없습니다",
                fontFamily = PretendardFamily,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = Brown700N
            )
        }
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NotificationScreenPreview() {
    SiheungGagaeTheme { NotificationScreen() }
}
