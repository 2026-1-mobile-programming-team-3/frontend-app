# Phase 5 — Empty States · Skeletons · Filters Implementation Plan

> Use superpowers:subagent-driven-development.

**Goal:** Phase 1 foundation 컴포넌트(`EmptyStateView`, `ShimmerBox`, `SheetHandle`)를 화면 전반에 일관 적용 + News 화면 filter/search 보완.

**Roadmap:** §Phase 5

---

## Bundle A — Empty States + Sheet Handle (3 items, ~1h)

### Item A.1 — #13 Map 빈 상태 → EmptyStateView

File: `app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt`

Find the empty state block in `MapBottomSheetContent` (현재 텍스트 2줄만):

```kotlin
if (stores.isEmpty()) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "이 영역에는 표시할 매장이 없어요", ...)
            Spacer(Modifier.height(4.dp))
            Text(text = "지도를 더 넓혀 보거나 다른 동네로 이동해 주세요", ...)
        }
    }
}
```

Replace with:

```kotlin
import com.example.siheunggagae.ui.component.EmptyStateView

if (stores.isEmpty()) {
    EmptyStateView(
        title = "이 영역에 매장이 없어요",
        subtitle = "지도를 옮기거나 확대해 보세요",
        iconRes = R.drawable.ic_map,  // 확인 — 없으면 ic_search fallback
    )
}
```

Pre-check `R.drawable.ic_map`:
```bash
ls app/src/main/res/drawable/ | grep -E "^ic_map\.xml"
```
없으면 `ic_search` 또는 `ic_location_on` 사용.

### Item A.2 — #28 Notification 빈 상태 → EmptyStateView

File: `app/src/main/java/com/example/siheunggagae/ui/screen/NotificationScreen.kt`

Find `EmptyNotification` Composable. Currently uses `Icons.Default.PriorityHigh` (느낌표 — 부적절).

Replace with `EmptyStateView`:

```kotlin
import com.example.siheunggagae.ui.component.EmptyStateView

EmptyStateView(
    title = "아직 알림이 없어요",
    subtitle = "새로운 활동이 있으면 여기에 표시됩니다",
    iconRes = R.drawable.ic_notifications,  // 확인 — 없으면 ic_notification 또는 ic_bell
    iconTint = Color(0xFF8A6E58),
    iconBackground = Color(0xFFE8D3C2),
)
```

Pre-check drawable:
```bash
ls app/src/main/res/drawable/ | grep -E "ic_(notification|bell|alert)" | head
```

### Item A.3 — #45 MapFilter dragHandle 커스텀 (SheetHandle)

File: `app/src/main/java/com/example/siheunggagae/ui/screen/MapFilterBottomSheet.kt`

Find `dragHandle = null` (line 81). Replace with:

```kotlin
import com.example.siheunggagae.ui.component.SheetHandle

dragHandle = { SheetHandle() },
```

Add the import.

### 컴파일 + 커밋

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/NotificationScreen.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/MapFilterBottomSheet.kt
git commit -m "feat(ui): Phase 5 bundle A — 빈 상태·sheet handle 일관성 3건

#13 Map 빈 상태 → EmptyStateView (원형 아이콘 + 제목·부제)
#28 Notif 빈 상태 PriorityHigh(느낌표) → EmptyStateView (ic_notifications)
#45 MapFilter dragHandle null → SheetHandle 커스텀 인디케이터"
```

---

## Bundle B — News Filter & Search (2 items, ~3h)

### Item B.1 — #9 News CategoryFilter pill 스타일

File: `app/src/main/java/com/example/siheunggagae/ui/screen/NewsScreen.kt`

Find `NewsCategoryFilter` Composable (line ~304). 현재 색상만 바뀌고 pill 없음.

Replace with pill style (Map/Matching 와 동일 패턴):

```kotlin
import com.example.siheunggagae.ui.util.appleTapScale
import com.example.siheunggagae.ui.util.rememberAppleInteractionSource

Row(...) {
    categories.forEach { cat ->
        val isSelected = cat == selected
        val bg by animateColorAsState(
            targetValue = if (isSelected) Color(0xFF1A1A1A) else Color.White,
            animationSpec = appleSpec(),
            label = "newsChipBg",
        )
        val fg by animateColorAsState(
            targetValue = if (isSelected) Color.White else Brown700Ns,
            animationSpec = appleSpec(),
            label = "newsChipFg",
        )
        val interaction = rememberAppleInteractionSource()
        Box(
            modifier = Modifier
                .appleTapScale(interaction)
                .shadow(2.dp, RoundedCornerShape(50.dp))
                .clip(RoundedCornerShape(50.dp))
                .background(bg)
                .clickable(interactionSource = interaction, indication = null) { onSelect(cat) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = cat,
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                color = fg,
            )
        }
    }
}
```

`appleSpec` 의 정확한 import path 는 grep:
```bash
grep -rn "fun appleSpec" app/src/main/java/com/example/siheunggagae/ui/util/
```

`Brown700Ns` 토큰은 News 화면 내부에 정의된 것 사용 (혹은 같은 색 hex).

### Item B.2 — #16 News SearchBar 실동작

같은 파일. `NewsSearchBar` 가 현재 더미 (clickable·input 없음).

MapScreen 의 `MapSearchOverlay` 패턴을 참고해 `NewsSearchOverlay` 추가:

```kotlin
// NewsScreen 의 메인 Composable 내부
var showSearch by remember { mutableStateOf(false) }

// NewsSearchBar 호출에 clickable 추가
NewsSearchBar(onClick = { showSearch = true })

// 오버레이
if (showSearch) {
    NewsSearchOverlay(
        allNews = uiState.newsList,  // 또는 viewModel.allNews
        onDismiss = { showSearch = false },
        onResultClick = { newsId ->
            showSearch = false
            onNavigate(Screen.NewsDetail.createRoute(newsId))
        },
    )
}
```

`NewsSearchBar` 가 `clickable` 받도록 시그니처 변경:
```kotlin
@Composable
private fun NewsSearchBar(onClick: () -> Unit = {}) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(...)
        .shadow(...)
        .clip(RoundedCornerShape(50.dp))
        .background(Color.White)
        .clickable { onClick() }    // 추가
        .padding(horizontal = 16.dp, vertical = 12.dp),
        ...
    ) { ... }
}
```

`NewsSearchOverlay` Composable 작성 (MapSearchOverlay 와 유사 구조):

```kotlin
@Composable
private fun NewsSearchOverlay(
    allNews: List<NewsItem>,
    onDismiss: () -> Unit,
    onResultClick: (newsId: String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query, allNews) {
        if (query.isBlank()) emptyList()
        else allNews.filter { (it.title ?: "").contains(query, ignoreCase = true) }
            .take(30)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // 검색 입력 Row (MapSearchOverlay 와 유사)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .clickable { onDismiss() },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "닫기",
                        tint = Brown700Ns,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .shadow(2.dp, RoundedCornerShape(50.dp))
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "뉴스 · 정책 · 행사 검색",
                            fontFamily = PretendardFamily,
                            fontSize = 15.sp,
                            color = Brown700Ns,
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = PretendardFamily,
                            fontSize = 15.sp,
                            color = Color(0xFF1E120A),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFF3F4F6))

            if (query.isBlank()) {
                EmptyStateView(
                    title = "어떤 뉴스를 찾으세요?",
                    subtitle = "키워드를 입력해 보세요",
                    iconRes = R.drawable.ic_search,
                )
            } else if (results.isEmpty()) {
                EmptyStateView(
                    title = "검색 결과가 없어요",
                    subtitle = "다른 키워드로 시도해 보세요",
                    iconRes = R.drawable.ic_search,
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(results, key = { it.newsId ?: it.hashCode() }) { news ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { news.newsId?.let { onResultClick(it) } }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = news.title ?: "",
                                    fontFamily = PretendardFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 20.sp,
                                    color = Color(0xFF1E120A),
                                )
                                Text(
                                    text = news.category ?: "",
                                    fontFamily = PretendardFamily,
                                    fontSize = 12.sp,
                                    color = Brown700Ns,
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = Color(0xFFF3F4F6),
                        )
                    }
                }
            }
        }
    }
}
```

**중요**: `NewsItem.newsId`, `.title`, `.category` 필드명을 실제 모델에 맞춰 조정. grep:
```bash
grep -n "data class NewsItem\|newsId\|title\|category" app/src/main/java/com/example/siheunggagae/data/model/HomeModels.kt 2>/dev/null
```

만약 newsId 가 Int 면 `String` 대신 `Int` 로 시그니처 조정.

검색은 viewModel 에서 가진 `newsList` 를 클라이언트에서 필터링 (서버 검색 API 가 없다면).

### 컴파일 + 커밋

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/screen/NewsScreen.kt
git commit -m "feat(ui): Phase 5 bundle B — News 카테고리 pill + SearchBar 실동작 2건

#9 News CategoryFilter pill 스타일 (Map/Matching 통일)
#16 News SearchBar 더미 → NewsSearchOverlay (클라이언트 제목 필터)"
```

---

## Bundle C — Shimmer Skeleton 6개 화면 적용 (~3h)

기존 정적 회색 박스(`Color(0xFFF4F4F4)` 등) 로딩 스켈레톤을 `ShimmerBox` 로 교체.

**대상 화면 (skeleton 사용 위치):**
1. MatchingScreen.kt — `SkeletonCards` Composable (line ~311)
2. HomeScreen.kt — 매장 리스트·뉴스 카드 로딩 시
3. MapScreen.kt — bottomSheet 매장 리스트 로딩
4. NewsScreen.kt — 뉴스 카드 로딩
5. FavoriteStoresScreen.kt — 즐겨찾기 리스트 로딩
6. MyScreen.kt — 통계·뱃지 로딩 시 (있다면)

각 화면에서 다음 패턴 찾기:
```kotlin
Box(Modifier.size(...).clip(...).background(Color(0xFFF4F4F4)))
```
→ 교체:
```kotlin
import com.example.siheunggagae.ui.component.ShimmerBox
ShimmerBox(modifier = Modifier.size(...).clip(...))  // 또는 ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(20.dp))
```

**전략:**
- grep `Color(0xFFF4F4F4)\|Color(0xFFE8E8E8)\|skeleton\|Skeleton` 으로 위치 식별
- 단순 색 배경 + clip 패턴은 ShimmerBox 로 교체 가능
- 콘텐츠 일부 (예: 회색 구분선) 는 ShimmerBox 가 부적절 — 로딩이 아니라면 그대로 두기

**우선순위:**
1. MatchingScreen.SkeletonCards — 가장 명확한 skeleton 위치
2. HomeScreen — 매장 리스트 로딩 시 skeleton
3. NewsScreen — 카드 skeleton
4. 나머지는 stretch

만약 일부 화면에 skeleton 자체가 없다면 (CircularProgressIndicator 만 있는 경우) 그 화면은 skip 하고 commit 메시지에 노트.

### 컴파일 + 커밋

```bash
./gradlew :app:compileDebugKotlin
git add -A
git commit -m "feat(ui): Phase 5 bundle C — ShimmerBox 적용 (#7 확장)

기존 정적 회색 박스 skeleton 을 ShimmerBox 로 교체.
적용 화면: MatchingScreen.SkeletonCards [+ 발견된 다른 화면]
정적 그레이 박스가 실제 skeleton 이 아닌 placeholder 인 경우 보존."
```

---

## Verification

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```
