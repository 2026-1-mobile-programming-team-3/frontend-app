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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.Brown40
import com.example.siheunggagae.ui.theme.Gray10
import com.example.siheunggagae.ui.theme.Gray40
import com.example.siheunggagae.ui.theme.Gray80
import com.example.siheunggagae.ui.theme.Gray90
import com.example.siheunggagae.ui.theme.Gray95
import com.example.siheunggagae.ui.theme.MapDeep
import com.example.siheunggagae.ui.theme.MapMint
import com.example.siheunggagae.ui.theme.MapSky
import com.example.siheunggagae.ui.theme.Orange40
import com.example.siheunggagae.ui.theme.Pink60
import com.example.siheunggagae.ui.theme.Pink90
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import com.example.siheunggagae.ui.theme.WalkGood

// ══════════════════════════════════════════════════════════════════════════════
// NewsScreen
// ══════════════════════════════════════════════════════════════════════════════

internal data class NewsItem(
    val id: Int,
    val category: String,
    val title: String,
    val date: String,
    val source: String? = null,
)

private val newsCategories = listOf("전체", "정책", "행사", "봉사", "지원", "정보")

internal data class TagStyle(val textColor: Color, val bgColor: Color)

private val newsTagStyleMap = mapOf(
    "정책" to TagStyle(Color(0xFF0284C7), Color(0xFFE0F2FE)),
    "행사" to TagStyle(Color(0xFF7C3AED), Color(0xFFF5F3FF)),
    "봉사" to TagStyle(Color(0xFF059669), Color(0xFFECFDF5)),
    "지원" to TagStyle(Color(0xFF2563EB), Color(0xFFEFF6FF)),
    "정보" to TagStyle(Orange40, Color(0xFFFFF3E0)),
)

private val sampleNews = listOf(
    NewsItem(1, "정책", "2025년 반려동물 등록 의무화 안내", "5월 10일", "시흥시청"),
    NewsItem(2, "행사", "봄맞이 반려동물 입양 행사", "5월 12일", "시흥유기견센터"),
    NewsItem(3, "봉사", "이동 봉사 모집 중 (5~6월)", "5월 14일"),
    NewsItem(4, "지원", "중성화 수술 지원 사업 신청 안내", "5월 15일", "시흥시청"),
    NewsItem(5, "정보", "반려동물 동반 가능 공원 TOP 5", "5월 16일"),
    NewsItem(6, "정책", "외래 동물 검역 강화 안내", "5월 17일", "농림축산식품부"),
    NewsItem(7, "행사", "시흥 펫 페어 2026 개최", "5월 18일"),
    NewsItem(8, "봉사", "노령견 의료비 지원 봉사 모집", "5월 20일"),
)

@Composable
fun NewsScreen(
    onPlaceDetailClick: (Int) -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    var selectedCategory by remember { mutableStateOf("전체") }
    val filtered = if (selectedCategory == "전체") sampleNews
    else sampleNews.filter { it.category == selectedCategory }

    Scaffold(
        containerColor = Gray95,
        topBar = { NewsTopBar() },
        bottomBar = { AppBottomBar(currentRoute = Screen.News.route, onNavigate = onNavigate) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // 카테고리 필터 칩
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                newsCategories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) Gray10 else Gray90)
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Gray40
                        )
                    }
                }
            }
            HorizontalDivider(color = Gray90)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered, key = { it.id }) { item ->
                    NewsListItem(item = item, onClick = { onPlaceDetailClick(item.id) })
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = Gray90
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun NewsTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "반려동물 소식",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Gray10,
        )
    }
}

@Composable
private fun NewsListItem(item: NewsItem, onClick: () -> Unit = {}) {
    val style = newsTagStyleMap[item.category] ?: TagStyle(Gray40, Gray90)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(style.bgColor)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = item.category,
                fontSize = 11.sp,
                color = style.textColor,
                fontWeight = FontWeight.Medium
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 14.sp,
                color = Gray10,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (item.source != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = item.source, fontSize = 11.sp, color = Gray40)
            }
        }
        Text(text = item.date, fontSize = 12.sp, color = Gray40)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PlaceDetailScreen
// ══════════════════════════════════════════════════════════════════════════════

internal data class PlaceReview(
    val id: Int,
    val authorName: String,
    val rating: Int,
    val date: String,
    val content: String,
    val avatarColor: Color,
)

private val sampleReviews = listOf(
    PlaceReview(1, "댕댕맘", 5, "2025.04.15", "친절하게 잘 도와주셨어요! 강아지가 무서워하지 않아서 좋았습니다.", Color(0xFFE91E63)),
    PlaceReview(2, "냥이아빠", 4, "2025.04.02", "전반적으로 만족스러웠어요. 다음에도 이용할 것 같습니다.", Color(0xFF7C3AED)),
    PlaceReview(3, "초코사랑", 5, "2025.03.28", "예약도 편하고 직원분들이 너무 친절했어요.", Color(0xFFF97316)),
)

@Composable
fun PlaceDetailScreen(placeId: Int = 0, onBack: () -> Unit = {}) {
    Scaffold(
        containerColor = Gray95,
        topBar = { PlaceDetailTopBar(onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 상단 배너 (지도 미리보기 placeholder)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Brush.linearGradient(listOf(MapSky, MapMint, MapDeep)))
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Pink60,
                    modifier = Modifier.size(40.dp).align(Alignment.Center)
                )
            }

            // 장소명 + 업종 + 별점
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text(text = "댕댕 카페", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gray10)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "카페 · 0.3km", fontSize = 14.sp, color = Gray40)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(text = "4.8", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Gray10)
                    Text(text = "(후기 128)", fontSize = 13.sp, color = Gray40)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 정보 카드 (영업시간 / 주소 / 전화)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                PlaceInfoRow(
                    icon = Icons.Default.Schedule,
                    text = "매일 09:00 ~ 21:00"
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFECFDF5))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "영업 중", fontSize = 12.sp, color = WalkGood, fontWeight = FontWeight.Bold)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Gray90)
                PlaceInfoRow(
                    icon = Icons.Default.LocationOn,
                    text = "경기도 시흥시 정왕동 1234-5"
                ) { SmallCopyButton() }
                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Gray90)
                PlaceInfoRow(
                    icon = Icons.Default.Call,
                    text = "031-123-4567"
                ) { SmallCopyButton() }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 위치 지도 미리보기 카드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(MapSky, MapMint, MapDeep)))
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Pink60,
                    modifier = Modifier.size(32.dp).align(Alignment.Center)
                )
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
                        tint = Orange40,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(text = "정왕동", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Gray10)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 리뷰 섹션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "4.8", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gray10)
                    Text(
                        text = "/ 5.0  (총 128개)",
                        fontSize = 13.sp,
                        color = Gray40,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                sampleReviews.forEachIndexed { idx, review ->
                    ReviewItem(review = review)
                    if (idx < sampleReviews.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Gray90)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PlaceDetailTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 4.dp, vertical = 10.dp)
    ) {
        Text(
            text = "< 뒤로",
            fontSize = 14.sp,
            color = Gray40,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable { onBack() }
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Row(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = "북마크", tint = Gray40)
            }
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "공유", tint = Gray40)
            }
        }
    }
}

@Composable
private fun PlaceInfoRow(
    icon: ImageVector,
    text: String,
    action: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Gray40, modifier = Modifier.size(20.dp))
        Text(text = text, fontSize = 14.sp, color = Gray10, modifier = Modifier.weight(1f))
        action()
    }
}

@Composable
private fun SmallCopyButton() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Gray80, RoundedCornerShape(6.dp))
            .clickable { }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = "복사", fontSize = 12.sp, color = Gray40)
    }
}

@Composable
private fun ReviewItem(review: PlaceReview) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(review.avatarColor)
            ) {
                Text(
                    text = review.authorName.first().toString(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = review.authorName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray10)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row {
                        repeat(review.rating) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Text(text = review.date, fontSize = 11.sp, color = Gray40)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = review.content, fontSize = 13.sp, color = Gray10, lineHeight = 20.sp)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// MapFilterBottomSheet
// ══════════════════════════════════════════════════════════════════════════════

data class FilterCategory(
    val icon: ImageVector,
    val label: String,
    val isChecked: Boolean,
)

private val defaultFilterCategories = listOf(
    FilterCategory(Icons.Default.LocalCafe, "카페", true),
    FilterCategory(Icons.Default.Restaurant, "식당", false),
    FilterCategory(Icons.Default.Park, "공원", true),
    FilterCategory(Icons.Default.LocalHospital, "동물병원", false),
    FilterCategory(Icons.Default.ContentCut, "미용", false),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapFilterBottomSheet(
    onDismiss: () -> Unit = {},
    onApply: (List<FilterCategory>) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categories = remember { mutableStateListOf(*defaultFilterCategories.toTypedArray()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        MapFilterContent(
            categories = categories,
            onToggle = { idx -> categories[idx] = categories[idx].copy(isChecked = !categories[idx].isChecked) },
            onDismiss = onDismiss,
            onApply = { onApply(categories.toList()) }
        )
    }
}

@Composable
private fun MapFilterContent(
    categories: List<FilterCategory>,
    onToggle: (Int) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 24.dp)
    ) {
        // 제목 + X
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "지도 보기 설정",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Gray10
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "닫기", tint = Gray40)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Gray90)
        Spacer(modifier = Modifier.height(4.dp))

        // 체크박스 리스트
        categories.forEachIndexed { idx, cat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(idx) }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (cat.isChecked) Brown40.copy(alpha = 0.1f) else Gray90)
                ) {
                    Icon(
                        imageVector = cat.icon,
                        contentDescription = null,
                        tint = if (cat.isChecked) Brown40 else Gray40,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = cat.label,
                    fontSize = 15.sp,
                    fontWeight = if (cat.isChecked) FontWeight.Medium else FontWeight.Normal,
                    color = if (cat.isChecked) Gray10 else Gray40,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = cat.isChecked,
                    onCheckedChange = { onToggle(idx) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Brown40,
                        checkmarkColor = Color.White,
                        uncheckedColor = Gray80,
                    )
                )
            }
            if (idx < categories.lastIndex) {
                HorizontalDivider(color = Gray90)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 적용하기 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Brown40)
                .clickable { onApply() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "적용하기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}


// ══════════════════════════════════════════════════════════════════════════════
// Previews
// ══════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NewsScreenPreview() {
    SiheungGagaeTheme { NewsScreen() }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PlaceDetailScreenPreview() {
    SiheungGagaeTheme { PlaceDetailScreen() }
}

@Preview(showBackground = true)
@Composable
fun MapFilterContentPreview() {
    val cats = remember { mutableStateListOf(*defaultFilterCategories.toTypedArray()) }
    SiheungGagaeTheme {
        MapFilterContent(
            categories = cats,
            onToggle = { idx -> cats[idx] = cats[idx].copy(isChecked = !cats[idx].isChecked) },
            onDismiss = {},
            onApply = {}
        )
    }
}
