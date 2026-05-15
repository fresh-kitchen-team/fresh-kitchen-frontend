# Scan API 연동 요약 (다른 채팅/담당자 인수용)

이 문서는 **식재료/영수증 이미지 스캔**을 실서버 `api.app-fresh.com`에 붙인 내용을 한 번에 넘기기 위한 요약입니다. 새 Cursor 채팅에서는 이 파일을 `@docs/SCAN_API_HANDOFF.md` 로 첨부하면 됩니다.

---

## 1. 엔드포인트 (Swagger와 동일)

| 구분 | Method | Path (Retrofit 상대경로) |
|------|--------|---------------------------|
| 식재료 이미지 | `POST` | `api/v1/scan/ingredient-image` |
| 영수증 이미지 | `POST` | `api/v1/scan/receipt-image` |

**전체 URL 예:** `{SCAN_API_BASE_URL}` + 위 경로  
예: `http://api.app-fresh.com/` + `api/v1/scan/ingredient-image`

---

## 2. Base URL 설정

- **BuildConfig:** `SCAN_API_BASE_URL` (`app/build.gradle.kts`에서 `local.properties`의 `SCAN_API_BASE_URL` 읽음, 없으면 기본 `http://api.app-fresh.com/`).
- **로컬 오버라이드:** 프로젝트 루트 `local.properties`  
  `SCAN_API_BASE_URL=http://api.app-fresh.com/`  
  (에뮬레이터 → PC 로컬 등은 `http://10.0.2.2:8080/` 등으로 변경)
- **미연동 판별:** URL에 `placeholder.invalid` 이 포함되면 시뮬만 동작 (`ScanRepository.isApiConfigured()`).

---

## 3. 요청 형식

- **multipart/form-data**
- **파트 이름:** `file` (`MultipartBody.Part.createFormData("file", fileName, requestBody)`)
- **Content-Type:** 이미지는 `ContentResolver.getType` 기준, 없으면 `image/jpeg`
- **카메라/갤러리 구분:** 서버로 보내지 않음 (탭으로만 API 분기).

---

## 4. 인증

- **`Authorization: Bearer <JWT>`**  
- `MainActivity` → `AuthTokenStore.setAccessToken(...)`  
- **`ScanRepository`** 의 OkHttp에 **`scanAuthInterceptor()`** 가 매 요청마다 `AuthTokenStore.getAccessToken()`을 읽어 헤더 추가.  
- 토큰이 이미 `Bearer `로 시작하면 중복하지 않음.

---

## 5. 응답 봉투(Envelope) 성공 조건

백엔드가 **둘 중 한 형태**로 성공을 줄 수 있게 처리함 (`ScanRepository.isScanEnvelopeSuccess`).

- `status == 0` (Swagger 예시)
- **또는** `status` 가 `200`~`299`
- **또는** `code` 가 `COMMON-200` (대소문자 무시)

실패 시: `message` / `code` / 기본 문구로 `ScanApiException`.

---

## 6. 주요 코드 위치

| 역할 | 파일 |
|------|------|
| Retrofit 인터페이스 | `app/.../data/scan/ScanApiService.kt` |
| 업로드·파싱·인증 OkHttp | `app/.../data/scan/ScanRepository.kt` |
| DTO / Api 봉투 | `app/.../data/scan/ScanDtos.kt` |
| UI 모델·JSON 직렬화 | `app/.../data/scan/ScanResultUiModel.kt` |
| DTO → 화면 모델 | `app/.../data/scan/ScanResultMappers.kt` |
| 스캔 호출·상태 | `app/.../viewmodel/ScanViewModel.kt` |
| 카메라/탭/네비 | `app/.../ui/screens/ScanScreen.kt` |
| 결과 화면 | `app/.../ui/screens/ScanResultScreen.kt` |
| `SavedStateHandle` 키 | `app/.../navigation/ScanNav.kt` (`keyScanResultJson` 등) |
| 토큰 메모리 저장 | `app/.../AuthTokenStore.kt` |
| Logcat 태그 | `app/.../logging/ApiLog.kt` — 태그 **`MyFridgeApi`** |

---

## 7. UI 플로우 (간단)

1. 스캔 탭에서 촬영/갤러리 → `ScanViewModel.requestIngredientScan` / `requestReceiptScan`
2. `ScanRepository.isApiConfigured()` 가 true면 실제 POST, false면 로컬 시뮬
3. 성공 시 `ScanResultUiModel` JSON을 `ScanNav.keyScanResultJson` 등으로 넘기고 결과 화면으로 이동
4. **최종 식재료 저장(POST items 등)** 은 별도 TODO — 결과 화면 저장 버튼은 아직 공통 등록 API 미연동일 수 있음

---

## 8. HTTP(cleartext)

- `http://api.app-fresh.com` 사용을 위해 `app/src/main/res/xml/network_security_config.xml` 에 도메인 허용 + `AndroidManifest.xml` 의 `application` 에 `networkSecurityConfig` 연결.

---

## 9. Logcat으로 확인할 때

- 필터: `tag:MyFridgeApi` 또는 `MyFridgeApi`
- 성공 시 예: `ingredient-image OK items=...`, `ViewModel Success`
- OkHttp: `okhttp.OkHttpClient` — **Bearer는 로그에 노출됨**; 배포 전 로그 레벨/마스킹 권장.

---

## 10. 아직 다른 채팅에 안 넘겨도 되는 것

- AI 인앱 채팅(제품 기능)과 **Scan REST 연동은 별개** — 같은 레포 안에서만 공유되는 건 `AuthTokenStore` 정도.

---

*생성 목적: 다른 Cursor 대화에서 Scan 작업을 이어갈 때 맥락 공유용.*
