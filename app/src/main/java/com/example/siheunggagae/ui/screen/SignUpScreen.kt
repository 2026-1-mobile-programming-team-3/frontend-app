package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.ui.viewmodel.AuthUiState
import com.example.siheunggagae.ui.viewmodel.AuthViewModel

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.withLink
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.siheunggagae.data.local.SiheungRegions
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import kotlinx.coroutines.flow.MutableStateFlow

// ─── 컬러 ──────────────────────────────────────────────────────────────────────

private val BackgroundReg   = Color(0xFFFEFEFE)
private val TextBlackReg    = Color(0xFF1E120A)
private val Brown700Reg     = Color(0xFF8A6E58)
private val BorderBeigeReg  = Color(0xFFE8D3C2)
private val PlaceholderReg  = Color(0xFFC1AEA0)
private val Orange500Reg    = Color(0xFFF7A35B)
private val Pink500Reg      = Color(0xFFF04268)
private val Green600Reg     = Color(0xFF00A63E)
private val Gray300Reg      = Color(0xFFE8E8E8)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: AuthViewModel? = null,
    onBack: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by remember(viewModel) {
        viewModel?.uiState ?: MutableStateFlow(AuthUiState.Idle)
    }.collectAsStateWithLifecycle()

    var showEmailConflictSheet by remember { mutableStateOf(false) }
    var showTermsSheet by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val fieldErrors = (uiState as? AuthUiState.FieldErrors)?.errors ?: emptyMap()
    val isLoading = uiState is AuthUiState.Loading

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                viewModel?.resetState()
                onSignUpSuccess()
            }
            is AuthUiState.EmailConflict -> {
                showEmailConflictSheet = true
                viewModel?.resetState()
            }
            is AuthUiState.RateLimited -> {
                Toast.makeText(context, "잠시 후 다시 시도해주세요 (시간당 10번 제한)", Toast.LENGTH_SHORT).show()
                viewModel?.resetState()
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, (uiState as AuthUiState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel?.resetState()
            }
            else -> {}
        }
    }

    // 폼 상태
    var email by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirm by remember { mutableStateOf("") }
    var passwordConfirmVisible by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var dong by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var showRegionSheet by remember { mutableStateOf(false) }
    var regionInput by remember { mutableStateOf("") }
    val regionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val hasMinLength = password.length >= 8
    val hasLetter    = password.any { it.isLetter() }
    val hasDigit     = password.any { it.isDigit() }
    val hasSpecial   = password.any { !it.isLetterOrDigit() }
    val passwordsMatch = password == passwordConfirm

    // 전화번호는 선택값이지만 입력했다면 010-XXXX-XXXX 또는 숫자 11자리만 허용
    val phoneRegex = remember { Regex("""^(01\d-\d{3,4}-\d{4}|01\d{9})$""") }
    val isPhoneValid = phone.isBlank() || phoneRegex.matches(phone)

    val canSubmit = termsAccepted && email.isNotBlank() && nickname.isNotBlank() &&
                    password.isNotBlank() && passwordsMatch && isPhoneValid && !isLoading
    val focusManager = LocalFocusManager.current
    val moveNext: () -> Unit = { focusManager.moveFocus(FocusDirection.Down) }

    fun clearFieldErrors() {
        if (uiState is AuthUiState.FieldErrors) viewModel?.resetState()
    }

    // ─── 409 이메일 중복 바텀시트 ───────────────────────────────────────────────
    if (showEmailConflictSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEmailConflictSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "이미 가입된 이메일이에요",
                    fontFamily = PretendardFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 28.sp,
                    color = TextBlackReg,
                )
                Text(
                    text = "해당 이메일로 가입된 계정이 있습니다.\n로그인 화면으로 이동해 보세요.",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    color = Brown700Reg,
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Orange500Reg)
                        .clickable {
                            showEmailConflictSheet = false
                            onNavigateToLogin()
                        },
                ) {
                    Text(
                        text = "로그인하러 가기",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderBeigeReg, RoundedCornerShape(12.dp))
                        .clickable { showEmailConflictSheet = false },
                ) {
                    Text(
                        text = "취소",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Brown700Reg,
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundReg,
        topBar = { RegisterTopBar(onBack = onBack) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (canSubmit) Orange500Reg else Orange500Reg.copy(alpha = 0.4f)
                        )
                        .then(
                            if (canSubmit) Modifier.clickable {
                                viewModel?.signup(
                                    email = email,
                                    password = password,
                                    nickname = nickname,
                                    phone = phone.takeIf { it.isNotBlank() },
                                    regionSi = if (dong.isNotBlank()) "시흥시" else null,
                                    regionDong = dong.takeIf { it.isNotBlank() },
                                )
                            } else Modifier
                        ),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        Text(
                            text = "회원가입하기",
                            fontFamily = PretendardFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp,
                            color = Color.White,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "이미 계정이 있으신가요?  ",
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                        color = Brown700Reg,
                    )
                    Text(
                        text = "로그인",
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp,
                        color = Orange500Reg,
                        modifier = Modifier.clickable { onNavigateToLogin() },
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "시흥가개에\n오신 것을 환영해요!",
                fontFamily = PretendardFamily,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 42.sp,
                color = TextBlackReg,
            )

            Spacer(Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                // 이메일 (필수)
                RegInputSection(label = "이메일 *") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        RegTextField(
                            value = email,
                            onValueChange = { email = it; clearFieldErrors() },
                            placeholder = "이메일 입력",
                            keyboardType = KeyboardType.Email,
                        )
                        fieldErrors["email"]?.let { FieldErrorText(it) }
                    }
                }

                // 닉네임 (필수)
                RegInputSection(label = "닉네임 *") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        RegTextField(
                            value = nickname,
                            onValueChange = { nickname = it; clearFieldErrors() },
                            placeholder = "닉네임 입력",
                        )
                        fieldErrors["nickname"]?.let { FieldErrorText(it) }
                    }
                }

                // 비밀번호 (필수)
                RegInputSection(label = "비밀번호 *") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RegPasswordTextField(
                            value = password,
                            onValueChange = { password = it; clearFieldErrors() },
                            visible = passwordVisible,
                            onToggleVisibility = { passwordVisible = !passwordVisible },
                            placeholder = "비밀번호 입력",
                        )
                        PasswordValidationHints(
                            hasMinLength = hasMinLength,
                            hasLetter = hasLetter,
                            hasDigit = hasDigit,
                            hasSpecial = hasSpecial,
                        )
                        fieldErrors["password"]?.let { FieldErrorText(it) }
                    }
                }

                // 비밀번호 확인 (필수)
                RegInputSection(label = "비밀번호 확인 *") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        RegPasswordTextField(
                            value = passwordConfirm,
                            onValueChange = { passwordConfirm = it },
                            visible = passwordConfirmVisible,
                            onToggleVisibility = { passwordConfirmVisible = !passwordConfirmVisible },
                            placeholder = "비밀번호 재입력",
                        )
                        if (passwordConfirm.isNotEmpty() && !passwordsMatch) {
                            FieldErrorText("비밀번호가 일치하지 않아요.")
                        }
                    }
                }

                // 전화번호 (선택)
                RegInputSection(label = "전화번호") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        RegTextField(
                            value = phone,
                            onValueChange = { phone = it; clearFieldErrors() },
                            placeholder = "예: 010-1234-5678",
                            keyboardType = KeyboardType.Phone,
                        )
                        fieldErrors["phone"]?.let { FieldErrorText(it) }
                    }
                }

                // 활동 지역 (선택)
                RegInputSection(label = "활동 지역") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderBeigeReg, RoundedCornerShape(16.dp))
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
                                color = if (dong.isBlank()) PlaceholderReg else Color(0xFF1E120A),
                            )
                        }
                        fieldErrors["region_dong"]?.let { FieldErrorText(it) }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 약관 동의 (UI만, 백엔드로 전송 안 함)
            val linkStyle = SpanStyle(
                color = Color(0xFFF7A35B),
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.SemiBold,
            )
            val annotatedTerms = buildAnnotatedString {
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "service",
                        styles = TextLinkStyles(style = linkStyle),
                        linkInteractionListener = { showTermsSheet = "service" },
                    )
                ) { append("이용약관") }
                append(" 및 ")
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "privacy",
                        styles = TextLinkStyles(style = linkStyle),
                        linkInteractionListener = { showTermsSheet = "privacy" },
                    )
                ) { append("개인정보처리방침") }
                append("에 동의합니다")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (termsAccepted) Orange500Reg else Color.White)
                        .border(
                            1.dp,
                            if (termsAccepted) Orange500Reg else BorderBeigeReg,
                            RoundedCornerShape(6.dp),
                        )
                        .clickable { termsAccepted = !termsAccepted },
                ) {
                    if (termsAccepted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
                // 링크 영역(이용약관/개인정보처리방침)은 LinkAnnotation 이 우선 처리하고,
                // 일반 텍스트 영역을 탭하면 체크박스와 동일하게 동의 상태를 토글한다.
                Text(
                    text = annotatedTerms,
                    style = TextStyle(
                        fontFamily = PretendardFamily,
                        fontSize = 13.sp,
                        color = Color(0xFF1E120A),
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { termsAccepted = !termsAccepted },
                )
            }

            // ─── 활동 지역 선택 시트 ───────────────────────────────────────────────────────
    if (showRegionSheet) {
        val signupDongs = remember {
            listOf(
                "정왕동", "배곧동", "목감동", "신천동", "은행동",
                "대야동", "포동", "연성동", "군자동", "월곶동",
                "장곡동", "능곡동", "매화동", "화정동",
            ).chunked(4)
        }
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
                    color = Color(0xFF1E120A),
                )
                Text(
                    "시흥시 내 활동 동을 선택하거나 직접 입력해주세요.",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    color = Color(0xFF8A6E58),
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
                            color = PlaceholderReg,
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Orange500Reg,
                        unfocusedBorderColor = BorderBeigeReg,
                        disabledBorderColor = BorderBeigeReg.copy(alpha = 0.5f),
                        errorBorderColor = Pink500Reg,
                        disabledTextColor = TextBlackReg.copy(alpha = 0.5f),
                        errorTextColor = TextBlackReg,
                        cursorColor = Orange500Reg,
                        errorCursorColor = Pink500Reg,
                    ),
                )
                Text(
                    "빠른 선택",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8A6E58),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    signupDongs.forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { d ->
                                val selected = regionInput == d
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(if (selected) Color(0xFF614B3A) else Color.White)
                                        .border(
                                            1.dp,
                                            if (selected) Color(0xFF614B3A) else BorderBeigeReg,
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
                                        color = if (selected) Color.White else Color(0xFF8A6E58),
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
                        .background(
                            if (regionInput.isNotBlank()) Color(0xFF614B3A)
                            else Color(0xFFE8E8E8)
                        )
                        .clickable(enabled = regionInput.isNotBlank()) {
                            dong = regionInput
                            showRegionSheet = false
                        },
                ) {
                    Text(
                        "확인",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (regionInput.isNotBlank()) Color.White else Color(0xFF8A6E58),
                    )
                }
            }
        }
    }

    if (showTermsSheet != null) {
                val termsSheetState = rememberModalBottomSheetState()
                ModalBottomSheet(
                    onDismissRequest = { showTermsSheet = null },
                    sheetState = termsSheetState,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .verticalScroll(rememberScrollState())
                            .heightIn(max = 480.dp),
                    ) {
                        Text(
                            text = if (showTermsSheet == "service") "이용약관" else "개인정보처리방침",
                            fontFamily = PretendardFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E120A),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (showTermsSheet == "service") TERMS_SERVICE_PLACEHOLDER else TERMS_PRIVACY_PLACEHOLDER,
                            fontFamily = PretendardFamily,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF1E120A),
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun RegisterTopBar(onBack: () -> Unit) {
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
                tint = TextBlackReg,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = "회원가입",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackReg,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// ─── 입력 섹션 래퍼 ────────────────────────────────────────────────────────────

@Composable
private fun RegInputSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp,
            color = TextBlackReg,
        )
        content()
    }
}

// ─── 일반 텍스트 입력 ──────────────────────────────────────────────────────────

@Composable
private fun RegTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = if (onImeAction != null) KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() },
            onGo = { onImeAction() },
            onSend = { onImeAction() },
        ) else KeyboardActions.Default,
        textStyle = TextStyle(
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = TextBlackReg,
        ),
        cursorBrush = SolidColor(Orange500Reg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderBeigeReg, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = PlaceholderReg,
                )
            }
            inner()
        },
    )
}

// ─── 비밀번호 입력 ─────────────────────────────────────────────────────────────

@Composable
private fun RegPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    placeholder: String = "비밀번호 입력",
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderBeigeReg, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
            keyboardActions = if (onImeAction != null) KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() },
                onGo = { onImeAction() },
                onSend = { onImeAction() },
            ) else KeyboardActions.Default,
            textStyle = TextStyle(
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = TextBlackReg,
            ),
            cursorBrush = SolidColor(Orange500Reg),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = PlaceholderReg,
                    )
                }
                inner()
            },
        )
        Icon(
            imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
            contentDescription = if (visible) "비밀번호 숨기기" else "비밀번호 표시",
            tint = Brown700Reg,
            modifier = Modifier
                .size(22.dp)
                .clickable { onToggleVisibility() },
        )
    }
}

// ─── 비밀번호 유효성 힌트 ──────────────────────────────────────────────────────

@Composable
private fun PasswordValidationHints(
    hasMinLength: Boolean,
    hasLetter: Boolean,
    hasDigit: Boolean,
    hasSpecial: Boolean,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ValidationHint(met = hasMinLength, label = "8자 이상")
        ValidationHint(met = hasLetter,    label = "영문 포함")
        ValidationHint(met = hasDigit,     label = "숫자 포함")
        ValidationHint(met = hasSpecial,   label = "특수문자")
    }
}

@Composable
private fun ValidationHint(met: Boolean, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(if (met) Green600Reg else Gray300Reg),
        ) {
            if (met) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(9.dp),
                )
            }
        }
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = if (met) Green600Reg else PlaceholderReg,
        )
    }
}

// ─── 필드 오류 텍스트 ──────────────────────────────────────────────────────────

@Composable
private fun FieldErrorText(message: String) {
    Text(
        text = message,
        fontFamily = PretendardFamily,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = Pink500Reg,
    )
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    SiheungGagaeTheme { SignUpScreen() }
}

// ─── 약관 placeholder 본문 (실제 서비스 배포 전 법무 검토 필요) ─────────────────

private const val TERMS_SERVICE_PLACEHOLDER = """제1조 (목적)
본 약관은 시흥가개 서비스(이하 "서비스")의 이용과 관련하여 회사와 회원 간의 권리·의무 및 책임 사항을 규정함을 목적으로 합니다.

제2조 (정의)
1. "서비스"란 회사가 제공하는 모든 기능을 의미합니다.
2. "회원"이란 본 약관에 동의하고 서비스 이용 자격을 부여받은 자를 의미합니다.

제3조 (이용계약의 성립)
이용계약은 회원이 본 약관에 동의하고 회사가 정한 가입 양식에 따라 회원정보를 기입한 후 가입을 신청하면, 회사가 이를 승낙함으로써 성립됩니다.

* 상기 본문은 실제 약관이 아닌 시연용 예시입니다. 정식 서비스 배포 전 법무 검토를 거친 본문으로 교체될 예정입니다."""

private const val TERMS_PRIVACY_PLACEHOLDER = """제1조 (개인정보의 수집 및 이용 목적)
회사는 다음의 목적을 위하여 개인정보를 수집·이용합니다.
1. 회원가입 및 본인확인
2. 매칭·봉사·매장 정보 제공
3. 서비스 이용 통계 분석

제2조 (수집하는 개인정보 항목)
필수 항목: 이메일, 비밀번호, 닉네임, 거주지(시·동), 휴대전화 번호
선택 항목: 프로필 이미지, 반려동물 정보

제3조 (개인정보의 보유 및 이용기간)
회원 탈퇴 시까지 보유하며, 탈퇴 즉시 파기합니다. 단, 관련 법령에 따라 일정 기간 보존해야 하는 경우 그 기간 동안만 보관합니다.

* 상기 본문은 실제 처리방침이 아닌 시연용 예시입니다. 정식 서비스 배포 전 개인정보보호 담당자 검토를 거친 본문으로 교체될 예정입니다."""
