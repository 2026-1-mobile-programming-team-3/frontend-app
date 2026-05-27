package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.AppBottomBar
import com.example.siheunggagae.R
import com.example.siheunggagae.Screen
import com.example.siheunggagae.data.model.ActivityStatsResponse
import com.example.siheunggagae.data.model.PetGender
import com.example.siheunggagae.data.model.PetResponse
import com.example.siheunggagae.data.model.PetSpecies
import com.example.siheunggagae.data.model.UserMeResponse
import com.example.siheunggagae.data.model.UserRole
import com.example.siheunggagae.data.model.VolunteerBadgeInfo
import com.example.siheunggagae.data.model.VolunteerBadgeTier
import com.example.siheunggagae.ui.viewmodel.MyUiState
import com.example.siheunggagae.ui.viewmodel.MyViewModel

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import com.example.siheunggagae.ui.component.SiheungAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.siheunggagae.ui.component.CountUpText
import androidx.compose.material3.MaterialTheme
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import coil3.request.crossfade
import androidx.annotation.DrawableRes
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.flow.MutableStateFlow

// Apple HIG ease (NavGraph 상수와 동일 톤). 화면 내 state morph 에 재사용.
private val MyAppleEaseOut = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

// 스펙 컬러
private val Brown900My   = Color(0xFF614B3A)
private val Brown700My   = Color(0xFF8A6E58)
private val Brown400My   = Color(0xFFC4A882)
private val BrownBorderY = Color(0xFFE8D3C2)
private val Orange500My  = Color(0xFFF7A35B)
private val Pink500My    = Color(0xFFF04268)
private val Green500My   = Color(0xFF00A63E)
private val PinkSurface  = Color(0xFFFEE7EC)
private val Background9  = Color(0xFFFEFEFE)
private val Divider9     = Color(0xFFE8E8E8)
private val TextBlack    = Color(0xFF1E120A)

// ─── 뱃지 관련 상수 ────────────────────────────────────────────────────────────

private val tierOrder = listOf(
    VolunteerBadgeTier.SEED,
    VolunteerBadgeTier.FLOWER,
    VolunteerBadgeTier.FRUIT,
    VolunteerBadgeTier.TREE,
)


private val tierIconRes = mapOf(
    VolunteerBadgeTier.SEED to R.drawable.ic_psychiatry,
    VolunteerBadgeTier.TREE to R.drawable.ic_nature,
)

private val tierIconVector = mapOf(
    VolunteerBadgeTier.FLOWER to Icons.Default.LocalFlorist,
    VolunteerBadgeTier.FRUIT  to Icons.Default.Eco,
)

private val tierBgColor = mapOf(
    VolunteerBadgeTier.SEED   to Color(0xFF00A63E),
    VolunteerBadgeTier.FLOWER to Color(0xFFF7A35B),
    VolunteerBadgeTier.FRUIT  to Color(0xFFF04268),
    VolunteerBadgeTier.TREE   to Color(0xFF8A6E58),
)

private fun PetSpecies.label() = when (this) {
    PetSpecies.DOG   -> "강아지"
    PetSpecies.CAT   -> "고양이"
    PetSpecies.OTHER -> "기타"
}

private fun PetGender?.label() = when (this) {
    PetGender.MALE   -> "수컷"
    PetGender.FEMALE -> "암컷"
    else             -> ""
}

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun MyScreen(
    viewModel: MyViewModel? = null,
    localImageUri: String? = null,
    onNavigate: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onPetListClick: () -> Unit = {},
    onBadgeListClick: () -> Unit = {},
    onVolunteerHistoryClick: () -> Unit = {},
    onFavoriteStoresClick: () -> Unit = {},
    onMyStoreRequestsClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onVolunteerApplyClick: () -> Unit = {},
    onNotifSettingsClick: () -> Unit = {},
    onLocationSettingsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val uiState by remember(viewModel) {
        viewModel?.uiState ?: MutableStateFlow(MyUiState.Loading)
    }.collectAsStateWithLifecycle()

    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { MyTopBar(onSettingsClick = onSettingsClick) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        // targetState를 상태 종류(Loading/Error/Success)로 한정 — Success 내 데이터 변경 시
        // 애니메이션 없이 인플레이스 갱신됨 (silentRefresh 후 깜빡임 방지).
        val stateKind = when (uiState) {
            is MyUiState.Loading -> 0
            is MyUiState.Error   -> 1
            is MyUiState.Success -> 2
        }
        Crossfade(
            targetState = stateKind,
            animationSpec = tween(durationMillis = 280, easing = MyAppleEaseOut),
            label = "myState",
        ) { kind ->
        when (kind) {
            0 -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Orange500My)
                }
            }
            1 -> {
                val errorMsg = (uiState as? MyUiState.Error)?.message ?: ""
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = errorMsg,
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            color = Brown700My,
                            textAlign = TextAlign.Center,
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Orange500My)
                                .clickable { viewModel?.fetchData() }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                        ) {
                            Text("다시 시도", fontFamily = PretendardFamily, fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }
            else -> {
                val success = uiState as? MyUiState.Success
                val user = success?.user
                val stats = success?.stats
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(Modifier.height(12.dp))
                    ProfileCard(
                        nickname = user?.nickname ?: "—",
                        regionDong = user?.regionDong ?: "—",
                        localImageUri = localImageUri,
                        onEditClick = onEditProfileClick,
                    )
                    Spacer(Modifier.height(12.dp))
                    MyPetSection(
                        pet = user?.pets?.firstOrNull(),
                        onPetListClick = onPetListClick,
                    )
                    Spacer(Modifier.height(12.dp))
                    MySectionLabel("활동")
                    Spacer(Modifier.height(6.dp))
                    ActivityStatsRow(
                        myMatchCount = stats?.myMatchCount ?: 0,
                        volunteerCount = stats?.volunteerCompletedCount ?: 0,
                        favoriteCount = stats?.favoriteCount ?: 0,
                        onMyMatchClick = { onNavigate("my_requests") },
                        onVolunteerClick = onVolunteerHistoryClick,
                        onFavoriteClick = onFavoriteStoresClick
                    )
                    Spacer(Modifier.height(12.dp))
                    MySectionLabel("봉사 뱃지")
                    Spacer(Modifier.height(6.dp))
                    VolunteerBadgeCard(
                        badge = stats?.badge,
                        onAllBadgesClick = onBadgeListClick,
                    )
                    Spacer(Modifier.height(12.dp))
                    MySectionLabel("내 기록")
                    Spacer(Modifier.height(6.dp))
                    MyRecordsSection(
                        onVolunteerHistoryClick = onVolunteerHistoryClick,
                        onFavoriteStoresClick = onFavoriteStoresClick,
                        onMyStoreRequestsClick = onMyStoreRequestsClick,
                    )
                    Spacer(Modifier.height(12.dp))
                    MySectionLabel("설정")
                    Spacer(Modifier.height(6.dp))
                    SettingsSection(
                        regionDong = user?.regionDong ?: "미설정",
                        onNotifSettingsClick = onNotifSettingsClick,
                        onLocationSettingsClick = onLocationSettingsClick,
                        onPrivacyClick = onPrivacyClick,
                        onHelpClick = onHelpClick,
                        onVolunteerApplyClick = onVolunteerApplyClick,
                    )
                    Spacer(Modifier.height(16.dp))
                    LogoutButton(onClick = { showLogoutDialog = true })
                    Spacer(Modifier.height(96.dp))
                }
            }
        }
        }  // end Crossfade
    }

    if (showLogoutDialog) {
        SiheungAlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = "로그아웃",
            text = "정말 로그아웃하시겠어요?",
            confirmText = "로그아웃",
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            dismissText = "취소",
            onDismiss = { showLogoutDialog = false },
            confirmColor = Pink500My,
            dismissColor = Brown700My,
        )
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun MyTopBar(onSettingsClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "마이",
            fontFamily = PretendardFamily,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlack,
        )
        // design.md §17: 메인탭 우측 보조 아이콘 = 40×40dp 카드 (Home/Matching 과 정합).
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.size(40.dp).clickable { onSettingsClick() },
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = "설정",
                    tint = Brown700My,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

// ─── 프로필 Card ───────────────────────────────────────────────────────────────

@Composable
private fun ProfileCard(
    nickname: String,
    regionDong: String,
    localImageUri: String? = null,
    onEditClick: () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFFEE7EC), Color(0xFFFFEDD4))
                )
            )
            .padding(16.dp),
    ) {
        // 발바닥 워터마크 — ic_paw 가 일부 디바이스에서 깨져서 ic_pets 로 통일.
        Icon(
            painter = painterResource(R.drawable.ic_pets),
            contentDescription = null,
            tint = Color.Black.copy(alpha = 0.08f),
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.CenterEnd),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 프로필 이미지: 갤러리 선택 이미지 or 이니셜 원형
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Orange500My),
            ) {
                if (localImageUri != null) {
                    coil3.compose.AsyncImage(
                        model = coil3.request.ImageRequest.Builder(context)
                            .data(android.net.Uri.parse(localImageUri))
                            .crossfade(200)
                            .build(),
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    )
                } else {
                    Text(
                        text = nickname.take(1),
                        fontFamily = PretendardFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nickname,
                    fontFamily = PretendardFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp,
                    color = TextBlack
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_location_on),
                        contentDescription = null,
                        tint = Brown700My,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "${regionDong}에서 활동 중",
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        color = Brown700My
                    )
                }
            }
            // 편집 chip — gradient 위에서 잘 보이도록 흰 배경 + Brown900 border.
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White)
                    .border(1.dp, Brown900My, RoundedCornerShape(50.dp))
                    .clickable { onEditClick() }
                    .padding(horizontal = 13.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "편집",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 16.sp,
                    color = Brown900My,
                )
            }
        }
    }
}

// ─── 내 반려동물 ───────────────────────────────────────────────────────────────

@Composable
private fun MyPetSection(pet: PetResponse?, onPetListClick: () -> Unit = {}) {
    SectionCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "내 반려동물",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp,
                color = Brown700My,
            )
            Spacer(Modifier.height(12.dp))

            if (pet == null) {
                // 반려동물 없음 안내
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Divider9),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_pets),
                            contentDescription = null,
                            tint = Brown400My,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Text(
                        text = "반려동물이 없어요",
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        color = Brown700My,
                        modifier = Modifier.weight(1f),
                    )
                    // 추가하기 CTA — Orange500 (design.md §5 "Orange500 = 진입점/액션").
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Orange500My)
                            .clickable { onPetListClick() }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "추가하기",
                            fontFamily = PretendardFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp,
                            color = Color.White,
                        )
                    }
                }
            } else {
                val isCat = pet.species == PetSpecies.CAT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCat) Color(0xFFFEE7EC) else Color(0xFFFFF3E0)),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_pets),
                            contentDescription = null,
                            tint = if (isCat) Pink500My else Orange500My,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pet.name.orEmpty(),
                            fontFamily = PretendardFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp,
                            color = TextBlack
                        )
                        Spacer(Modifier.height(2.dp))
                        val ageText = pet.age?.let { "${it}살" } ?: ""
                        val genderText = pet.gender.label()
                        val detail = listOfNotNull(
                            pet.species.label(),
                            ageText.takeIf { it.isNotEmpty() },
                            genderText.takeIf { it.isNotEmpty() },
                        ).joinToString(" · ")
                        Text(
                            text = detail,
                            fontFamily = PretendardFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Brown700My
                        )
                    }
                    // 전체 보기 — 보조 액션이므로 Outline.
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White)
                            .border(1.dp, BrownBorderY, RoundedCornerShape(50.dp))
                            .clickable { onPetListClick() }
                            .padding(horizontal = 14.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = "전체 보기",
                            fontFamily = PretendardFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp,
                            color = Brown900My,
                        )
                    }
                }
            }
        }
    }
}

// ─── 활동 통계 Row ─────────────────────────────────────────────────────────────

@Composable
private fun ActivityStatsRow(
    myMatchCount: Int,
    volunteerCount: Int,
    favoriteCount: Int,
    onMyMatchClick: () -> Unit = {},
    onVolunteerClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard("내 요청",  myMatchCount.toString(),    Pink500My,   R.drawable.ic_help,      Modifier.weight(1f), onClick = onMyMatchClick)
        StatCard("봉사 참여", volunteerCount.toString(),  Green500My,  R.drawable.ic_handshake, Modifier.weight(1f), onClick = onVolunteerClick)
        StatCard("즐겨찾기", favoriteCount.toString(),   Orange500My, R.drawable.ic_favorite,  Modifier.weight(1f), onClick = onFavoriteClick)
    }
}
@Composable
private fun StatCard(
    label: String,
    value: String,
    valueColor: Color,
    @DrawableRes iconRes: Int,
    modifier: Modifier,
    onClick: () -> Unit = {},
) {
    val intValue = value.toIntOrNull() ?: 0
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier.clickable { onClick() },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(valueColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = valueColor,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            // 숫자는 Brown900 단일톤으로 통일 — 의미적 색은 아이콘에만 부여해서 카드 간 균형 잡음.
            // Pink/Green/Orange 가 동시에 노출되며 산만하던 문제 해소.
            CountUpText(
                value = intValue,
                durationMs = 900,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = PretendardFamily,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.84).sp,
                ),
                color = TextBlack,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp,
                letterSpacing = (-0.12).sp,
                color = Brown700My,
            )
        }
    }
}
// ─── 봉사 뱃지 Card ────────────────────────────────────────────────────────────

@Composable
private fun VolunteerBadgeCard(badge: VolunteerBadgeInfo?, onAllBadgesClick: () -> Unit = {}) {
    SectionCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "봉사 등급",
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = TextBlack,
                )
                Text(
                    text = "전체 보기 >",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    color = Brown700My,
                    modifier = Modifier.clickable { onAllBadgesClick() },
                )
            }
            Spacer(Modifier.height(12.dp))

            val currentTier  = badge?.tier ?: VolunteerBadgeTier.NONE
            val count        = badge?.count ?: 0
            val progressPct  = badge?.progressPct ?: 0
            val nextTier     = badge?.nextTier
            val nextThreshold = badge?.nextThreshold

            // 숫자만 ExtraBold 로 강조 — 사용자가 자기 진척도(건수·퍼센트)를 즉시 인식하도록.
            val numberSpan = SpanStyle(
                fontWeight = FontWeight.ExtraBold,
                color = TextBlack,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("${currentTier.label} 등급 · 누적 ")
                        withStyle(numberSpan) { append("$count") }
                        append("건")
                    },
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    letterSpacing = (-0.12).sp,
                    color = Brown700My
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(numberSpan) { append("$progressPct") }
                        append("%")
                        if (nextTier != null && nextThreshold != null) {
                            append(" · ${nextTier.label}까지 ")
                            withStyle(numberSpan) { append("${nextThreshold - count}") }
                            append("건")
                        } else {
                            append(" · 최고 등급 달성!")
                        }
                    },
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    letterSpacing = (-0.12).sp,
                    color = Brown700My
                )
            }
            Spacer(Modifier.height(12.dp))
            // progress=0 일 때 StrokeCap.Round 가 fill 의 끝 cap 을 트랙 우측에 둥글게 렌더해서
            // "100% 달성된 듯한" 착시가 나는 문제를 분기로 회피. 0% 면 Butt + 시작점 도트로만 표기.
            if (progressPct <= 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFE8D3C2)),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Brown900My)
                            .align(Alignment.CenterStart),
                    )
                }
            } else {
                LinearProgressIndicator(
                    progress = { progressPct / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Brown900My,
                    trackColor = Color(0xFFE8D3C2),
                    strokeCap = StrokeCap.Round,
                )
            }
            Spacer(Modifier.height(20.dp))

            val tierItems = remember(currentTier, count) {
                val currentTierIdx = tierOrder.indexOf(currentTier).takeIf { it >= 0 } ?: -1
                tierOrder.mapIndexed { idx, tier ->
                    val achieved = idx <= currentTierIdx
                    val remaining = tier.requiredCount - count
                    val subLabel = when {
                        achieved      -> "달성"
                        remaining > 0 -> "${remaining}건 필요"
                        else          -> "달성"
                    }
                    Triple(tier, achieved, subLabel)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tierItems.forEach { (tier, achieved, subLabel) ->
                    BadgeItem(
                        iconRes    = tierIconRes[tier],
                        iconVector = tierIconVector[tier],
                        bgColor    = if (achieved) tierBgColor[tier]!! else Color(0xFFE8E8E8),
                        label      = tier.label,
                        subLabel   = subLabel,
                        achieved   = achieved,
                        modifier   = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(
    iconRes: Int? = null,
    iconVector: ImageVector? = null,
    bgColor: Color,
    label: String,
    subLabel: String,
    achieved: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .scale(if (achieved) 1.1f else 1.0f)
                .clip(CircleShape)
                .background(if (achieved) Color(0xFFFFF7E0) else Color(0xFFF4F4F4))
                .then(
                    if (achieved) Modifier.border(1.5.dp, Color(0xFFFDC700), CircleShape)
                    else Modifier
                ),
        ) {
            when {
                iconVector != null -> Icon(
                    imageVector = iconVector,
                    contentDescription = label,
                    tint = if (achieved) bgColor else Color.Gray,
                    modifier = Modifier.size(24.dp),
                )
                iconRes != null -> Icon(
                    painter = painterResource(iconRes),
                    contentDescription = label,
                    tint = if (achieved) bgColor else Color.Gray,
                    modifier = Modifier.size(24.dp),
                )
            }
            if (!achieved) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = "잠금",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.TopEnd),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 16.sp,
            color = TextBlack,
        )
        Text(
            text = subLabel,
            fontFamily = PretendardFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp,
            color = if (achieved) Green500My else Brown700My,
        )
    }
}

// ─── 내 기록 ───────────────────────────────────────────────────────────────────

@Composable
private fun MyRecordsSection(
    onVolunteerHistoryClick: () -> Unit = {},
    onFavoriteStoresClick: () -> Unit = {},
    onMyStoreRequestsClick: () -> Unit = {},
) {
    SectionCard {
        RecordItem(
            iconRes = R.drawable.ic_volunteer_activism,
            iconTint = Green500My,
            title = "봉사 활동 이력",
            showDivider = true,
            onClick = onVolunteerHistoryClick,
        )
        RecordItem(
            iconRes = R.drawable.ic_store,
            iconTint = Orange500My,
            title = "내 매장 요청",
            showDivider = true,
            onClick = onMyStoreRequestsClick,
        )
        RecordItem(
            iconRes = R.drawable.ic_favorite,
            iconTint = Pink500My,
            title = "즐겨찾기 매장",
            showDivider = false,
            onClick = onFavoriteStoresClick,
        )
    }
}

@Composable
private fun RecordItem(
    iconRes: Int,
    iconTint: Color,
    title: String,
    showDivider: Boolean,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(painter = painterResource(iconRes), contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Text(
                text = title,
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
                color = TextBlack
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Brown400My,
            modifier = Modifier.size(20.dp),
        )
    }
    if (showDivider) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Divider9)
}

// ─── 설정 섹션 ─────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    regionDong: String = "미설정",
    onNotifSettingsClick: () -> Unit = {},
    onLocationSettingsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onVolunteerApplyClick: () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—" }
        catch (_: Exception) { "—" }
    }
    SectionCard {
        SettingsItem(title = "알림 설정", onClick = onNotifSettingsClick)
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Divider9)
        SettingsItem(title = "지역 설정", subtitle = regionDong, onClick = onLocationSettingsClick)
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Divider9)
        SettingsItem(title = "개인정보 및 보안", onClick = onPrivacyClick)
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Divider9)
        SettingsItem(title = "앱 정보", subtitle = versionName, onClick = onHelpClick)
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Divider9)
        SettingsItem(title = "봉사자 자격 신청", titleColor = Green500My, onClick = onVolunteerApplyClick)
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    titleColor: Color = TextBlack,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = title,
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
                color = titleColor
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Brown700My,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Brown400My,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ─── 로그아웃 버튼 ─────────────────────────────────────────────────────────────

@Composable
private fun LogoutButton(onClick: () -> Unit = {}) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Brown700My, RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(vertical = 16.dp),
    ) {
        Text(
            text = "로그아웃",
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp,
            color = Brown700My
        )
    }
}

// ─── 공통: 섹션 라벨 ───────────────────────────────────────────────────────────

@Composable
private fun MySectionLabel(label: String) {
    Text(
        text = label,
        fontFamily = PretendardFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 16.sp,
        color = Brown700My,
        modifier = Modifier.padding(horizontal = 20.dp),
    )
}

// ─── 공통: 섹션 카드 래퍼 ─────────────────────────────────────────────────────

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column { content() }
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

private val previewUser = UserMeResponse(
    id = 1,
    email = "test@example.com",
    nickname = "댕댕이주인",
    phone = null,
    role = UserRole.USER,
    profileImageUrl = null,
    regionSi = "시흥시",
    regionDong = "정왕동",
    pets = listOf(
        PetResponse(
            id = 1, name = "파댕이", species = PetSpecies.DOG,
            breed = "말티즈", age = 3, weightKg = 3.2f,
            isNeutered = false, gender = PetGender.MALE,
            photoUrl = null, note = null, createdAt = "", updatedAt = "",
        )
    ),
    createdAt = "",
)

private val previewStats = ActivityStatsResponse(
    myMatchCount = 3,
    volunteerCompletedCount = 2,
    favoriteCount = 5,
    badge = VolunteerBadgeInfo(
        tier = VolunteerBadgeTier.SEED,
        count = 2,
        nextTier = VolunteerBadgeTier.FLOWER,
        nextThreshold = 5,
        progressPct = 40,
    ),
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyScreenPreview() {
    SiheungGagaeTheme {
        MyScreen(
            viewModel = null,
            // Preview용 고정 uiState 없이 Loading 상태로 표시됨
            // 실제 데이터 보려면 별도 Preview 구성 필요
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyScreenSuccessPreview() {
    // MyUiState.Success를 직접 주입해 볼 수 없어 화면 구성 요소만 미리보기
    SiheungGagaeTheme {
        val dummyFlow = MutableStateFlow<MyUiState>(MyUiState.Success(previewUser, previewStats))
        Scaffold(
            topBar = {},
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(12.dp))
                ProfileCard(nickname = previewUser.nickname, regionDong = previewUser.regionDong ?: "정왕동")
                Spacer(Modifier.height(12.dp))
                MyPetSection(pet = previewUser.pets.firstOrNull())
                Spacer(Modifier.height(12.dp))
                MySectionLabel("활동")
                Spacer(Modifier.height(6.dp))
                ActivityStatsRow(
                    myMatchCount = previewStats.myMatchCount ?: 0,
                    volunteerCount = previewStats.volunteerCompletedCount ?: 0,
                    favoriteCount = previewStats.favoriteCount ?: 0,
                )
                Spacer(Modifier.height(12.dp))
                MySectionLabel("봉사 뱃지")
                Spacer(Modifier.height(6.dp))
                VolunteerBadgeCard(badge = previewStats.badge)
                Spacer(Modifier.height(12.dp))
                MySectionLabel("내 기록")
                Spacer(Modifier.height(6.dp))
                MyRecordsSection()
                Spacer(Modifier.height(12.dp))
                MySectionLabel("설정")
                Spacer(Modifier.height(6.dp))
                SettingsSection(regionDong = previewUser.regionDong ?: "정왕동")
                Spacer(Modifier.height(16.dp))
                LogoutButton()
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
