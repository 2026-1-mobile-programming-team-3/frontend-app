# 매칭 화면 개선 — 백엔드 변경 요청

작성일: 2026-05-24
요청자: 클라이언트(시흥가개 안드로이드)
관련 클라이언트 작업: `feature/matching-revamp` 브랜치

매칭 리스트 화면 UI/UX 개선 작업(P0+P1)에 필요한 백엔드 변경. 클라이언트만으로 구현 불가능한 항목만 모음.

---

## 1. matches 테이블 — 신규 컬럼

### 1.1 `category` 컬럼 추가 (필수)

| 항목 | 값 |
|---|---|
| 타입 | enum (NOT NULL) |
| enum 값 | `WALK` · `VET` · `SHOPPING` · `MOVE` · `VOLUNTEER` (협의 후 추가 가능) |
| 디폴트 | 없음 — 모든 신규 요청 작성 시 필수 입력 |
| 기존 데이터 마이그레이션 | 모두 `MOVE` 또는 `OTHER` (별도 enum 값 신설)로 임시 설정. 또는 백필 정책 협의 |

### 1.2 봉사자 전용 카테고리 작성 권한 검증 (필수)

- `POST /api/v1/matches` 호출 시 `category` 가 봉사자 전용(`VOLUNTEER`, `VET` 등 — 협의)이면 작성자가 `users.role = VOLUNTEER` 또는 봉사자 자격 보유자인지 검증.
- 권한 없으면 `403 Forbidden` + `{"error_code": "VOLUNTEER_ROLE_REQUIRED", "message": "..."}`.
- 일반 카테고리(`WALK`, `SHOPPING`, `MOVE`)는 모든 사용자 작성 가능.

> **봉사자 전용 카테고리 정의는 별도 협의**. 클라이언트 측 잠정 가정: `VOLUNTEER`, `VET`.

### 1.3 (선택) `author_user_id` 응답 노출

현재 `MatchListItem`/`MatchDetailResponse`는 `authorNickname` 만 제공. 클라이언트가 본인 글 식별 시 닉네임 문자열 비교를 쓰고 있어 닉네임 변경 시 깨짐.

응답에 `authorUserId: Int` 추가 시 클라이언트가 `Me.id == authorUserId` 비교로 안전하게 식별. 닉네임 노출은 그대로 유지.

---

## 2. `GET /api/v1/matches` 쿼리 파라미터 추가

기존 `status` / `region` / `fromDate` / `toDate` / `page` / `size` 외에:

| 파라미터 | 타입 | 디폴트 | 설명 |
|---|---|---|---|
| `sort` | string | `imminent` | `imminent`(출발시각 가까운 순) / `recent`(생성시각 내림차순) / `nearest`(거리 오름차순, `lat`/`lng` 필수) |
| `category` | string | (전체) | enum 값 단일. 미지정이면 모든 카테고리. multi-select 는 향후. |
| `max_distance` | int | (없음) | 미터. `lat`/`lng` 필수. 지정 시 그 반경 내 매칭만 |
| `lat` | float | (없음) | 사용자 위치 위도. `sort=nearest` 또는 `max_distance` 사용 시 필수 |
| `lng` | float | (없음) | 사용자 위치 경도. 위와 동일 |

### `sort=imminent` 정의
- 정렬 키: `desired_date + desired_time` 의 절대시각이 현재시각에서 가까운 순(미래만, 과거 매칭은 후순위로 보내거나 제외)
- 동일 시각일 때 tiebreaker: `created_at` 내림차순

### `max_distance` 정의
- Haversine 거리 계산. `lat`/`lng` 없으면 422 또는 무시(서버 정책 협의)

---

## 3. `MatchListItem` 응답에 신규 필드

기존 응답에 다음 필드 추가:

```json
{
  "match_id": 7,
  "title": "...",
  "address": "...",
  "desired_date": "2026-05-25",
  "desired_time": "14:00",
  "status": "RECRUITING",
  "applications_count": 2,
  "author_nickname": "...",
  "created_at": "2026-05-24T09:00:00Z",

  // 추가 요청
  "category": "WALK",              // enum
  "author_user_id": 14,            // 본인 식별용 (선택, §1.3)
  "latitude": 37.3752,             // 거리 계산용 (현재 매칭 위치)
  "longitude": 126.7281,
  "distance_m": 320.5              // sort=nearest 또는 lat/lng 제공 시. 그 외 null
}
```

`latitude`/`longitude` 가 매칭 작성 시 입력 안 됐다면 동(`region_dong`) 중심 좌표로 폴백하거나 null. 별도 협의.

---

## 4. 요청 작성 폼에 카테고리 입력

### 4.1 `MatchCreateRequest` 본문에 `category` 필수 필드 추가

```json
POST /api/v1/matches
{
  "title": "...",
  "category": "WALK",       // 신규 필수
  "desired_date": "...",
  "desired_time": "...",
  "address": "...",
  ...
}
```

### 4.2 카테고리별 권한 검증 (§1.2 참조)
`VOLUNTEER`/`VET` 같은 봉사자 전용 카테고리는 사용자 role 검사.

---

## 5. (선택) 신청 권한 검증

봉사자 전용 카테고리 매칭에 `POST /api/v1/matches/{matchId}/applications` 신청 시 신청자도 봉사자 자격 보유 여부 검증할지 협의 필요. 클라이언트는 일단 가능하면 모두 신청 허용으로 가정.

---

## 6. 우선순위 / 일정

| # | 항목 | 우선순위 | 클라이언트 차단? |
|---|---|---|---|
| §1.1 | `category` 컬럼 + enum | P0 | yes — 이 없으면 카테고리 필터·칩 전부 작동 안 함 |
| §1.2 | 카테고리별 작성 권한 | P0 | 봉사자 전용 카테고리 도입에 필수 |
| §1.3 | `author_user_id` 응답 | P1 | 닉네임 비교 폴백 가능 |
| §2 sort=imminent | 정렬 | P0 | 클라이언트 정렬도 가능하지만 페이지네이션과 충돌 |
| §2 max_distance / nearest | 거리 필터·정렬 | P0 | 클라이언트 정렬 시 한 페이지 안에서만 정확. 백엔드 처리 권장 |
| §2 category 필터 | 필터 | P0 | yes — §1.1과 한 묶음 |
| §3 latitude/longitude | 응답 | P0 | 거리 표시·정렬에 필수 |
| §3 distance_m | 응답 | P1 | 클라이언트도 Haversine 계산 가능. 백엔드 계산이 일관 |

---

## 7. 클라이언트 동작 (참고)

백엔드 변경이 일부만 반영돼도 클라이언트는 점진 적용:
- `category` 응답 없으면 카테고리 칩은 "전체" 만 노출 + 카드 카테고리 아이콘 숨김
- `latitude`/`longitude` 없으면 거리 표시 "동 명만" 폴백
- `sort=imminent` 미지원 시 클라이언트가 현재 페이지 안에서 imminent 재정렬
- `author_user_id` 없으면 현재 nickname 비교 fallback

질문·협의 사항 있으면 클라이언트 측 (이 문서 작성자) 에 알려주세요.
