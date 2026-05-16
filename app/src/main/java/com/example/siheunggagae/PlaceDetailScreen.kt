package com.example.siheunggagae

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
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

// ─── Colors ────────────────────────────────────────────────────────────────────
private val TextBlackPL  = Color(0xFF1F130B)
private val Brown700PL   = Color(0xFF8B6F59)
private val Brown400PL   = Color(0xFFC4A882)
private val BrownBorderPL = Color(0xFFE8D3C2)
private val Orange500PL  = Color(0xFFF7A45C)
private val Pink500PL    = Color(0xFFF14369)
private val MintLightPL  = Color(0xFFD1FFE2)
private val Green500PL   = Color(0xFF00A63E)
private val GreenBgPL    = Color(0xFFDCFCE7)
private val StarYellowPL = Color(0xFFFDC700)
private val Gray100PL    = Color(0xFFF3F3F3)
private val BgPL         = Color(0xFFFEFEFE)
private val DividerPL    = Color(0xFFF3F4F6)
private val MapMintPL    = Color(0xFFD0FEE1)
private val MapSkyPL     = Color(0xFFE0F7FA)

// ─── 데이터 ────────────────────────────────────────────────────────────────────

data class PlaceReview(
    val id: Int,
    val authorName: String,
    val rating: Int,
    val timeAgo: String,
    val content: String,
    val avatarColor: Color,
)

data class Place(
    val id: Int,
    val name: String,
    val category: String,
    val distance: String,
    val rating: Float,
    val reviewCount: Int,
    val score: Float,
    val hours: String,
    val isOpen: Boolean,
    val address: String,
    val phone: String,
    val areaName: String,
    val reviews: List<PlaceReview>,
)

val allPlaces: List<Place> = listOf(
    Place(
        id = 1,
        name = "댕댕 카페",
        category = "카페",
        distance = "0.3km",
        rating = 4.9f,
        reviewCount = 128,
        score = 4.8f,
        hours = "매일 09:00 ~ 21:00",
        isOpen = true,
        address = "경기도 시흥시 정왕동 댕댕로 123",
        phone = "031-111-2233",
        areaName = "정왕동",
        reviews = listOf(
            PlaceReview(1, "댕댕맘",  5, "30일 전", "친절하게 잘 도와주셨어요! 강아지가 무서워하지 않아서 좋았습니다.", Color(0xFFF04268)),
            PlaceReview(2, "냥이아빠", 4, "43일 전", "전반적으로 만족스러웠어요. 다음에도 이용할 것 같습니다.",       Color(0xFF7C3AED)),
            PlaceReview(3, "초코사랑", 5, "47일 전", "예약도 편하고 직원분들이 너무 친절했어요.",                    Color(0xFFF7A45C)),
        ),
    ),
    Place(
        id = 2,
        name = "행복 동물병원",
        category = "병원",
        distance = "1.2km",
        rating = 4.7f,
        reviewCount = 89,
        score = 4.7f,
        hours = "월 ~ 금 09:00 ~ 18:00\n토 ~ 일 09:00 ~ 14:00",
        isOpen = true,
        address = "경기도 시흥시 정왕동 행복길 45",
        phone = "031-222-3344",
        areaName = "정왕동",
        reviews = listOf(
            PlaceReview(1, "몽이맘",  5, "10일 전", "수의사 선생님이 정말 친절하고 꼼꼼하게 진료해주셨어요.",       Color(0xFF00A63E)),
            PlaceReview(2, "루나집사", 4, "25일 전", "예약하고 갔더니 대기 없이 바로 진료받을 수 있었어요.",         Color(0xFF388AF5)),
        ),
    ),
    Place(
        id = 3,
        name = "반려동물 카페",
        category = "카페",
        distance = "1.8km",
        rating = 4.9f,
        reviewCount = 128,
        score = 4.9f,
        hours = "매일 10:00 ~ 20:00",
        isOpen = true,
        address = "경기도 시흥시 정왕동 1234-5",
        phone = "031-123-4567",
        areaName = "정왕동",
        reviews = listOf(
            PlaceReview(1, "댕댕맘",  5, "30일 전", "넓고 깔끔해서 강아지와 함께 오기 딱 좋아요.",                 Color(0xFFF04268)),
            PlaceReview(2, "냥이아빠", 4, "43일 전", "커피도 맛있고 반려동물 공간도 넉넉해요.",                     Color(0xFF7C3AED)),
        ),
    ),
    Place(
        id = 4,
        name = "배곧 댕댕카페",
        category = "카페",
        distance = "2.5km",
        rating = 4.8f,
        reviewCount = 76,
        score = 4.8f,
        hours = "매일 09:00 ~ 20:00",
        isOpen = false,
        address = "경기도 시흥시 배곧동 배곧대로 56",
        phone = "031-333-4455",
        areaName = "배곧동",
        reviews = listOf(
            PlaceReview(1, "초코사랑", 5, "5일 전", "넓은 공간에 강아지들이 뛰어놀기 좋아요!", Color(0xFFF7A45C)),
        ),
    ),
    Place(
        id = 5,
        name = "반려동물 병원",
        category = "병원",
        distance = "1.8km",
        rating = 4.9f,
        reviewCount = 95,
        score = 4.9f,
        hours = "월 ~ 금 09:00 ~ 19:00\n토 09:00 ~ 14:00",
        isOpen = true,
        address = "경기도 시흥시 정왕동 번영로 78",
        phone = "031-444-5566",
        areaName = "정왕동",
        reviews = listOf(
            PlaceReview(1, "뭉이아빠", 5, "8일 전",  "정말 친절하고 실력 있는 병원입니다.",                         Color(0xFF00A63E)),
            PlaceReview(2, "하루엄마", 5, "20일 전", "걱정했는데 선생님이 잘 봐주셔서 감사했어요.",                  Color(0xFF388AF5)),
        ),
    ),
    Place(
        id = 6,
        name = "정왕 동물병원",
        category = "병원",
        distance = "0.8km",
        rating = 4.6f,
        reviewCount = 52,
        score = 4.6f,
        hours = "월 ~ 금 09:00 ~ 18:00",
        isOpen = false,
        address = "경기도 시흥시 정왕동 정왕대로 12",
        phone = "031-555-6677",
        areaName = "정왕동",
        reviews = listOf(
            PlaceReview(1, "파댕이주인", 5, "3일 전", "슬개골 수술 후 경과 검진에서 친절하게 설명해주셨어요.", Color(0xFFF04268)),
        ),
    ),
)

fun findPlaceById(id: Int): Place = allPlaces.find { it.id == id } ?: allPlaces.first()

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun PlaceDetailScreen(
    placeId: Int = 1,
    onBack: () -> Unit = {},
) {
    val place = findPlaceById(placeId)

    Scaffold(
        containerColor = BgPL,
        topBar = { PlaceDetailTopBar(onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // 상단 배너 — Figma: 200dp, 민트 그라디언트
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Brush.linearGradient(listOf(MapMintPL, Color(0xFFA5D6A7), Color(0xFFC8E6C9)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_location_on),
                    contentDescription = null,
                    tint = Pink500PL,
                    modifier = Modifier.size(48.dp),
                )
            }

            // 장소 정보 — Figma pad=20 horizontal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // 장소명 — Figma: fw=700 fs=30 lh=36 #1F130B
                Text(
                    text = place.name,
                    fontFamily = PretendardFamily,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp,
                    color = TextBlackPL,
                )
                // 카테고리·거리 — Figma: fw=400 fs=16 lh=24 #8B6F59
                Text(
                    text = "${place.category} · ${place.distance}",
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 24.sp,
                    color = Brown700PL,
                )
                Spacer(Modifier.height(8.dp))
                // 별점 Row — Figma: Star + "4.9" fw=700 fs=16 + "(리뷰 N)" fw=400 fs=16
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(painter = painterResource(R.drawable.ic_star), null, tint = StarYellowPL, modifier = Modifier.size(20.dp))
                    Text(
                        text = "${place.rating}",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        color = TextBlackPL,
                    )
                    Text(
                        text = "(리뷰 ${place.reviewCount})",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 24.sp,
                        color = Brown700PL,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 상세 정보 섹션 — Figma: pad=16/24/16/24, gap=20
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // 영업 시간
                PlaceInfoRowPL(
                    iconRes = R.drawable.ic_schedule,
                    label = "영업 시간",
                    content = place.hours,
                ) {
                    if (place.isOpen) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(GreenBgPL)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "영업 중",
                                fontFamily = PretendardFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 16.sp,
                                color = Green500PL,
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Gray100PL)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "영업 종료",
                                fontFamily = PretendardFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 16.sp,
                                color = Brown700PL,
                            )
                        }
                    }
                }

                HorizontalDivider(color = DividerPL, thickness = 1.dp)

                // 주소
                PlaceInfoRowPL(
                    iconRes = R.drawable.ic_location_on,
                    label = "주소",
                    content = place.address,
                ) { CopyButtonPL() }

                HorizontalDivider(color = DividerPL, thickness = 1.dp)

                // 전화
                PlaceInfoRowPL(
                    iconRes = R.drawable.ic_call,
                    label = "전화",
                    content = place.phone,
                ) { CopyButtonPL() }
            }

            Spacer(Modifier.height(8.dp))

            // 위치 섹션 — Figma pad=16/24/16/24
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "위치",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 16.sp,
                    color = Brown700PL,
                )
                // 지도 카드 — Figma: r=24, h≈127, 민트 그라디언트
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(MapSkyPL, MapMintPL))),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_location_on),
                        contentDescription = null,
                        tint = Pink500PL,
                        modifier = Modifier.size(32.dp).align(Alignment.Center),
                    )
                    // 장소명 pill — Figma: 좌하단, white bg, orange pin + name
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_location_on), null, tint = Orange500PL, modifier = Modifier.size(12.dp))
                        Text(
                            text = place.areaName,
                            fontFamily = PretendardFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp,
                            color = TextBlackPL,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 리뷰 섹션 — Figma pad=8/24/8/24
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "리뷰",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 16.sp,
                    color = Brown700PL,
                )
                // 별점 요약 — Figma: "4.8" fs=30 fw=600 + stars + "총 N개의 리뷰" fs=12 fw=400
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "${place.score}",
                        fontFamily = PretendardFamily,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 36.sp,
                        color = TextBlackPL,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(5) { Icon(painter = painterResource(R.drawable.ic_star), null, tint = StarYellowPL, modifier = Modifier.size(18.dp)) }
                    }
                    Text(
                        text = "총 ${place.reviewCount}개의 리뷰",
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 16.sp,
                        color = Brown700PL,
                    )
                }
            }

            // 리뷰 카드 목록 — Figma pad=8/24/60/24, gap=8
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                place.reviews.forEach { review ->
                    PlaceReviewCard(review = review)
                }
            }
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun PlaceDetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 뒤로가기 — Figma: 30x30, r=8
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .shadow(1.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .clickable { onBack() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "뒤로",
                tint = TextBlackPL,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(Modifier.weight(1f))

        // 북마크 + 공유 — Figma: 70x30 container, gap=10
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PlaceTopIconBtn(iconRes = R.drawable.ic_bookmark, desc = "북마크")
            PlaceTopIconBtn(iconRes = R.drawable.ic_share, desc = "공유")
        }
    }
}

@Composable
private fun PlaceTopIconBtn(iconRes: Int, desc: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(30.dp)
            .shadow(1.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { },
    ) {
        Icon(painter = painterResource(iconRes), contentDescription = desc, tint = TextBlackPL, modifier = Modifier.size(16.dp))
    }
}

// ─── 정보 Row ──────────────────────────────────────────────────────────────────

@Composable
private fun PlaceInfoRowPL(
    iconRes: Int,
    label: String,
    content: String,
    action: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 아이콘 박스 — Figma: 40x40, r=10, white bg + border
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, BrownBorderPL, RoundedCornerShape(10.dp)),
        ) {
            Icon(painter = painterResource(iconRes), contentDescription = null, tint = Brown700PL, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // 라벨 — Figma: fs=12, fw=400, #8B6F59
            Text(
                text = label,
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = Brown700PL,
            )
            // 내용 — Figma: fs=14, fw=400, #1F130B
            Text(
                text = content,
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = TextBlackPL,
            )
        }
        action()
    }
}

// ─── 복사 버튼 ─────────────────────────────────────────────────────────────────

@Composable
private fun CopyButtonPL() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Gray100PL)
            .clickable { }
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = "복사",
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp,
            color = Brown700PL,
        )
    }
}

// ─── 리뷰 카드 ─────────────────────────────────────────────────────────────────

@Composable
private fun PlaceReviewCard(review: PlaceReview) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, DividerPL, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 아바타 — Figma: 40x40 circle, fill=#D1FFE2
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MintLightPL),
            ) {
                Text(
                    text = review.authorName.first().toString(),
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00A73F),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.authorName,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 20.sp,
                    color = TextBlackPL,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(review.rating) {
                        Icon(painter = painterResource(R.drawable.ic_star), null, tint = StarYellowPL, modifier = Modifier.size(12.dp))
                    }
                }
            }
            Text(
                text = review.timeAgo,
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = Brown400PL,
            )
        }
        Text(
            text = review.content,
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = Brown700PL,
        )
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PlaceDetailScreenPreview() {
    SiheungGagaeTheme { PlaceDetailScreen(placeId = 1) }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PlaceDetailHospitalPreview() {
    SiheungGagaeTheme { PlaceDetailScreen(placeId = 2) }
}
