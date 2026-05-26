package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.R

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import com.example.siheunggagae.ui.viewmodel.AccountSettingsViewModel
import com.example.siheunggagae.ui.viewmodel.LocationSettingsViewModel
import com.example.siheunggagae.ui.viewmodel.NotificationSettingsUiState
import com.example.siheunggagae.ui.viewmodel.NotificationSettingsViewModel
import com.example.siheunggagae.ui.viewmodel.NotifKey
import kotlinx.coroutines.launch

// ── 스펙 컬러 ──────────────────────────────────────────────────────────────────
private val Brown900St    = Color(0xFF614B3A)
private val Brown700St    = Color(0xFF8A6E58)
private val Orange500St   = Color(0xFFF7A35B)
private val OrangeSandSt  = Color(0xFFFFEDD4)
private val Pink500St     = Color(0xFFF04268)
private val PinkSurfaceSt = Color(0xFFFEE7EC)
private val Gray300St     = Color(0xFFE8E8E8)
private val SwitchTrackOffSt = Color(0xFFD0C4BA) // 비활성 Switch 트랙 (베이지 톤)
private val BrownBorderSt = Color(0xFFE8D3C2)
private val BackgroundSt  = Color(0xFFFEFEFE)
private val TextBlackSt   = Color(0xFF1E120A)

private val siheungDongs = listOf(
    "정왕동", "배곧동", "목감동", "신천동", "은행동",
    "대야동", "포동", "연성동", "군자동", "월곶동",
    "장곡동", "능곡동", "매화동", "화정동",
)
private val siheungDongsChunked = siheungDongs.chunked(4)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onProfileEditClick: () -> Unit = {},
    onPetListClick: () -> Unit = {},
    onVolunteerHistoryClick: () -> Unit = {},
    onBlockManageClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAccountDeleted: () -> Unit = {},
    notifViewModel: NotificationSettingsViewModel? = null,
    locationViewModel: LocationSettingsViewModel? = null,
    accountViewModel: AccountSettingsViewModel? = null,
    initialSection: String? = null,
) {
    val scope = rememberCoroutineScope()

    // 서브 화면(ProfileEdit 등)에서 돌아올 때 지역 정보 갱신
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            locationViewModel?.reload()
        }
    }

    // ── 알림 설정 ──────────────────────────────────────────────────────────────
    val notifState by (notifViewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf<NotificationSettingsUiState>(NotificationSettingsUiState.Loading) })
    val notifSuccess = notifState as? NotificationSettingsUiState.Success
    var matchingNotifLocal     by remember { mutableStateOf(true) }
    var announcementNotifLocal by remember { mutableStateOf(true) }
    var reviewNotifLocal       by remember { mutableStateOf(false) }
    val matchingNotif     = notifSuccess?.matchEnabled  ?: matchingNotifLocal
    val announcementNotif = notifSuccess?.newsEnabled   ?: announcementNotifLocal
    val reviewNotif       = notifSuccess?.reviewEnabled ?: reviewNotifLocal

    // ── 위치 설정 ──────────────────────────────────────────────────────────────
    val currentDong   by (locationViewModel?.regionDong?.collectAsState()
        ?: remember { mutableStateOf<String?>(null) })
    val isLocUpdating by (locationViewModel?.isUpdating?.collectAsState()
        ?: remember { mutableStateOf(false) })
    var showLocationSheet by remember { mutableStateOf(false) }
    var locationInput     by remember { mutableStateOf("") }
    var locationError     by remember { mutableStateOf<String?>(null) }
    val locationSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── 비밀번호 변경 ──────────────────────────────────────────────────────────
    val isAccLoading by (accountViewModel?.isLoading?.collectAsState()
        ?: remember { mutableStateOf(false) })
    var showPasswordSheet by remember { mutableStateOf(false) }
    var pwCurrent         by remember { mutableStateOf("") }
    var pwNew             by remember { mutableStateOf("") }
    var pwConfirm         by remember { mutableStateOf("") }
    var pwError           by remember { mutableStateOf<String?>(null) }
    var pwSuccess         by remember { mutableStateOf(false) }
    val passwordSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── 회원 탈퇴 ──────────────────────────────────────────────────────────────
    var showDeleteSheet  by remember { mutableStateOf(false) }
    var deletePw         by remember { mutableStateOf("") }
    var deleteReason     by remember { mutableStateOf("") }
    var deleteError      by remember { mutableStateOf<String?>(null) }
    val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── 버전 ──────────────────────────────────────────────────────────────────
    val context = LocalContext.current
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—" }
        catch (_: Exception) { "—" }
    }

    // ── 섹션 스크롤 ───────────────────────────────────────────────────────────
    val scrollState = rememberScrollState()
    var notifSectionY by remember { mutableIntStateOf(-1) }
    var privacySectionY by remember { mutableIntStateOf(-1) }
    var sectionScrollDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (initialSection == "location") {
            locationInput = currentDong ?: ""
            locationError = null
            showLocationSheet = true
        }
    }
    LaunchedEffect(notifSectionY) {
        if (initialSection == "notifications" && notifSectionY >= 0 && !sectionScrollDone) {
            sectionScrollDone = true
            scrollState.animateScrollTo(notifSectionY)
        }
    }
    LaunchedEffect(privacySectionY) {
        if (initialSection == "privacy" && privacySectionY >= 0 && !sectionScrollDone) {
            sectionScrollDone = true
            scrollState.animateScrollTo(privacySectionY)
        }
    }

    Scaffold(
        containerColor = BackgroundSt,
        topBar = { SettingsTopBar(onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // ── 계정 ──────────────────────────────────────────────────────────
            SettingsSectionLabel("계정")
            SettingsSectionCard {
                SettingsNavItem(iconRes = R.drawable.ic_account_circle, label = "프로필 편집", onClick = onProfileEditClick)
                SettingsDivider()
                SettingsNavItem(iconRes = R.drawable.ic_pets, label = "반려동물 정보", onClick = onPetListClick)
                SettingsDivider()
                SettingsNavItem(iconRes = R.drawable.ic_social_leaderboard, label = "봉사 이력", onClick = onVolunteerHistoryClick)
            }

            Spacer(Modifier.height(4.dp))

            // ── 알림 ──────────────────────────────────────────────────────────
            Box(modifier = Modifier.onGloballyPositioned { notifSectionY = it.positionInParent().y.toInt() }) {
                SettingsSectionLabel("알림")
            }
            SettingsSectionCard {
                SettingsSwitchItem(imageVector = Icons.Default.Notifications, label = "매칭 알림", checked = matchingNotif,
                    onCheckedChange = { v -> matchingNotifLocal = v; notifViewModel?.toggle(NotifKey.MATCH, v) })
                SettingsDivider()
                SettingsSwitchItem(iconRes = R.drawable.ic_campaign, label = "공지 알림", checked = announcementNotif,
                    onCheckedChange = { v -> announcementNotifLocal = v; notifViewModel?.toggle(NotifKey.NEWS, v) })
                SettingsDivider()
                SettingsSwitchItem(iconRes = R.drawable.ic_chat_bubble, label = "리뷰 알림", checked = reviewNotif,
                    onCheckedChange = { v -> reviewNotifLocal = v; notifViewModel?.toggle(NotifKey.REVIEW, v) })
            }

            Spacer(Modifier.height(4.dp))

            // ── 앱 ────────────────────────────────────────────────────────────
            SettingsSectionLabel("앱")
            SettingsSectionCard {
                SettingsNavItem(
                    iconRes = R.drawable.ic_location_on,
                    label = "위치 설정",
                    rightContent = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = currentDong ?: "미설정", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp, color = Orange500St)
                            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Gray300St, modifier = Modifier.size(20.dp))
                        }
                    },
                    onClick = { locationInput = currentDong ?: ""; locationError = null; showLocationSheet = true },
                )
                SettingsDivider()
                SettingsNavItem(
                    imageVector = Icons.Default.PhoneAndroid,
                    label = "버전",
                    rightContent = { Text(text = versionName, fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp, color = Orange500St) },
                    onClick = null,
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── 기타 ──────────────────────────────────────────────────────────
            Box(modifier = Modifier.onGloballyPositioned { privacySectionY = it.positionInParent().y.toInt() }) {
                SettingsSectionLabel("기타")
            }
            SettingsSectionCard {
                SettingsNavItem(iconRes = R.drawable.ic_help, label = "도움말", onClick = onHelpClick)
                SettingsDivider()
                SettingsNavItem(
                    imageVector = Icons.Default.Key,
                    label = "비밀번호 변경",
                    onClick = { pwCurrent = ""; pwNew = ""; pwConfirm = ""; pwError = null; pwSuccess = false; showPasswordSheet = true },
                )
                SettingsDivider()
                SettingsNavItem(imageVector = Icons.Default.Block, label = "차단 관리", onClick = onBlockManageClick)
                SettingsDivider()
                // 회원 탈퇴
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { deletePw = ""; deleteReason = ""; deleteError = null; showDeleteSheet = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(PinkSurfaceSt),
                    ) {
                        Icon(imageVector = Icons.Default.PersonOff, contentDescription = null, tint = Pink500St, modifier = Modifier.size(20.dp))
                    }
                    Text(text = "회원 탈퇴", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, color = Pink500St)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── 위치 설정 시트 ─────────────────────────────────────────────────────────
    if (showLocationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLocationSheet = false },
            sheetState = locationSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                    .padding(horizontal = 24.dp).padding(top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("활동 지역 설정", fontFamily = PretendardFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlackSt)
                Text("시흥시 내 활동 동을 선택하거나 직접 입력해주세요.", fontFamily = PretendardFamily, fontSize = 14.sp, color = Brown700St)

                OutlinedTextField(
                    value = locationInput, onValueChange = { locationInput = it; locationError = null },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("예: 정왕동", fontFamily = PretendardFamily, fontSize = 14.sp, color = Color(0xFFC1AEA0)) },
                    isError = locationError != null,
                    supportingText = locationError?.let { { Text(it, color = Pink500St, fontSize = 12.sp, fontFamily = PretendardFamily) } },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500St, unfocusedBorderColor = BrownBorderSt, errorBorderColor = Pink500St),
                )

                Text("빠른 선택", fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Brown700St)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    siheungDongsChunked.forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { dong ->
                                val selected = locationInput == dong
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(if (selected) Brown900St else Color.White)
                                        .border(1.dp, if (selected) Brown900St else BrownBorderSt, RoundedCornerShape(50.dp))
                                        .clickable { locationInput = dong; locationError = null }
                                        .padding(horizontal = 14.dp, vertical = 7.dp),
                                ) {
                                    Text(dong, fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (selected) Color.White else Brown700St)
                                }
                            }
                        }
                    }
                }

                ActionButton(
                    label = "저장",
                    enabled = locationInput.isNotBlank() && !isLocUpdating,
                    isLoading = isLocUpdating,
                    color = Brown900St,
                    onClick = {
                        locationViewModel?.updateRegionDong(
                            dong = locationInput,
                            onSuccess = { scope.launch { locationSheetState.hide() }.invokeOnCompletion { showLocationSheet = false } },
                            onError = { msg -> locationError = msg },
                        ) ?: run { showLocationSheet = false }
                    },
                )
            }
        }
    }

    // ── 비밀번호 변경 시트 ─────────────────────────────────────────────────────
    if (showPasswordSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPasswordSheet = false },
            sheetState = passwordSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().imePadding().navigationBarsPadding()
                    .padding(horizontal = 24.dp).padding(top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("비밀번호 변경", fontFamily = PretendardFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlackSt)

                if (pwSuccess) {
                    // 성공 상태
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0FDF4)).padding(16.dp),
                    ) {
                        Text("비밀번호가 변경되었어요.", fontFamily = PretendardFamily, fontSize = 14.sp, color = Color(0xFF00A63E), fontWeight = FontWeight.Medium)
                    }
                    ActionButton(label = "확인", enabled = true, isLoading = false, color = Brown900St,
                        onClick = { scope.launch { passwordSheetState.hide() }.invokeOnCompletion { showPasswordSheet = false } })
                } else {
                    PasswordField(label = "현재 비밀번호", value = pwCurrent, onValueChange = { pwCurrent = it; pwError = null }, placeholder = "현재 비밀번호 입력")
                    PasswordField(label = "새 비밀번호", value = pwNew, onValueChange = { pwNew = it; pwError = null }, placeholder = "8자 이상")
                    PasswordField(label = "새 비밀번호 확인", value = pwConfirm, onValueChange = { pwConfirm = it; pwError = null }, placeholder = "새 비밀번호 재입력",
                        isError = pwError != null, errorText = pwError)

                    ActionButton(
                        label = "변경하기",
                        enabled = pwCurrent.isNotBlank() && pwNew.isNotBlank() && pwConfirm.isNotBlank() && !isAccLoading,
                        isLoading = isAccLoading,
                        color = Brown900St,
                        onClick = {
                            accountViewModel?.changePassword(
                                current = pwCurrent, new = pwNew, confirm = pwConfirm,
                                onSuccess = { pwSuccess = true },
                                onError = { msg -> pwError = msg },
                            )
                        },
                    )
                }
            }
        }
    }

    // ── 회원 탈퇴 시트 ─────────────────────────────────────────────────────────
    if (showDeleteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteSheet = false },
            sheetState = deleteSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().imePadding().navigationBarsPadding()
                    .padding(horizontal = 24.dp).padding(top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("회원 탈퇴", fontFamily = PretendardFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Pink500St)

                // 경고 박스
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PinkSurfaceSt).padding(14.dp),
                ) {
                    Text(
                        "탈퇴하시면 모든 매칭 이력, 반려동물 정보, 봉사 기록이 영구 삭제되며 복구할 수 없어요.",
                        fontFamily = PretendardFamily, fontSize = 13.sp, lineHeight = 20.sp, color = Pink500St,
                    )
                }

                PasswordField(
                    label = "비밀번호 확인", value = deletePw,
                    onValueChange = { deletePw = it; deleteError = null },
                    placeholder = "현재 비밀번호 입력",
                    isError = deleteError != null, errorText = deleteError,
                )

                OutlinedTextField(
                    value = deleteReason, onValueChange = { deleteReason = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    placeholder = { Text("탈퇴 사유 (선택)", fontFamily = PretendardFamily, fontSize = 14.sp, color = Color(0xFFC1AEA0)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500St, unfocusedBorderColor = BrownBorderSt),
                    maxLines = 4,
                )

                ActionButton(
                    label = "탈퇴하기",
                    enabled = deletePw.isNotBlank() && !isAccLoading,
                    isLoading = isAccLoading,
                    color = Pink500St,
                    onClick = {
                        accountViewModel?.deleteAccount(
                            password = deletePw, reason = deleteReason,
                            onSuccess = {
                                scope.launch { deleteSheetState.hide() }.invokeOnCompletion {
                                    showDeleteSheet = false
                                    onAccountDeleted()
                                }
                            },
                            onError = { msg -> deleteError = msg },
                        )
                    },
                )
            }
        }
    }
}

// ── 공통 시트 컴포넌트 ─────────────────────────────────────────────────────────

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    errorText: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontFamily = PretendardFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Brown700St)
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            placeholder = { Text(placeholder, fontFamily = PretendardFamily, fontSize = 14.sp, color = Color(0xFFC1AEA0)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = isError,
            supportingText = errorText?.let { { Text(it, color = Pink500St, fontSize = 12.sp, fontFamily = PretendardFamily) } },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Orange500St, unfocusedBorderColor = BrownBorderSt, errorBorderColor = Pink500St,
            ),
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    enabled: Boolean,
    isLoading: Boolean,
    color: Color,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth().height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) color else Gray300St)
            .clickable(enabled = enabled) { onClick() },
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
        } else {
            Text(label, fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                color = if (enabled) Color.White else Brown700St)
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.align(Alignment.CenterStart).size(40.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)).background(Color.White).clickable { onBack() },
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "뒤로", tint = TextBlackSt, modifier = Modifier.size(22.dp))
        }
        Text("설정", fontFamily = PretendardFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp, color = TextBlackSt, modifier = Modifier.align(Alignment.Center))
    }
}

// ─── 공통 컴포넌트 ──────────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionLabel(label: String) {
    Text(label, fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 16.sp, color = Brown700St, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
}

@Composable
private fun SettingsSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
    ) { Column { content() } }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF3F4F6))
}

@Composable
private fun SettingsNavItem(
    iconRes: Int? = null,
    imageVector: ImageVector? = null,
    label: String,
    rightContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(OrangeSandSt),
        ) {
            when {
                imageVector != null -> Icon(imageVector = imageVector, contentDescription = null, tint = Orange500St, modifier = Modifier.size(20.dp))
                iconRes != null     -> Icon(painter = painterResource(iconRes), contentDescription = null, tint = Orange500St, modifier = Modifier.size(20.dp))
            }
        }
        Text(label, fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, color = TextBlackSt, modifier = Modifier.weight(1f))
        if (rightContent != null) rightContent()
        else Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Gray300St, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsSwitchItem(
    iconRes: Int? = null,
    imageVector: ImageVector? = null,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(OrangeSandSt),
        ) {
            when {
                imageVector != null -> Icon(imageVector = imageVector, contentDescription = null, tint = Orange500St, modifier = Modifier.size(20.dp))
                iconRes != null     -> Icon(painter = painterResource(iconRes), contentDescription = null, tint = Orange500St, modifier = Modifier.size(20.dp))
            }
        }
        Text(label, fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, color = TextBlackSt, modifier = Modifier.weight(1f))
        Switch(
            checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White, checkedTrackColor = Brown900St, checkedBorderColor = Brown900St,
                uncheckedThumbColor = Color.White, uncheckedTrackColor = SwitchTrackOffSt, uncheckedBorderColor = SwitchTrackOffSt,
            ),
        )
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    SiheungGagaeTheme { SettingsScreen() }
}
