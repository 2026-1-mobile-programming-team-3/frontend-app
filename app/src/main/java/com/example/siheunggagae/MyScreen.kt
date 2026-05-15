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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// 스펙 컬러
private val Brown900My   = Color(0xFF614B3A)
private val Brown700My   = Color(0xFF8A6E58)
private val Brown400My   = Color(0xFFC4A882)
private val BrownBorderY = Color(0xFFE8D3C2)
private val Orange500My  = Color(0xFFF7A35B)
private val Pink500My    = Color(0xFFF04268)
private val Green500My   = Color(0xFF00A63E)
private val PinkSurface  = Color(0xFFFEE7EC)
private val Background9  = Color(0xFFFEFEFE)
private val Divider9     = Color(0xFFE8E8E8)
private val TextBlack    = Color(0xFF1E120A)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun MyScreen(
    onNavigate: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onPetListClick: () -> Unit = {},
    onVolunteerApplyClick: () -> Unit = {},
) {
    Scaffold(
        topBar = { MyTopBar(onSettingsClick = onSettingsClick) },
        bottomBar = { AppBottomBar(currentRoute = Screen.My.route, onNavigate = onNavigate) },
        containerColor = Background9,
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
            MyPetSection(onPetListClick = onPetListClick)
            Spacer(Modifier.height(12.dp))
            MySectionLabel("활동")
            Spacer(Modifier.height(6.dp))
            ActivityStatsRow()
            Spacer(Modifier.height(12.dp))
            MySectionLabel("봉사 뱃지", fontSize = 18)
            Spacer(Modifier.height(6.dp))
            VolunteerBadgeCard()
            Spacer(Modifier.height(12.dp))
            MySectionLabel("내 기록")
            Spacer(Modifier.height(6.dp))
            MyRecordsSection()
            Spacer(Modifier.height(12.dp))
            MySectionLabel("설정")
            Spacer(Modifier.height(6.dp))
            SettingsSection(onVolunteerApplyClick = onVolunteerApplyClick)
            Spacer(Modifier.height(16.dp))
            LogoutButton()
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun MyTopBar(onSettingsClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "마이",
            fontFamily = PretendardFamily,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlack,
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clickable { onSettingsClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "설정",
                tint = Brown700My,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

// ─── 프로필 Card ───────────────────────────────────────────────────────────────

@Composable
private fun ProfileCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PinkSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFD4A574)),
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "댕댕이주인",
                fontFamily = PretendardFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp,
                color = TextBlack
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(Icons.Default.LocationOn, null, tint = Brown700My, modifier = Modifier.size(14.dp))
                Text(
                    text = "정왕동",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    color = Brown700My
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFFEFEFE))
                .clickable { }
                .padding(horizontal = 13.dp, vertical = 6.dp),
        ) {
            Text(
                text = "편집",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp,
                color = Brown700My,
            )
        }
    }
}

// ─── 내 반려동물 ───────────────────────────────────────────────────────────────

@Composable
private fun MyPetSection(onPetListClick: () -> Unit = {}) {
    SectionCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "내 반려동물",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp,
                color = Brown700My,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF3E0)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = Orange500My,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "파댕이",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        color = TextBlack
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "강아지 · 3살 · 수컷",
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = Brown700My
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Divider9)
                        .clickable { onPetListClick() }
                        .padding(horizontal = 14.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = "전체 보기",
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp,
                        color = Brown700My
                    )
                }
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard("내 요청",  "3", Pink500My,    Modifier.weight(1f))
        StatCard("봉사 참여", "2", Green500My,   Modifier.weight(1f))
        StatCard("즐겨찾기", "5", Orange500My,  Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { }
            .padding(vertical = 16.dp),
    ) {
        Text(
            text = value,
            fontFamily = PretendardFamily,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp,
            color = valueColor
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp,
            color = Brown700My
        )
    }
}

// ─── 봉사 뱃지 Card ────────────────────────────────────────────────────────────

private data class BadgeInfo(
    val emoji: String,
    val label: String,
    val subLabel: String,
    val achieved: Boolean,
    val bgColor: Color,
)

private val myBadges = listOf(
    BadgeInfo("", "새싹", "달성",     true,  Green500My),
    BadgeInfo("", "꽃",  "3건 필요",  false, Color(0xFFF7A35B)),
    BadgeInfo("", "열매", "8건 필요", false, Color(0xFFF04268)),
    BadgeInfo("", "나무", "15건 필요", false, Color(0xFF8A6E58)),
)

@Composable
private fun VolunteerBadgeCard() {
    SectionCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "봉사 등급",
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = TextBlack,
                )
                Text(
                    text = "전체 보기 >",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    color = Brown700My,
                    modifier = Modifier.clickable { },
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "새싹 등급 · 누적 2건",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    color = Brown700My
                )
                Text(
                    text = "꽃까지 3건",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp,
                    color = Brown700My
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 2f / 5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Orange500My,
                trackColor = Color(0xFFE5E7EB),
                strokeCap = StrokeCap.Round,
            )
            Spacer(Modifier.height(20.dp))
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
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(badge.bgColor),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = badge.label,
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 16.sp,
            color = TextBlack,
        )
        Text(
            text = badge.subLabel,
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp,
            color = if (badge.achieved) Green500My else Brown700My,
        )
    }
}

// ─── 내 기록 ───────────────────────────────────────────────────────────────────

@Composable
private fun MyRecordsSection() {
    SectionCard {
        RecordItem(
            icon = Icons.Default.VolunteerActivism,
            iconTint = Green500My,
            title = "봉사 활동 이력",
            showDivider = true,
        )
        RecordItem(
            icon = Icons.Default.Favorite,
            iconTint = Pink500My,
            title = "즐겨찾기 매장",
            showDivider = false,
        )
    }
}

@Composable
private fun RecordItem(icon: ImageVector, iconTint: Color, title: String, showDivider: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Text(
                text = title,
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
                color = TextBlack
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Brown400My,
            modifier = Modifier.size(20.dp),
        )
    }
    if (showDivider) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Divider9)
}

// ─── 설정 섹션 ─────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(onVolunteerApplyClick: () -> Unit = {}) {
    SectionCard {
        SettingsItem(title = "알림 설정")
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Divider9)
        SettingsItem(title = "지역 설정", subtitle = "정왕동")
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Divider9)
        SettingsItem(title = "개인정보 및 보안")
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Divider9)
        SettingsItem(title = "앱 정보", subtitle = "v3.0.0")
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Divider9)
        SettingsItem(title = "봉사자 자격 신청", titleColor = Green500My, onClick = onVolunteerApplyClick)
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    titleColor: Color = TextBlack,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = title,
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
                color = titleColor
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Orange500My
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Brown400My,
            modifier = Modifier.size(20.dp),
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
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Brown700My, RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { }
            .padding(vertical = 16.dp),
    ) {
        Text(
            text = "로그아웃",
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp,
            color = Brown700My
        )
    }
}

// ─── 공통: 섹션 라벨 ───────────────────────────────────────────────────────────

@Composable
private fun MySectionLabel(label: String, fontSize: Int = 12) {
    Text(
        text = label,
        fontFamily = PretendardFamily,
        fontSize = fontSize.sp,
        fontWeight = if (fontSize == 12) FontWeight.Bold else FontWeight.Bold,
        lineHeight = if (fontSize == 12) 16.sp else 27.sp,
        color = if (fontSize == 12) Brown700My else TextBlack,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

// ─── 공통: 섹션 카드 래퍼 ─────────────────────────────────────────────────────

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
    ) {
        content()
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyScreenPreview() {
    SiheungGagaeTheme { MyScreen() }
}
