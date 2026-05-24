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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import kotlinx.coroutines.flow.MutableStateFlow

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
    }.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    val user  = (uiState as? MyUiState.Success)?.user
    val stats = (uiState as? MyUiState.Success)?.stats

    Scaffold(
        topBar = { MyTopBar(onSettingsClick = onSettingsClick) },
        containerColor = Background9,
    ) { padding ->
        when (uiState) {
            is MyUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Orange500My)
                }
            }
            is MyUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = (uiState as MyUiState.Error).message,
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
                    MySectionLabel("봉사 뱃지", fontSize = 18)
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
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "로그아웃",
                    fontFamily = PretendardFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack,
                )
            },
            text = {
                Text(
                    text = "정말 로그아웃하시겠어요?",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    color = Brown700My,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text(
                        text = "로그아웃",
                        fontFamily = PretendardFamily,
                        fontWeight = FontWeight.Bold,
                        color = Pink500My,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = "취소",
                        fontFamily = PretendardFamily,
                        color = Brown700My,
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clickable { onSettingsClick() }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = "설정",
                tint = Brown700My,
                modifier = Modifier.size(22.dp),
            )
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
    var imageBitmap by remember(localImageUri) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(localImageUri) {
        imageBitmap = if (localImageUri != null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                runCatching {
                    val uri = android.net.Uri.parse(localImageUri)
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source).asImageBitmap()
                }.getOrNull()
            }
        } else null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PinkSurface)
            .padding(16.dp),
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
            if (imageBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = imageBitmap!!,
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
                    text = regionDong,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    color = Brown700My
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFFEFEFE))
                .clickable { onEditClick() }
                .padding(horizontal = 13.dp, vertical = 6.dp),
        ) {
            Text(
                text = "편집",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp,
                color = Brown700My,
            )
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Divider9)
                            .clickable { onPetListClick() }
                            .padding(horizontal = 14.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "추가하기",
                            fontFamily = PretendardFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp,
                            color = Brown700My
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
                            text = pet.name,
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Divider9)
                            .clickable { onPetListClick() }
                            .padding(horizontal = 14.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "전체 보기",
                            fontFamily = PretendardFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp,
                            color = Brown700My
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
        StatCard("내 요청",  myMatchCount.toString(),    Pink500My,   Modifier.weight(1f), onClick = onMyMatchClick)
        StatCard("봉사 참여", volunteerCount.toString(),  Green500My,  Modifier.weight(1f), onClick = onVolunteerClick)
        StatCard("즐겨찾기", favoriteCount.toString(),   Orange500My, Modifier.weight(1f), onClick = onFavoriteClick)
    }
}
@Composable
private fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier, onClick: () -> Unit = {}) {
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
            Text(text = value, fontFamily = PretendardFamily, fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp, color = valueColor)
            Spacer(Modifier.height(4.dp))
            Text(text = label, fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp, color = Brown700My)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${currentTier.label} 등급 · 누적 ${count}건",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    color = Brown700My
                )
                Text(
                    text = if (nextTier != null && nextThreshold != null)
                        "${nextTier.label}까지 ${nextThreshold - count}건"
                    else
                        "최고 등급 달성!",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp,
                    color = Brown400My
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progressPct / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Orange500My,
                trackColor = Color(0xFFE5E7EB),
                strokeCap = StrokeCap.Round,
            )
            Spacer(Modifier.height(20.dp))

            val currentTierIdx = tierOrder.indexOf(currentTier).takeIf { it >= 0 } ?: -1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tierOrder.forEachIndexed { idx, tier ->
                    val achieved = idx <= currentTierIdx
                    val remaining = tier.requiredCount - count
                    val subLabel = when {
                        achieved      -> "달성"
                        remaining > 0 -> "${remaining}건 필요"
                        else          -> "달성"
                    }
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
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor),
        ) {
            when {
                iconVector != null -> Icon(imageVector = iconVector, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
                iconRes != null    -> Icon(painter = painterResource(iconRes), contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
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
                    color = Orange500My
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
private fun MySectionLabel(label: String, fontSize: Int = 12) {
    Text(
        text = label,
        fontFamily = PretendardFamily,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = if (fontSize == 12) 16.sp else 27.sp,
        color = if (fontSize == 12) Brown700My else TextBlack,
        modifier = Modifier.padding(horizontal = 16.dp),
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
            containerColor = Background9,
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
                MySectionLabel("봉사 뱃지", fontSize = 18)
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
