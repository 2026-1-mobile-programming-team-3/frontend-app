package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.R

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.data.model.NewsDetailResponse
import com.example.siheunggagae.data.network.RetrofitClient
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

private val Brown900ND    = Color(0xFF614B3A)
private val Brown700ND    = Color(0xFF8A6E58)
private val BrownBorderND = Color(0xFFE8D3C2)
private val Orange500ND   = Color(0xFFF7A35B)
private val OrangeSandND  = Color(0xFFFFEDD4)
private val PinkSurfaceND = Color(0xFFFEE7EC)
private val BackgroundND  = Color(0xFFFEFEFE)
private val TextBlackND   = Color(0xFF1E120A)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun NewsDetailScreen(newsId: String = "", onBack: () -> Unit = {}) {
    var detail by remember { mutableStateOf<NewsDetailResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(newsId) {
        if (newsId.isNotEmpty()) {
            detail = runCatching { RetrofitClient.api.getNewsDetail(newsId).body() }.getOrNull()
        }
        isLoading = false
    }

    Scaffold(
        containerColor = BackgroundND,
        topBar = { NewsDetailTopBar(onBack = onBack) },
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Orange500ND)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
            ) {
                HeaderSection(detail = detail)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Spacer(Modifier.height(4.dp))

                    if (!detail?.content.isNullOrEmpty()) {
                        Text(
                            text = detail?.content ?: "",
                            fontFamily = PretendardFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 26.sp,
                            color = TextBlackND,
                        )
                    }

                    if (!detail?.officialLink.isNullOrEmpty()) {
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
                                Text(
                                    text = "· 공식 링크: ${detail?.officialLink}",
                                    fontFamily = PretendardFamily,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = TextBlackND,
                                )
                            }
                        }
                    }

                    RelatedNewsSectionStatic()

                    Spacer(Modifier.height(24.dp))
                }
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
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = TextBlackND,
                modifier = Modifier.size(22.dp),
            )
        }

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TopBarIconBtnND { Icon(painter = painterResource(R.drawable.ic_bookmark), null, tint = TextBlackND, modifier = Modifier.size(20.dp)) }
            TopBarIconBtnND { Icon(painter = painterResource(R.drawable.ic_share), null, tint = TextBlackND, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
private fun TopBarIconBtnND(icon: @Composable () -> Unit) {
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
private fun HeaderSection(detail: NewsDetailResponse?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(OrangeSandND, PinkSurfaceND)))
            .padding(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val category = detail?.category ?: "소식"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Orange500ND)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Text(
                    text = category,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    color = Color.White,
                )
            }

            Text(
                text = detail?.title ?: "소식을 불러오는 중...",
                fontFamily = PretendardFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp,
                color = TextBlackND,
            )

            val meta = buildString {
                if (!detail?.publisher.isNullOrEmpty()) append(detail!!.publisher)
                if (!detail?.publishedDate.isNullOrEmpty()) {
                    if (isNotEmpty()) append(" · ")
                    append(detail!!.publishedDate)
                }
            }
            if (meta.isNotEmpty()) {
                Text(
                    text = meta,
                    fontFamily = PretendardFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Brown700ND,
                )
            }
        }
    }
}

// ─── 관련 소식 (정적) ──────────────────────────────────────────────────────────

private data class RelatedNewsStub(val category: String, val title: String)

private val relatedStubs = listOf(
    RelatedNewsStub("정책", "2026년 반려동물 등록 의무화 개정 안내"),
    RelatedNewsStub("지원", "노령견 의료비 지원 사업 2차 접수 시작"),
)

@Composable
private fun RelatedNewsSectionStatic() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "관련 소식",
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp,
            color = Brown900ND,
        )
        relatedStubs.forEach { news ->
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
                        Icon(
                            painter = painterResource(R.drawable.ic_priority_high),
                            null,
                            tint = Orange500ND,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = news.category,
                            fontFamily = PretendardFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Brown700ND,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = news.title,
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp,
                            color = TextBlackND,
                        )
                    }
                }
            }
        }
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NewsDetailScreenPreview() {
    SiheungGagaeTheme { NewsDetailScreen(newsId = "") }
}
