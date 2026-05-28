# Phase 4 — Brand Entry (Splash·Login·SignUp) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development.

**Goal:** 앱 진입 화면 (스플래시·로그인·회원가입) 의 브랜드 임팩트·UX 결함 4건 해결.

**Architecture:** 기존 화면만 수정. 신규 컴포넌트 없음.

**Roadmap:** §Phase 4

---

## File Structure

| 파일 | 항목 |
|---|---|
| Splash 관련 화면 (`AutoSplash`, `Splash`, `SplashLogo`) | #2 |
| `ui/screen/LoginScreen.kt` | #11 |
| `ui/screen/SignUpScreen.kt` | #21, #22 |
| `data/local/SiheungRegions.kt` | #21 데이터 소스 (기존) |

---

## Bundle — Brand Entry (4 items, ~7h)

### Item 1 — #2 Splash 진입 애니메이션

**Pre-check:**
```bash
grep -rn "SplashLogo\|AutoSplashScreen\|SplashScreen" app/src/main/java/com/example/siheunggagae/ui/screen/ | head
ls app/src/main/res/drawable/ | grep -E "logo|splash" | head
```

Find the splash logo Composable. Currently likely:
```kotlin
Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
    AsyncImage(... modifier = Modifier.size(200.dp))
}
```

**Variant 1: 단순 fade+scale 진입 (가장 단순)**

```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.graphics.Brush

@Composable
fun SplashLogo() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFEDD4), Color(0xFFFEFEFE))
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        // 로고 + 슬로건
        val visible = remember { MutableTransitionState(false).apply { targetState = true } }
        AnimatedVisibility(
            visibleState = visible,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                    scaleIn(initialScale = 0.7f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 로고 이미지 (기존)
                Image(
                    painter = painterResource(R.drawable.ic_logo),  // 실제 로고 리소스명
                    contentDescription = "시흥가개 로고",
                    modifier = Modifier.size(140.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "시흥가개",
                    fontFamily = PretendardFamily,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF614B3A),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "시흥의 모든 댕댕이를 위해 🐾",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8A6E58),
                )
            }
        }

        // 하단 로딩 인디케이터
        LinearProgressIndicator(
            color = Color(0xFFF7A35B),
            trackColor = Color(0xFFFEE7EC),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .width(120.dp),
        )
    }
}
```

**참고:**
- 기존 로고 drawable 명을 grep 으로 확인 (`ic_logo`, `ic_app_logo`, `logo` 등)
- 기존 SplashLogo 의 800ms timer 등 로직은 보존 (애니메이션이 끝나기 전 navigate 해버리지 않게)
- 기존 코드가 단일 Composable 이 아닌 여러 함수로 분리되어 있으면 그 구조를 따라 적용
- Color 토큰이 정의된 파일에서 가져오기

### Item 2 — #11 Login 헤드라인 OrangeSand 진입

File: `app/src/main/java/com/example/siheunggagae/ui/screen/LoginScreen.kt`

**Pre-check:**
```bash
grep -n "Scaffold\|반가워요\|BackgroundLogin\|TextBlackLogin" app/src/main/java/com/example/siheunggagae/ui/screen/LoginScreen.kt | head
```

Find the headline area. Likely:
```kotlin
Scaffold(containerColor = BackgroundLogin) {
    Column(modifier = Modifier.padding(...)) {
        Text(
            text = "반가워요!\n로그인을 진행해 주세요.",
            fontSize = 30.sp,
            color = TextBlackLogin,
        )
        // ... 로그인 폼
    }
}
```

Replace with: 상단 1/3 OrangeSand 그라디언트 영역 + 로고 + 슬로건, 그 아래 기존 폼.

```kotlin
Scaffold(containerColor = BackgroundLogin) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        // 브랜드 영역 (상단)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFEDD4), Color(0xFFFEFEFE))
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_logo),  // 확인
                    contentDescription = "시흥가개",
                    modifier = Modifier.size(64.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "반가워요, 시흥가개에서 함께해요 🐾",
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF614B3A),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        // 기존 입력 폼 (이메일·비밀번호·로그인 버튼 등) — 그대로 유지
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "로그인을 진행해 주세요",
                fontFamily = PretendardFamily,
                fontSize = 22.sp,  // 30 → 22 로 살짝 줄임 (브랜드 영역과 위계)
                fontWeight = FontWeight.ExtraBold,
                color = TextBlackLogin,
            )
            // ... 기존 입력 필드/버튼
        }
    }
}
```

기존 폼 (이메일 입력, 비밀번호, 로그인 버튼, 회원가입 링크 등) 의 구조 보존. 헤더 영역만 교체.

### Item 3 — #21 SignUp 거주지 드롭다운

File: `app/src/main/java/com/example/siheunggagae/ui/screen/SignUpScreen.kt`

**Pre-check:**
```bash
grep -n "거주\|RegTextField\|dong\|district" app/src/main/java/com/example/siheunggagae/ui/screen/SignUpScreen.kt | head -15
grep -n "object SiheungRegions\|val dongCoordinates\|dongs" app/src/main/java/com/example/siheunggagae/data/local/SiheungRegions.kt
```

Find the "거주 동" (or similar) text field — currently a free-text input.

Add an `AnimatedVisibility` dropdown below the dong input that shows matching siheung dongs as chips. User can tap a chip to fill the field.

```kotlin
import com.example.siheunggagae.data.local.SiheungRegions
import androidx.compose.animation.AnimatedVisibility

// 기존 RegTextField for dong:
RegTextField(value = dong, onValueChange = { dong = it }, placeholder = "예: 정왕동")

// 새로 추가 — 자동완성 드롭다운
val matchingDongs = remember(dong) {
    if (dong.isBlank()) emptyList()
    else SiheungRegions.dongCoordinates.keys
        .filter { it.contains(dong) || dong.contains(it) }
        .take(8)
}
AnimatedVisibility(visible = matchingDongs.isNotEmpty() && dong !in SiheungRegions.dongCoordinates.keys) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 8.dp),
    ) {
        matchingDongs.forEach { d ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFFFEDD4))
                    .clickable { dong = d }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = d,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF614B3A),
                )
            }
        }
    }
}
```

dong 이 완전 일치 (시흥 동 키 안에 정확히 존재) 면 드롭다운 숨김 + 선택 완료 표시 (체크 아이콘 같은 것).

**City 필드**: "거주 시" 도 자유 텍스트라면, 마찬가지로 시흥시 만 자동완성하거나, 차라리 disabled 고정 + "시흥시" 로 표시.

만약 city/dong 의 onValueChange/state hoisting 패턴이 다르면 그것에 맞춰 적용.

### Item 4 — #22 SignUp 약관 키워드 링크

Same file. Find the terms agreement Text.

**현재 (대략):**
```kotlin
Row(modifier = Modifier.clickable { termsAccepted = !termsAccepted }) {
    Checkbox(checked = termsAccepted, ...)
    Text("이용약관 및 개인정보처리방침에 동의합니다")
}
```

**변경:** `buildAnnotatedString` 으로 "이용약관" / "개인정보처리방침" 두 키워드에 색·밑줄 + ClickableText 또는 InlineContent 로 클릭 시 약관 시트 띄움.

```kotlin
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

var showTermsSheet by remember { mutableStateOf<TermsType?>(null) }

val annotated = buildAnnotatedString {
    append("                                ") // checkbox space
    pushStringAnnotation(tag = "TERMS", annotation = "service")
    withStyle(SpanStyle(
        color = Color(0xFFF7A35B),  // Orange500
        textDecoration = TextDecoration.Underline,
        fontWeight = FontWeight.SemiBold,
    )) {
        append("이용약관")
    }
    pop()
    append(" 및 ")
    pushStringAnnotation(tag = "TERMS", annotation = "privacy")
    withStyle(SpanStyle(
        color = Color(0xFFF7A35B),
        textDecoration = TextDecoration.Underline,
        fontWeight = FontWeight.SemiBold,
    )) {
        append("개인정보처리방침")
    }
    pop()
    append("에 동의합니다")
}

Row(verticalAlignment = Alignment.CenterVertically) {
    Checkbox(checked = termsAccepted, onCheckedChange = { termsAccepted = it })
    ClickableText(
        text = annotated,
        style = androidx.compose.ui.text.TextStyle(
            fontFamily = PretendardFamily,
            fontSize = 13.sp,
            color = Color(0xFF1E120A),
        ),
        onClick = { offset ->
            annotated.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                .firstOrNull()?.let { ann ->
                    showTermsSheet = if (ann.item == "service") TermsType.Service else TermsType.Privacy
                }
                ?: run { termsAccepted = !termsAccepted }  // 비링크 영역 탭 → 토글
        },
    )
}

// 약관 시트
if (showTermsSheet != null) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = { showTermsSheet = null },
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
            Text(
                text = if (showTermsSheet == TermsType.Service) "이용약관" else "개인정보처리방침",
                fontFamily = PretendardFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (showTermsSheet == TermsType.Service) TERMS_SERVICE_TEXT else TERMS_PRIVACY_TEXT,
                fontFamily = PretendardFamily,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = Color(0xFF1E120A),
            )
        }
    }
}

// 파일 끝 또는 별도 객체:
private enum class TermsType { Service, Privacy }
private const val TERMS_SERVICE_TEXT = """제1조 (목적)
본 약관은 시흥가개(이하 "서비스") 의 이용과 관련하여 회사와 회원 간의 권리·의무 및 책임 사항을 규정함을 목적으로 합니다.

제2조 (정의)
본 약관에서 사용하는 용어의 정의는 다음과 같습니다.
...
"""  // 추후 실제 약관 텍스트로 교체 (Phase 0 범위는 placeholder 텍스트로 시작)
private const val TERMS_PRIVACY_TEXT = """제1조 (개인정보의 수집)
회사는 회원가입, 원활한 고객상담, 각종 서비스의 제공을 위해 아래와 같은 최소한의 개인정보를 필수항목으로 수집하고 있습니다.
...
"""
```

**Note:** 약관 본문은 placeholder 로 시작 (실제 법무팀 검토 본문은 별도 작업). 데모에서 "약관이 클릭되어 본문이 시트로 뜬다" 는 것 자체가 wow.

## Verification

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest
```

## Commit

```bash
git add app/src/main/java/com/example/siheunggagae/ui/screen/SplashScreen.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/LoginScreen.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/SignUpScreen.kt
git commit -m "feat(ui): Phase 4 — 브랜드 진입 화면 폴리시 4건

#2 Splash OrangeSand 그라디언트 + 로고 fade+scale 진입 + 슬로건 + LinearProgressIndicator
#11 Login 상단 1/3 OrangeSand 영역 + 로고 + 슬로건
#21 SignUp 거주 동 자동완성 칩 드롭다운 (SiheungRegions 14개 동)
#22 SignUp 약관 키워드 ClickableText + 약관 본문 ModalBottomSheet"
```

## Self-Review

- 로고 drawable 명이 실제 존재 확인
- LoginScreen 의 기존 폼 (이메일·비밀번호·버튼) 위치 안 깨짐
- SignUp 거주지 자동완성이 city 와 dong 어느 쪽에 적용했는지 명확
- 약관 시트 본문은 placeholder 라는 점 명시
- 컴파일 SUCCESS

## Report

Status: DONE / DONE_WITH_CONCERNS / BLOCKED
- Splash/Login/SignUp 각 화면별 적용 결과
- placeholder 텍스트 / 실 데이터 구분
- commit SHA
- 발견된 문제 (특히 drawable 명 등)
