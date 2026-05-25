# Phase 8 — Infrastructure (Pull-to-Refresh · Dark Mode · Shared Element) Implementation Plan

> Use superpowers:subagent-driven-development.

**Goal:** 마지막 인프라 3건 — Pull-to-Refresh 전역, 다크 모드 토대, Shared Element Transition 1건 (데모).

**Architecture:** 3 sub-phase 로 분할 — 각각 독립 PR.

**Roadmap:** §Phase 8

**Risk:** 다크 모드는 전면 마이그레이션 불가 (모든 화면이 hardcoded color 사용 중) → 토대만 구축 + 데모 화면만 부분 적용. Shared Element 도 1 transition 만.

---

## Phase 8a — Pull-to-Refresh (#48, ~4h)

### Item — PullToRefreshBox 전역 적용

대상 화면 (refresh 의미 있는 곳):
- NotificationScreen
- FavoriteStoresScreen
- MatchingScreen
- NewsScreen

각 화면에서 ViewModel 에 `refresh()` 메서드 확인 + `PullToRefreshBox` 로 LazyColumn 감싸기.

```kotlin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XxxScreen(viewModel: XxxViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing = uiState.isLoading  // or 별도 isRefreshing state

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(...) { ... }
    }
}
```

**Pre-check:** Material 3 PullToRefreshBox 가 현재 Compose 버전에서 가능한지 확인:
```bash
grep -E "compose-material3 = |androidx.compose.material3:material3" gradle/libs.versions.toml app/build.gradle.kts | head
```

Material3 1.3.0+ 에서 PullToRefreshBox 정식. 그 미만이면 deprecated PullRefreshIndicator + pullRefresh modifier 사용. Compose BOM 2024.06+ 이면 OK.

각 ViewModel 의 refresh() 메서드 존재 확인. 없으면 추가:
```bash
grep -rn "fun refresh\b" app/src/main/java/com/example/siheunggagae/ui/viewmodel/ | grep -E "(Notification|FavoriteStores|Matching|News)"
```

없는 ViewModel 에는 단순 reload 메서드 추가:
```kotlin
fun refresh() {
    viewModelScope.launch {
        loadInitial()  // 또는 기존 init 시 호출하는 패턴
    }
}
```

### 커밋

```bash
git add -A
git commit -m "feat(ui): #48 Pull-to-Refresh 적용 (Notification, Favorite, Matching, News)"
```

---

## Phase 8b — 다크 모드 토대 (#49, ~6h, 토대만)

전면 다크 모드는 50+ 화면이 hardcoded color 사용 중이라 비현실적. 본 plan 은:

1. **다크 colorScheme 정의** — Theme.kt 에 `darkColorScheme(...)` 추가
2. **Surface/Background 등 기본 색은 MaterialTheme 활용 확인** — Scaffold containerColor 등이 toggle 가능한지
3. **데모용 1-2 화면 변환** — Home + My 화면을 MaterialTheme.colorScheme 활용으로 부분 마이그레이션 (단, 모든 hardcoded color 제거는 아님)
4. **시스템 설정 따라가기** — `isSystemInDarkTheme()` 기본 활성화

### Step 1: `Theme.kt` 확장

```bash
grep -n "darkColorScheme\|lightColorScheme\|SiheungGagaeTheme" app/src/main/java/com/example/siheunggagae/ui/theme/Theme.kt
```

기존 Theme.kt 가 어떤 구조인지 확인 후, 부족하면 `darkColorScheme` 추가.

```kotlin
// Theme.kt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

private val LightColors = lightColorScheme(
    primary = Color(0xFF614B3A),
    onPrimary = Color.White,
    background = Color(0xFFFEFEFE),
    onBackground = Color(0xFF1E120A),
    surface = Color.White,
    onSurface = Color(0xFF1E120A),
    surfaceVariant = Color(0xFFFEE7EC),
    secondary = Color(0xFFF7A35B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC4A882),         // 베이지 톤 (다크에서 잘 보임)
    onPrimary = Color(0xFF1E120A),
    background = Color(0xFF1A1410),      // 매우 어두운 갈색
    onBackground = Color(0xFFFEFEFE),
    surface = Color(0xFF2A1F18),         // 카드 배경 톤
    onSurface = Color(0xFFFEFEFE),
    surfaceVariant = Color(0xFF3D2F25),
    secondary = Color(0xFFF7A35B),       // 오렌지는 그대로
)

@Composable
fun SiheungGagaeTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,  // 기존
        content = content,
    )
}
```

기존 Theme 정의 형태에 맞춰 조정.

### Step 2: 최소 검증

빌드 + 시스템 다크 모드 켜고 진입 시 화이트 배경 화면이 회색~검정으로 자동 전환되는지 (Scaffold containerColor 가 MaterialTheme 기반인 곳만). 대부분 hardcoded `Color(0xFFFEFEFE)` 라 효과 제한적 — 그래도 토대는 마련.

### Step 3: 데모용 부분 적용 (선택)

`HomeScreen` 의 `Scaffold(containerColor = ...)` 를 `MaterialTheme.colorScheme.background` 로 교체 등. 1-2개만.

### 커밋

```bash
git add -A
git commit -m "feat(theme): #49 다크 모드 colorScheme 토대 + 시스템 설정 자동 반영

전면 마이그레이션은 별도 — 현재는 colorScheme 만 정의 + Theme wrapper 분기.
이후 화면별로 hardcoded color 를 MaterialTheme.colorScheme.* 로 점진 마이그레이션 필요."
```

---

## Phase 8c — Shared Element Transition (#50, ~6h, 데모 1건만)

### Item — Home StoreItem → PlaceDetail hero shared element

Compose 1.7+ `SharedTransitionLayout` API 활용. 가장 임팩트 큰 1 transition 만 적용:
- Home 화면의 `HomeStoreItem` 카드 썸네일/이름
- PlaceDetail 의 hero 영역

### Step 1: Compose 버전 확인

```bash
grep -E "compose|composeBom" gradle/libs.versions.toml | head
```

Compose BOM 2024.06 이상 이어야 SharedTransitionLayout 안정 사용 가능.

### Step 2: NavGraph 에 SharedTransitionLayout 적용

Compose Navigation 1.8+ 의 `SharedTransitionLayout` + `AnimatedContent` 사용:

```kotlin
// NavGraph.kt
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavGraph(...) {
    SharedTransitionLayout {
        NavHost(...) {
            composable(Home) { backStack ->
                HomeScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    ...
                )
            }
            composable(PlaceDetail) { backStack ->
                PlaceDetailScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    ...
                )
            }
        }
    }
}
```

### Step 3: HomeStoreItem · PlaceDetail hero 에 sharedElement modifier

```kotlin
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeStoreItem(
    place: StoreResponse,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: (Int) -> Unit,
) {
    with(sharedTransitionScope) {
        Row(
            modifier = Modifier
                .sharedElement(
                    rememberSharedContentState(key = "store_${place.resolvedId}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                .clickable { onClick(place.resolvedId) },
            ...
        ) { ... }
    }
}
```

PlaceDetail hero 영역도 동일 key 로 sharedElement modifier 적용.

### Risk

- NavGraph 전체에 `SharedTransitionLayout` 도입은 모든 composable 의 시그니처에 영향
- 1 transition 만 검증 후 다른 화면은 그대로 (key 안 맞으면 sharedElement 가 그냥 무시됨)
- Compose 버전이 낮으면 SKIP — 그 경우 plan 갱신 후 fallback to fadeIn/Out

### 커밋

```bash
git add -A
git commit -m "feat(nav): #50 SharedTransitionLayout 도입 + Home → PlaceDetail hero shared element

Compose 1.7+ shared transition 1건만 데모 적용.
다른 transition 은 자동 fadeIn/Out fallback."
```

---

## Verification (after all sub-phases)

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## Order

순서대로:
1. 8a (가장 단순, 즉시 가치)
2. 8b (토대만 — 회귀 위험 낮음)
3. 8c (가장 위험 — 마지막)

각 sub-phase 별로 별도 commit + 압축 검증.
