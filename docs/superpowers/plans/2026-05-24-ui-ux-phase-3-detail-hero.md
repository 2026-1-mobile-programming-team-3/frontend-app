# Phase 3 — Detail Hero & Card Interaction Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development.

**Goal:** 매장 상세 시트·매장 상세 화면·매칭 카드·리뷰 화면의 시각 깊이·상호작용 강화.

**Architecture:** Phase 1 의 `CategoryVisual` 단일 소스 활용. 신규 컴포넌트 없음.

**Roadmap:** §Phase 3

---

## File Structure

| 파일 | 항목 |
|---|---|
| `ui/screen/MapScreen.kt` | #3 StoreDetailSheet 히어로 |
| `ui/screen/PlaceDetailScreen.kt` | #4 hero, #14 LocationCard 미니맵 |
| `ui/screen/MatchingScreen.kt` | #6 MatchCardR appleTapScale |
| `ui/screen/MatchReviewScreen.kt` | #33 별점 단계별 이모지 |

---

## Bundle A — Map StoreDetailSheet + PlaceDetail hero (3 items, ~6h)

### Item A.1 — #3 Map StoreDetailSheet 히어로

File: `app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt`

**Pre-check:**
```bash
grep -n "StoreDetailSheet\|0xFFD0FEE1\|ic_location_on" app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt | head -20
```

**현재 (대략):**
```kotlin
Box(modifier = Modifier.fillMaxWidth().height(90.dp)
    .background(Brush.linearGradient(listOf(Color(0xFFD0FEE1), Color(0xFFE0F7FA))))) {
    Icon(painterResource(R.drawable.ic_location_on), null, tint = Pink500Mp, modifier = Modifier.size(36.dp))
}
```

**변경:** 카테고리별 그라디언트 + 카테고리 대형 아이콘 (CategoryVisual 활용):

```kotlin
import androidx.compose.ui.graphics.Brush
import com.example.siheunggagae.ui.util.CategoryVisual

// store.category 로 카테고리 비주얼 가져오기
val visual = CategoryVisual.forCategory(store.category)
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
        .background(Brush.linearGradient(visual.gradient)),
    contentAlignment = Alignment.Center,
) {
    // 워터마크 (대형, 반투명)
    Icon(
        painter = painterResource(visual.drawableRes),
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.18f),
        modifier = Modifier.size(140.dp).align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 8.dp),
    )
    // 전경 카테고리 아이콘 (선명)
    Icon(
        painter = painterResource(visual.drawableRes),
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(56.dp),
    )
}
```

높이 90dp → 120dp.

### Item A.2 — #4 PlaceDetail hero 강화 (160dp → 220dp + 워터마크)

File: `app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt`

**Pre-check:**
```bash
grep -n "MapMintPL\|160.dp\|0xFFB2DFBF\|linearGradient" app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt | head -20
```

**현재 hero (대략):**
```kotlin
Box(modifier = Modifier.fillMaxWidth().height(160.dp)
    .background(Brush.linearGradient(listOf(MapMintPL, Color(0xFFB2DFBF), Color(0xFFD8F2DC))))) {
    // 텍스트만 하단에 위치, 아이콘 없음
}
```

**변경:**
1. 높이 160dp → 220dp
2. 그라디언트를 카테고리별로 (CategoryVisual)
3. 우하단 워터마크 패턴 (대형 카테고리 아이콘, alpha 0.12)
4. 중앙 전경 아이콘 (72dp)
5. 하단에 흰 카드 연결 (선택적 — 콘텐츠 영역 위로 살짝 올라오는 R(28dp) topRound)

```kotlin
val visual = CategoryVisual.forCategory(store.category)  // store/detail 객체
Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
    // 카테고리 그라디언트 배경
    Box(
        modifier = Modifier
            .matchParentSize()
            .background(Brush.linearGradient(visual.gradient)),
    )
    // 워터마크 (배경 패턴)
    Icon(
        painter = painterResource(visual.drawableRes),
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.12f),
        modifier = Modifier
            .size(180.dp)
            .align(Alignment.BottomEnd)
            .padding(end = 8.dp, bottom = 8.dp),
    )
    // 전경 카테고리 아이콘
    Icon(
        painter = painterResource(visual.drawableRes),
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(72.dp).align(Alignment.Center),
    )
    // 하단 흰 카드 연결 (콘텐츠 영역 흐름 자연)
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White),
    )
}
```

기존 텍스트 (매장명, 주소 등) 가 hero 안에 있었으면, 그 텍스트는 hero 박스 밖 (콘텐츠 영역) 으로 이동 필요. 또는 hero 안 흰 카드 영역 안에 배치.

### Item A.3 — #14 PlaceDetail LocationCard 미니맵 160dp + CTA

같은 파일. `LocationCardPL` 또는 미니맵 관련 컴포저블 찾기.

**Pre-check:**
```bash
grep -n "LocationCardPL\|100.dp.*Map\|MapView" app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt | head
```

**변경:** 100dp → 160dp + 우하단 "지도에서 보기 →" pill

```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(160.dp)
        .clip(RoundedCornerShape(16.dp)),
) {
    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
    // 우하단 CTA pill
    Row(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(8.dp)
            .shadow(2.dp, RoundedCornerShape(50.dp))
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .clickable { /* onMapClick or navigate to Map */ }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "지도에서 보기 →",
            fontFamily = PretendardFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Brown900PL,
        )
    }
}
```

기존 클릭 핸들러가 있으면 그대로 보존.

### 컴파일 + 커밋

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt
git commit -m "feat(ui): Phase 3 bundle A — 상세 hero 폴리시 3건

#3 Map StoreDetailSheet 히어로: 카테고리 그라디언트 + 대형 아이콘 (CategoryVisual)
#4 PlaceDetail hero 160→220dp + 카테고리 워터마크 패턴 + 전경 아이콘
#14 PlaceDetail LocationCard 미니맵 100→160dp + '지도에서 보기 →' pill"
```

---

## Bundle B — Matching card + MatchReview (2 items, ~4h)

### Item B.1 — #6 Matching MatchCardR appleTapScale + haptic

File: `app/src/main/java/com/example/siheunggagae/ui/screen/MatchingScreen.kt`

**Pre-check:**
```bash
grep -n "MatchCardR\|fun MatchCardR\|appleTapScale" app/src/main/java/com/example/siheunggagae/ui/screen/MatchingScreen.kt | head
```

**현재:** Surface(...).clickable { onClick() } — 단순 클릭, 피드백 없음.

**변경:**
```kotlin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.siheunggagae.ui.util.appleTapScale
import com.example.siheunggagae.ui.util.rememberAppleInteractionSource

@Composable
fun MatchCardR(...) {
    val interaction = rememberAppleInteractionSource()
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(...)
            .alpha(cardAlpha)
            .appleTapScale(interaction)
            .clickable(interactionSource = interaction, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
    ) { ... }
}
```

기존 `.clickable { onClick() }` 가 indication 갖고 있으면 ripple 제거 + interaction source 일관.

### Item B.2 — #33 MatchReview 별점 단계별 이모지

File: `app/src/main/java/com/example/siheunggagae/ui/screen/MatchReviewScreen.kt`

**Pre-check:**
```bash
grep -n "rating\|Star\|isSelected" app/src/main/java/com/example/siheunggagae/ui/screen/MatchReviewScreen.kt | head -10
```

**현재:** 별 5개 Icon Row, 색상만 바뀜.

**변경:**
1. 별 클릭 시 scale punch (animateFloatAsState spring)
2. 별점 1~5에 대응하는 텍스트+이모지 라벨 (rating > 0 일 때 표시)

```kotlin
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.scale

val ratingLabels = mapOf(
    1 to "아쉬웠어요 😢",
    2 to "조금 아쉬워요 😕",
    3 to "보통이에요 😐",
    4 to "좋았어요 😊",
    5 to "훌륭했어요! 🤩",
)

// Star Row
Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    (1..5).forEach { idx ->
        val isSelected = idx <= rating
        val scale by animateFloatAsState(
            targetValue = if (isSelected) 1.15f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessHigh),
            label = "starScale_$idx",
        )
        Icon(
            painter = painterResource(R.drawable.ic_star),
            contentDescription = "별점 $idx",
            tint = if (isSelected) Color(0xFFFFB200) else Color(0xFFE8D3C2),
            modifier = Modifier
                .size(40.dp)
                .scale(scale)
                .clickable { rating = idx },
        )
    }
}

// Rating label
AnimatedVisibility(visible = rating > 0) {
    Text(
        text = ratingLabels[rating] ?: "",
        fontFamily = PretendardFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = Orange500MR,  // 또는 기존 토큰
        modifier = Modifier.padding(top = 12.dp),
    )
}
```

기존 별 클릭 → `rating = idx` 로직 보존. label 위치는 별 Row 아래.

### 컴파일 + 커밋

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/screen/MatchingScreen.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/MatchReviewScreen.kt
git commit -m "feat(ui): Phase 3 bundle B — 매칭 카드·리뷰 폴리시 2건

#6 MatchingScreen MatchCardR appleTapScale + haptic LongPress
#33 MatchReview 별점 scale punch (spring 1.15×) + 단계별 이모지+텍스트 라벨"
```

---

## Verification

- [ ] **Final: 전체 빌드 + 테스트**

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Expected: BUILD SUCCESSFUL.

- [ ] **Visual smoke check**

1. Map 매장 탭 → 시트 상단이 카테고리별 다른 색 그라디언트 + 카테고리 아이콘
2. PlaceDetail 진입 시 hero 220dp + 워터마크 패턴 보임
3. PlaceDetail LocationCard 미니맵이 160dp 로 확대됨, 우하단 "지도에서 보기 →" pill 보임
4. MatchingScreen 카드 탭 시 scale shrink + haptic
5. MatchReview 별 탭 시 scale punch + 아래 단계별 텍스트 "훌륭했어요! 🤩" 등
