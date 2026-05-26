package com.example.siheunggagae.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import com.example.siheunggagae.ui.component.SiheungAlertDialog
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.siheunggagae.R
import com.example.siheunggagae.ui.viewmodel.ProfileEditUiState
import com.example.siheunggagae.ui.viewmodel.ProfileEditViewModel

private val BgPE          = Color(0xFFFEFEFE)
private val TextBlackPE   = Color(0xFF1E120A)
private val BorderPE      = Color(0xFFE8D3C2)
private val PlaceholderPE = Color(0xFFC1AEA0)
private val Orange500PE   = Color(0xFFF7A35B)
private val Pink500PE     = Color(0xFFF04268)
private val Brown900PE    = Color(0xFF614B3A)
private val Brown700PE    = Color(0xFF8A6E58)
private val Gray300PE     = Color(0xFFE8E8E8)

private val siheungDongsPE = listOf(
    "정왕동", "배곧동", "목감동", "신천동", "은행동",
    "대야동", "포동", "연성동", "군자동", "월곶동",
    "장곡동", "능곡동", "매화동", "화정동",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    viewModel: ProfileEditViewModel? = null,
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by remember(viewModel) {
        viewModel?.uiState ?: kotlinx.coroutines.flow.MutableStateFlow(ProfileEditUiState.Loading)
    }.collectAsStateWithLifecycle()

    val localImageUri by remember(viewModel) {
        viewModel?.localImageUri ?: kotlinx.coroutines.flow.MutableStateFlow(null)
    }.collectAsStateWithLifecycle()

    // 갤러리 피커
    val imagePicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel?.setLocalImageUri(uri.toString())
        }
    }

    val scope = rememberCoroutineScope()

    // 폼 상태 — 초기값은 Loaded 상태에서 한 번만 세팅
    var nickname        by rememberSaveable { mutableStateOf("") }
    var phone           by rememberSaveable { mutableStateOf("") }
    var dong            by rememberSaveable { mutableStateOf("") }
    var formInitialized by rememberSaveable { mutableStateOf(false) }
    var showRegionSheet by remember { mutableStateOf(false) }
    var regionInput     by remember { mutableStateOf("") }
    val regionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState) {
        val loaded = uiState as? ProfileEditUiState.Loaded
        if (loaded != null && !formInitialized) {
            nickname = loaded.user.nickname
            phone    = loaded.user.phone ?: ""
            dong     = loaded.user.regionDong ?: ""
            formInitialized = true
        }
        when (uiState) {
            is ProfileEditUiState.SaveSuccess -> {
                Toast.makeText(context, "프로필이 저장되었어요 ✓", Toast.LENGTH_SHORT).show()
                kotlinx.coroutines.delay(800L)
                onBack()
            }
            is ProfileEditUiState.Error -> {
                Toast.makeText(context, (uiState as ProfileEditUiState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel?.clearSaveResult()
            }
            else -> {}
        }
    }

    val fieldErrors = (uiState as? ProfileEditUiState.FieldErrors)?.errors ?: emptyMap()
    val nicknameConflict = (uiState as? ProfileEditUiState.NicknameConflict)?.message

    val phoneRegex = remember { Regex("""^(01\d-\d{3,4}-\d{4}|01\d{9})$""") }
    val isPhoneValid = phone.isBlank() || phoneRegex.matches(phone)

    val isSaving = uiState is ProfileEditUiState.Saving || uiState is ProfileEditUiState.Loading
    val canSave  = nickname.isNotBlank() && isPhoneValid && !isSaving

    // 변경 사항 추적 (dirty)
    val loadedUser = (uiState as? ProfileEditUiState.Loaded)?.user
    val initialNickname = loadedUser?.nickname ?: ""
    val initialPhone    = loadedUser?.phone ?: ""
    val initialDong     = loadedUser?.regionDong ?: ""
    val isDirty = formInitialized && (
        nickname != initialNickname || phone != initialPhone || dong != initialDong || localImageUri != null
    )
    var showDiscardDialog by remember { mutableStateOf(false) }
    val handleBack: () -> Unit = { if (isDirty) showDiscardDialog = true else onBack() }
    BackHandler(enabled = isDirty) { showDiscardDialog = true }

    fun clearErrors() {
        if (uiState is ProfileEditUiState.FieldErrors ||
            uiState is ProfileEditUiState.NicknameConflict
        ) viewModel?.clearSaveResult()
    }

    Scaffold(
        containerColor = BgPE,
        topBar = { ProfileEditTopBar(onBack = handleBack) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (canSave) Orange500PE else Orange500PE.copy(alpha = 0.4f)
                        )
                        .then(
                            if (canSave) Modifier.clickable {
                                viewModel?.save(
                                    nickname = nickname,
                                    phone = phone.takeIf { it.isNotBlank() },
                                    regionSi = if (dong.isNotBlank()) "시흥시" else null,
                                    regionDong = dong.takeIf { it.isNotBlank() },
                                )
                            } else Modifier
                        ),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        Text(
                            text = "저장하기",
                            fontFamily = PretendardFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        when {
            uiState is ProfileEditUiState.Loading && !formInitialized -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Orange500PE)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                ) {
                    Spacer(Modifier.height(24.dp))

                    // 프로필 아바타 — 탭하면 갤러리 열림
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        val existingProfileUrl =
                            (uiState as? ProfileEditUiState.Loaded)?.user?.profileImageUrl
                        val imageModel: Any? =
                            localImageUri?.let { Uri.parse(it) } ?: existingProfileUrl

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Orange500PE)
                                .clickable {
                                    imagePicker.launch(
                                        PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                                    )
                                },
                        ) {
                            if (imageModel != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(imageModel)
                                        .crossfade(200)
                                        .build(),
                                    contentDescription = "프로필 사진",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                )
                            } else {
                                Text(
                                    text = nickname.take(1),
                                    fontFamily = PretendardFamily,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color.White,
                                )
                            }
                        }
                        // 카메라 아이콘 뱃지
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF614B3A)),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "사진 변경",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                        // 닉네임 (필수)
                        PEInputSection(label = "닉네임 *") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                PETextField(
                                    value = nickname,
                                    onValueChange = { nickname = it; clearErrors() },
                                    placeholder = "닉네임을 입력하세요",
                                )
                                nicknameConflict?.let { PEFieldError(it) }
                                fieldErrors["nickname"]?.let { PEFieldError(it) }
                            }
                        }

                        // 전화번호 (선택)
                        PEInputSection(label = "전화번호") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                PETextField(
                                    value = phone,
                                    onValueChange = { phone = it; clearErrors() },
                                    placeholder = "예: 010-1234-5678",
                                    keyboardType = KeyboardType.Phone,
                                )
                                if (!isPhoneValid) {
                                    PEFieldError("전화번호 형식이 올바르지 않아요 (예: 010-1234-5678)")
                                }
                                fieldErrors["phone"]?.let { PEFieldError(it) }
                            }
                        }

                        // 활동 지역 (선택)
                        PEInputSection(label = "활동 지역") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, BorderPE, RoundedCornerShape(16.dp))
                                        .clickable {
                                            regionInput = dong
                                            showRegionSheet = true
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                ) {
                                    Text(
                                        text = dong.ifBlank { "예: 정왕동" },
                                        fontFamily = PretendardFamily,
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp,
                                        color = if (dong.isBlank()) PlaceholderPE else TextBlackPE,
                                    )
                                }
                                fieldErrors["region_dong"]?.let { PEFieldError(it) }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (showDiscardDialog) {
        SiheungAlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = "변경 사항을 버릴까요?",
            text = "저장하지 않은 변경 사항이 사라져요.",
            confirmText = "나가기",
            confirmColor = Color(0xFFEE6A46),
            onConfirm = {
                showDiscardDialog = false
                onBack()
            },
            dismissText = "계속 편집",
            onDismiss = { showDiscardDialog = false },
        )
    }

    // ─── 활동 지역 선택 시트 ───────────────────────────────────────────────────────
    if (showRegionSheet) {
        val dongsChunked = remember { siheungDongsPE.chunked(4) }
        ModalBottomSheet(
            onDismissRequest = { showRegionSheet = false },
            sheetState = regionSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "활동 지역 설정",
                    fontFamily = PretendardFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlackPE,
                )
                Text(
                    "시흥시 내 활동 동을 선택하거나 직접 입력해주세요.",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    color = Brown700PE,
                )
                OutlinedTextField(
                    value = regionInput,
                    onValueChange = { regionInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            "예: 정왕동",
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            color = PlaceholderPE,
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Orange500PE,
                        unfocusedBorderColor = BorderPE,
                    ),
                )
                Text(
                    "빠른 선택",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Brown700PE,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dongsChunked.forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { d ->
                                val selected = regionInput == d
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(if (selected) Brown900PE else Color.White)
                                        .border(
                                            1.dp,
                                            if (selected) Brown900PE else BorderPE,
                                            RoundedCornerShape(50.dp),
                                        )
                                        .clickable { regionInput = d }
                                        .padding(horizontal = 14.dp, vertical = 7.dp),
                                ) {
                                    Text(
                                        d,
                                        fontFamily = PretendardFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (selected) Color.White else Brown700PE,
                                    )
                                }
                            }
                        }
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (regionInput.isNotBlank()) Brown900PE else Gray300PE)
                        .clickable(enabled = regionInput.isNotBlank()) {
                            dong = regionInput
                            scope.launch { regionSheetState.hide() }
                                .invokeOnCompletion { showRegionSheet = false }
                        },
                ) {
                    Text(
                        "확인",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (regionInput.isNotBlank()) Color.White else Brown700PE,
                    )
                }
            }
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileEditTopBar(onBack: () -> Unit) {
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
                tint = TextBlackPE,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = "프로필 편집",
            fontFamily = PretendardFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextBlackPE,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// ─── 입력 섹션 래퍼 ────────────────────────────────────────────────────────────

@Composable
private fun PEInputSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp,
            color = TextBlackPE,
        )
        content()
    }
}

// ─── 텍스트 입력 ───────────────────────────────────────────────────────────────

@Composable
private fun PETextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = TextStyle(
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = TextBlackPE,
        ),
        cursorBrush = SolidColor(Orange500PE),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderPE, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = PlaceholderPE,
                )
            }
            inner()
        },
    )
}

// ─── 필드 오류 텍스트 ──────────────────────────────────────────────────────────

@Composable
private fun PEFieldError(message: String) {
    Text(
        text = message,
        fontFamily = PretendardFamily,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = Pink500PE,
    )
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileEditScreenPreview() {
    SiheungGagaeTheme { ProfileEditScreen() }
}
