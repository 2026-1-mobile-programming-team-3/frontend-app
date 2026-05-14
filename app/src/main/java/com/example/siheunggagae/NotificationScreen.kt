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
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pets
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
import com.example.siheunggagae.ui.theme.Gray90
import com.example.siheunggagae.ui.theme.Gray95
import com.example.siheunggagae.ui.theme.Orange40
import com.example.siheunggagae.ui.theme.Pink90
import com.example.siheunggagae.ui.theme.WalkGood
import com.example.siheunggagae.ui.theme.WalkNormal

// ─── 데이터 ────────────────────────────────────────────────────────────────────

enum class NotiCategory(val label: String) {
    ALL("전체"), MATCHING("매칭"), NEWS("소식"), SYSTEM("시스템")
}

data class NotificationItem(
    val id: Int,
    val title: String,
    val content: String,
    val time: String,
    val isRead: Boolean,
    val category: NotiCategory,
    val icon: ImageVector,
    val iconColor: Color,
)

private val sampleNotifications = listOf(
    NotificationItem(
        id = 1,
        title = "매칭 신청 수락",
        content = "'정왕동 실외견 검진 이동' 요청이 수락되었습니다.",
        time = "방금 전",
        isRead = false,
        category = NotiCategory.MATCHING,
        icon = Icons.Default.CheckCircle,
        iconColor = WalkGood,
    ),
    NotificationItem(
        id = 2,
        title = "봉사 완료 인증",
        content = "봉사 활동이 인증되어 새로운 뱃지를 획득했습니다.",
        time = "2시간 전",
        isRead = true,
        category = NotiCategory.SYSTEM,
        icon = Icons.Default.EmojiEvents,
        iconColor = Orange40,
    ),
    NotificationItem(
        id = 3,
        title = "새로운 소식 등록",
        content = "'2026년 실외 사육견 중성화 수술비 지원'을 확인해 보세요.",
        time = "5시간 전",
        isRead = true,
        category = NotiCategory.NEWS,
        icon = Icons.AutoMirrored.Filled.Article,
        iconColor = WalkNormal,
    ),
    NotificationItem(
        id = 4,
        title = "파댕이 등록!",
        content = "새로운 반려동물을 추가하셨습니다.",
        time = "1일 전",
        isRead = false,
        category = NotiCategory.SYSTEM,
        icon = Icons.Default.Pets,
        iconColor = Brown40,
    ),
    NotificationItem(
        id = 5,
        title = "매칭 신청 수락",
        content = "'중성화 수술 이동 도와주세요' 요청이 수락되었습니다.",
        time = "3일 전",
        isRead = true,
        category = NotiCategory.MATCHING,
        icon = Icons.Default.CheckCircle,
        iconColor = WalkGood,
    ),
)

// ─── 화면 ──────────────────────────────────────────────────────────────────────

@Composable
fun NotificationScreen(onBack: () -> Unit = {}) {
    var selectedCategory by remember { mutableStateOf(NotiCategory.ALL) }
    val notifications = remember { mutableStateListOf(*sampleNotifications.toTypedArray()) }

    val filtered = if (selectedCategory == NotiCategory.ALL) notifications
    else notifications.filter { it.category == selectedCategory }

    Scaffold(
        containerColor = Gray95,
        topBar = {
            NotificationTopBar(
                onBack = onBack,
                onMarkAllRead = {
                    val indices = notifications.indices.filter { !notifications[it].isRead }
                    indices.forEach { notifications[it] = notifications[it].copy(isRead = true) }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            NotiFilterChipRow(
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )
            HorizontalDivider(color = Gray90)
            if (filtered.isEmpty()) {
                EmptyNotification()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered, key = { it.id }) { item ->
                        NotificationItemRow(
                            item = item,
                            onClick = {
                                val idx = notifications.indexOfFirst { it.id == item.id }
                                if (idx >= 0) notifications[idx] = notifications[idx].copy(isRead = true)
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = Gray90
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
            .background(Color.White)
            .padding(horizontal = 4.dp, vertical = 14.dp)
    ) {
        Text(
            text = "< 뒤로",
            fontSize = 14.sp,
            color = Gray40,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable { onBack() }
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Text(
            text = "알림",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Gray10,
            modifier = Modifier.align(Alignment.Center)
        )
        Text(
            text = "모두 읽음",
            fontSize = 14.sp,
            color = Brown40,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable { onMarkAllRead() }
                .padding(horizontal = 16.dp, vertical = 4.dp)
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
            val isSelected = category == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Gray10 else Gray90)
                    .clickable { onSelect(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category.label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Gray40
                )
            }
        }
    }
}

// ─── 알림 아이템 ───────────────────────────────────────────────────────────────

@Composable
private fun NotificationItemRow(item: NotificationItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (item.isRead) Color.White else Pink90)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 원형 아이콘
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(item.iconColor.copy(alpha = 0.12f))
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        // 텍스트 영역
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray10
                    )
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE91E63))
                        )
                    }
                }
                Text(
                    text = item.time,
                    fontSize = 12.sp,
                    color = Gray40
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.content,
                fontSize = 13.sp,
                color = Gray40,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
        }
    }
}

// ─── 빈 상태 ───────────────────────────────────────────────────────────────────

@Composable
private fun EmptyNotification() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PriorityHigh,
                contentDescription = null,
                tint = Gray90,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "알림이 없습니다", fontSize = 15.sp, color = Gray40)
        }
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NotificationScreenPreview() {
    com.example.siheunggagae.ui.theme.SiheungGagaeTheme {
        NotificationScreen()
    }
}
