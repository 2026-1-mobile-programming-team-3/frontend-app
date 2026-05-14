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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BrownPrimary = Color(0xFF7B4F2E)
private val BrownText = Color(0xFFA0522D)
private val ScoreGreen = Color(0xFF22C55E)
private val ScoreBlue = Color(0xFF3B82F6)
private val ScoreOrange = Color(0xFFF97316)
private val PinkLight = Color(0xFFFFF0F3)
private val PinkAccent = Color(0xFFFF6B8A)
private val TextGray = Color(0xFF6B7280)
private val OrangeAccent = Color(0xFFF97316)
private val MapSky = Color(0xFFE0F7FA)
private val MapMint = Color(0xFFB2EBF2)

@Composable
fun HomeScreen(
    onNotificationClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    var selectedCategory by remember { mutableStateOf("전체") }

    Scaffold(
        containerColor = Color(0xFFF9F9F9),
        bottomBar = { AppBottomBar(currentRoute = Screen.Home.route, onNavigate = onNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            HomeTopBar(onNotificationClick = onNotificationClick)
            Spacer(modifier = Modifier.height(8.dp))
            WalkIndexSection()
            Spacer(modifier = Modifier.height(8.dp))
            NearbyStoresSection(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PetNewsSection()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ─── TopBar ──────────────────────────────────────────────────────────────────

@Composable
fun HomeTopBar(onNotificationClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "안녕하세요, 댕댕이주인님",
                fontSize = 13.sp,
                color = BrownText
            )
            Text(
                text = "시흥가개",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = BrownPrimary
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LocationPill()
            IconButton(onClick = onNotificationClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "알림",
                    tint = TextGray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun LocationPill() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(50))
            .clickable { }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = OrangeAccent,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "정왕동",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151)
        )
    }
}

// ─── 산책지수 섹션 ────────────────────────────────────────────────────────────

@Composable
fun WalkIndexSection() {
    val score = 92
    val (scoreColor, statusText) = when {
        score >= 80 -> ScoreGreen to "좋아요"
        score >= 50 -> ScoreBlue to "보통이에요"
        else -> ScoreOrange to "나빠요"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "오늘 산책지수",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = scoreColor, fontWeight = FontWeight.Bold, fontSize = 24.sp)) {
                    append("${score}점")
                }
                withStyle(SpanStyle(color = Color(0xFF111827), fontSize = 24.sp)) {
                    append("으로 $statusText")
                }
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "맑음 · 18° · 미세먼지 좋음",
            fontSize = 13.sp,
            color = TextGray
        )
        Spacer(modifier = Modifier.height(14.dp))
        DDayBanner()
    }
}

@Composable
private fun DDayBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PinkLight)
            .clickable { }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)) {
                    append("#D-2  ")
                }
                withStyle(SpanStyle(color = Color(0xFF374151))) {
                    append("병원 이동 · 신청 2건 검토하기")
                }
            },
            fontSize = 14.sp
        )
        Text(text = ">", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

// ─── 주변 매장 섹션 ───────────────────────────────────────────────────────────

private val storeCategories = listOf("전체", "카페", "공원", "병원", "미용", "식당")

private data class PlaceInfo(val name: String, val category: String, val distance: String, val rating: Float)

private val samplePlaces = listOf(
    PlaceInfo("댕댕 카페", "카페", "0.3km", 4.8f),
    PlaceInfo("행복 동물병원", "병원", "0.7km", 4.5f)
)

@Composable
fun NearbyStoresSection(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 20.dp)
    ) {
        SectionHeader(
            title = "주변 매장 24곳",
            actionText = "🗺 지도 보기"
        )
        Spacer(modifier = Modifier.height(12.dp))
        MapPreviewCard()
        Spacer(modifier = Modifier.height(12.dp))
        CategoryChipRow(selected = selectedCategory, onSelect = onCategorySelected)
        Spacer(modifier = Modifier.height(4.dp))
        samplePlaces.forEachIndexed { idx, place ->
            PlaceItem(number = idx + 1, place = place)
            if (idx < samplePlaces.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color(0xFFF3F4F6)
                )
            }
        }
    }
}

@Composable
private fun MapPreviewCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(MapSky, MapMint, Color(0xFF80DEEA))))
    ) {
        // 장식용 위치 핀들
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = PinkAccent,
            modifier = Modifier.size(28.dp).align(Alignment.Center).offset(20.dp, (-28).dp)
        )
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = PinkAccent,
            modifier = Modifier.size(22.dp).align(Alignment.Center).offset((-40).dp, 8.dp)
        )
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = PinkAccent,
            modifier = Modifier.size(20.dp).align(Alignment.Center).offset(55.dp, 18.dp)
        )
        // 현재 위치 핀 (갈색)
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = BrownPrimary,
            modifier = Modifier.size(34.dp).align(Alignment.Center)
        )
        // 하단 좌측 위치 pill
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = OrangeAccent,
                modifier = Modifier.size(12.dp)
            )
            Text(text = "정왕동", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
        }
    }
}

@Composable
private fun CategoryChipRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        storeCategories.forEach { category ->
            val isSelected = category == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Color(0xFF111827) else Color(0xFFF3F4F6))
                    .clickable { onSelect(category) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color(0xFF374151)
                )
            }
        }
    }
}

@Composable
private fun PlaceItem(number: Int, place: PlaceInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(PinkLight)
        ) {
            Text(
                text = "$number",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE91E63)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = place.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "${place.category} · ${place.distance}", fontSize = 12.sp, color = TextGray)
                Text(text = "·", fontSize = 12.sp, color = TextGray)
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(12.dp)
                )
                Text(text = "${place.rating}", fontSize = 12.sp, color = TextGray)
            }
        }
        IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "즐겨찾기",
                tint = Color(0xFFD1D5DB),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─── 반려동물 소식 섹션 ───────────────────────────────────────────────────────

private data class NewsTag(val label: String, val textColor: Color, val bgColor: Color)

private val tagStyles = mapOf(
    "[행사]" to NewsTag("[행사]", Color(0xFF7C3AED), Color(0xFFF5F3FF)),
    "[봉사]" to NewsTag("[봉사]", Color(0xFF059669), Color(0xFFECFDF5)),
    "[지원]" to NewsTag("[지원]", Color(0xFF2563EB), Color(0xFFEFF6FF))
)

private data class PetNewsItem(val tag: String, val title: String, val date: String)


private val sampleNews = listOf(
    PetNewsItem("[행사]", "반려동물 입양 행사", "5월 10일"),
    PetNewsItem("[봉사]", "이동 봉사 모집 중", "5월 12일"),
    PetNewsItem("[지원]", "중성화 수술 지원 사업", "5월 14일")
)

@Composable
fun PetNewsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 20.dp)
    ) {
        SectionHeader(title = "반려동물 소식", actionText = "📰 전체 보기")
        Spacer(modifier = Modifier.height(12.dp))
        FeaturedNewsCard()
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF3F4F6))
        sampleNews.forEach { item ->
            NewsListItem(item)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF3F4F6))
        }
        Spacer(modifier = Modifier.height(12.dp))
        VolunteerBanner()
    }
}

@Composable
private fun FeaturedNewsCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9F9F9))
            .clickable { }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                tint = Color(0xFFF57C00),
                modifier = Modifier.size(40.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            CategoryTag(text = "정책", textColor = Color(0xFF0284C7), bgColor = Color(0xFFE0F2FE))
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "2025년 반려동물 등록 의무화 안내",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "5월 10일 · 시흥시청", fontSize = 11.sp, color = TextGray)
        }
    }
}

@Composable
private fun NewsListItem(item: PetNewsItem) {
    val tag = tagStyles[item.tag] ?: NewsTag(item.tag, TextGray, Color(0xFFF3F4F6))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryTag(text = tag.label, textColor = tag.textColor, bgColor = tag.bgColor)
        Text(
            text = item.title,
            fontSize = 14.sp,
            color = Color(0xFF111827),
            modifier = Modifier.weight(1f)
        )
        Text(text = item.date, fontSize = 12.sp, color = TextGray)
    }
}

@Composable
private fun VolunteerBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PinkLight)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "이동 지원 봉사자가 되어 보세요",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE91E63))
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(text = "신청 →", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── 공통 컴포넌트 ─────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, actionText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Text(
            text = actionText,
            fontSize = 14.sp,
            color = OrangeAccent,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
private fun CategoryTag(text: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(text = text, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
