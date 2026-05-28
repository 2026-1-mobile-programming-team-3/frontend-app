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
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.example.siheunggagae.data.model.NewsDetailResponse
import com.example.siheunggagae.data.model.NewsItem
import com.example.siheunggagae.data.network.RetrofitClient
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import com.example.siheunggagae.ui.util.newsFallbackDrawable

private fun categoryToKorean(category: String?) = when (category?.uppercase()) {
    "POLICY"    -> "정책"
    "EVENT"     -> "행사"
    "VOLUNTEER" -> "봉사"
    "SUPPORT"   -> "지원"
    else        -> category ?: "소식"
}

private val Brown900ND    = Color(0xFF614B3A)
private val Brown700ND    = Color(0xFF8A6E58)
private val BrownBorderND = Color(0xFFE8D3C2)
private val Orange500ND   = Color(0xFFF7A35B)
private val Green500ND    = Color(0xFF00A63E)
private val OrangeSandND  = Color(0xFFFFEDD4)
private val PinkSurfaceND = Color(0xFFFEE7EC)
private val BackgroundND  = Color(0xFFFEFEFE)

private fun categoryColor(category: String?) = when (categoryToKorean(category)) {
    "행사" -> Green500ND
    "봉사" -> Pink500ND
    "지원" -> Orange500ND
    else   -> Brown700ND
}
private val TextBlackND   = Color(0xFF1E120A)
private val Pink500ND     = Color(0xFFF04268)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun NewsDetailScreen(
    newsId: String = "",
    onBack: () -> Unit = {},
    onRelatedClick: (String) -> Unit = {},
) {
    val context = LocalContext.current
    var detail by remember { mutableStateOf<NewsDetailResponse?>(null) }
    var relatedNews by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(newsId) {
        if (newsId.isNotEmpty()) {
            detail = runCatching { RetrofitClient.api.getNewsDetail(newsId).body() }.getOrNull()
            val allNews = runCatching { RetrofitClient.api.getNews().body()?.news ?: emptyList() }.getOrDefault(emptyList())
            val category = detail?.category
            relatedNews = if (category != null) {
                allNews.filter { it.newsId != newsId && it.category == category }.take(2)
            } else {
                allNews.filter { it.newsId != newsId }.take(2)
            }
        }
        isLoading = false
    }

    fun shareNews() {
        val current = detail ?: return
        val text = buildString {
            current.title?.let { append(it).append("\n") }
            current.officialLink?.let { append(it) }
        }
        if (text.isBlank()) return
        runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, "공유하기"))
        }.onFailure {
            android.widget.Toast.makeText(
                context,
                "공유할 수 있는 앱이 없어요",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        containerColor = BackgroundND,
        topBar = {
            NewsDetailTopBar(
                onBack = onBack,
                onShare = ::shareNews,
            )
        },
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

                HeroImage(detail = detail)

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

                    val link = detail?.officialLink
                    if (!link.isNullOrEmpty()) {
                        OfficialLinkCard(link = link, onClick = {
                            runCatching {
                                val normalized = if (link.startsWith("http")) link else "https://$link"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        })
                    }

                    if (relatedNews.isNotEmpty()) {
                        RelatedNewsSection(items = relatedNews, onItemClick = onRelatedClick)
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun NewsDetailTopBar(
    onBack: () -> Unit,
    onShare: () -> Unit = {},
) {
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

        TopBarIconBtnND(
            onClick = onShare,
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            Icon(painter = painterResource(R.drawable.ic_share), null, tint = TextBlackND, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun TopBarIconBtnND(onClick: () -> Unit = {}, modifier: Modifier = Modifier, icon: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick() },
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
            val category = categoryToKorean(detail?.category)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(categoryColor(detail?.category))
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
                lineHeight = 32.sp,
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

// ─── 본문 상단 히어로 이미지 ───────────────────────────────────────────────────

@Composable
private fun HeroImage(detail: NewsDetailResponse?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp)),
    ) {
        val imageUrl = detail?.imageUrl
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = detail.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Image(
                painter = painterResource(newsFallbackDrawable(detail?.newsId)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

// ─── 공식 링크 카드 (외부 브라우저로 이동) ─────────────────────────────────────

@Composable
private fun OfficialLinkCard(link: String, onClick: () -> Unit) {
    val displayUrl = link.removePrefix("https://").removePrefix("http://").trimEnd('/')
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OrangeSandND)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_paperclip),
                contentDescription = null,
                tint = Orange500ND,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "원문 바로가기",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp,
                color = TextBlackND,
            )
            Text(
                text = displayUrl,
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = Brown700ND,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Brown700ND,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ─── 관련 소식 (동적) ──────────────────────────────────────────────────────────

@Composable
private fun RelatedNewsSection(items: List<NewsItem>, onItemClick: (String) -> Unit = {}) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "관련 소식",
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp,
            color = Brown900ND,
        )
        items.forEach { news ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PinkSurfaceND),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth().clickable { news.newsId?.let { onItemClick(it) } },
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
                            text = categoryToKorean(news.category),
                            fontFamily = PretendardFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Brown700ND,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = news.title ?: "",
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
