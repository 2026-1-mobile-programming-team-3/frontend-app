# Phase 2 — Home & My Hero Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax.

**Goal:** 데모 시연자가 가장 자주 보는 Home·My 탭에서 즉시 wow 모먼트 발생 (카운트업·hero 시각·personalization).

**Architecture:** Phase 1 foundation 컴포넌트 (`CountUpText`, `AppAsyncImage`) 활용. 신규 컴포넌트 없음. 기존 화면만 수정.

**Tech Stack:** Kotlin · Jetpack Compose · Coil 3

**Roadmap:** §Phase 2

**의존:** Phase 1 완료 (`CountUpText`, `AppAsyncImage`, `Dimens` 사용 가능)

---

## File Structure

| 파일 | 항목 |
|---|---|
| `ui/screen/HomeScreen.kt` | #1, #5, #10, #15 |
| `ui/screen/MyScreen.kt` | #8, #12, #19 |
| `ui/viewmodel/MyViewModel.kt` (필요 시) | #12 personalization 데이터 |

---

## Bundle A — HomeScreen hero (4 items, ~5h)

**Files:** `app/src/main/java/com/example/siheunggagae/ui/screen/HomeScreen.kt`

- [ ] **Step A.1: #1 + #5 산책지수 카운트업 + 타이포 위계 (WalkIndexSection)**

Find `WalkIndexSection` Composable. Currently it has structure roughly:

```kotlin
Text(text = "오늘 산책지수", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
// ...
withStyle(SpanStyle(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)) { append("${walkScore}점") }
```

**먼저 grep** 로 정확한 라인 위치 확인:
```bash
grep -n "오늘 산책지수\|walkScore\|WalkIndexSection" app/src/main/java/com/example/siheunggagae/ui/screen/HomeScreen.kt
```

**위계 복원** (Item #5):
- 레이블 "오늘 산책지수": `fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Brown700H` (보조)
- 점수 (숫자): `fontSize = 52.sp, fontWeight = FontWeight.ExtraBold` (히어로)
- 뒤 부연 텍스트 (예: "으로 좋아요"): `fontSize = 20.sp, fontWeight = FontWeight.Normal`

**카운트업 적용** (Item #1):
점수 표시를 `Text(text = "${walkScore}점", ...)` 직접 호출에서 `CountUpText(value = walkScore, suffix = "점", ...)` 로 교체.

코드 골격:
```kotlin
import com.example.siheunggagae.ui.component.CountUpText
// ...
Column {
    Text(
        text = "오늘 산책지수",
        fontFamily = PretendardFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Brown700H,
    )
    Spacer(Modifier.height(4.dp))
    Row(verticalAlignment = Alignment.Bottom) {
        CountUpText(
            value = walkScore,
            suffix = "점",
            durationMs = 1200,
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = PretendardFamily,
                fontSize = 52.sp,
                fontWeight = FontWeight.ExtraBold,
            ),
            color = scoreColor,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "으로 좋아요",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            color = TextBlackH,
        )
    }
}
```

**주의:** 기존 `buildAnnotatedString` + `withStyle` 패턴이 있을 수 있음 — 그 패턴을 두 개의 단순 Text 로 분리. `walkScore` 가 0(로딩 중) 일 때는 "—" 표시:
```kotlin
if (walkScore == 0) {
    Text("—", fontSize = 52.sp, ...)
} else {
    CountUpText(value = walkScore, ...)
}
```

(또는 ShimmerBox 로 로딩 표시, 단 이 화면에서 추가 복잡도는 피함)

- [ ] **Step A.2: #10 PetNewsSection 메인 카드 AsyncImage 적용**

Find `PetNewsSection`. Locate the main news card thumbnail rendering:

```kotlin
Image(
    painter = painterResource(R.drawable.img_home_news_thumb),
    contentDescription = "뉴스 썸네일",
    contentScale = ContentScale.Crop,
    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
)
```

Replace with conditional `AppAsyncImage`:

```kotlin
import com.example.siheunggagae.ui.component.AppAsyncImage
// ...
val mainImageUrl = mainNews?.imageUrl
if (!mainImageUrl.isNullOrBlank()) {
    AppAsyncImage(
        model = mainImageUrl,
        contentDescription = "뉴스 썸네일",
        placeholderRes = R.drawable.img_home_news_thumb,
        errorRes = R.drawable.img_home_news_thumb,
        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
    )
} else {
    Image(
        painter = painterResource(R.drawable.img_home_news_thumb),
        contentDescription = "뉴스 썸네일",
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
    )
}
```

**Pre-check:** 확인 — `mainNews` (or whatever the variable is) 가 `imageUrl` 필드를 가진 데이터 모델인가? Grep:
```bash
grep -n "imageUrl\|thumbnailUrl" app/src/main/java/com/example/siheunggagae/data/model/NewsModels.kt 2>/dev/null || grep -rn "imageUrl\|thumbnailUrl" app/src/main/java/com/example/siheunggagae/data/model/ | head -5
```
변수명·필드명에 맞춰 코드 조정.

- [ ] **Step A.3: #15 미니맵 클릭 피드백**

Find `NearbyStoresSection` or 미니맵 관련 코드 (grep `miniMap\|onMapClick`). Currently the mini-map Box has a transparent clickable overlay:

```kotlin
Box(modifier = Modifier.fillMaxSize().clickable { onMapClick() })
```

Replace with appleTapScale + haptic + CTA pill overlay:

```kotlin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.siheunggagae.ui.util.appleTapScale
import com.example.siheunggagae.ui.util.rememberAppleInteractionSource
// ...
val interaction = rememberAppleInteractionSource()
val haptic = LocalHapticFeedback.current
Box(
    modifier = Modifier
        .fillMaxSize()
        .appleTapScale(interaction)
        .clickable(interactionSource = interaction, indication = null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onMapClick()
        }
)
// 우상단 CTA pill — 별도 Box (BoxScope.align)
Row(
    modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(8.dp)
        .shadow(2.dp, RoundedCornerShape(50.dp))
        .clip(RoundedCornerShape(50.dp))
        .background(Color.White.copy(alpha = 0.92f))
        .padding(horizontal = 10.dp, vertical = 5.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    Text(
        text = "전체 지도 →",
        fontFamily = PretendardFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Brown900H,
    )
}
```

**Pre-check:**
- `appleTapScale`, `rememberAppleInteractionSource` 가 `com.example.siheunggagae.ui.util` 에 존재하는지 확인 (Map/Matching 화면이 이미 사용 중이므로 존재할 가능성 높음). 없으면 import 경로 검색: `grep -rn "fun appleTapScale\|fun rememberAppleInteractionSource" app/src/main/java/`.
- 미니맵 박스가 `Box` 가 아닌 다른 layout 이면 그에 맞춰 적용.
- CTA pill 의 `Alignment.TopEnd` 는 상위 Box scope 에서만 동작 — 상위 Box 없으면 미니맵 컨테이너를 Box 로 감싸야 함.

- [ ] **Step A.4: 컴파일 + 빌드**

```bash
./gradlew :app:compileDebugKotlin
```

- [ ] **Step A.5: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/screen/HomeScreen.kt
git commit -m "feat(ui): Phase 2 bundle A — Home hero 폴리시 4건

#1 산책지수 0→값 1.2s 카운트업 (CountUpText)
#5 WalkIndex 타이포 위계 복원 (레이블 14sp, 점수 52sp)
#10 PetNews 메인 카드 AsyncImage 적용 (imageUrl 있을 때)
#15 미니맵 클릭 appleTapScale + haptic + 우상단 CTA pill"
```

---

## Bundle B — MyScreen hero (3 items, ~5h)

**Files:** `app/src/main/java/com/example/siheunggagae/ui/screen/MyScreen.kt`

- [ ] **Step B.1: #8 StatCard 카운트업 + 아이콘**

Find `StatCard` 컴포저블 (또는 `ActivityStatsRow`). Currently structure roughly:

```kotlin
Column(...) {
    Text(text = value, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = valueColor)
    Text(text = label, fontSize = 12.sp, color = Brown700My)
}
```

각 stat 마다 적절한 아이콘 (요청·봉사·즐겨찾기) 추가하고 카운트업 적용.

**호출 처 변경 (ActivityStatsRow):**
3개 stat 의 호출에 새 `iconRes` 파라미터 추가.
- 내 요청 → `R.drawable.ic_help` 또는 `R.drawable.ic_paw` (확인 후 선택)
- 봉사 → `R.drawable.ic_handshake`
- 즐겨찾기 → `R.drawable.ic_favorite` (filled)

Pre-check drawable 존재:
```bash
ls app/src/main/res/drawable/ | grep -E "ic_(help|paw|handshake|favorite)\.xml"
```

**StatCard 시그니처 변경:**
```kotlin
import androidx.annotation.DrawableRes
import com.example.siheunggagae.ui.component.CountUpText
// ...
@Composable
private fun StatCard(
    value: String,                    // "12" 같은 문자열
    label: String,
    valueColor: Color,
    @DrawableRes iconRes: Int,
) {
    val intValue = value.toIntOrNull() ?: 0
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
        CountUpText(
            value = intValue,
            durationMs = 900,
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = PretendardFamily,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = valueColor,
        )
        Text(text = label, fontSize = 12.sp, color = Brown700My, fontFamily = PretendardFamily)
    }
}
```

**호출 처에서 새 iconRes 전달:**
```kotlin
StatCard(value = "...", label = "내 요청", valueColor = Orange500My, iconRes = R.drawable.ic_help)
StatCard(value = "...", label = "봉사", valueColor = Green600My, iconRes = R.drawable.ic_handshake)
StatCard(value = "...", label = "즐겨찾기", valueColor = Pink500My, iconRes = R.drawable.ic_favorite)
```

만약 그 drawable 들이 없으면 `ic_star` 로 fallback 하고 commit 메시지에 노트.

- [ ] **Step B.2: #12 ProfileCard 그라디언트 + 워터마크 + personalization**

Find `ProfileCard`. Currently 단색 `PinkSurface(#FEE7EC)` 배경.

**변경:**
1. 배경을 `PinkSurface → OrangeSand` horizontal 그라디언트
2. 우하단에 발바닥 아이콘 워터마크 (alpha 0.06)
3. 닉네임 아래 "{regionDong}에서 활동 중" 텍스트 추가 (regionDong 이 ui state에 있는 경우)

코드 골격:
```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(
            Brush.horizontalGradient(
                listOf(Color(0xFFFEE7EC), Color(0xFFFFEDD4))
            )
        )
        .padding(20.dp),
) {
    // 워터마크 (BoxScope.align)
    Icon(
        painter = painterResource(R.drawable.ic_paw),
        contentDescription = null,
        tint = Color.Black.copy(alpha = 0.06f),
        modifier = Modifier.size(120.dp).align(Alignment.BottomEnd),
    )
    // 기존 프로필 콘텐츠 (이름, 등급, 아바타 등)
    Row(...) {
        // 아바타 + 정보
        Column {
            Text(닉네임, ...)
            Spacer(Modifier.height(4.dp))
            val dong = uiState.regionDong  // 실제 필드명 확인 필요
            if (!dong.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_location_on),
                        contentDescription = null,
                        tint = Brown700My,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        "${dong}에서 활동 중",
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        color = Brown700My,
                    )
                }
            }
        }
    }
}
```

**Pre-check:**
- `R.drawable.ic_paw` 존재? (`ls app/src/main/res/drawable/ic_paw*`). 없으면 `ic_pets` 같은 alternative 확인하고 substitute.
- `uiState` 에 `regionDong` 또는 `address` 같은 필드 존재? `grep -n "regionDong\|address\|dong" app/src/main/java/com/example/siheunggagae/ui/viewmodel/MyViewModel.kt`. 없으면 personalization 텍스트는 stretch 로 skip (commit 메시지에 노트).

- [ ] **Step B.3: #19 VolunteerBadgeCard 진행바 + 뱃지 시각 강화**

Find `VolunteerBadgeCard` 또는 봉사 뱃지 진행도 표시 부분.

**변경:**
1. `LinearProgressIndicator` height 6dp → 10dp
2. 우측에 퍼센트 텍스트 overlay (예: "65%")
3. 뱃지 아이콘 Row 에서:
   - 달성한 뱃지: scale(1.1) + StarYellow 1.5dp ring
   - 미달성 뱃지: `colorFilter = ColorFilter.tint(Color.Gray)` + 우상단 lock 아이콘

코드 골격:
```kotlin
import androidx.compose.material3.LinearProgressIndicator
// ...
Box(modifier = Modifier.fillMaxWidth()) {
    LinearProgressIndicator(
        progress = { progressFraction },
        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
        color = Brown900My,
        trackColor = Color(0xFFE8D3C2),
    )
    Text(
        text = "${(progressFraction * 100).toInt()}%",
        fontFamily = PretendardFamily,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
    )
}
```

뱃지 Row 변경:
```kotlin
Row(...) {
    badges.forEachIndexed { idx, badge ->
        val isAchieved = badge.isAchieved  // 실 필드명 확인
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(if (isAchieved) 1.1f else 1.0f)
                .clip(CircleShape)
                .background(if (isAchieved) Color(0xFFFFF7E0) else Color(0xFFF4F4F4))
                .then(
                    if (isAchieved) Modifier.border(1.5.dp, Color(0xFFFDC700), CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(badge.iconRes),
                contentDescription = badge.name,
                tint = if (isAchieved) Color.Unspecified else Color.Gray,
                modifier = Modifier.size(24.dp),
            )
            if (!isAchieved) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = "잠금",
                    tint = Color.Gray,
                    modifier = Modifier.size(12.dp).align(Alignment.TopEnd),
                )
            }
        }
    }
}
```

**Pre-check:**
- `R.drawable.ic_lock` 존재? 없으면 `ic_lock_outline` 또는 다른 대체.
- 뱃지 객체에 `isAchieved` 필드 존재? 실제 필드명 확인 후 조정.

- [ ] **Step B.4: 컴파일 + 빌드**

```bash
./gradlew :app:compileDebugKotlin
```

- [ ] **Step B.5: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/screen/MyScreen.kt
git commit -m "feat(ui): Phase 2 bundle B — My hero 폴리시 3건

#8 StatCard 카운트업 + 카테고리별 아이콘 (요청·봉사·즐겨찾기)
#12 ProfileCard 그라디언트 + 발바닥 워터마크 + 동네 personalization
#19 VolunteerBadgeCard 진행바 10dp + 퍼센트 overlay + 달성/미달성 뱃지 차별화"
```

---

## Verification

- [ ] **Final: 전체 빌드 + 단위 테스트**

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Expected: BUILD SUCCESSFUL, all tests pass (73 from Phase 1).

- [ ] **Visual smoke check 체크리스트** (PR description):

1. Home 진입 시 산책지수 숫자가 0→실값으로 1.2s 카운트업
2. "오늘 산책지수" 14sp Medium · 숫자 52sp ExtraBold (위계 명확)
3. 뉴스 메인 카드 이미지가 API URL 있으면 실제 이미지, 없으면 기존 fallback
4. 미니맵 탭 시 scale 0.97f shrink + haptic + 우상단 "전체 지도 →" 항상 보임
5. My 진입 시 3개 통계 카드 숫자 0→실값 0.9s 카운트업
6. 각 통계 카드 상단에 컬러 톤 아이콘 (요청·봉사·즐겨찾기 시각 구분)
7. ProfileCard 배경이 핑크→오렌지 그라디언트 + 우하단 발바닥 워터마크
8. 닉네임 아래 "{동}에서 활동 중" 노출 (데이터 있을 때만)
9. VolunteerBadge 진행바 두꺼워지고 퍼센트 표시
10. 달성한 뱃지가 미달성과 시각적으로 명확히 구분 (금색 ring + 잠금)
