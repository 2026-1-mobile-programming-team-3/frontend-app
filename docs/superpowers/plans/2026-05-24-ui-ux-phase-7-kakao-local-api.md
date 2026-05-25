# Phase 7 — Kakao Local API Integration Implementation Plan

> Use superpowers:subagent-driven-development.

**Goal:** RequestFlow 의 장소 검색 (#34) 을 5개 하드코딩 → Kakao Local API (`/v2/local/search/keyword.json`) 실제 검색으로 교체.

**Architecture:** 별도 Retrofit 서비스 (`kapi.kakao.com` base), 인증 헤더 `Authorization: KakaoAK <REST_KEY>`. 단일 파일 viewmodel 통합. debounce 300ms.

**Roadmap:** §Phase 7

---

## Files

| 파일 | 변경 |
|---|---|
| `local.properties` | `kakao.rest.key=...` 추가 (선택, native key 로 fallback) |
| `app/build.gradle.kts` | `KAKAO_REST_KEY` buildConfigField 추가 |
| `data/network/api/KakaoLocalApi.kt` | 신규 Retrofit 인터페이스 |
| `data/network/KakaoLocalClient.kt` | 신규 Retrofit + OkHttp builder |
| `data/model/KakaoLocalModels.kt` | 신규 검색 응답 dto |
| `ui/screen/RequestFlowScreen.kt` | siheungPlaces 하드코딩 → Kakao API 호출 |

---

## Task 1: build config + key placeholder

- [ ] **Step 1.1: `local.properties` 에 REST key 항목 추가 (없으면)**

Pre-check:
```bash
grep "kakao.rest.key" local.properties
```

없으면 placeholder 추가 (실 키는 사용자가 채워야 함):
```
kakao.rest.key=
```

또는 native key 를 폴백으로 쓰려면 비워두고 코드에서 fallback 처리.

- [ ] **Step 1.2: `app/build.gradle.kts` buildConfigField 추가**

Find:
```kotlin
buildConfigField("String", "KAKAO_APP_KEY", "\"${localProps["kakao.app.key"]}\"")
```

Add below:
```kotlin
// REST API 키 — 비어 있으면 native key 로 fallback
val kakaoRestKey = localProps["kakao.rest.key"]?.toString().orEmpty().ifBlank {
    localProps["kakao.app.key"]?.toString().orEmpty()
}
buildConfigField("String", "KAKAO_REST_KEY", "\"$kakaoRestKey\"")
```

```bash
./gradlew :app:assembleDebug --quiet 2>&1 | tail -3
# Expected: BUILD SUCCESSFUL
git add local.properties app/build.gradle.kts
git commit -m "build: KAKAO_REST_KEY buildConfigField (native key fallback)"
```

---

## Task 2: Retrofit 서비스 + dto

- [ ] **Step 2.1: dto 생성 (`KakaoLocalModels.kt`)**

```kotlin
package com.example.siheunggagae.data.model

import com.google.gson.annotations.SerializedName

data class KakaoLocalSearchResponse(
    val documents: List<KakaoLocalDocument> = emptyList(),
    val meta: KakaoLocalMeta? = null,
)

data class KakaoLocalDocument(
    @SerializedName("place_name")  val placeName: String = "",
    @SerializedName("address_name") val addressName: String? = null,
    @SerializedName("road_address_name") val roadAddressName: String? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("x") val longitude: String = "",   // 경도 (문자열)
    @SerializedName("y") val latitude: String = "",    // 위도 (문자열)
    @SerializedName("place_url") val placeUrl: String? = null,
)

data class KakaoLocalMeta(
    @SerializedName("total_count") val totalCount: Int = 0,
    @SerializedName("pageable_count") val pageableCount: Int = 0,
    @SerializedName("is_end") val isEnd: Boolean = true,
)
```

- [ ] **Step 2.2: API 인터페이스 (`KakaoLocalApi.kt`)**

```kotlin
package com.example.siheunggagae.data.network.api

import com.example.siheunggagae.data.model.KakaoLocalSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface KakaoLocalApi {
    /**
     * Kakao Local 키워드 검색.
     * @param query 검색어
     * @param x 중심 경도 (선택)
     * @param y 중심 위도 (선택)
     * @param radius 검색 반경 m (선택, 0~20000)
     * @param size 한 페이지 결과 수 (기본 15, 최대 15)
     * @param page 페이지 (기본 1)
     */
    @GET("v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Query("query") query: String,
        @Query("x") x: Double? = null,
        @Query("y") y: Double? = null,
        @Query("radius") radius: Int? = null,
        @Query("size") size: Int = 15,
        @Query("page") page: Int = 1,
    ): Response<KakaoLocalSearchResponse>
}
```

- [ ] **Step 2.3: 클라이언트 (`KakaoLocalClient.kt`)**

```kotlin
package com.example.siheunggagae.data.network

import com.example.siheunggagae.BuildConfig
import com.example.siheunggagae.data.network.api.KakaoLocalApi
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object KakaoLocalClient {
    private const val BASE_URL = "https://dapi.kakao.com/"

    private val authInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("Authorization", "KakaoAK ${BuildConfig.KAKAO_REST_KEY}")
            .build()
        chain.proceed(req)
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val api: KakaoLocalApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(KakaoLocalApi::class.java)
}
```

`BuildConfig.KAKAO_REST_KEY` 가 빌드되어야 함 — Task 1 완료가 선행.

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/data/model/KakaoLocalModels.kt \
        app/src/main/java/com/example/siheunggagae/data/network/api/KakaoLocalApi.kt \
        app/src/main/java/com/example/siheunggagae/data/network/KakaoLocalClient.kt
git commit -m "feat(api): Kakao Local keyword 검색 Retrofit 서비스 + dto"
```

---

## Task 3: RequestFlowScreen 검색 통합

- [ ] **Step 3.1: 기존 siheungPlaces 하드코딩 제거 + API 호출 추가**

File: `app/src/main/java/com/example/siheunggagae/ui/screen/RequestFlowScreen.kt`

Find `siheungPlaces` 와 검색 UI (line ~1065).

기존:
```kotlin
private val siheungPlaces = listOf(
    "시흥시청", "갯골생태공원", "오이도",
    "정왕동 행복마트", "월곶 동물병원",
)
// ...
val filtered = remember(searchQuery) {
    if (searchQuery.isBlank()) siheungPlaces
    else siheungPlaces.filter { it.contains(searchQuery, ignoreCase = true) }
}
```

새로:
```kotlin
import com.example.siheunggagae.data.network.KakaoLocalClient
import com.example.siheunggagae.data.model.KakaoLocalDocument
import kotlinx.coroutines.delay

// 검색 결과 state
var searchResults by remember { mutableStateOf<List<KakaoLocalDocument>>(emptyList()) }
var isSearching by remember { mutableStateOf(false) }
var searchError by remember { mutableStateOf<String?>(null) }

// debounce 300ms + API 호출
LaunchedEffect(searchQuery) {
    if (searchQuery.isBlank()) {
        searchResults = emptyList()
        isSearching = false
        searchError = null
        return@LaunchedEffect
    }
    delay(300L)
    isSearching = true
    searchError = null
    val response = runCatching {
        KakaoLocalClient.api.searchKeyword(
            query = "시흥 $searchQuery",   // 시흥 prefix 로 지역 제한
            // 중심 좌표 — 시흥시청
            x = 126.8030,
            y = 37.3799,
            radius = 20000,   // 시흥 시 반경
            size = 15,
        )
    }
    isSearching = false
    val body = response.getOrNull()?.body()
    if (body != null) {
        searchResults = body.documents
    } else {
        searchError = "검색 중 오류가 발생했어요"
        searchResults = emptyList()
    }
}
```

- [ ] **Step 3.2: 검색 결과 렌더링**

기존 `filtered.forEach { Text(it, ...) }` 또는 LazyColumn 패턴을 새 `searchResults` 로 교체:

```kotlin
when {
    searchQuery.isBlank() -> {
        // 기본 추천 (선택적 — 빈 상태 또는 단순 안내)
        Text(
            text = "장소나 매장명을 입력해 주세요",
            fontFamily = PretendardFamily,
            fontSize = 13.sp,
            color = Brown700R,  // 토큰명 확인
            modifier = Modifier.padding(16.dp),
        )
    }
    isSearching -> {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = Orange500R,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp),
            )
        }
    }
    searchError != null -> {
        Text(
            text = searchError!!,
            fontFamily = PretendardFamily,
            fontSize = 13.sp,
            color = Color(0xFFE84B6A),
            modifier = Modifier.padding(16.dp),
        )
    }
    searchResults.isEmpty() -> {
        Text(
            text = "검색 결과가 없어요",
            fontFamily = PretendardFamily,
            fontSize = 13.sp,
            color = Brown700R,
            modifier = Modifier.padding(16.dp),
        )
    }
    else -> {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(searchResults) { doc ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // 선택 시 doc.placeName 을 destination 으로 세팅
                            selectedDestination = doc.placeName  // 실제 state 변수명
                            // 좌표도 필요하면: doc.latitude.toDouble(), doc.longitude.toDouble()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_location_on),
                        contentDescription = null,
                        tint = Orange500R,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = doc.placeName,
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextBlackR,
                        )
                        val addr = doc.roadAddressName ?: doc.addressName
                        if (!addr.isNullOrBlank()) {
                            Text(
                                text = addr,
                                fontFamily = PretendardFamily,
                                fontSize = 11.sp,
                                color = Brown700R,
                            )
                        }
                    }
                }
            }
        }
    }
}
```

기존 `selectedDestination` (또는 다른 state 변수) 의 이름 확인. 좌표가 필요한 다음 단계가 있다면 `doc.latitude.toDouble()`, `doc.longitude.toDouble()` 도 같이 저장.

- [ ] **Step 3.3: 컴파일 + 커밋**

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/screen/RequestFlowScreen.kt
git commit -m "feat(request-flow): #34 Kakao Local 키워드 검색 통합 — 5개 하드코딩 제거

300ms debounce + 시흥 prefix + radius 20km. 검색 중·에러·없음 상태 분기 처리."
```

---

## Verification

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

**실제 동작 검증은 디바이스 필요:**
- KAKAO_REST_KEY 가 비어 있으면 401 → "검색 중 오류" 표시
- 정상 키로 검색 시 결과 표시

If `BuildConfig.KAKAO_REST_KEY` is empty at runtime, the API call returns 401. The implementation should still compile and degrade gracefully (show error). Note in commit message.
