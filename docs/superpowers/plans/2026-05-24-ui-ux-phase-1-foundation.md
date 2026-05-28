# Phase 1 — Design System Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Phase 2~6 에서 사용할 재사용 컴포넌트·토큰·유틸을 만든다.

**Architecture:** 모두 신규 파일 (`ui/component/*`, `ui/theme/Dimens.kt`, `ui/util/CategoryVisual.kt`). 기존 화면 미수정. 각 컴포넌트는 `@Preview` 포함, 유틸은 JUnit 테스트 포함.

**Tech Stack:** Kotlin · Jetpack Compose · Material 3 · Coil 3 · JUnit 4

**Roadmap:** `docs/superpowers/specs/2026-05-24-ui-ux-50-improvements-roadmap.md` §Phase 1

**기존 자산 (Phase 0 산출)**:
- `ui/util/StatusLabels.kt` (matchStatusToKorean — Task 7 에서 확장)
- `ui/component/SiheungAlertDialog.kt` (Task 9 에서 추가 사용 패턴 없음, 그대로 둠)

---

## File Structure

| 파일 | 책임 | 신규/수정 |
|---|---|---|
| `ui/theme/Dimens.kt` | spacing·radius·icon size 토큰 | 신규 |
| `ui/component/ShimmerBox.kt` | 로딩 스켈레톤 (translateX 브러시 애니메이션) | 신규 |
| `ui/component/CountUpText.kt` | Int 카운트업 텍스트 (0→target 1.2s) | 신규 |
| `ui/component/AppAsyncImage.kt` | Coil AsyncImage + placeholder/error 일관 처리 | 신규 |
| `ui/component/EmptyStateView.kt` | 원형 아이콘 + 제목 + 부제 + 선택적 CTA | 신규 |
| `ui/component/SheetHandle.kt` | BottomSheet drag 인디케이터 (32×4dp 베이지) | 신규 |
| `ui/util/StatusLabels.kt` | request/store 상태 매핑 추가 | 수정 (확장) |
| `ui/util/CategoryVisual.kt` | 카테고리 → emoji/color/gradient 단일 소스 | 신규 |
| `test/.../StatusLabelsTest.kt` | StatusLabels 단위 테스트 | 신규 |
| `test/.../CategoryVisualTest.kt` | CategoryVisual 단위 테스트 | 신규 |

---

## Task 1: Dimens 토큰 (`Dimens.kt`)

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/ui/theme/Dimens.kt`

- [ ] **Step 1: 파일 생성**

```kotlin
package com.example.siheunggagae.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 시흥가개 디자인 토큰 — Spacing, Radius, Icon, Elevation.
 * 모든 화면이 이 토큰을 사용하면 미세 일관성 깨짐 방지.
 */
object Dimens {
    // ─── Spacing ───────────────────────────────────────────────────────────
    /** 화면 좌우 공통 padding (이전엔 16/20 혼재) */
    val screenH = 20.dp
    /** 화면 위·아래 padding */
    val screenV = 16.dp
    /** 카드 내부 padding */
    val cardPad = 16.dp
    /** 섹션 간 vertical gap */
    val sectionGap = 24.dp
    /** 리스트 아이템 vertical padding */
    val itemV = 14.dp
    /** 작은 간격 */
    val s = 4.dp
    val m = 8.dp
    val l = 12.dp
    val xl = 16.dp
    val xxl = 24.dp

    // ─── Radius ────────────────────────────────────────────────────────────
    val radiusS = 8.dp
    val radiusM = 12.dp
    val radiusL = 16.dp
    val radiusXL = 20.dp
    /** Pill / fully rounded */
    val radiusPill = 50.dp

    // ─── Icon ──────────────────────────────────────────────────────────────
    val iconS = 16.dp
    val iconM = 20.dp
    val iconL = 24.dp
    val iconXL = 32.dp

    // ─── Elevation ─────────────────────────────────────────────────────────
    val elevS = 1.dp
    val elevM = 2.dp
    val elevL = 4.dp
    val elevXL = 8.dp
}
```

- [ ] **Step 2: 컴파일 확인 + 커밋**

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/theme/Dimens.kt
git commit -m "feat(theme): Dimens 토큰 — spacing·radius·icon·elevation 일관성 기반"
```

---

## Task 2: ShimmerBox 컴포넌트 (`ShimmerBox.kt`)

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/ui/component/ShimmerBox.kt`

- [ ] **Step 1: 파일 생성 (with Preview)**

```kotlin
package com.example.siheunggagae.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 로딩 스켈레톤. 베이지(#E8D3C2) → 화이트 → 베이지 그라디언트가 좌→우로 1.2s 무한 슬라이드.
 *
 * 사용:
 * ```
 * ShimmerBox(Modifier.height(20.dp).fillMaxWidth(0.6f))
 * ```
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    val widthPx = with(LocalDensity.current) { 600.dp.toPx() }
    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEAE0D6),
            Color(0xFFFFFAF7),
            Color(0xFFEAE0D6),
        ),
        start = Offset(x = -widthPx + translate * 2 * widthPx, y = 0f),
        end   = Offset(x = translate * 2 * widthPx, y = 0f),
    )
    Box(modifier = modifier.clip(shape).background(brush))
}

@Preview(showBackground = true, backgroundColor = 0xFFFEFEFE)
@Composable
private fun ShimmerBoxPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        ShimmerBox(Modifier.size(60.dp), shape = RoundedCornerShape(12.dp))
        Box(Modifier.height(12.dp))
        ShimmerBox(Modifier.fillMaxWidth(0.7f).height(16.dp))
        Box(Modifier.height(8.dp))
        ShimmerBox(Modifier.fillMaxWidth(0.5f).height(12.dp))
    }
}
```

- [ ] **Step 2: 컴파일 + 커밋**

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/component/ShimmerBox.kt
git commit -m "feat(ui): ShimmerBox 컴포넌트 — 로딩 스켈레톤 표준"
```

---

## Task 3: CountUpText 컴포저블 (`CountUpText.kt`)

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/ui/component/CountUpText.kt`

- [ ] **Step 1: 파일 생성 (with Preview)**

```kotlin
package com.example.siheunggagae.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily

/**
 * Int 값이 변경되면 0(또는 직전 값)에서 새 값까지 부드럽게 카운트업.
 *
 * - 기본 1200ms tween · FastOutSlowInEasing
 * - 사용처: 산책지수, 활동 통계, 매장 개수 등 수치 강조 위치
 *
 * @param value 표시할 최종 값
 * @param suffix 숫자 뒤 접미 (예: "점", "개", "건")
 */
@Composable
fun CountUpText(
    value: Int,
    modifier: Modifier = Modifier,
    suffix: String = "",
    durationMs: Int = 1200,
    style: TextStyle = TextStyle(
        fontFamily = PretendardFamily,
        fontSize = 30.sp,
        fontWeight = FontWeight.ExtraBold,
    ),
    color: Color = Color(0xFF1E120A),
) {
    val animated by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "countUp",
    )
    Text(text = "$animated$suffix", modifier = modifier, style = style, color = color)
}

@Preview(showBackground = true, backgroundColor = 0xFFFEFEFE)
@Composable
private fun CountUpTextPreview() {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .background(Color.White)
            .padding(16.dp),
    ) {
        CountUpText(value = 87, suffix = "점")
        CountUpText(value = 12, suffix = "개")
        CountUpText(value = 0, suffix = "건")
    }
}
```

- [ ] **Step 2: 컴파일 + 커밋**

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/component/CountUpText.kt
git commit -m "feat(ui): CountUpText 컴포저블 — 0→값 카운트업 애니메이션"
```

---

## Task 4: AppAsyncImage 컴포저블 (`AppAsyncImage.kt`)

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/ui/component/AppAsyncImage.kt`

- [ ] **Step 1: 파일 생성**

```kotlin
package com.example.siheunggagae.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.siheunggagae.R

/**
 * Coil AsyncImage 표준 wrapper.
 *
 * - 200ms crossfade
 * - placeholder 가 로드 전 ShimmerBox 로 표시 (또는 명시된 drawable)
 * - error 시 fallback drawable + 회색 배경
 * - URL 이 null/빈 문자열이면 placeholder/error 우선 표시
 *
 * @param model 이미지 URL (null 가능)
 * @param contentDescription 접근성용 설명
 * @param placeholderRes 로드 중 표시할 drawable (선택)
 * @param errorRes 로드 실패 시 표시할 drawable (선택)
 * @param showShimmer placeholder 가 없으면 ShimmerBox 표시 (기본 true)
 */
@Composable
fun AppAsyncImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    @DrawableRes placeholderRes: Int? = null,
    @DrawableRes errorRes: Int? = R.drawable.ic_image,
    contentScale: ContentScale = ContentScale.Crop,
    showShimmer: Boolean = true,
) {
    val context = LocalContext.current
    if (model.isNullOrBlank()) {
        Box(
            modifier = modifier
                .background(Color(0xFFF4F4F4))
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (errorRes != null) {
                androidx.compose.material3.Icon(
                    painter = painterResource(errorRes),
                    contentDescription = null,
                    tint = Color(0xFFC1AEA0),
                )
            }
        }
        return
    }
    Box(modifier = modifier) {
        if (showShimmer && placeholderRes == null) {
            ShimmerBox(modifier = Modifier.fillMaxSize())
        }
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(model)
                .crossfade(200)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            placeholder = placeholderRes?.let { painterResource(it) },
            error = errorRes?.let { painterResource(it) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
```

**Pre-check before coding:** Run `ls app/src/main/res/drawable/ | grep -i "image\|photo"` to confirm a reasonable default error drawable exists. If `ic_image.xml` is missing, substitute another existing icon (e.g., `ic_star.xml`) for the default and note in the commit message.

- [ ] **Step 2: 컴파일 + 커밋**

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/component/AppAsyncImage.kt
git commit -m "feat(ui): AppAsyncImage wrapper — Coil 일관 + Shimmer placeholder + error fallback"
```

---

## Task 5: EmptyStateView 컴포저블 (`EmptyStateView.kt`)

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/ui/component/EmptyStateView.kt`

- [ ] **Step 1: 파일 생성 (with Preview)**

```kotlin
package com.example.siheunggagae.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.R
import com.example.siheunggagae.ui.theme.PretendardFamily

/**
 * 화면 빈 상태 표준 컴포저블.
 *
 * 시각 구조:
 *   ┌──────────────┐
 *   │  ⭕ (80dp)    │  ← 원형 컬러 배경 + 아이콘
 *   │              │
 *   │   타이틀      │  ← 16sp ExtraBold
 *   │   부제목      │  ← 13sp, brown700
 *   │              │
 *   │   [CTA 버튼]  │  ← 선택적
 *   └──────────────┘
 */
@Composable
fun EmptyStateView(
    title: String,
    subtitle: String? = null,
    @DrawableRes iconRes: Int = R.drawable.ic_search,
    iconTint: Color = Color(0xFFF7A35B),         // Orange500
    iconBackground: Color = Color(0xFFFFEDD4),    // OrangeSand
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E120A),
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontFamily = PretendardFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF8A6E58),
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF614B3A))
                    .clickable { onAction() }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text(
                    text = actionLabel,
                    fontFamily = PretendardFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFEFEFE)
@Composable
private fun EmptyStateViewPreview() {
    EmptyStateView(
        title = "이 영역에 매장이 없어요",
        subtitle = "지도를 옮기거나 확대해 보세요",
        actionLabel = "전체 보기",
        onAction = {},
    )
}
```

**Pre-check:** Confirm `R.drawable.ic_search` exists. If not, substitute another safe default (`ic_star`, `ic_map`).

- [ ] **Step 2: 컴파일 + 커밋**

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/component/EmptyStateView.kt
git commit -m "feat(ui): EmptyStateView — 원형 아이콘 + 제목/부제 + CTA 표준"
```

---

## Task 6: SheetHandle 컴포저블 (`SheetHandle.kt`)

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/ui/component/SheetHandle.kt`

- [ ] **Step 1: 파일 생성**

```kotlin
package com.example.siheunggagae.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 표준 BottomSheet drag 인디케이터 — 32dp × 4dp 베이지 라운드 막대.
 *
 * 사용:
 * ```
 * ModalBottomSheet(dragHandle = { SheetHandle() }, ...)
 * ```
 */
@Composable
fun SheetHandle(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFE0D4CC),
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFEFEFE)
@Composable
private fun SheetHandlePreview() {
    SheetHandle()
}
```

- [ ] **Step 2: 컴파일 + 커밋**

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/component/SheetHandle.kt
git commit -m "feat(ui): SheetHandle — BottomSheet drag 인디케이터 표준"
```

---

## Task 7: StatusLabels 확장 + 테스트

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/util/StatusLabels.kt`
- Create: `app/src/test/java/com/example/siheunggagae/ui/util/StatusLabelsTest.kt`

기존 `matchStatusToKorean` 외에 `storeRequestStatusToKorean`(매장 요청 상태) 등 추가.

- [ ] **Step 1: failing test 작성**

Create `app/src/test/java/com/example/siheunggagae/ui/util/StatusLabelsTest.kt`:

```kotlin
package com.example.siheunggagae.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StatusLabelsTest {

    @Test fun matchStatus_known_codes() {
        assertEquals("지원자 모집 중", matchStatusToKorean("WAITING"))
        assertEquals("매칭 진행 중", matchStatusToKorean("MATCHING"))
        assertEquals("봉사 진행 중", matchStatusToKorean("PROGRESS"))
        assertEquals("봉사 진행 중", matchStatusToKorean("ONGOING"))
        assertEquals("봉사 완료", matchStatusToKorean("DONE"))
        assertEquals("봉사 완료", matchStatusToKorean("COMPLETED"))
        assertEquals("취소됨", matchStatusToKorean("CANCELED"))
        assertEquals("취소됨", matchStatusToKorean("CANCELLED"))
    }

    @Test fun matchStatus_null_or_blank_returns_default() {
        assertEquals("상태 없음", matchStatusToKorean(null))
        assertEquals("상태 없음", matchStatusToKorean(""))
        assertEquals("상태 없음", matchStatusToKorean("   "))
    }

    @Test fun matchStatus_case_insensitive() {
        assertEquals("지원자 모집 중", matchStatusToKorean("waiting"))
        assertEquals("지원자 모집 중", matchStatusToKorean("Waiting"))
    }

    @Test fun matchStatus_unknown_passes_through() {
        assertEquals("FOO_BAR", matchStatusToKorean("FOO_BAR"))
    }

    @Test fun storeRequestStatus_known_codes() {
        assertEquals("검토 중", storeRequestStatusToKorean("PENDING"))
        assertEquals("승인됨", storeRequestStatusToKorean("APPROVED"))
        assertEquals("거절됨", storeRequestStatusToKorean("REJECTED"))
    }

    @Test fun storeRequestStatus_unknown_passes_through() {
        assertEquals("XYZ", storeRequestStatusToKorean("XYZ"))
    }
}
```

- [ ] **Step 2: 테스트 실행해 실패 확인**

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.ui.util.StatusLabelsTest"
```
Expected: FAIL — `storeRequestStatusToKorean` unresolved.

- [ ] **Step 3: StatusLabels 확장**

Modify `app/src/main/java/com/example/siheunggagae/ui/util/StatusLabels.kt` — add at the bottom:

```kotlin
/**
 * 매장 등록 요청 상태 영문 코드 → 한국어.
 */
fun storeRequestStatusToKorean(status: String?): String = when (status?.trim()?.uppercase()) {
    "PENDING"  -> "검토 중"
    "APPROVED" -> "승인됨"
    "REJECTED" -> "거절됨"
    null, ""   -> "상태 없음"
    else       -> status!!
}
```

- [ ] **Step 4: 테스트 통과 확인 + 커밋**

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.ui.util.StatusLabelsTest"
# Expected: 5 tests pass
git add app/src/main/java/com/example/siheunggagae/ui/util/StatusLabels.kt \
        app/src/test/java/com/example/siheunggagae/ui/util/StatusLabelsTest.kt
git commit -m "feat(util): StatusLabels — storeRequestStatusToKorean 추가 + 단위 테스트"
```

---

## Task 8: CategoryVisual — 카테고리 단일 소스

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/ui/util/CategoryVisual.kt`
- Create: `app/src/test/java/com/example/siheunggagae/ui/util/CategoryVisualTest.kt`

현재 카테고리 → emoji, color, gradient 가 4곳에 흩어져 있음. 단일 소스로 정리.

- [ ] **Step 1: failing test 작성**

Create `app/src/test/java/com/example/siheunggagae/ui/util/CategoryVisualTest.kt`:

```kotlin
package com.example.siheunggagae.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CategoryVisualTest {

    @Test fun visualFor_known_categories_distinct() {
        val cafe = CategoryVisual.forCategory("CAFE")
        val park = CategoryVisual.forCategory("PARK")
        val hospital = CategoryVisual.forCategory("HOSPITAL")
        // 각 카테고리는 서로 다른 색
        assertEquals(false, cafe.colorInt == park.colorInt)
        assertEquals(false, park.colorInt == hospital.colorInt)
        // 그라디언트는 2색 이상
        assertEquals(true, cafe.gradient.size >= 2)
    }

    @Test fun visualFor_unknown_returns_default() {
        val default = CategoryVisual.forCategory("UNKNOWN_XYZ")
        assertNotNull(default)
        assertEquals(CategoryVisual.DEFAULT, default)
    }

    @Test fun visualFor_null_returns_default() {
        assertEquals(CategoryVisual.DEFAULT, CategoryVisual.forCategory(null))
    }

    @Test fun visualFor_case_insensitive() {
        assertEquals(CategoryVisual.forCategory("CAFE"), CategoryVisual.forCategory("cafe"))
        assertEquals(CategoryVisual.forCategory("CAFE"), CategoryVisual.forCategory("Cafe"))
    }

    @Test fun korean_label_present() {
        assertEquals("카페", CategoryVisual.forCategory("CAFE").koreanLabel)
        assertEquals("공원", CategoryVisual.forCategory("PARK").koreanLabel)
    }
}
```

- [ ] **Step 2: 테스트 실행 (실패 확인)**

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.ui.util.CategoryVisualTest"
```
Expected: FAIL — `CategoryVisual` unresolved.

- [ ] **Step 3: CategoryVisual 구현**

Create `app/src/main/java/com/example/siheunggagae/ui/util/CategoryVisual.kt`:

```kotlin
package com.example.siheunggagae.ui.util

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.siheunggagae.R

/**
 * 카테고리(영문 코드) → emoji, drawable, color, gradient, 한국어 라벨 단일 소스.
 *
 * 사용 예:
 * ```
 * val v = CategoryVisual.forCategory("CAFE")
 * Box(background = Brush.linearGradient(v.gradient)) { Icon(v.drawableRes, tint = v.color) }
 * ```
 */
data class CategoryVisual(
    val code: String,
    val koreanLabel: String,
    val emoji: String,
    @DrawableRes val drawableRes: Int,
    val colorInt: Int,
    val gradient: List<Color>,
) {
    val color: Color get() = Color(colorInt)

    companion object {
        val DEFAULT = CategoryVisual(
            code = "DEFAULT",
            koreanLabel = "매장",
            emoji = "★",
            drawableRes = R.drawable.ic_star,
            colorInt = 0xFF614B3A.toInt(),
            gradient = listOf(Color(0xFFE8D3C2), Color(0xFFFFFAF7)),
        )

        private val MAP: Map<String, CategoryVisual> = listOf(
            CategoryVisual("CAFE",       "카페",     "☕", R.drawable.ic_coffee,       0xFF8A6E58.toInt(),
                gradient = listOf(Color(0xFFFFEDD4), Color(0xFFF7A35B))),
            CategoryVisual("PARK",       "공원",     "🌳", R.drawable.ic_forest,       0xFF4CAF50.toInt(),
                gradient = listOf(Color(0xFFD0FEE1), Color(0xFFB2DFBF))),
            CategoryVisual("HOSPITAL",   "동물병원", "🏥", R.drawable.ic_health_cross, 0xFFF04268.toInt(),
                gradient = listOf(Color(0xFFFEE7EC), Color(0xFFFFC1CC))),
            CategoryVisual("GROOMING",   "미용",     "✂",  R.drawable.ic_content_cut,  0xFF9C27B0.toInt(),
                gradient = listOf(Color(0xFFEBD4F5), Color(0xFFD1B0E5))),
            CategoryVisual("RESTAURANT", "음식점",   "🍽", R.drawable.ic_utensils,     0xFFF7A35B.toInt(),
                gradient = listOf(Color(0xFFFFEDD4), Color(0xFFFFCB91))),
            CategoryVisual("PET_HOTEL",  "펫호텔",   "🏨", R.drawable.ic_hotel,        0xFF614B3A.toInt(),
                gradient = listOf(Color(0xFFE8D3C2), Color(0xFFC4A882))),
        ).associateBy { it.code }

        fun forCategory(category: String?): CategoryVisual =
            category?.trim()?.uppercase()?.let { MAP[it] } ?: DEFAULT
    }
}
```

**Pre-check:** Verify all referenced drawables exist:
```bash
ls app/src/main/res/drawable/ | grep -E "ic_(coffee|forest|health_cross|content_cut|utensils|hotel|star)\.xml$"
```
Expected: 7 files. (Phase 0 의 MapViewWrapper refactor 에서 이미 사용 검증됨.)

- [ ] **Step 4: 테스트 통과 + 커밋**

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.ui.util.CategoryVisualTest"
# Expected: 5 tests pass
git add app/src/main/java/com/example/siheunggagae/ui/util/CategoryVisual.kt \
        app/src/test/java/com/example/siheunggagae/ui/util/CategoryVisualTest.kt
git commit -m "feat(util): CategoryVisual — 카테고리 시각 토큰 단일 소스 + 단위 테스트"
```

---

## Verification (after all tasks)

- [ ] **Final: 전체 빌드 + 단위 테스트**

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: BUILD SUCCESSFUL, all tests pass (기존 + 신규 ~10개), APK produced.

- [ ] **Preview 점검 (수동)**

Android Studio Preview 에서 각 컴포넌트 Preview 가 정상 렌더되는지:
- ShimmerBox: 그라디언트 슬라이드 보이는지
- CountUpText: 숫자 표시 (애니메이션은 Preview 에서 안 보임)
- AppAsyncImage: model = null 시 회색 박스 + 아이콘
- EmptyStateView: 원형 아이콘 + 제목 + CTA 버튼
- SheetHandle: 32dp 베이지 막대

## Self-Review

- 모든 신규 파일이 정확한 패키지 (`com.example.siheunggagae.ui.component/theme/util`) 에 위치
- `R.drawable.ic_*` 참조가 실제 존재 (사전 ls 확인)
- 테스트 5+5 = 10 tests pass
- 기존 화면 코드 미수정 (Phase 2+ 의 책임)
