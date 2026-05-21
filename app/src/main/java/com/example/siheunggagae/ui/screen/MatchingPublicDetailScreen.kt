package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.R
import com.example.siheunggagae.Screen

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.data.model.MatchDetailResponse
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import com.example.siheunggagae.ui.viewmodel.MatchDetailUiState
import com.example.siheunggagae.ui.viewmodel.MatchDetailViewModel
import kotlinx.coroutines.flow.MutableStateFlow

// ─── 스펙 컬러 ────────────────────────────────────────────────────────────
private val Brown700P     = Color(0xFF8A6E58)
private val Brown400P     = Color(0xFFC4A882)
private val BrownBorderP  = Color(0xFFE8D3C2)
private val Orange500P    = Color(0xFFF7A35B)
private val Pink500P      = Color(0xFFF04268)
private val MintLightP    = Color(0xFFD0FEE1)
private val OrangeSandP   = Color(0xFFFFEDD4)
private val PinkSurfaceP  = Color(0xFFFEE7EC)
private val BackgroundP   = Color(0xFFFEFEFE)
private val Gray300P      = Color(0xFFE8E8E8)
private val TextBlackP    = Color(0xFF1E120A)
private val Green600P     = Color(0xFF00A63E)

// ─── 메인 화면 ──────────────────────────────────────────────────────────────────

@Composable
fun MatchingPublicDetailScreen(
    requestId: Int = 0,
    viewModel: MatchDetailViewModel? = null,
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    val uiState by remember(viewModel) {
        viewModel?.uiState ?: MutableStateFlow(
            MatchDetailUiState.Success(
                MatchDetailResponse(
                    title = "정왕동 실외견 병원 이동 부탁드립니다",
                    status = "봉사자 모집 중",
                    desiredDate = "2026-05-10T10:00:00Z",
                    address = "정왕 동물병원",
                    content = "슬개골 탈구 수술 후 경과 검진입니다. 차량 이동이 가능하신 분이면 좋겠어요."
                )
            )
        )
    }.collectAsState()

    LaunchedEffect(requestId) {
        viewModel?.fetchDetail(requestId)
    }

    Scaffold(
        containerColor = BackgroundP,
        topBar = { PublicDetailTopBar(onBack = onBack) },
        bottomBar = { PublicDetailBottomBar(onApply = {}, onChat = { onNavigate(Screen.Chat.createRoute(requestId)) }) },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is MatchDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        color = Orange500P,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                }
                is MatchDetailUiState.Error -> {
                    Text(
                        text = state.message,
                        color = Pink500P,
                        fontFamily = PretendardFamily,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                }
                is MatchDetailUiState.Success -> {
                    val request = state.detail

                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            PublicStatusBanner(statusText = request.status ?: "모집 중")
                            PublicRequestInfoCard(request = request)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            PublicRouteRow(destination = request.address ?: "목적지 미정")
                            PublicMapCard()
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "요청자 정보",
                                fontFamily = PretendardFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 20.sp,
                                color = Brown700P,
                            )
                            // 👈 실제 작성자의 중첩 객체 닉네임 매핑 완료!
                            RequesterCard(
                                authorNickname = request.author?.nickname ?: "요청자",
                                onChat = { onNavigate(Screen.Chat.createRoute(requestId)) }
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun PublicDetailTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = androidx.compose.ui.Alignment.Center,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.CenterStart)
                .size(40.dp)
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onBack() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "뒤로",
                tint = TextBlackP,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = "이동 지원 요청",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackP,
            modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
        )
        Row(
            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PublicTopBarIcon(iconRes = R.drawable.ic_bookmark, desc = "북마크")
            PublicTopBarIcon(iconRes = R.drawable.ic_share, desc = "공유")
        }
    }
}

@Composable
private fun PublicTopBarIcon(iconRes: Int, desc: String) {
    Box(
        contentAlignment = androidx.compose.ui.Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { },
    ) {
        Icon(painter = painterResource(iconRes), contentDescription = desc, tint = TextBlackP, modifier = Modifier.size(20.dp))
    }
}

// ─── 상태 배너 ─────────────────────────────────────────────────────────────────

@Composable
private fun PublicStatusBanner(statusText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(PinkSurfaceP)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(
            text = statusText,
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
            color = TextBlackP,
        )
    }
}

// ─── 요청 정보 Card ────────────────────────────────────────────────────────────

@Composable
private fun PublicRequestInfoCard(request: MatchDetailResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = request.title ?: "제목 없음",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp,
            color = TextBlackP,
        )

        // T 나 공백( ) 기준 모두 안전하게 분리해 내는 스펙 파서로 교체!
        val dateValue = request.desiredDate ?: "일정 미정"
        val timeValue = request.desiredTime?.take(5) ?: "시간 미정"

        PublicInfoRow(iconBg = PinkSurfaceP, iconRes = R.drawable.ic_calendar_today, iconTint = Pink500P, label = "일정", value = dateValue, valueFontWeight = FontWeight.Bold)
        HorizontalDivider(color = Gray300P)
        PublicInfoRow(iconBg = OrangeSandP, iconRes = R.drawable.ic_schedule, iconTint = Orange500P, label = "시간", value = timeValue)
        HorizontalDivider(color = Gray300P)
        PublicInfoRow(iconBg = MintLightP, iconRes = R.drawable.ic_location_on, iconTint = Green600P, label = "목적지", value = request.address ?: "미정")
        HorizontalDivider(color = Gray300P)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PublicIconBox(bg = BrownBorderP) {
                Icon(painter = painterResource(R.drawable.ic_pets), null, tint = Brown700P, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "반려동물", fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp, color = Brown700P)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(OrangeSandP)
                        .padding(horizontal = 12.dp, vertical = 3.dp),
                ) {
                    // 👈 하드코딩 걷어내고 실제 내 펫의 이름과 종이 나오도록 실시간 연결!
                    val petName = request.pet?.name ?: "이름 없음"
                    val petSpecies = request.pet?.species ?: "종 미정"
                    Text(text = "$petName · $petSpecies", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, color = Brown700P)
                }
            }
        }
        HorizontalDivider(color = Gray300P)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PublicIconBox(bg = Gray300P) {
                Icon(painter = painterResource(R.drawable.ic_chat_bubble), null, tint = TextBlackP, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "요청 메모", fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp, color = Brown700P)
                Spacer(Modifier.height(4.dp))
                Text(text = request.content ?: "메모 없음", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp, color = TextBlackP)
            }
        }
    }
}

@Composable
private fun PublicInfoRow(
    iconBg: Color,
    iconRes: Int,
    iconTint: Color,
    label: String,
    value: String,
    valueFontWeight: FontWeight = FontWeight.Medium,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PublicIconBox(bg = iconBg) {
            Icon(painter = painterResource(iconRes), null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp, color = Brown700P)
            Spacer(Modifier.height(2.dp))
            Text(text = value, fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = valueFontWeight, lineHeight = 24.sp, color = TextBlackP)
        }
    }
}

@Composable
private fun PublicIconBox(bg: Color, content: @Composable () -> Unit) {
    Box(
        contentAlignment = androidx.compose.ui.Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg),
    ) { content() }
}

// ─── 경로 Row ──────────────────────────────────────────────────────────────────

@Composable
private fun PublicRouteRow(destination: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painter = painterResource(R.drawable.ic_location_on), null, tint = Orange500P, modifier = Modifier.size(16.dp))
        Text(text = "출발지 인근", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp, color = TextBlackP)
        Text(text = "→", fontFamily = PretendardFamily, fontSize = 14.sp, color = Brown400P)
        Text(text = destination, fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp, color = TextBlackP, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ─── 경로 지도 Card ────────────────────────────────────────────────────────────

@Composable
private fun PublicMapCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(MintLightP, Color(0xFFE8FAF0), Color(0xFFF4FDFB)))),
    ) {
        Icon(painter = painterResource(R.drawable.ic_location_on), null, tint = Pink500P,
            modifier = Modifier.size(28.dp).align(androidx.compose.ui.Alignment.Center).offset((-40).dp, (-20).dp))
        Icon(painter = painterResource(R.drawable.ic_location_on), null, tint = Orange500P,
            modifier = Modifier.size(28.dp).align(androidx.compose.ui.Alignment.Center).offset(40.dp, 20.dp))

        Row(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White)
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(painter = painterResource(R.drawable.ic_map), null, tint = TextBlackP, modifier = Modifier.size(14.dp))
            Text(text = "지도에서 보기", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, color = TextBlackP)
        }
    }
}

// ─── 요청자 정보 Card ──────────────────────────────────────────────────────────

@Composable
private fun RequesterCard(authorNickname: String, onChat: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = androidx.compose.ui.Alignment.Center,
            modifier = Modifier.size(48.dp).clip(CircleShape).background(MintLightP),
        ) {
            Text(text = authorNickname.firstOrNull()?.toString() ?: "요", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextBlackP)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = authorNickname, fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp, color = TextBlackP)
            Spacer(Modifier.height(2.dp))
            Text(text = "⭐ 4.9 · 시흥개개 신뢰 회원", fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp, color = Brown700P)
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White)
                .border(1.dp, BrownBorderP, RoundedCornerShape(50.dp))
                .clickable { onChat() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(painter = painterResource(R.drawable.ic_chat_bubble), null, tint = Brown700P, modifier = Modifier.size(14.dp))
            Text(text = "채팅", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, color = Brown700P)
        }
    }
}

// ─── 하단 고정 버튼 바 ─────────────────────────────────────────────────────────

@Composable
private fun PublicDetailBottomBar(onApply: () -> Unit, onChat: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = androidx.compose.ui.Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, BrownBorderP, CircleShape)
                .clickable { onChat() },
        ) {
            Icon(painter = painterResource(R.drawable.ic_chat_bubble), contentDescription = "채팅", tint = Brown700P, modifier = Modifier.size(20.dp))
        }

        Box(
            contentAlignment = androidx.compose.ui.Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Pink500P)
                .clickable { onApply() },
        ) {
            Text(text = "봉사 신청하기", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp, color = Color.White)
        }
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MatchingPublicDetailScreenPreview() {
    SiheungGagaeTheme { MatchingPublicDetailScreen() }
}