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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// ─── 컬러 ──────────────────────────────────────────────────────────────────────

private val BackgroundLogin  = Color(0xFFFEFEFE)
private val TextBlackLogin   = Color(0xFF1E120A)
private val Brown700Login    = Color(0xFF8A6E58)
private val BorderBeigeLogin = Color(0xFFE8D3C2)
private val PlaceholderLogin = Color(0xFFC1AEA0)
private val Orange500Login   = Color(0xFFF7A35B)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    onBack: () -> Unit = {},
    onLogin: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLogin,
        topBar = { LoginTopBar(onBack = onBack) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Orange500Login)
                        .clickable { onLogin() },
                ) {
                    Text(
                        text = "로그인",
                        fontFamily = PretendardFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp,
                        color = Color.White,
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
                text = "반가워요!\n로그인을 진행해 주세요.",
                fontFamily = PretendardFamily,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 42.sp,
                color = TextBlackLogin,
            )

            Spacer(Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                LoginInputSection(label = "아이디") {
                    LoginTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        placeholder = "아이디 또는 이메일 입력",
                        keyboardType = KeyboardType.Email,
                    )
                }

                LoginInputSection(label = "비밀번호") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PasswordTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            visible = passwordVisible,
                            onToggleVisibility = { passwordVisible = !passwordVisible },
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            Text(
                                text = "비밀번호를 잊으셨나요?",
                                fontFamily = PretendardFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 20.sp,
                                color = Brown700Login,
                                modifier = Modifier.clickable { onForgotPassword() },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun LoginTopBar(onBack: () -> Unit) {
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
                tint = TextBlackLogin,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = "로그인",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackLogin,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// ─── 입력 섹션 래퍼 ────────────────────────────────────────────────────────────

@Composable
private fun LoginInputSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp,
            color = TextBlackLogin,
        )
        content()
    }
}

// ─── 일반 텍스트 입력 ──────────────────────────────────────────────────────────

@Composable
private fun LoginTextField(
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
            color = TextBlackLogin,
        ),
        cursorBrush = SolidColor(Orange500Login),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderBeigeLogin, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = PlaceholderLogin,
                )
            }
            inner()
        },
    )
}

// ─── 비밀번호 입력 ─────────────────────────────────────────────────────────────

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderBeigeLogin, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = TextStyle(
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = TextBlackLogin,
            ),
            cursorBrush = SolidColor(Orange500Login),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = "비밀번호 입력",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = PlaceholderLogin,
                    )
                }
                inner()
            },
        )
        Icon(
            painter = painterResource(
                if (visible) R.drawable.ic_visibility else R.drawable.ic_visibility_off,
            ),
            contentDescription = if (visible) "비밀번호 숨기기" else "비밀번호 표시",
            tint = Brown700Login,
            modifier = Modifier
                .size(22.dp)
                .clickable { onToggleVisibility() },
        )
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    SiheungGagaeTheme { LoginScreen() }
}
