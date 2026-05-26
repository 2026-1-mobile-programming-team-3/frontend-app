package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.R
import com.example.siheunggagae.data.model.MatchListItem
import com.example.siheunggagae.data.model.VolunteerStatsResponse
import com.example.siheunggagae.ui.viewmodel.VolunteerHistoryUiState
import com.example.siheunggagae.ui.viewmodel.VolunteerHistoryViewModel

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
private val Brown900H    = Color(0xFF614B3A)
private val Brown700H    = Color(0xFF8A6E58)
private val Brown400H    = Color(0xFFC4A882)
private val BrownBorderH = Color(0xFFE8D3C2)
private val Orange500H   = Color(0xFFF7A35B)
private val Pink500H     = Color(0xFFF04268)
private val Green500H    = Color(0xFF00A63E)
private val Gray300H     = Color(0xFFE8E8E8)
private val BackgroundH  = Color(0xFFFEFEFE)
private val TextBlackH   = Color(0xFF1E120A)
private val StarYellow   = Color(0xFFFDC700)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun VolunteerHistoryScreen(
    viewModel: VolunteerHistoryViewModel? = null,
    onBack: () -> Unit = {},
    onMatchClick: (matchId: Int) -> Unit = {},
) {
    val uiState by remember(viewModel) {
        viewModel?.uiState ?: MutableStateFlow(VolunteerHistoryUiState.Loading)
    }.collectAsState()

    Scaffold(
        containerColor = BackgroundH,
        topBar = { HistoryTopBar(onBack = onBack) },
    ) { innerPadding ->
        when (val state = uiState) {
            is VolunteerHistoryUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Orange500H)
                }
            }

            is VolunteerHistoryUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        color = Brown700H,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            is VolunteerHistoryUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    item { VolunteerStatsCard(stats = state.stats) }

                    if (state.matches.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 36.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = "아직 완료된 봉사 활동이 없어요",
                                    fontFamily = PretendardFamily,
                                    fontSize = 14.sp,
                                    color = Brown400H,
                                )
                                Text(
                                    text = "진행 중인 매칭이 있는지 확인해 보세요.",
                                    fontFamily = PretendardFamily,
                                    fontSize = 12.sp,
                                    color = Brown400H,
                                )
                            }
                        }
                    } else {
                        items(state.matches) { match ->
                            MatchHistoryCard(
                                match = match,
                                onClick = { match.matchId?.let { onMatchClick(it) } },
                            )
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun HistoryTopBar(onBack: () -> Unit) {
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
                tint = TextBlackH,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = "봉사 활동 이력",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackH,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// ─── 통계 카드 ─────────────────────────────────────────────────────────────────

@Composable
private fun VolunteerStatsCard(stats: VolunteerStatsResponse) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem(
                value = "${stats.totalCount ?: 0}건",
                label = "누적 봉사",
                valueColor = Green500H,
                modifier = Modifier.weight(1f),
            )
            StatDivider()
            StatItem(
                value = stats.avgRating?.let { String.format("%.1f", it) } ?: "—",
                label = "평균 평점",
                valueColor = StarYellow,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            text = value,
            fontFamily = PretendardFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 30.sp,
            color = valueColor,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp,
            color = Brown700H,
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(Gray300H),
    )
}

// ─── 매칭 이력 카드 ─────────────────────────────────────────────────────────────

@Composable
private fun MatchHistoryCard(match: MatchListItem, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 완료 chip + 평점
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFFF0FDF4))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "완료",
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Green500H,
                    )
                }
                if (match.receivedRating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = StarYellow,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", match.receivedRating.toDouble()),
                            fontFamily = PretendardFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextBlackH,
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // 제목
            Text(
                text = match.title ?: "제목 없음",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp,
                color = TextBlackH,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Gray300H)
            Spacer(Modifier.height(10.dp))

            // 메타 정보 행
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!match.desiredDate.isNullOrBlank()) {
                        MetaChip(
                            icon = { Icon(Icons.Default.CalendarMonth, null, tint = Brown700H, modifier = Modifier.size(13.dp)) },
                            text = match.desiredDate.take(10),
                        )
                    }
                    if (!match.address.isNullOrBlank()) {
                        MetaChip(
                            icon = { Icon(Icons.Default.LocationOn, null, tint = Brown700H, modifier = Modifier.size(13.dp)) },
                            text = match.address,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Brown400H,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun MetaChip(icon: @Composable () -> Unit, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        icon()
        Text(
            text = text,
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Brown700H,
        )
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VolunteerHistoryScreenPreview() {
    SiheungGagaeTheme { VolunteerHistoryScreen() }
}