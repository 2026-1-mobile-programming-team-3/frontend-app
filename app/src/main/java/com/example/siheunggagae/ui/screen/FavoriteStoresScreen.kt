package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.data.model.FavoriteStoreItem
import com.example.siheunggagae.data.model.StoreCategory
import com.example.siheunggagae.ui.viewmodel.FavoriteStoresUiState
import com.example.siheunggagae.ui.viewmodel.FavoriteStoresViewModel

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import kotlinx.coroutines.flow.MutableStateFlow

// 컬러
private val Brown900F    = Color(0xFF614B3A)
private val Brown700F    = Color(0xFF8A6E58)
private val Brown400F    = Color(0xFFC4A882)
private val Orange500F   = Color(0xFFF7A35B)
private val Pink500F     = Color(0xFFF04268)
private val Green500F    = Color(0xFF00A63E)
private val Gray300F     = Color(0xFFE8E8E8)
private val BackgroundF  = Color(0xFFFEFEFE)
private val TextBlackF   = Color(0xFF1E120A)
private val StarYellowF  = Color(0xFFFDC700)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun FavoriteStoresScreen(
    viewModel: FavoriteStoresViewModel? = null,
    onBack: () -> Unit = {},
    onPlaceDetailClick: (storeId: Int) -> Unit = {},
) {
    val uiState by remember(viewModel) {
        viewModel?.uiState ?: MutableStateFlow(FavoriteStoresUiState.Loading)
    }.collectAsState()

    Scaffold(
        containerColor = BackgroundF,
        topBar = { FavoriteStoresTopBar(onBack = onBack) },
    ) { innerPadding ->
        when (val state = uiState) {
            is FavoriteStoresUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Pink500F)
                }
            }

            is FavoriteStoresUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = state.message,
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            color = Brown700F,
                            textAlign = TextAlign.Center,
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Orange500F)
                                .clickable { viewModel?.fetchStores() }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                        ) {
                            Text("다시 시도", fontFamily = PretendardFamily, fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }

            is FavoriteStoresUiState.Success -> {
                if (state.items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Gray300F,
                                modifier = Modifier.size(48.dp),
                            )
                            Text(
                                text = "즐겨찾기한 매장이 없어요",
                                fontFamily = PretendardFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Brown700F,
                            )
                            Text(
                                text = "매장 상세에서 하트를 눌러 추가해보세요",
                                fontFamily = PretendardFamily,
                                fontSize = 13.sp,
                                color = Brown400F,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "즐겨찾기 ${state.items.size}곳",
                                fontFamily = PretendardFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 20.sp,
                                color = Brown700F,
                            )
                        }
                        items(state.items, key = { it.favoriteId ?: 0 }) { item ->
                            FavoriteStoreCard(
                                item = item,
                                onCardClick = { item.storeId?.let { onPlaceDetailClick(it) } },
                                onHeartClick = { item.storeId?.let { viewModel?.removeFavorite(it) } },
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun FavoriteStoresTopBar(onBack: () -> Unit) {
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
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onBack() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "뒤로",
                tint = TextBlackF,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = "즐겨찾기 매장",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackF,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// ─── 매장 카드 ─────────────────────────────────────────────────────────────────

@Composable
private fun FavoriteStoreCard(
    item: FavoriteStoreItem,
    onCardClick: () -> Unit,
    onHeartClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 썸네일 (그라디언트 플레이스홀더 + 카테고리 아이콘)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFD0FEE1), Color(0xFFE0F7FA))
                        )
                    ),
            ) {
                Icon(
                    imageVector = item.category.categoryIcon(),
                    contentDescription = null,
                    tint = Brown700F,
                    modifier = Modifier.size(32.dp),
                )
            }

            // 이름 + 카테고리·평점
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name ?: "",
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 24.sp,
                    color = TextBlackF,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = item.category ?: "",
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Brown700F,
                    )
                    Text("·", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown400F)
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = StarYellowF,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = item.ratingAvg?.let { "%.1f".format(it) } ?: "-",
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Brown700F,
                    )
                }
            }

            // 하트 버튼 (채워진 상태 → 탭하면 해제)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .clickable { onHeartClick() },
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "즐겨찾기 해제",
                    tint = Pink500F,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

private fun String?.categoryIcon(): ImageVector = when (this) {
    StoreCategory.CAFE.label, StoreCategory.CAFE.apiValue       -> Icons.Default.LocalCafe
    StoreCategory.RESTAURANT.label, StoreCategory.RESTAURANT.apiValue -> Icons.Default.Restaurant
    StoreCategory.PARK.label, StoreCategory.PARK.apiValue       -> Icons.Default.NaturePeople
    else                                                          -> Icons.Default.LocalCafe
}

// ─── Preview ───────────────────────────────────────────────────────────────────

private val previewItems = listOf(
    FavoriteStoreItem(1, 101, "댕댕 카페",  StoreCategory.CAFE.label,       null, 4.9, "2026-05-01T10:00:00"),
    FavoriteStoreItem(2, 102, "행복 공원",  StoreCategory.PARK.label,       null, 4.5, "2026-05-02T10:00:00"),
    FavoriteStoreItem(3, 103, "멍이네 식당", StoreCategory.RESTAURANT.label, null, 4.2, "2026-05-03T10:00:00"),
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavoriteStoresScreenPreview() {
    SiheungGagaeTheme {
        val flow = MutableStateFlow<FavoriteStoresUiState>(FavoriteStoresUiState.Success(previewItems))
        FavoriteStoresScreen(
            viewModel = null,
            onBack = {},
            onPlaceDetailClick = {},
        )
    }
}
