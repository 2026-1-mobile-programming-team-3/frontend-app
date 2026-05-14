package com.example.siheunggagae

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
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

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun MyScreen(onNavigate: (String) -> Unit = {}) {
    Scaffold(
        topBar = { MyTopBar() },
        bottomBar = { AppBottomBar(currentRoute = Screen.My.route, onNavigate = onNavigate) },
        containerColor = Gray95,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(12.dp))
            ProfileCard()
            Spacer(Modifier.height(12.dp))
            MyPetSection()
            Spacer(Modifier.height(12.dp))
            ActivityStatsRow()
            Spacer(Modifier.height(12.dp))
            VolunteerBadgeCard()
            Spacer(Modifier.height(12.dp))
            MyRecordsSection()
            Spacer(Modifier.height(12.dp))
            SettingsSection()
            Spacer(Modifier.height(20.dp))
            LogoutButton()
            Spacer(Modifier.height(20.dp))
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun MyTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "마이",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Gray10,
        )
        IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "설정",
                tint = Gray40,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

// ─── 프로필 카드 ───────────────────────────────────────────────────────────────

@Composable
private fun ProfileCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Pink90)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 원형 프로필 placeholder
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(Color(0xFFD4A574)),
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(42.dp),
            )
        }

        // 닉네임 + 위치
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "댕댕이주인",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Gray10,
            )
            Spacer(Modifier.height(5.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Gray40,
                    modifier = Modifier.size(14.dp),
                )
                Text(text = "정왕동", fontSize = 13.sp, color = Gray40)
            }
        }

        // 편집 아웃라인 버튼
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .border(1.dp, Brown40, RoundedCornerShape(50.dp))
                .clickable { }
                .padding(horizontal = 14.dp, vertical = 7.dp),
        ) {
            Text(text = "편집", fontSize = 13.sp, color = Brown40, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── 내 반려동물 섹션 ──────────────────────────────────────────────────────────

@Composable
private fun MyPetSection() {
    SectionCard {
        Text(
            text = "내 반려동물",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Gray10,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )
        HorizontalDivider(color = Gray90)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF3E0)),
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = Orange40,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "파댕이", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Gray10)
                Spacer(Modifier.height(2.dp))
                Text(text = "강아지 · 3살 · 수컷", fontSize = 13.sp, color = Gray40)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Gray90)
                    .clickable { }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(text = "전체 보기", fontSize = 12.sp, color = Gray40)
            }
        }
    }
}

// ─── 활동 통계 Row ─────────────────────────────────────────────────────────────

@Composable
private fun ActivityStatsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatCard(label = "내 요청", value = "3", valueColor = Color(0xFFE84B6A), modifier = Modifier.weight(1f))
        StatCard(label = "봉사 참여", value = "2", valueColor = WalkGood, modifier = Modifier.weight(1f))
        StatCard(label = "즐겨찾기", value = "5", valueColor = Orange40, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable { }
            .padding(vertical = 16.dp),
    ) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Spacer(Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, color = Gray40)
    }
}

// ─── 봉사 뱃지 카드 ────────────────────────────────────────────────────────────

private data class BadgeInfo(
    val emoji: String,
    val label: String,
    val subLabel: String,
    val achieved: Boolean,
    val bgColor: Color,
)

private val myBadges = listOf(
    BadgeInfo("🌱", "새싹", "달성", true, Color(0xFFDCFCE7)),
    BadgeInfo("🌸", "꽃", "3건 필요", false, Color(0xFFFEF3E2)),
    BadgeInfo("🍎", "열매", "8건 필요", false, Color(0xFFFFF0F3)),
    BadgeInfo("🌳", "나무", "15건 필요", false, Color(0xFFF3F4F6)),
)

@Composable
private fun VolunteerBadgeCard() {
    SectionCard {
        Text(
            text = "봉사 뱃지",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Gray10,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )
        HorizontalDivider(color = Gray90)

        Column(modifier = Modifier.padding(16.dp)) {
            // 봉사 등급 + 전체 보기
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "🌱 봉사 등급", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Gray10)
                Text(
                    text = "전체 보기 >",
                    fontSize = 13.sp,
                    color = Gray40,
                    modifier = Modifier.clickable { },
                )
            }

            Spacer(Modifier.height(12.dp))

            // 현재 등급 + 목표
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "새싹 등급 · 누적 2건",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Gray10,
                )
                Text(text = "꽃까지 3건", fontSize = 12.sp, color = Orange40)
            }

            Spacer(Modifier.height(8.dp))

            // 진행 바 (2건 / 5건 = 0.4)
            LinearProgressIndicator(
                progress = { 0.4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Orange40,
                trackColor = Color(0xFFF3F4F6),
                strokeCap = StrokeCap.Round,
            )

            Spacer(Modifier.height(18.dp))

            // 뱃지 4개
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                myBadges.forEach { badge ->
                    BadgeItem(badge = badge, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(badge: BadgeInfo, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(badge.bgColor),
        ) {
            Text(text = badge.emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = badge.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (badge.achieved) Gray10 else Gray40,
        )
        Text(
            text = badge.subLabel,
            fontSize = 10.sp,
            color = if (badge.achieved) WalkGood else Gray80,
        )
    }
}

// ─── 내 기록 섹션 ──────────────────────────────────────────────────────────────

@Composable
private fun MyRecordsSection() {
    SectionCard {
        SimpleListItem(title = "봉사 활동 이력", showDivider = true)
        SimpleListItem(title = "즐겨찾기 매장", showDivider = false)
    }
}

// ─── 설정 섹션 ─────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection() {
    SectionCard {
        SettingsItem(title = "알림 설정")
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Gray90)
        SettingsItem(title = "지역 설정", subtitle = "정왕동")
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Gray90)
        SettingsItem(title = "개인정보 및 보안")
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Gray90)
        SettingsItem(title = "앱 정보", subtitle = "v3.0.0")
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Gray90)
        SettingsItem(title = "봉사자 자격 신청", titleColor = WalkGood)
    }
}

// ─── 공통 컴포넌트 ─────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White),
    ) {
        content()
    }
}

@Composable
private fun SimpleListItem(title: String, showDivider: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, fontSize = 14.sp, color = Gray10)
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Gray80,
            modifier = Modifier.size(18.dp),
        )
    }
    if (showDivider) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Gray90)
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    titleColor: Color = Gray10,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = title, fontSize = 14.sp, color = titleColor)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 12.sp, color = Gray40)
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Gray80,
            modifier = Modifier.size(18.dp),
        )
    }
}

// ─── 로그아웃 버튼 ─────────────────────────────────────────────────────────────

@Composable
private fun LogoutButton() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(50.dp))
            .border(1.dp, Gray80, RoundedCornerShape(50.dp))
            .clickable { }
            .padding(vertical = 14.dp),
    ) {
        Text(text = "로그아웃", fontSize = 15.sp, color = Gray40, fontWeight = FontWeight.Medium)
    }
}


// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyScreenPreview() {
    SiheungGagaeTheme { MyScreen() }
}
