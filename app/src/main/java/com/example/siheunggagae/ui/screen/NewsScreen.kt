package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.AppBottomBar
import com.example.siheunggagae.R
import com.example.siheunggagae.Screen
import com.example.siheunggagae.data.model.NewsItem
import com.example.siheunggagae.data.network.RetrofitClient
import com.example.siheunggagae.ui.component.EmptyStateView

import androidx.compose.foundation.Image
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import com.example.siheunggagae.ui.util.appleSpec
import com.example.siheunggagae.ui.util.appleTapScale
import com.example.siheunggagae.ui.util.newsFallbackDrawable
import com.example.siheunggagae.ui.util.rememberAppleInteractionSource
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    onNewsDetailClick: (String) -> Unit = {},
    onPlaceDetailClick: (Int) -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
) {
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var newsList by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("전체") }
    var showSearch by remember { mutableStateOf(false) }

    LaunchedEffect(refreshKey) {
        if (refreshKey > 0) isRefreshing = true else isLoading = true
        newsList = runCatching {
            RetrofitClient.api.getNews().body()?.news ?: emptyList()
        }.getOrDefault(emptyList())
        isLoading = false
        isRefreshing = false
    }

    val filteredList = remember(selectedCategory, newsList) {
        if (selectedCategory == "전체") newsList
        else newsList.filter { categoryToKorean(it.category) == selectedCategory }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(containerColor = BackgroundNs) { innerPadding ->
            // TopBar 를 Column 본문 안에 직접 그려 Home/Matching/My 와 패턴 통일 (design.md §17).
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                NewsTopBar(onNotificationClick = onNotificationClick)
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { refreshKey++ },
                    modifier = Modifier.fillMaxSize(),
                ) {
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
                                NewsSearchBar(onClick = { showSearch = true })
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
                    }  // end LazyColumn
                }  // end else (isLoading)
                }  // end PullToRefreshBox
            }  // end Column (TopBar wrapper)
        }

        if (showSearch) {
            NewsSearchOverlay(
                allNews = newsList,
                onDismiss = { showSearch = false },
                onResultClick = { newsId ->
                    showSearch = false
                    onNewsDetailClick(newsId)
                },
            )
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun NewsTopBar(onNotificationClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "소식",
            fontFamily = PretendardFamily,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            letterSpacing = (-0.91).sp,
            color = TextBlackNs,
        )
        // T2: 우측 알림 액션 — Home/Matching/My 와 정합. 휑하던 우측 공간 해소.
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.size(40.dp).clickable { onNotificationClick() },
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    painter = painterResource(R.drawable.ic_notifications),
                    contentDescription = "알림",
                    tint = Brown700Ns,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

// ─── 검색바 ────────────────────────────────────────────────────────────────────

@Composable
private fun NewsSearchBar(onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(50.dp))
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White)
            .clickable { onClick() }
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        newsCategories.forEach { cat ->
            val isSelected = cat == selected
            val bg by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF1A1A1A) else Color.White,
                animationSpec = appleSpec(),
                label = "newsChipBg",
            )
            val fg by animateColorAsState(
                targetValue = if (isSelected) Color.White else Brown700Ns,
                animationSpec = appleSpec(),
                label = "newsChipFg",
            )
            val interaction = rememberAppleInteractionSource()
            Box(
                modifier = Modifier
                    .appleTapScale(interaction)
                    .shadow(2.dp, RoundedCornerShape(50.dp))
                    .clip(RoundedCornerShape(50.dp))
                    .background(bg)
                    .clickable(interactionSource = interaction, indication = null) { onSelect(cat) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = cat,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    color = fg,
                )
            }
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
            // N2: 메인 카드 그림자 강화 — 2dp → 4dp 로 분리감 보강.
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.BottomStart,
    ) {
        val fallbackPainter = painterResource(newsFallbackDrawable(item.newsId))
        if (!item.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                error = fallbackPainter,
                fallback = fallbackPainter,
                placeholder = fallbackPainter,
            )
        } else {
            Image(
                painter = fallbackPainter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
        // N1: 텍스트 가독성을 위한 어두운 그라디언트 오버레이 강화.
        // 카드 상단 40% 까지는 투명, 하단으로 갈수록 0.85 까지 어두워짐.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.4f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.85f),
                        ),
                    ),
                ),
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
            // N2: 2열 카드 그림자 강화 — 2dp → 4dp, 배경과 분리되도록.
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(categoryImageBg(item.category)),
        ) {
            val listFallback = painterResource(newsFallbackDrawable(item.newsId))
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = listFallback,
                    fallback = listFallback,
                    placeholder = listFallback,
                )
            } else {
                Image(
                    painter = listFallback,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // N3: 카테고리 텍스트 → 컬러 칩 (배경 + 라운드) 으로 변환.
            // 회색 단일에서 의미적 컬러 분리: 정책=Blue, 행사=Green, 봉사=Pink, 지원=Orange.
            val catColor = categoryColor(item.category)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(catColor.copy(alpha = 0.14f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = categoryToKorean(item.category),
                    fontFamily = PretendardFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.11).sp,
                    color = catColor,
                )
            }
            Text(
                text = item.title ?: "",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp,
                letterSpacing = (-0.28).sp,
                color = TextBlackNs,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.publishedDate ?: "",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.12).sp,
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
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), clip = false)
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

// ─── 검색 오버레이 ────────────────────────────────────────────────────────────────

@Composable
private fun NewsSearchOverlay(
    allNews: List<NewsItem>,
    onDismiss: () -> Unit,
    onResultClick: (newsId: String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query, allNews) {
        if (query.isBlank()) emptyList()
        else allNews.filter { (it.title ?: "").contains(query, ignoreCase = true) }
            .take(30)
    }
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // 검색 입력 영역
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .clickable { onDismiss() },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "닫기",
                        tint = Brown700Ns,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .shadow(2.dp, RoundedCornerShape(50.dp))
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "뉴스 · 정책 · 행사 검색",
                            fontFamily = PretendardFamily,
                            fontSize = 15.sp,
                            color = Brown700Ns,
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = PretendardFamily,
                            fontSize = 15.sp,
                            color = Color(0xFF1E120A),
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFF3F4F6))

            when {
                query.isBlank() -> EmptyStateView(
                    title = "어떤 뉴스를 찾으세요?",
                    subtitle = "키워드를 입력해 보세요",
                    iconRes = R.drawable.ic_search,
                )
                results.isEmpty() -> EmptyStateView(
                    title = "검색 결과가 없어요",
                    subtitle = "다른 키워드로 시도해 보세요",
                    iconRes = R.drawable.ic_search,
                )
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(results, key = { it.newsId ?: it.hashCode().toString() }) { news ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { news.newsId?.let { onResultClick(it) } }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = news.title ?: "",
                                    fontFamily = PretendardFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 20.sp,
                                    color = Color(0xFF1E120A),
                                )
                                Text(
                                    text = categoryToKorean(news.category),
                                    fontFamily = PretendardFamily,
                                    fontSize = 12.sp,
                                    color = Brown700Ns,
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = Color(0xFFF3F4F6),
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
fun NewsScreenPreview() {
    SiheungGagaeTheme { NewsScreen() }
}
