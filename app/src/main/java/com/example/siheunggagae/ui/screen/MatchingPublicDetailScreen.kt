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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// ── 스펙 컬러 (Figma 실측) ────────────────────────────────────────────────────
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

// ── 데이터 ────────────────────────────────────────────────────────────────────

data class PublicRequestDetail(
    val id: Int,
    val title: String,
    val statusText: String,
    val applicantCount: Int,
    val dDay: Int?,
    val date: String,
    val time: String,
    val destination: String,
    val petInfo: String,
    val memo: String,
    val originName: String,
    val distanceKm: Float,
)

data class RequesterInfo(
    val name: String,
    val rating: Float,
    val requestCount: Int,
    val distanceKm: Float,
    val avatarColor: Color,
)

private val samplePublicRequest = PublicRequestDetail(
    id = 1,
    title = "정왕동 실외견 병원 이동 부탁드립니다",
    statusText = "봉사자 모집 중 · 신청 2건",
    applicantCount = 2,
    dDay = 2,
    date = "2026년 5월 10일 (일)",
    time = "오전 10:00 출발 예정",
    destination = "정왕 동물병원",
    petInfo = "파댕이 · 강아지 · 중형",
    memo = "슬개골 탈구 수술 후 경과 검진입니다. 차량 이동이 가능하신 분이면 좋겠어요.",
    originName = "정왕동 자택",
    distanceKm = 0.8f,
)

private val sampleRequester = RequesterInfo(
    name = "정왕동주민",
    rating = 4.9f,
    requestCount = 6,
    distanceKm = 1.2f,
    avatarColor = MintLightP,
)

// ── 메인 화면 ──────────────────────────────────────────────────────────────────

@Composable
fun MatchingPublicDetailScreen(
    requestId: Int = 0,
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    Scaffold(
        containerColor = BackgroundP,
        topBar = { PublicDetailTopBar(onBack = onBack) },
        bottomBar = { PublicDetailBottomBar(onApply = {}, onChat = { onNavigate(Screen.Chat.createRoute(samplePublicRequest.id)) }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // 상태 배너 + 요청 정보 Card — pT=8 pB=8 pH=24 gap=16
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PublicStatusBanner(request = samplePublicRequest)
                PublicRequestInfoCard(request = samplePublicRequest)
            }

            // 경로 Row + 지도 Card — pT=16 pB=16 pH=24 gap=12
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PublicRouteRow(request = samplePublicRequest)
                PublicMapCard()
            }

            // 요청자 정보 섹션 — pT=8 pB=8 pH=24 gap=4
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 16.dp),
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
                RequesterCard(requester = sampleRequester, onChat = { onNavigate(Screen.Chat.createRoute(samplePublicRequest.id)) })
            }
        }
    }
}

// ── TopBar ────────────────────────────────────────────────────────────────────

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
            modifier = Modifier.align(Alignment.Center),
        )
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
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
        contentAlignment = Alignment.Center,
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

// ── 상태 배너 ─────────────────────────────────────────────────────────────────

@Composable
private fun PublicStatusBanner(request: PublicRequestDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(PinkSurfaceP)
            .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = request.statusText,
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
            color = TextBlackP,
        )
        if (request.dDay != null) {
            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    text = "D-${request.dDay}",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 16.sp,
                    color = Pink500P,
                )
            }
        }
    }
}

// ── 요청 정보 Card ────────────────────────────────────────────────────────────

@Composable
private fun PublicRequestInfoCard(request: PublicRequestDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 제목 — fs=20 fw=700 lh=28
        Text(
            text = request.title,
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp,
            color = TextBlackP,
        )

        // 일정 — fw=700
        PublicInfoRow(
            iconBg = PinkSurfaceP,
            iconRes = R.drawable.ic_calendar_today,
            iconTint = Pink500P,
            label = "일정",
            value = request.date,
            valueFontWeight = FontWeight.Bold,
        )
        HorizontalDivider(color = Gray300P)

        // 시간 — fw=500
        PublicInfoRow(
            iconBg = OrangeSandP,
            iconRes = R.drawable.ic_schedule,
            iconTint = Orange500P,
            label = "시간",
            value = request.time,
        )
        HorizontalDivider(color = Gray300P)

        // 목적지 — fw=500
        PublicInfoRow(
            iconBg = MintLightP,
            iconRes = R.drawable.ic_location_on,
            iconTint = Green600P,
            label = "목적지",
            value = request.destination,
        )
        HorizontalDivider(color = Gray300P)

        // 반려동물 — chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PublicIconBox(bg = BrownBorderP) {
                Icon(painter = painterResource(R.drawable.ic_pets), null, tint = Brown700P, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "반려동물",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp,
                    color = Brown700P,
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(OrangeSandP)
                        .padding(horizontal = 12.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = request.petInfo,
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                        color = Brown700P,
                    )
                }
            }
        }
        HorizontalDivider(color = Gray300P)

        // 요청 메모 — fs=14 fw=400 lh=20
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PublicIconBox(bg = Gray300P) {
                Icon(painter = painterResource(R.drawable.ic_chat_bubble), null, tint = TextBlackP, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "요청 메모",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp,
                    color = Brown700P,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = request.memo,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    color = TextBlackP,
                )
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PublicIconBox(bg = iconBg) {
            Icon(painter = painterResource(iconRes), null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = Brown700P,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = valueFontWeight,
                lineHeight = 24.sp,
                color = TextBlackP,
            )
        }
    }
}

@Composable
private fun PublicIconBox(bg: Color, content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg),
    ) { content() }
}

// ── 경로 Row ──────────────────────────────────────────────────────────────────

@Composable
private fun PublicRouteRow(request: PublicRequestDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painter = painterResource(R.drawable.ic_location_on), null, tint = Orange500P, modifier = Modifier.size(16.dp))
        Text(
            text = request.originName,
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = TextBlackP,
        )
        Text(text = "→", fontFamily = PretendardFamily, fontSize = 14.sp, color = Brown400P)
        Text(
            text = request.destination,
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = TextBlackP,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${"%.1f".format(request.distanceKm)}km",
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = Brown700P,
        )
    }
}

// ── 경로 지도 Card ────────────────────────────────────────────────────────────

@Composable
private fun PublicMapCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(listOf(MintLightP, Color(0xFFE8FAF0), Color(0xFFF4FDFB)))
            ),
    ) {
        // 출발 핀
        Icon(
            painter = painterResource(R.drawable.ic_location_on), null, tint = Pink500P,
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.Center)
                .offset((-40).dp, (-20).dp),
        )
        // 도착 핀
        Icon(
            painter = painterResource(R.drawable.ic_location_on), null, tint = Orange500P,
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.Center)
                .offset(40.dp, 20.dp),
        )

        // 지도에서 보기 버튼 — fill=#FFFFFF pill, 좌하단
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White)
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(painter = painterResource(R.drawable.ic_map), null, tint = TextBlackP, modifier = Modifier.size(14.dp))
            Text(
                text = "지도에서 보기",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                color = TextBlackP,
            )
        }
    }
}

// ── 요청자 정보 Card ──────────────────────────────────────────────────────────

@Composable
private fun RequesterCard(requester: RequesterInfo, onChat: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 아바타 — 48x48 circle, fill=#D0FEE1
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(requester.avatarColor),
        ) {
            Text(
                text = requester.name.first().toString(),
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlackP,
            )
        }

        // 이름 + 정보
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = requester.name,
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = TextBlackP,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "⭐ ${requester.rating} · 요청 ${requester.requestCount}건 · 📍 ${requester.distanceKm}km",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = Brown700P,
            )
        }

        // 채팅 버튼 — fill=#FFFFFF pill h=32 pL=12 pR=12 pT=6 pB=6
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White)
                .border(1.dp, BrownBorderP, RoundedCornerShape(50.dp))
                .clickable { onChat() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(painter = painterResource(R.drawable.ic_chat_bubble), null, tint = Brown700P, modifier = Modifier.size(14.dp))
            Text(
                text = "채팅",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                color = Brown700P,
            )
        }
    }
}

// ── 하단 고정 버튼 바 ─────────────────────────────────────────────────────────

@Composable
private fun PublicDetailBottomBar(onApply: () -> Unit, onChat: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ChatBubble 아이콘 버튼 — fill=#FFFFFF pill 44x44
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, BrownBorderP, CircleShape)
                .clickable { onChat() },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chat_bubble),
                contentDescription = "채팅",
                tint = Brown700P,
                modifier = Modifier.size(20.dp),
            )
        }

        // 봉사 신청하기 — fill=#F04268 pill h=44 fs=14 fw=600 white
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Pink500P)
                .clickable { onApply() },
        ) {
            Text(
                text = "봉사 신청하기",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp,
                color = Color.White,
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MatchingPublicDetailScreenPreview() {
    SiheungGagaeTheme { MatchingPublicDetailScreen() }
}
