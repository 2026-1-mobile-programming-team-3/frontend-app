package com.example.siheunggagae

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// 스펙 컬러
private val Brown900ND    = Color(0xFF614B3A)
private val Brown700ND    = Color(0xFF8A6E58)
private val BrownBorderND = Color(0xFFE8D3C2)
private val Orange500ND   = Color(0xFFF7A35B)
private val OrangeSandND  = Color(0xFFFFEDD4)
private val PinkSurfaceND = Color(0xFFFEE7EC)
private val BackgroundND  = Color(0xFFFEFEFE)
private val MintLightND   = Color(0xFFD0FEE1)
private val TextBlackND   = Color(0xFF1E120A)

// ─── 데이터 ────────────────────────────────────────────────────────────────────

private data class NewsDetail(
    val id: Int,
    val category: String,
    val title: String,
    val author: String,
    val date: String,
    val hashtags: List<String>,
    val bodyText: String,
    val infoItems: List<String>,
    val additionalText: String,
)

private data class RelatedNews(
    val category: String,
    val title: String,
)

private val sampleNewsDetail = NewsDetail(
    id = 1,
    category = "지원",
    title = "2026년 실외 사육견 중성화 수술비 지원",
    author = "시흥시청 동물보호과",
    date = "4월 10일",
    hashtags = listOf("#정책", "#시흥시", "#중성화"),
    bodyText = "시흥시는 반려동물 복지 향상을 위해 2026년 실외 사육견 중성화 수술비 지원 사업을 시행합니다. " +
        "이번 사업은 시흥시에 거주하는 실외 사육견 소유자를 대상으로 하며, 수술비 전액을 지원합니다. " +
        "보호자께서는 지정 동물병원을 방문하여 수술을 받으신 후 영수증과 함께 신청하시면 됩니다.",
    infoItems = listOf(
        "· 신청 기간: 2026.04.10 - 2026.05.15",
        "· 지원 대상: 시흥시 거주 실외 사육견 소유자",
        "· 지원 내용: 수술비 전액 및 내장형 칩 등록 지원",
    ),
    additionalText = "신청은 시흥시청 동물보호과 방문 또는 온라인으로 가능하며, 자세한 사항은 시흥시청 공식 홈페이지를 참고해 주세요. " +
        "신청 기간 내에 접수된 건에 한해 지원이 이루어지며, 예산 소진 시 조기 마감될 수 있습니다.",
)

private val relatedNewsList = listOf(
    RelatedNews("정책", "2026년 반려동물 등록 의무화 개정 안내"),
    RelatedNews("지원", "노령견 의료비 지원 사업 2차 접수 시작"),
)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun NewsDetailScreen(newsId: Int = 1, onBack: () -> Unit = {}) {
    Scaffold(
        containerColor = BackgroundND,
        topBar = { NewsDetailTopBar(onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            HeaderSection(detail = sampleNewsDetail)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                HashtagRow(hashtags = sampleNewsDetail.hashtags)

                Text(
                    text = sampleNewsDetail.bodyText,
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 26.sp,
                    color = TextBlackND,
                )

                ImagePlaceholderND()

                InfoBoxCard(items = sampleNewsDetail.infoItems)

                Text(
                    text = sampleNewsDetail.additionalText,
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 26.sp,
                    color = TextBlackND,
                )

                RelatedNewsSection()

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun NewsDetailTopBar(onBack: () -> Unit) {
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
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onBack() },
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = TextBlackND, modifier = Modifier.size(22.dp))
        }

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TopBarIconBtn(icon = { Icon(Icons.Default.BookmarkBorder, null, tint = TextBlackND, modifier = Modifier.size(20.dp)) })
            TopBarIconBtn(icon = { Icon(Icons.Default.Share, null, tint = TextBlackND, modifier = Modifier.size(20.dp)) })
        }
    }
}

@Composable
private fun TopBarIconBtn(icon: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { },
    ) { icon() }
}

// ─── 상단 헤더 ─────────────────────────────────────────────────────────────────

@Composable
private fun HeaderSection(detail: NewsDetail) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(OrangeSandND, PinkSurfaceND))
            )
            .padding(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Orange500ND)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Text(
                    text = detail.category,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    color = Color.White
                )
            }

            Text(
                text = detail.title,
                fontFamily = PretendardFamily,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp,
                color = TextBlackND,
            )

            Text(
                text = "${detail.author} · ${detail.date}",
                fontFamily = PretendardFamily,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Brown700ND,
            )
        }
    }
}

// ─── 해시태그 Row ──────────────────────────────────────────────────────────────

@Composable
private fun HashtagRow(hashtags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        hashtags.forEach { tag ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .border(1.dp, BrownBorderND, RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = tag,
                    fontFamily = PretendardFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Brown700ND
                )
            }
        }
    }
}

// ─── 본문 이미지 ───────────────────────────────────────────────────────────────

@Composable
private fun ImagePlaceholderND() {
    Image(
        painter = painterResource(R.drawable.img_news_detail_body),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp)),
    )
}

// ─── 정보 박스 Card ────────────────────────────────────────────────────────────

@Composable
private fun InfoBoxCard(items: List<String>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { item ->
                Text(
                    text = item,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = TextBlackND,
                )
            }
        }
    }
}

// ─── 관련 소식 섹션 ─────────────────────────────────────────────────────────────

@Composable
private fun RelatedNewsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "관련 소식",
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp,
            color = Brown900ND,
        )
        relatedNewsList.forEach { news ->
            RelatedNewsCard(news = news)
        }
    }
}

@Composable
private fun RelatedNewsCard(news: RelatedNews) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PinkSurfaceND),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().clickable { },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(OrangeSandND),
            ) {
                Icon(Icons.Default.PriorityHigh, null, tint = Orange500ND, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = news.category,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Brown700ND
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = news.title,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 20.sp,
                    color = TextBlackND
                )
            }
        }
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NewsDetailScreenPreview() {
    SiheungGagaeTheme { NewsDetailScreen() }
}
