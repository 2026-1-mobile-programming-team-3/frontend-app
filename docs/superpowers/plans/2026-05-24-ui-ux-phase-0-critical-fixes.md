# Phase 0 — Critical Quick Fix Bundle Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** UI/UX 리뷰 50개 중 foundation 없이 즉시 수정 가능한 16개 결함을 5개 bundle 로 묶어 빠르게 정리.

**Architecture:** 각 bundle 은 logically 관련된 항목들을 한 subagent 디스패치로 처리. 텍스트·padding·하드코딩·imePadding·로직 가드 수준의 small fix 만 포함. 새 컴포넌트는 #47(SiheungAlertDialog) 1개만 도입.

**Tech Stack:** Kotlin · Jetpack Compose · Material 3 · JUnit 4

**Roadmap:** `docs/superpowers/specs/2026-05-24-ui-ux-50-improvements-roadmap.md` §Phase 0

---

## File Structure

| 파일 | 변경 종류 |
|---|---|
| `ui/screen/MapScreen.kt` | #17 |
| `ui/screen/HomeScreen.kt` | #18 |
| `ui/screen/SignUpScreen.kt` | #23 |
| `ui/screen/ChatScreen.kt` | #25, #27 |
| `ui/screen/NotificationScreen.kt` | #29, #30 |
| `ui/screen/PetAddScreen.kt` | #32 |
| `ui/screen/RequestFlowScreen.kt` | #35 |
| `ui/screen/MatchingDetailScreen.kt` | #38 |
| `ui/screen/SettingsScreen.kt` | #41, #42 |
| `ui/screen/NewsDetailScreen.kt` | #43 |
| `ui/screen/VolunteerApplyScreen.kt` | #44 |
| `ui/screen/PetHotelCompareScreen.kt` | #46 |
| `ui/component/SiheungAlertDialog.kt` (NEW) | #47 |
| `ui/screen/*` (AlertDialog 호출처) | #47 마이그레이션 |

---

## Bundle 1 — Visual Micro-fixes (5 items, ~40m)

**Files:**
- Modify: `MapScreen.kt`, `HomeScreen.kt`, `NotificationScreen.kt` (×2), `SettingsScreen.kt`

- [ ] **Step 1.1: #17 Map SearchCard placeholder 대비 강화**

In `app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt` find the `MapSearchCard` Composable, locate the placeholder Text inside the Row:

```kotlin
        Text(
            text = "매장 · 병원 · 공원 검색",
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = Brown400Mp,
        )
```

Replace `color = Brown400Mp` with `color = Brown700Mp`.

Also change the leading search Icon `tint`:
```kotlin
        Icon(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            tint = Brown400Mp,
            modifier = Modifier.size(20.dp),
        )
```
→ `tint = Brown700Mp`.

- [ ] **Step 1.2: #18 HomeScreen HomeStoreItem 텍스트 위계 복원**

In `app/src/main/java/com/example/siheunggagae/ui/screen/HomeScreen.kt` find the `HomeStoreItem` Composable. Locate the two Text blocks where the store name and category/distance/rating string are rendered. The current state:
- 매장명: `fontSize = 14.sp, fontWeight = FontWeight.Bold`
- 서브정보: `fontSize = 16.sp, fontWeight = FontWeight.SemiBold`

Swap so 매장명 is bigger:
- 매장명: `fontSize = 16.sp, fontWeight = FontWeight.Bold`
- 서브정보: `fontSize = 13.sp, fontWeight = FontWeight.Normal`

Keep colors and `lineHeight` consistent (lineHeight 24 for 16sp title, 18 for 13sp sub).

- [ ] **Step 1.3: #29 NotificationScreen 읽음/미읽음 padding 통일**

In `app/src/main/java/com/example/siheunggagae/ui/screen/NotificationScreen.kt` find `NotificationItemRow`. The two visual paths (`isRead = false` carded vs `isRead = true` flat) currently use different horizontal padding (16dp vs 20dp).

Unify both branches to `horizontal = 16.dp`. Specifically — in the "read" (flat) branch, change `.padding(horizontal = 20.dp, vertical = 14.dp)` to `.padding(horizontal = 16.dp, vertical = 14.dp)`.

- [ ] **Step 1.4: #30 NotificationScreen 탭 컬러 브랜드 통일**

In the same file, find `NotiTabRow`. The selected tab uses `background(Color(0xFF1A1A1A))`. Change to use the existing Brown900 token color `Color(0xFF614B3A)` (or use the file's existing `Brown900N` if defined; otherwise inline the hex).

- [ ] **Step 1.5: #41 SettingsScreen Switch off 대비**

In `app/src/main/java/com/example/siheunggagae/ui/screen/SettingsScreen.kt` find the `SwitchDefaults.colors(...)` invocation. The `uncheckedTrackColor` is currently `Gray300St` (#E8E8E8) which is too light. Change to a darker beige for visibility:

```kotlin
uncheckedTrackColor = Color(0xFFD0C4BA)
```

Apply to every place SwitchDefaults is configured in this file (there may be 2-3 occurrences).

- [ ] **Step 1.6: 컴파일 + 커밋**

```bash
./gradlew :app:compileDebugKotlin
git add -A
git commit -m "fix(ui): Phase 0 bundle 1 — placeholder/타이포/padding/Switch 대비 5건"
```

---

## Bundle 2 — Form & Keyboard fixes (4 items, ~50m)

**Files:** `SignUpScreen.kt`, `ChatScreen.kt`, `SettingsScreen.kt`, `PetAddScreen.kt`

- [ ] **Step 2.1: #23 SignUp 비번 힌트 FlowRow 전환**

In `app/src/main/java/com/example/siheunggagae/ui/screen/SignUpScreen.kt` find `PasswordValidationHints` composable. The hints are wrapped in `Row(horizontalArrangement = Arrangement.spacedBy(12.dp))`.

Replace `Row` with `FlowRow`:
```kotlin
import androidx.compose.foundation.layout.FlowRow
// ...
FlowRow(
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
)
```

Add the `@OptIn(ExperimentalLayoutApi::class)` annotation to the enclosing Composable if needed for FlowRow.

- [ ] **Step 2.2: #25 ChatScreen 입력바 imePadding 추가**

In `app/src/main/java/com/example/siheunggagae/ui/screen/ChatScreen.kt` find `ChatInputBar`. The Row currently has `.navigationBarsPadding()` only. Add `.imePadding()` BEFORE `.navigationBarsPadding()`:

```kotlin
Row(
    modifier = Modifier
        ... 기존 ...
        .imePadding()
        .navigationBarsPadding()
        ...
)
```

`import androidx.compose.foundation.layout.imePadding` if not already imported.

- [ ] **Step 2.3: #42 SettingsScreen 탈퇴/비번 변경 시트 imePadding**

In `app/src/main/java/com/example/siheunggagae/ui/screen/SettingsScreen.kt` find `ModalBottomSheet(...)` invocations used for the 탈퇴 (withdraw) sheet and 비밀번호 변경 (password change) sheet. Locate the inner `Column` modifier. Add `.imePadding()` to each before `.navigationBarsPadding()` (if missing).

If a sheet uses `windowInsets = WindowInsets(0)` pattern, `.imePadding()` on the Column is still required because the sheet otherwise lets the keyboard cover the input.

- [ ] **Step 2.4: #32 PetAddScreen 나이 상한 추가**

In `app/src/main/java/com/example/siheunggagae/ui/screen/PetAddScreen.kt` find the Stepper for age. The current logic:

```kotlin
onIncrement = { age++ }
onDecrement = { if (age > 1) age-- }
```

Update increment to respect age unit. The age unit is tracked nearby (e.g., `ageUnit` variable representing "살" vs "개월"). Use:

```kotlin
onIncrement = {
    val max = if (ageUnit == "개월") 36 else 30
    if (age < max) age++
}
```

If `ageUnit` is a different identifier in the actual code, adapt accordingly. Verify both unit selections still work after change.

- [ ] **Step 2.5: 컴파일 + 커밋**

```bash
./gradlew :app:compileDebugKotlin
git add -A
git commit -m "fix(ui): Phase 0 bundle 2 — 폼·키보드 회피·나이 상한 4건"
```

---

## Bundle 3 — Data/Logic fixes (4 items, ~2h)

**Files:** `ChatScreen.kt`, `RequestFlowScreen.kt`, `MatchingDetailScreen.kt`, `VolunteerApplyScreen.kt`, `MatchingPublicDetailScreen.kt` (status helper 공유)

- [ ] **Step 3.1: #27 Chat 별점 하드코딩 제거**

In `app/src/main/java/com/example/siheunggagae/ui/screen/ChatScreen.kt` find `ChatTopBar`. The subtitle currently shows `"4.9 · 동네 매칭 회원"` with hardcoded "4.9":

```kotlin
Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
    Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, modifier = Modifier.size(11.dp), tint = StarYellowC)
    Text(text = "4.9 · 동네 매칭 회원", ...)
}
```

Decision: since `ChatUiState` does NOT yet carry the opponent's rating, the simplest correct fix is to remove the rating portion entirely. Replace the Row with a simpler subtitle:

```kotlin
Text(
    text = "동네 매칭 회원",
    fontFamily = PretendardFamily,
    fontSize = 12.sp,
    color = Brown700C,
)
```

Note: full rating support (passing opponent ratingAvg through ChatUiState) is deferred to Phase 6 #27 enhanced fix.

- [ ] **Step 3.2: #38 MatchingDetail 상태 영문 → 한국어**

Create `app/src/main/java/com/example/siheunggagae/ui/util/StatusLabels.kt`:

```kotlin
package com.example.siheunggagae.ui.util

fun matchStatusToKorean(status: String?): String = when (status?.trim()?.uppercase()) {
    "WAITING"   -> "지원자 모집 중"
    "MATCHING"  -> "매칭 진행 중"
    "PROGRESS"  -> "봉사 진행 중"
    "DONE"      -> "봉사 완료"
    "CANCELED", "CANCELLED" -> "취소됨"
    null, ""    -> "상태 없음"
    else        -> status
}
```

In `app/src/main/java/com/example/siheunggagae/ui/screen/MatchingDetailScreen.kt` find every place `StatusBannerD(statusText = request.status ?: "상태 없음")` (or similar) is called and replace with:

```kotlin
StatusBannerD(statusText = matchStatusToKorean(request.status))
```

Also grep for `request.status` and other raw `status` usages in `MatchingDetailScreen.kt`, `MatchingPublicDetailScreen.kt`, `MatchingScreen.kt` — apply `matchStatusToKorean(...)` everywhere a user-facing label is rendered. Do NOT change places that pass raw status to API or comparisons.

Add `import com.example.siheunggagae.ui.util.matchStatusToKorean` to each modified file.

- [ ] **Step 3.3: #35 RequestFlow 과거 날짜 비활성**

In `app/src/main/java/com/example/siheunggagae/ui/screen/RequestFlowScreen.kt` find `CalendarSection` and the day cell rendering. Each day cell currently renders all days uniformly clickable.

Locate the iteration over days of the displayed month. For each `day: Int` add a guard:

```kotlin
val cellDate = yearMonth.atDay(day)
val isPast = cellDate.isBefore(java.time.LocalDate.now())
val cellAlpha = if (isPast) 0.3f else 1f
Box(
    modifier = Modifier
        .alpha(cellAlpha)
        .clickable(enabled = !isPast) { onSelect(cellDate) }
        ...
)
```

Adjust to match the actual cell structure. Today's date should remain enabled (`isBefore`, not `isBefore + isEqual`).

- [ ] **Step 3.4: #44 VolunteerApply 제목 필드 안내 추가**

In `app/src/main/java/com/example/siheunggagae/ui/screen/VolunteerApplyScreen.kt` find the comment `// 제목: UI 전용, 백엔드 미전송` and the surrounding 제목 input field.

Decision: keep the field (it may be useful as a user reminder) but add a clarifying helper text BELOW the input:

```kotlin
Text(
    text = "* 제목은 본인 확인용입니다 (서버에 저장되지 않음)",
    fontFamily = PretendardFamily,
    fontSize = 11.sp,
    color = Brown700V,  // 또는 해당 파일의 회색 토큰
)
```

If the file has a clearer alternative (e.g., the field is genuinely unused), instead remove the field entirely. Prefer the helper-text approach to minimize layout disruption.

- [ ] **Step 3.5: 컴파일 + 커밋**

```bash
./gradlew :app:compileDebugKotlin
git add -A
git commit -m "fix(ui): Phase 0 bundle 3 — 채팅 하드코딩·매칭 상태 한국어·과거날짜 가드·봉사 제목 안내 4건"
```

---

## Bundle 4 — Interactive feature fixes (2 items, ~1h)

**Files:** `NewsDetailScreen.kt`, `PetHotelCompareScreen.kt`

- [ ] **Step 4.1: #43 NewsDetail 북마크 동작 추가**

In `app/src/main/java/com/example/siheunggagae/ui/screen/NewsDetailScreen.kt` find the TopBar icon button:

```kotlin
TopBarIconBtnND { Icon(R.drawable.ic_bookmark, ...) }
// onClick = {}
```

Replace with toggleable state + icon swap + scale punch:

```kotlin
var isBookmarked by rememberSaveable { mutableStateOf(false) }
val haptic = LocalHapticFeedback.current
val scale by animateFloatAsState(
    targetValue = if (isBookmarked) 1.2f else 1f,
    animationSpec = spring(stiffness = Spring.StiffnessMedium),
    label = "bookmarkScale"
)
TopBarIconBtnND(
    onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        isBookmarked = !isBookmarked
    }
) {
    Icon(
        painter = painterResource(if (isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark),
        contentDescription = "북마크",
        tint = if (isBookmarked) Pink500Nd else Brown700Nd,
        modifier = Modifier.size(22.dp).scale(scale),
    )
}
```

Verify `R.drawable.ic_bookmark_filled` exists. If not, fall back to `R.drawable.ic_bookmark` with `tint = Pink500Nd` when bookmarked. Check with `ls app/src/main/res/drawable/ | grep bookmark` BEFORE writing the code.

If `Pink500Nd` token does not exist in this file, define it at the top as `private val Pink500Nd = Color(0xFFF04268)` per existing pattern.

Persistence (DataStore) is OUT OF SCOPE for Phase 0 — local-session toggle only. Note it for future Phase 6+ enhancement.

- [ ] **Step 4.2: #46 PetHotelCompare 가격 단위 표시**

In `app/src/main/java/com/example/siheunggagae/ui/screen/PetHotelCompareScreen.kt` grep for price rendering. The price is currently displayed as a plain number string like `"25,000"`.

Find every Text rendering price (search for keywords like `price`, `pricePerNight`, `kr.format`, or `String.format`). Wrap with `₩{price}/박` format:

```kotlin
Text("₩${"%,d".format(price)}/박", ...)
```

Apply to both the cards in the LazyColumn AND any sort-axis price column. If `PetHotelMatrixCompareScreen.kt` has similar price rendering, apply there as well.

- [ ] **Step 4.3: 컴파일 + 커밋**

```bash
./gradlew :app:compileDebugKotlin
git add -A
git commit -m "fix(ui): Phase 0 bundle 4 — 북마크 토글·가격 단위 표시 2건"
```

---

## Bundle 5 — SiheungAlertDialog wrapper (1 item, ~1.5h)

**Files:** Create `ui/component/SiheungAlertDialog.kt`. Modify all `AlertDialog` call sites.

- [ ] **Step 5.1: SiheungAlertDialog wrapper 생성**

Create `app/src/main/java/com/example/siheunggagae/ui/component/SiheungAlertDialog.kt`:

```kotlin
package com.example.siheunggagae.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily

/**
 * 시흥가개 표준 AlertDialog — 모든 텍스트에 Pretendard 폰트 강제 적용.
 * 기본 Material AlertDialog 는 fontFamily 지정이 누락되면 Roboto 로 fallback.
 */
@Composable
fun SiheungAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmText: String = "확인",
    onConfirm: () -> Unit,
    dismissText: String? = "취소",
    onDismiss: (() -> Unit)? = onDismissRequest,
    confirmColor: Color = Color(0xFF614B3A),
    dismissColor: Color = Color(0xFF8A6E58),
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            Text(
                text = title,
                fontFamily = PretendardFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E120A),
            )
        },
        text = {
            Text(
                text = text,
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = Color(0xFF1E120A),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = confirmColor,
                )
            }
        },
        dismissButton = if (dismissText != null && onDismiss != null) {
            {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = dismissText,
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = dismissColor,
                    )
                }
            }
        } else null,
    )
}
```

- [ ] **Step 5.2: 마이그레이션 대상 식별**

```bash
grep -rn "AlertDialog(" app/src/main/java/com/example/siheunggagae/ui/screen/ | grep -v "SiheungAlertDialog"
```

Expected hits: `MatchingDetailScreen.kt`, `PetListScreen.kt`, `ChatScreen.kt`, possibly more. For each call site, decide:
- If it matches the wrapper's signature (title + text + confirm + dismiss) → migrate to `SiheungAlertDialog`
- If it has custom composable content (e.g., text field inside) → leave as-is but verify each inner Text has `fontFamily = PretendardFamily`

- [ ] **Step 5.3: 마이그레이션 실행**

For each AlertDialog matching the simple title+text+confirm+dismiss pattern, replace with `SiheungAlertDialog(...)`. Preserve existing color customizations by mapping to `confirmColor` / `dismissColor` parameters.

For dialogs with custom content (text fields, lists), only add `fontFamily = PretendardFamily` to the inner Text composables; leave structure alone.

- [ ] **Step 5.4: 컴파일 + 커밋**

```bash
./gradlew :app:compileDebugKotlin
git add -A
git commit -m "feat(ui): SiheungAlertDialog wrapper + 다이얼로그 폰트 일관성 마이그레이션 (#47)"
```

---

## Verification (after all bundles)

- [ ] **Final Step: 전체 빌드 + 단위 테스트**

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: BUILD SUCCESSFUL, all tests pass, APK produced.

- [ ] **Smoke 시각 점검 (수동)**

Phase 0 산출 PR 본문에 다음 시나리오 체크리스트 추가:
1. Map 검색바 placeholder 가독성 OK
2. Home 매장 리스트 제목 vs 서브 위계 OK
3. 매칭 상세에서 영문 상태가 한국어로 표시
4. 채팅 진입 시 별점 "4.9" 사라지고 "동네 매칭 회원"만 표시
5. 회원가입 비밀번호 힌트가 좁은 폭에서도 줄바꿈
6. 채팅·설정 시트에서 키보드 올라와도 입력창 가림 없음
7. 도움요청 캘린더에서 어제·그제 날짜 회색 비활성
8. 알림 탭 색이 검정 → 브라운으로 변경
9. 알림 카드 padding 일관
10. Settings Switch off 상태가 명확히 보임
11. 펫호텔 가격에 ₩…/박 표시
12. 뉴스 상세 북마크 탭 시 채워진 핑크 하트 + 햅틱
13. 펫 추가 나이가 30살(살) / 36개월(개월) 이상 안 올라감
14. 회원가입 약관 → 동의 (Phase 0 안 함, 표시만)
15. 매칭 상태가 영문 RAW 아닌 "지원자 모집 중" 등으로 표시
16. AlertDialog 폰트가 Pretendard
