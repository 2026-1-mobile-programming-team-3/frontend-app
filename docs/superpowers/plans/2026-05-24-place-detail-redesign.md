# PlaceDetail UI 개선 + 펫호텔 가격표 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** PlaceDetailScreen을 섹션 카드 스택 레이아웃으로 재편하고, `StoreDetailResponse`에 `category`·`plans` 필드를 추가하여 PET_HOTEL 카테고리에서 요금 플랜 카드를 표시한다.

**Architecture:** (1) 모델 수정으로 백엔드가 이미 내려주는 `plans` 필드를 매핑한다. (2) `PlaceDetailScreen.kt` 내부에 `SectionCardPL`, `PlanCardPL`, `LocationCardPL` private 컴포저블을 추가한다. (3) 기존 LazyColumn 아이템을 카드 구조로 재조정하고 히어로 헤더를 오버레이 방식으로 전환한다.

**Tech Stack:** Kotlin, Jetpack Compose, Gson (`@SerializedName`), JUnit 4 (모델 단위 테스트)

---

## File Map

| 역할 | 파일 |
|---|---|
| 수정 | `app/src/main/java/com/example/siheunggagae/data/model/MapModels.kt` |
| 수정 | `app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt` |
| 신규 | `app/src/test/java/com/example/siheunggagae/StoreDetailModelsTest.kt` |

---

### Task 1: StoreDetailResponse 모델 수정 + 테스트

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/data/model/MapModels.kt`
- Create: `app/src/test/java/com/example/siheunggagae/StoreDetailModelsTest.kt`

- [ ] **Step 1: 실패 테스트 작성**

`app/src/test/java/com/example/siheunggagae/StoreDetailModelsTest.kt` 새 파일:

```kotlin
package com.example.siheunggagae

import com.example.siheunggagae.data.model.StoreDetailResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StoreDetailModelsTest {
    private val gson = Gson()

    @Test
    fun deserializes_category_and_plans() {
        val json = """
            {
              "store_id": 42,
              "name": "해피포우 펫호텔",
              "category": "PET_HOTEL",
              "plans": [
                {"plan_name": "소형견 1박", "price_krw": 30000, "display_order": 0},
                {"plan_name": "중형견 1박", "price_krw": 40000, "display_order": 1}
              ]
            }
        """.trimIndent()

        val parsed = gson.fromJson(json, StoreDetailResponse::class.java)
        assertEquals("PET_HOTEL", parsed.category)
        assertEquals(2, parsed.plans.size)
        assertEquals("소형견 1박", parsed.plans[0].planName)
        assertEquals(30000, parsed.plans[0].priceKrw)
        assertEquals(0, parsed.plans[0].displayOrder)
    }

    @Test
    fun plans_defaults_to_empty_list_when_absent() {
        val json = """{"store_id": 1, "name": "카페"}"""
        val parsed = gson.fromJson(json, StoreDetailResponse::class.java)
        assertNull(parsed.category)
        assertEquals(emptyList<Any>(), parsed.plans)
    }

    @Test
    fun plans_defaults_to_empty_list_when_null() {
        val json = """{"store_id": 1, "name": "카페", "plans": null}"""
        val parsed = gson.fromJson(json, StoreDetailResponse::class.java)
        assertEquals(emptyList<Any>(), parsed.plans)
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

```bash
./gradlew test --tests "com.example.siheunggagae.StoreDetailModelsTest" 2>&1 | tail -20
```
Expected: `StoreDetailResponse`에 `category`·`plans` 없으므로 컴파일 에러 또는 assertion 실패.

- [ ] **Step 3: MapModels.kt 수정**

`StoreDetailResponse` data class에 두 필드 추가. 기존 `ownerUserId` 필드 바로 아래에 삽입:

```kotlin
// 기존 마지막 두 필드 (변경 없음)
@SerializedName(value = "is_owner", alternate = ["isOwner"])
val isOwner: Boolean = false,
@SerializedName(value = "owner_user_id", alternate = ["ownerUserId"])
val ownerUserId: Int? = null,

// 추가
@SerializedName("category")
val category: String? = null,

@SerializedName("plans")
private val _plans: List<com.example.siheunggagae.data.model.PetHotelPlan>? = null,
) {
    val plans: List<com.example.siheunggagae.data.model.PetHotelPlan> get() = _plans ?: emptyList()
}
```

> **주의:** 기존 `StoreDetailResponse`는 `data class`의 닫는 `)` 로 끝나고 별도 body가 없다. `plans` getter를 추가하려면 `)` 를 `) {` 로 바꾸고 `}` 로 닫아야 한다.
>
> 최종 형태:
> ```kotlin
> data class StoreDetailResponse(
>     @SerializedName(value = "store_id", alternate = ["storeId", "id"])
>     val storeId: Int? = null,
>     val name: String? = null,
>     val address: String? = null,
>     val phone: String? = null,
>     @SerializedName(value = "operating_hours", alternate = ["operatingHours"])
>     val operatingHours: String? = null,
>     @SerializedName(value = "photo_urls", alternate = ["photoUrls"])
>     val photoUrls: List<String>? = null,
>     @SerializedName(value = "is_pet_allowed", alternate = ["isPetAllowed"])
>     val isPetAllowed: Boolean? = null,
>     @SerializedName(value = "rating_avg", alternate = ["ratingAvg"])
>     val ratingAvg: Double? = null,
>     @SerializedName(value = "review_pet_allowed_rate", alternate = ["reviewPetAllowedRate"])
>     val reviewPetAllowedRate: Double? = null,
>     @SerializedName(value = "is_favorited", alternate = ["isFavorited"])
>     val isFavorited: Boolean? = null,
>     val latitude: Double? = null,
>     val longitude: Double? = null,
>     @SerializedName(value = "is_owner", alternate = ["isOwner"])
>     val isOwner: Boolean = false,
>     @SerializedName(value = "owner_user_id", alternate = ["ownerUserId"])
>     val ownerUserId: Int? = null,
>     @SerializedName("category")
>     val category: String? = null,
>     @SerializedName("plans")
>     private val _plans: List<PetHotelPlan>? = null,
> ) {
>     val plans: List<PetHotelPlan> get() = _plans ?: emptyList()
> }
> ```

- [ ] **Step 4: 테스트 통과 확인**

```bash
./gradlew test --tests "com.example.siheunggagae.StoreDetailModelsTest" 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`, 3개 테스트 PASS.

- [ ] **Step 5: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/data/model/MapModels.kt \
        app/src/test/java/com/example/siheunggagae/StoreDetailModelsTest.kt
git commit -m "feat(model): StoreDetailResponse에 category·plans 필드 추가"
```

---

### Task 2: 히어로 헤더 리팩터

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt`

히어로 배너 `item { }` 블록(현재 라인 257–276)과 "장소 기본 정보" `item { }` 블록(279–321)을 하나의 새 히어로 item으로 교체한다. 플로팅 버튼(뒤로가기·즐겨찾기·공유)은 유지.

- [ ] **Step 1: 기존 배너 + 기본 정보 블록 제거 후 히어로 item 삽입**

LazyColumn 내부의 첫 두 `item { }` (배너, 기본정보)와 그 사이 `Spacer` 를 아래 코드로 통째 교체:

```kotlin
// ── 히어로 헤더 ────────────────────────────────────────────────────
item {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(MapMintPL, Color(0xFFB2DFBF), Color(0xFFD8F2DC)))
            ),
    ) {
        // 상태바 높이 + 콘텐츠 높이
        Column {
            Spacer(Modifier.statusBarsPadding())
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                // 다크 오버레이 (하단 → 상단)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f to Color(0x80000000),
                                startY = 0f,
                            )
                        ),
                )
                // 플로팅 버튼 (상단)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 뒤로가기
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "뒤로",
                            tint = TextBlackPL,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    // 즐겨찾기 + 공유
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .clickable { toggleFavorite() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (isFavorited) R.drawable.ic_favorite
                                    else R.drawable.ic_favorite_border
                                ),
                                contentDescription = "즐겨찾기",
                                tint = if (isFavorited) Pink500PL else Brown700PL,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .clickable { shareStore() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_share),
                                contentDescription = "공유",
                                tint = Brown700PL,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
                // 매장 정보 (하단 좌측)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // 카테고리 배지
                    val catLabel = when (s.category) {
                        "PET_HOTEL"  -> "🏨 반려동물 호텔"
                        "CAFE"       -> "☕ 카페"
                        "RESTAURANT" -> "🍽 식당"
                        "PARK"       -> "🌳 공원"
                        else         -> null
                    }
                    if (catLabel != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color(0x40FFFFFF))
                                .border(1.dp, Color(0x80FFFFFF), RoundedCornerShape(50.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = catLabel,
                                fontFamily = PretendardFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                        }
                    }
                    // 매장명
                    Text(
                        text = s.name ?: "",
                        fontFamily = PretendardFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 28.sp,
                        color = Color.White,
                    )
                    // 별점 + 영업상태
                    if (s.ratingAvg != null || s.operatingHours != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (s.ratingAvg != null) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = StarYellowPL,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = "%.1f".format(s.ratingAvg),
                                    fontFamily = PretendardFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Text(
                                    text = "(후기 ${reviews.size})",
                                    fontFamily = PretendardFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xCCFFFFFF),
                                )
                            }
                            val isOpen = s.operatingHours?.let { isOpenNow(it) }
                            if (isOpen != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(if (isOpen) GreenBgPL else Color(0xFFFFE4E6))
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = if (isOpen) "영업 중" else "영업 마감",
                                        fontFamily = PretendardFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isOpen) Green500PL else Pink500PL,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: 빌드 확인**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt
git commit -m "style(place-detail): 히어로 헤더 오버레이 + 매장명·별점 인라인 배치"
```

---

### Task 3: SectionCardPL + PlanCardPL 컴포저블 추가

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt`

파일 하단(기존 helper composable들 직전)에 private 컴포저블 두 개를 추가한다.

- [ ] **Step 1: SectionCardPL 추가**

파일 하단 `// ─── 헬퍼 컴포저블` 영역 위에 삽입:

```kotlin
@Composable
private fun SectionCardPL(
    title: String,
    modifier: Modifier = Modifier,
    actionContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlackPL,
            )
            actionContent?.invoke()
        }
        HorizontalDivider(color = DividerPL, thickness = 1.dp)
        content()
    }
}
```

- [ ] **Step 2: PlanCardPL 추가**

`SectionCardPL` 바로 아래에 삽입:

```kotlin
@Composable
private fun PlanCardPL(plans: List<com.example.siheunggagae.data.model.PetHotelPlan>) {
    var expanded by remember { mutableStateOf(false) }
    val sorted = remember(plans) { plans.sortedBy { it.displayOrder ?: 0 } }
    val visible = if (expanded) sorted else sorted.take(3)
    val remaining = sorted.size - 3

    SectionCardPL(
        title = "💰 요금 플랜",
        actionContent = {
            Text(
                text = "${plans.size}개",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Brown700PL,
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            visible.forEach { plan ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Gray100PL)
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = plan.planName,
                        fontFamily = PretendardFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextBlackPL,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "%,d원".format(plan.priceKrw),
                        fontFamily = PretendardFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brown900PL,
                    )
                }
            }
            if (!expanded && remaining > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+ ${remaining}개 더 보기",
                        fontFamily = PretendardFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Brown900PL,
                    )
                }
            }
            Text(
                text = "* 가격은 업체 등록 기준이며 실제와 다를 수 있습니다.",
                fontFamily = PretendardFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Brown400PL,
                lineHeight = 16.sp,
            )
        }
    }
}
```

> `Brown900PL`은 파일 상단 컬러 섹션에 이미 `private val Brown900PL = Color(0xFF614B3A)` 로 정의돼 있다.
> `Gray100PL`은 `private val Gray100PL = Color(0xFFF3F3F3)` — 기존 정의 확인.
> `Brown400PL`은 `private val Brown400PL = Color(0xFFC4A882)` — 기존 정의 확인.

- [ ] **Step 3: 빌드 확인**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt
git commit -m "feat(place-detail): SectionCardPL·PlanCardPL 컴포저블 추가"
```

---

### Task 4: LocationCardPL 컴포저블 + LazyColumn 재조정

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt`

기존 "상세 정보" + "위치 카드" + "수정 요청" 블록(3개 item)을 `LocationCardPL` 하나로 합친다.

- [ ] **Step 1: LocationCardPL 컴포저블 추가**

`PlanCardPL` 바로 아래에 삽입:

```kotlin
@Composable
private fun LocationCardPL(
    store: com.example.siheunggagae.data.model.StoreDetailResponse,
    initialLat: Double,
    initialLng: Double,
    mapView: MapView,
    onNavigateToMap: (Double, Double, Int) -> Unit,
    onEditRequestClick: (Int) -> Unit,
    onCopyAddress: () -> Unit,
    onCopyPhone: () -> Unit,
    onDialPhone: () -> Unit,
) {
    SectionCardPL(title = "📍 위치 & 연락처") {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 지도
            val mapLat = store.latitude?.takeIf { it != 0.0 } ?: initialLat.takeIf { it != 0.0 }
            val mapLng = store.longitude?.takeIf { it != 0.0 } ?: initialLng.takeIf { it != 0.0 }
            if (mapLat != null && mapLng != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                ) {
                    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onNavigateToMap(mapLat, mapLng, store.storeId ?: 0) },
                    )
                    if (!store.address.isNullOrEmpty()) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_location_on),
                                contentDescription = null,
                                tint = Orange500PL,
                                modifier = Modifier.size(10.dp),
                            )
                            Text(
                                text = store.address.take(12),
                                fontFamily = PretendardFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextBlackPL,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(MapSkyPL, MapMintPL))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("위치 정보 없음", fontFamily = PretendardFamily, fontSize = 13.sp, color = Brown700PL)
                }
            }
            // 정보 행
            if (!store.operatingHours.isNullOrEmpty()) {
                PlaceInfoRowPL(
                    iconRes = R.drawable.ic_schedule,
                    label = "영업 시간",
                    content = store.operatingHours,
                ) {
                    val isOpen = isOpenNow(store.operatingHours)
                    if (isOpen != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isOpen) GreenBgPL else Color(0xFFFFE4E6))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = if (isOpen) "영업 중" else "영업 마감",
                                fontFamily = PretendardFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isOpen) Green500PL else Pink500PL,
                            )
                        }
                    }
                }
            }
            if (!store.address.isNullOrEmpty()) {
                PlaceInfoRowPL(
                    iconRes = R.drawable.ic_location_on,
                    label = "주소",
                    content = store.address,
                ) { CopyButtonPL { onCopyAddress() } }
            }
            if (!store.phone.isNullOrEmpty()) {
                PlaceInfoRowPL(
                    iconRes = R.drawable.ic_call,
                    label = "전화",
                    content = store.phone,
                ) { CopyButtonPL { onCopyPhone() } }
            }
            // 액션 버튼 행
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!store.phone.isNullOrEmpty()) {
                    OutlinedButton(
                        onClick = { onDialPhone() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BrownBorderPL),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_call),
                            contentDescription = null,
                            tint = Brown700PL,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("전화", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700PL)
                    }
                }
                OutlinedButton(
                    onClick = { if (mapLat != null && mapLng != null) onNavigateToMap(mapLat, mapLng, store.storeId ?: 0) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BrownBorderPL),
                ) {
                    Text("🧭 지도에서 보기", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700PL)
                }
                if (!store.address.isNullOrEmpty()) {
                    OutlinedButton(
                        onClick = { onCopyAddress() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BrownBorderPL),
                    ) {
                        Text("📋 복사", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700PL)
                    }
                }
            }
            // 수정 요청 / 클레임 버튼
            val storeId = store.storeId ?: 0
            if (store.isOwner) {
                OutlinedButton(
                    onClick = { onEditRequestClick(storeId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BrownBorderPL),
                ) {
                    Text("내 매장 정보 수정", fontFamily = PretendardFamily, fontSize = 13.sp, color = Brown700PL)
                }
            } else if (store.ownerUserId == null) {
                OutlinedButton(
                    onClick = { onEditRequestClick(storeId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BrownBorderPL),
                ) {
                    Text("이 매장 클레임하기", fontFamily = PretendardFamily, fontSize = 13.sp, color = Brown700PL)
                }
            }
        }
    }
}
```

- [ ] **Step 2: LazyColumn 아이템 재조정**

히어로 헤더 item 이후의 LazyColumn 내용을 아래 순서로 교체:

기존 제거 대상:
- `item { Spacer(Modifier.height(8.dp)) }` (배너 아래 모든 Spacer)
- 기존 "상세 정보" item 블록 (운영시간·주소·전화)
- 기존 "위치 카드" item 블록
- 기존 수정/클레임 버튼 item 블록

교체 후 순서:

```kotlin
// 카드 간격
item { Spacer(Modifier.height(8.dp)) }

// 요금 플랜 카드 (PET_HOTEL만)
if (s.category == "PET_HOTEL" && s.plans.isNotEmpty()) {
    item {
        PlanCardPL(plans = s.plans)
    }
    item { Spacer(Modifier.height(8.dp)) }
}

// 위치 & 연락처 카드
item {
    LocationCardPL(
        store = s,
        initialLat = initialLat,
        initialLng = initialLng,
        mapView = mapView,
        onNavigateToMap = onNavigateToMap,
        onEditRequestClick = onEditRequestClick,
        onCopyAddress = { copyToClipboard(s.address ?: "") },
        onCopyPhone = { copyToClipboard(s.phone ?: "") },
        onDialPhone = { s.phone?.let { dialPhone(it) } },
    )
}
item { Spacer(Modifier.height(8.dp)) }

// 후기 카드 헤더
item {
    SectionCardPL(
        title = "💬 후기",
        actionContent = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Pink500PL)
                    .clickable { showReviewSheet = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "후기 쓰기",
                    fontFamily = PretendardFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        },
    ) {
        if (s.ratingAvg != null) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "%.1f".format(s.ratingAvg),
                    fontFamily = PretendardFamily,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 36.sp,
                    color = TextBlackPL,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val filled = Math.round(s.ratingAvg).toInt()
                    repeat(5) { i ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (i < filled) StarYellowPL else Gray100PL,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Text(
                    text = "총 ${reviews.size}개의 리뷰",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp,
                    color = Brown700PL,
                )
            }
        }
    }
}

// 후기 목록 (기존 로직 유지)
items(reviews.take(displayCount)) { review -> /* 기존 코드 그대로 */ }

// 더 보기 버튼 (기존 코드 그대로)
```

- [ ] **Step 3: 빌드 확인**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt
git commit -m "feat(place-detail): LocationCardPL 추가·LazyColumn 섹션 카드 재조정"
```

---

### Task 5: Preview 업데이트

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt`

파일 하단의 `@Preview` 함수를 찾아 펫호텔 더미 데이터가 포함된 버전으로 교체한다.

- [ ] **Step 1: Preview 함수 교체**

기존 `@Preview` 함수(파일 하단)를 아래로 교체:

```kotlin
@Preview(showBackground = true)
@Composable
private fun PlaceDetailScreenPreview() {
    SiheungGagaeTheme {
        // 펫호텔 더미 데이터
        val dummyStore = com.example.siheunggagae.data.model.StoreDetailResponse(
            storeId = 1,
            name = "해피포우 펫호텔",
            category = "PET_HOTEL",
            address = "경기도 시흥시 정왕동 1234-5",
            phone = "031-123-4567",
            operatingHours = "09:00-20:00",
            ratingAvg = 4.8,
            isOwner = false,
            ownerUserId = null,
        )
        // Preview는 실제 화면 대신 카드 컴포저블만 미리보기
        Column(modifier = Modifier.background(Color(0xFFF7F4F1))) {
            PlanCardPL(
                plans = listOf(
                    com.example.siheunggagae.data.model.PetHotelPlan("소형견 1박 (5kg 이하)", 30000, 0),
                    com.example.siheunggagae.data.model.PetHotelPlan("중형견 1박 (5–15kg)", 40000, 1),
                    com.example.siheunggagae.data.model.PetHotelPlan("대형견 1박 (15kg+)", 55000, 2),
                    com.example.siheunggagae.data.model.PetHotelPlan("소형견 데이케어", 20000, 3),
                    com.example.siheunggagae.data.model.PetHotelPlan("중형견 데이케어", 25000, 4),
                ),
            )
        }
    }
}
```

- [ ] **Step 2: 최종 빌드 + 테스트**

```bash
./gradlew assembleDebug test 2>&1 | grep -E "error:|FAILED|BUILD"
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: 최종 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt
git commit -m "style(place-detail): Preview 펫호텔 더미 데이터로 업데이트"
```

---

## 구현 체크리스트 (스펙 대조)

- [ ] `StoreDetailResponse`에 `category`, `plans` 필드 추가 → Task 1
- [ ] 히어로 헤더 리팩터 (오버레이 + 매장명·별점·배지 이동) → Task 2
- [ ] 요금 플랜 카드 컴포저블 (`PlanCardPL`) → Task 3
- [ ] 위치 & 연락처 카드 컴포저블 (`LocationCardPL`) → Task 4
- [ ] 후기 카드 래퍼 적용 → Task 4
- [ ] `LazyColumn` 아이템 순서 재조정 → Task 4
- [ ] `@Preview` 업데이트 → Task 5
