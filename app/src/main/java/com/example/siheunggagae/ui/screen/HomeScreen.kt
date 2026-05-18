package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.AppBottomBar
import com.example.siheunggagae.R
import com.example.siheunggagae.Screen

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
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// 스펙 컬러
private val Brown900      = Color(0xFF614B3A)
private val Brown700      = Color(0xFF8A6E58)
private val BrownBorderC  = Color(0xFFE8D3C2)
private val Orange500     = Color(0xFFF7A35B)
private val Pink500       = Color(0xFFF04268)
private val Green500      = Color(0xFF00A63E)
private val Blue400       = Color(0xFF388AF5)
private val PinkSurface   = Color(0xFFFEE7EC)
private val Background    = Color(0xFFFEFEFE)
private val GrayText      = Color(0xFF6B7280)
private val MapMintC      = Color(0xFFD0FEE1)
private val TextBlack     = Color(0xFF1E120A)
private val StarYellow    = Color(0xFFFDC700)

@Composable
fun HomeScreen(
    unreadCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onPlaceDetailClick: (Int) -> Unit = {},
) {
    var selectedCategory by remember { mutableStateOf("전체") }

    Scaffold(
        containerColor = Background,
        bottomBar = { AppBottomBar(currentRoute = Screen.Home.route, onNavigate = onNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            HomeTopBar(unreadCount = unreadCount, onNotificationClick = onNotificationClick)
            Spacer(Modifier.height(8.dp))
            WalkIndexSection()
            Spacer(Modifier.height(8.dp))
            NearbyStoresSection(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                onPlaceDetailClick = onPlaceDetailClick,
            )
            Spacer(Modifier.height(8.dp))
            PetNewsSection()
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── TopBar ──────────────────────────────────────────────────────────────────

@Composable
fun HomeTopBar(unreadCount: Int = 0, onNotificationClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            // w400 / 12sp / lh20 — Figma 실측
            Text(
                text = "안녕하세요, 댕댕이주인님",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = Brown700,
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFF101828))) { append("시흥") }
                    withStyle(SpanStyle(color = Color(0xFFFDC700))) { append("가개") }
                },
                fontFamily = PretendardFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 32.sp,
            )

        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .border(1.dp, BrownBorderC, RoundedCornerShape(50.dp))
                    .clickable { }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(painter = painterResource(R.drawable.ic_location_on), null, tint = Orange500, modifier = Modifier.size(14.dp))
                // w500 / 14sp / lh20 — Figma 실측 (기존 12sp → 수정)
                Text(
                    text = "정왕동",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    color = Brown700,
                )
            }
            Box {
                IconButton(onClick = onNotificationClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "알림",
                        tint = Brown700,
                        modifier = Modifier.size(22.dp),
                    )
                }
                if (unreadCount > 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Pink500)
                            .align(Alignment.TopEnd),
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            fontFamily = PretendardFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 8.sp,
                        )
                    }
                }
            }
        }
    }
}

// ─── 산책지수 섹션 ────────────────────────────────────────────────────────────

@Composable
fun WalkIndexSection() {
    val score = 92
    val scoreColor = when {
        score >= 80 -> Green500
        score >= 50 -> Blue400
        else        -> Orange500
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // w500 / 14sp / lh20 — Figma 실측
        Text(
            text = "오늘 산책지수",
            fontFamily = PretendardFamily,
            color = TextBlack,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp
        )
        Spacer(Modifier.height(6.dp))
        // w800 / 30sp / lh32 — Figma 실측 (기존 36sp/Bold → 수정)
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontFamily = PretendardFamily, color = scoreColor, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)) {
                    append("${score}점")
                }
                withStyle(SpanStyle(fontFamily = PretendardFamily, color = TextBlack, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)) {
                    append("으로 좋아요")
                }
            },
            lineHeight = 32.sp,
        )
        Spacer(Modifier.height(4.dp))
        // w400 / 14sp / lh20 — Figma 실측 (기존 12sp → 수정)
        Text(
            text = "맑음 · 18° · 미세먼지 좋음",
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = GrayText,
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50.dp))
                .background(PinkSurface)
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = buildAnnotatedString {
                    // w700 / 12sp — D-day 배지
                    withStyle(SpanStyle(fontFamily = PretendardFamily, color = Pink500, fontWeight = FontWeight.Bold, fontSize = 12.sp)) {
                        append("[D-2]  ")
                    }
                    // w500 / 14sp — 배너 본문
                    withStyle(SpanStyle(fontFamily = PretendardFamily, color = Color(0xFF374151), fontWeight = FontWeight.Medium, fontSize = 14.sp)) {
                        append("병원 이동 · 신청 2건 검토하기  ")
                    }
                },
            )
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Brown700, modifier = Modifier.size(18.dp))
        }
    }
}

// ─── 주변 매장 섹션 ───────────────────────────────────────────────────────────

private val storeCategories = listOf("전체", "카페", "공원", "병원", "미용", "식당")

private data class PlaceInfo(
    val id: Int,
    val name: String,
    val category: String,
    val distance: String,
    val rating: Float,
    val isFavorite: Boolean = false,
)

private val samplePlaces = listOf(
    PlaceInfo(5, "반려동물 병원", "병원", "1.8km", 4.9f, isFavorite = true),
    PlaceInfo(4, "배곧 댕댕카페",  "카페", "2.5km", 4.8f),
)

@Composable
fun NearbyStoresSection(selectedCategory: String, onCategorySelected: (String) -> Unit, onPlaceDetailClick: (Int) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // w700 / 20sp / lh24 — Figma 실측 (기존 18sp → 수정)
                Text(
                    text = "주변 매장",
                    fontFamily = PretendardFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = TextBlack,
                )
                Text(
                    text = "24곳",
                    fontFamily = PretendardFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = Pink500,
                )
            }
            // w600 / 14sp — Figma 실측 (기존 13sp/Medium → 수정)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_map),
                    contentDescription = null,
                    tint = Orange500,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "지도 보기",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Orange500,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(MapMintC, Color(0xFFE8FAF0), Color.White)))
        ) {
            Icon(painter = painterResource(R.drawable.ic_location_on), null, tint = Pink500,
                modifier = Modifier.size(28.dp).align(Alignment.Center).offset(20.dp, (-28).dp))
            Icon(painter = painterResource(R.drawable.ic_location_on), null, tint = Pink500,
                modifier = Modifier.size(22.dp).align(Alignment.Center).offset((-40).dp, 8.dp))
            Icon(painter = painterResource(R.drawable.ic_location_on), null, tint = Pink500,
                modifier = Modifier.size(20.dp).align(Alignment.Center).offset(55.dp, 18.dp))
            Icon(painter = painterResource(R.drawable.ic_location_on), null, tint = Orange500,
                modifier = Modifier.size(34.dp).align(Alignment.Center))
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(painter = painterResource(R.drawable.ic_location_on), null, tint = Orange500, modifier = Modifier.size(12.dp))
                // w500 / 14sp — Figma 실측 (기존 12sp → 수정)
                Text(
                    text = "정왕동",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            storeCategories.forEach { cat ->
                val isSel = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isSel) Color(0xFF1A1A1A) else Color.White)
                        .then(if (!isSel) Modifier.border(1.dp, BrownBorderC, RoundedCornerShape(50.dp)) else Modifier)
                        .clickable { onCategorySelected(cat) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // w500 / 14sp / lh20 — Figma 실측
                    Text(
                        text = cat,
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                        color = if (isSel) Color.White else Brown700,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        samplePlaces.forEachIndexed { idx, place ->
            PlaceItem(number = idx + 1, place = place, onPlaceDetailClick = onPlaceDetailClick)
            if (idx < samplePlaces.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF3F4F6))
            }
        }
    }
}

@Composable
private fun PlaceItem(number: Int, place: PlaceInfo, onPlaceDetailClick: (Int) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlaceDetailClick(place.id) }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 번호 원형: 1=Pink500, 나머지=Brown900
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(32.dp).clip(CircleShape)
                .background(if (number == 1) Pink500 else Brown900)
        ) {
            // w500 / 14sp — Figma: "1"=w500, "2"=w400
            Text(
                text = "$number",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = if (number == 1) FontWeight.Medium else FontWeight.Normal,
                color = Color.White,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            // w700 / 14sp / lh20 — Figma 실측 (기존 16sp/SemiBold → 수정)
            Text(
                text = place.name,
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp,
                color = TextBlack,
            )
            // w600 / 16sp / lh24 — Figma 실측 (기존 12sp → 수정)
            Text(
                text = "${place.category} · ${place.distance} · ⭐ ${place.rating}",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp,
                color = Brown700,
            )
        }
        IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_favorite),
                contentDescription = "즐겨찾기",
                tint = if (place.isFavorite) Pink500 else BrownBorderC,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─── 반려동물 소식 섹션 ───────────────────────────────────────────────────────

private data class PetNewsItem(val tag: String, val tagColor: Color, val tagBg: Color, val title: String, val date: String)

private val sampleNews = listOf(
    PetNewsItem("행사", Color(0xFF7C3AED), Color(0xFFFFFFFF), "반려동물 입양 행사",    "4.5"),
    PetNewsItem("봉사", Color(0xFF059669), Color(0xFFFFFFFF), "이동 봉사 모집 중",    "4.12"),
    PetNewsItem("지원", StarYellow, Color(0xFFFFFFFF), "중성화 수술 지원 사업", "4.5"),
)

@Composable
fun PetNewsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // w700 / 20sp / lh24 — 섹션 헤더 (기존 18sp → 수정)
            Text(
                text = "반려동물 소식",
                fontFamily = PretendardFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = TextBlack,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_newsmode),
                    contentDescription = null,
                    tint = Orange500,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "전체 보기",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Orange500,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        // 메인 뉴스 카드
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
            Image(
                painter = painterResource(R.drawable.img_home_news_thumb),
                contentDescription = "뉴스 썸네일",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                SmallTagChip(text = "지원", textColor = Color(0xFF0284C7), bgColor = Color(0xFFE0F2FE))
                Spacer(Modifier.height(5.dp))
                // w800 / 20sp / lh28 — Figma 실측 (메인 뉴스 카드 제목)
                Text(
                    text = "2026년 실외 사육견\n중성화 수술비 지원",
                    fontFamily = PretendardFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 28.sp,
                    color = TextBlack,
                    maxLines = 2,
                )
                Spacer(Modifier.height(4.dp))
                // w400 / 14sp / lh20 — Figma 실측
                Text(
                    text = "4월 10일 · 네이버 뉴스",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    color = GrayText,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF3F4F6))
        sampleNews.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallTagChip(text = item.tag, textColor = item.tagColor, bgColor = item.tagBg)
                // w700 / 14sp / lh20 — 소식 리스트 제목
                Text(
                    text = item.title,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp,
                    color = TextBlack,
                    modifier = Modifier.weight(1f),
                )
                // w400 / 12sp / lh16 — 날짜 캡션
                Text(
                    text = item.date,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp,
                    color = GrayText,
                )
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF3F4F6))
        }
        Spacer(Modifier.height(12.dp))
        // 봉사자 배너
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PinkSurface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(R.drawable.ic_handshake), contentDescription = null, tint = Orange500, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            // w700 / 14sp / lh20 — Figma 실측
            Text(
                text = "이동 지원 봉사자가 되어 보세요",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp,
                color = TextBlack,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White)
                    .clickable { }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "신청 →",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brown700,
                )
            }
        }
    }
}

@Composable
private fun SmallTagChip(text: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        // w500 / 12sp / lh16 — Figma 실측
        Text(
            text = text,
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 16.sp,
            color = textColor,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    SiheungGagaeTheme { HomeScreen() }
}
