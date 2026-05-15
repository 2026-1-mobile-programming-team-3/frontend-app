package com.example.siheunggagae

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// ─── 컬러 ──────────────────────────────────────────────────────────────────────
private val TextBlackNs    = Color(0xFF1F130B)
private val Brown700Ns     = Color(0xFF8B6F59)
private val Brown900Ns     = Color(0xFF614B3A)
private val Orange500Ns    = Color(0xFFF7A45C)
private val Pink500Ns      = Color(0xFFF14369)
private val Green500Ns     = Color(0xFF00A73F)
private val MintNs         = Color(0xFFD1FFE2)
private val PinkSurfaceNs  = Color(0xFFFEE8ED)
private val BackgroundNs   = Color(0xFFFEFEFE)
private val PlaceholderNs  = Color(0xFFC1AFA0)

// ─── 데이터 ────────────────────────────────────────────────────────────────────

internal data class NewsItem(
    val id: Int,
    val category: String,
    val title: String,
    val date: String,
    val source: String? = null,
    val imageRes: Int? = null,
)

private val newsCategories = listOf("전체", "정책", "행사", "봉사", "지원")

private val featuredNews = NewsItem(1, "지원",
    "2026년 실외 사육견\n중성화 수술비 지원", "4월 10일", "네이버 뉴스",
    imageRes = R.drawable.img_news_banner)

private val gridNews = listOf(
    NewsItem(2, "행사", "반려동물 등록 무료 캠페인 5월 13일",          "4.5",  imageRes = R.drawable.img_news_thumb_1),
    NewsItem(3, "봉사", "정왕동 유기견 산책 봉사자 10명 모집",          "4.12", imageRes = R.drawable.img_news_thumb_2),
    NewsItem(4, "행사", "봄맞이 펫 사진 공모전",                       "4.5",  imageRes = R.drawable.img_news_thumb_3),
    NewsItem(5, "봉사", "노령견 의료비 지원 봉사 모집",                 "4.12", imageRes = R.drawable.img_news_thumb_4),
)

private val listNews = listOf(
    NewsItem(6, "행사", "반려동물 등록 무료 캠페인",         "4.5"),
    NewsItem(7, "봉사", "정왕동 유기견 산책 봉사자 모집",    "4.12"),
    NewsItem(8, "정책", "반려동물 보호법 개정사항",           "4.5"),
)

private val allNews = gridNews + listNews

private fun categoryColor(category: String) = when (category) {
    "행사" -> Green500Ns
    "봉사" -> Pink500Ns
    "지원" -> Orange500Ns
    else   -> Brown700Ns
}

private fun categoryImageBg(category: String) = when (category) {
    "행사" -> MintNs
    "봉사" -> PinkSurfaceNs
    "지원" -> Color(0xFFFFF3E0)
    else   -> Color(0xFFF5F0EB)
}

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun NewsScreen(
    onNewsDetailClick: (Int) -> Unit = {},
    onPlaceDetailClick: (Int) -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    var selectedCategory by remember { mutableStateOf("전체") }

    val filteredList = if (selectedCategory == "전체") allNews
                       else allNews.filter { it.category == selectedCategory }

    Scaffold(
        containerColor = BackgroundNs,
        topBar = { NewsTopBar() },
        bottomBar = { AppBottomBar(currentRoute = Screen.News.route, onNavigate = onNavigate) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            // 검색 + 카테고리 필터 (흰 배경)
            item {
                Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                    NewsSearchBar()
                    NewsCategoryFilter(
                        selected = selectedCategory,
                        onSelect = { selectedCategory = it },
                    )
                }
            }

            if (selectedCategory == "전체") {
                // 피처드 카드
                item {
                    Spacer(Modifier.height(16.dp))
                    FeaturedNewsCard(
                        item = featuredNews,
                        onClick = { onNewsDetailClick(featuredNews.id) },
                    )
                    Spacer(Modifier.height(12.dp))
                }
                // 그리드 2열 1행
                item {
                    NewsGridRow(
                        left = gridNews[0],
                        right = gridNews[1],
                        onItemClick = onNewsDetailClick,
                    )
                    Spacer(Modifier.height(12.dp))
                }
                // 그리드 2열 2행
                item {
                    NewsGridRow(
                        left = gridNews[2],
                        right = gridNews[3],
                        onItemClick = onNewsDetailClick,
                    )
                    Spacer(Modifier.height(12.dp))
                }
                // 리스트 아이템들
                items(listNews, key = { it.id }) { item ->
                    NewsListRow(item = item, onClick = { onNewsDetailClick(item.id) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                // 필터된 리스트
                item { Spacer(Modifier.height(16.dp)) }
                items(filteredList, key = { it.id }) { item ->
                    NewsListRow(item = item, onClick = { onNewsDetailClick(item.id) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun NewsTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "소식",
            fontFamily = PretendardFamily,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackNs,
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NewsTopBarIcon { Icon(Icons.Default.BookmarkBorder, null, tint = TextBlackNs, modifier = Modifier.size(18.dp)) }
            Box {
                NewsTopBarIcon { Icon(Icons.Default.Notifications, null, tint = TextBlackNs, modifier = Modifier.size(18.dp)) }
                // 알림 뱃지
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Pink500Ns),
                )
            }
        }
    }
}

@Composable
private fun NewsTopBarIcon(content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(30.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { },
    ) { content() }
}

// ─── 검색바 ────────────────────────────────────────────────────────────────────

@Composable
private fun NewsSearchBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(50.dp))
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Default.Search, null, tint = Brown700Ns, modifier = Modifier.size(20.dp))
        Text(
            text = "뉴스 · 정책 · 행사 검색",
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = PlaceholderNs,
        )
    }
}

// ─── 카테고리 필터 ──────────────────────────────────────────────────────────────

@Composable
private fun NewsCategoryFilter(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        newsCategories.forEach { cat ->
            val isSelected = cat == selected
            Text(
                text = cat,
                fontFamily = PretendardFamily,
                fontSize = if (isSelected) 16.sp else 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) TextBlackNs else Brown700Ns,
                modifier = Modifier.clickable { onSelect(cat) },
            )
        }
    }
}

// ─── 피처드 카드 ────────────────────────────────────────────────────────────────

@Composable
private fun FeaturedNewsCard(item: NewsItem, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.BottomStart,
    ) {
        if (item.imageRes != null) {
            Image(
                painter = painterResource(item.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        } else {
            Box(modifier = Modifier.matchParentSize().background(Brush.linearGradient(listOf(Brown900Ns, Color(0xFFBF8A63)))))
        }
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // 태그 칩
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(categoryColor(item.category))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = item.category,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Text(
                text = item.title,
                fontFamily = PretendardFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 28.sp,
                color = Color.White,
                maxLines = 2,
            )
            Text(
                text = listOfNotNull(item.date, item.source).joinToString(" · "),
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

// ─── 2열 그리드 ────────────────────────────────────────────────────────────────

@Composable
private fun NewsGridRow(
    left: NewsItem,
    right: NewsItem,
    onItemClick: (Int) -> Unit = {},
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NewsGridCard(item = left,  modifier = Modifier.weight(1f), onClick = { onItemClick(left.id) })
        NewsGridCard(item = right, modifier = Modifier.weight(1f), onClick = { onItemClick(right.id) })
    }
}

@Composable
private fun NewsGridCard(item: NewsItem, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() },
    ) {
        // 이미지
        if (item.imageRes != null) {
            Image(
                painter = painterResource(item.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(100.dp),
            )
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(categoryImageBg(item.category)))
        }
        // 정보 영역
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.category,
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = categoryColor(item.category),
            )
            Text(
                text = item.title,
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp,
                color = TextBlackNs,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.date,
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Brown700Ns,
            )
        }
    }
}

// ─── 리스트 아이템 ──────────────────────────────────────────────────────────────

@Composable
private fun NewsListRow(item: NewsItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = item.category,
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = categoryColor(item.category),
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = item.title,
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = TextBlackNs,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = item.date,
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Brown700Ns,
        )
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NewsScreenPreview() {
    SiheungGagaeTheme { NewsScreen() }
}
