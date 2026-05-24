package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.AppBottomBar
import com.example.siheunggagae.R
import com.example.siheunggagae.Screen
import com.example.siheunggagae.data.model.NewsItem
import com.example.siheunggagae.data.network.RetrofitClient

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// ─── 컬러 (design.md / CLAUDE.md 단일 진실에 맞춤) ──────────────────────────────
private val TextBlackNs    = Color(0xFF1E120A)
private val Brown700Ns     = Color(0xFF8A6E58)
private val Brown900Ns     = Color(0xFF614B3A)
private val Orange500Ns    = Color(0xFFF7A35B)
private val Pink500Ns      = Color(0xFFF04268)
private val Green500Ns     = Color(0xFF00A63E)
private val MintNs         = Color(0xFFD0FEE1)
private val PinkSurfaceNs  = Color(0xFFFEE7EC)
private val BackgroundNs   = Color(0xFFFEFEFE)
private val PlaceholderNs  = Color(0xFFC1AEA0)

private val newsCategories = listOf("전체", "정책", "행사", "봉사", "지원")

private fun categoryToKorean(category: String?) = when (category?.uppercase()) {
    "POLICY"    -> "정책"
    "EVENT"     -> "행사"
    "VOLUNTEER" -> "봉사"
    "SUPPORT"   -> "지원"
    else        -> category ?: "소식"
}

private fun categoryColor(category: String?) = when (categoryToKorean(category)) {
    "행사" -> Green500Ns
    "봉사" -> Pink500Ns
    "지원" -> Orange500Ns
    else   -> Brown700Ns
}

private fun categoryImageBg(category: String?) = when (categoryToKorean(category)) {
    "행사" -> MintNs
    "봉사" -> PinkSurfaceNs
    "지원" -> Color(0xFFFFF3E0)
    else   -> Color(0xFFF5F0EB)
}

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun NewsScreen(
    unreadCount: Int = 0,
    onNewsDetailClick: (String) -> Unit = {},
    onPlaceDetailClick: (Int) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    var isLoading by remember { mutableStateOf(true) }
    var newsList by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("전체") }

    LaunchedEffect(Unit) {
        newsList = runCatching {
            RetrofitClient.api.getNews().body()?.news ?: emptyList()
        }.getOrDefault(emptyList())
        isLoading = false
    }

    val filteredList = if (selectedCategory == "전체") newsList
                       else newsList.filter { categoryToKorean(it.category) == selectedCategory }

    Scaffold(containerColor = BackgroundNs) { innerPadding ->
        // TopBar 를 Column 본문 안에 직접 그려 Home/Matching/My 와 패턴 통일 (design.md §17).
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NewsTopBar(unreadCount = unreadCount, onNotificationClick = onNotificationClick)
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Orange500Ns)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                ) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                            NewsSearchBar()
                            NewsCategoryFilter(
                                selected = selectedCategory,
                                onSelect = { selectedCategory = it },
                            )
                        }
                    }

                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "소식이 없습니다",
                                fontFamily = PretendardFamily,
                                fontSize = 16.sp,
                                color = Brown700Ns,
                            )
                        }
                    }
                } else {
                    val featuredItem = filteredList[0]
                    val gridItems = filteredList.drop(1).take(4)
                    val listItems = filteredList.drop(5)

                    item {
                        Spacer(Modifier.height(16.dp))
                        FeaturedNewsCard(
                            item = featuredItem,
                            onClick = { onNewsDetailClick(featuredItem.newsId ?: "") },
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    if (gridItems.size >= 2) {
                        item {
                            NewsGridRow(
                                left = gridItems[0],
                                right = gridItems[1],
                                onItemClick = onNewsDetailClick,
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    if (gridItems.size >= 4) {
                        item {
                            NewsGridRow(
                                left = gridItems[2],
                                right = gridItems[3],
                                onItemClick = onNewsDetailClick,
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    items(listItems, key = { it.newsId ?: it.hashCode().toString() }) { item ->
                        NewsListRow(item = item, onClick = { onNewsDetailClick(item.newsId ?: "") })
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
        }  // end Column (TopBar wrapper)
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun NewsTopBar(unreadCount: Int = 0, onNotificationClick: () -> Unit = {}) {
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
            NewsTopBarIcon { Icon(painter = painterResource(R.drawable.ic_bookmark), null, tint = TextBlackNs, modifier = Modifier.size(18.dp)) }
            Box {
                NewsTopBarIcon(onClick = onNotificationClick) {
                    Icon(painter = painterResource(R.drawable.ic_notifications), contentDescription = "알림", tint = TextBlackNs, modifier = Modifier.size(18.dp))
                }
                if (unreadCount > 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Pink500Ns),
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            fontFamily = PretendardFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsTopBarIcon(onClick: () -> Unit = {}, content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(30.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { onClick() },
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
        Icon(painter = painterResource(R.drawable.ic_search), null, tint = Brown700Ns, modifier = Modifier.size(20.dp))
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
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Brush.linearGradient(listOf(Brown900Ns, Color(0xFFBF8A63)))),
        )
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(categoryColor(item.category))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = categoryToKorean(item.category),
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Text(
                text = item.title ?: "",
                fontFamily = PretendardFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 28.sp,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOfNotNull(item.publishedDate, item.publisher).joinToString(" · "),
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
    onItemClick: (String) -> Unit = {},
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NewsGridCard(item = left,  modifier = Modifier.weight(1f), onClick = { onItemClick(left.newsId ?: "") })
        NewsGridCard(item = right, modifier = Modifier.weight(1f), onClick = { onItemClick(right.newsId ?: "") })
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(categoryImageBg(item.category)),
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = categoryToKorean(item.category),
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = categoryColor(item.category),
            )
            Text(
                text = item.title ?: "",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp,
                color = TextBlackNs,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.publishedDate ?: "",
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
            text = categoryToKorean(item.category),
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = categoryColor(item.category),
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = item.title ?: "",
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = TextBlackNs,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = item.publishedDate ?: "",
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
