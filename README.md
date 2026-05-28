# 매칭온 — Android

[Back-Kyung](https://github.com/Mohaemohae-kyung/Back-Kyung) 백엔드(Spring Boot 3.2.3 + Oracle)와 연동하는 매칭온 안드로이드 클라이언트.

사용자 — 고수 매칭, 견적 요청·결제, 마켓 예약 결제, 실시간 채팅, 커뮤니티/고수센터 공지, 관리자 웹뷰까지 단일 APK로 제공.

> 셋업·도구·트러블슈팅 상세는 **[SETUP.md](./SETUP.md)** 참고.

---

## 환경

| 항목 | 버전 |
|---|---|
| Kotlin | 2.0.21 |
| Android Gradle Plugin | 8.13.2 |
| Gradle | 8.14.5 |
| compileSdk / targetSdk | 36 (Android 16) |
| minSdk | 28 (Android 9.0+) |
| UI | Jetpack Compose + Material3 |
| DI | Hilt 2.56.2 |
| 네트워크 | Retrofit 2.11.0 + OkHttp 4.12.0 + Kotlinx Serialization |
| 이미지 | Coil 3 (3.0.4) |
| 실시간 채팅 | krossbow (STOMP over WebSocket) |
| 결제 | TossPayments Android SDK 0.1.22 (jitpack) |
| NDK / CMake | 17 (C++), AGP 기본값 (16 KB 페이지 정렬) |

---

## 주요 기능

| 영역 | 화면 |
|---|---|
| 인증 | 로그인 / 회원가입 / 비밀번호 변경 / 계정 탈퇴 / 자동 로그인 (토큰 갱신) |
| 홈 | 키워드/카테고리/지역 검색바, 추천 고수, 추천 마켓, 챗봇 FAB |
| 고수찾기 | 클라이언트 사이드 필터링(키워드 통합 검색 + 대분류 자식 확장) |
| 고수 상세 | 프로필, 포트폴리오 웹뷰, 견적 요청 진입 |
| 견적 요청 | 카테고리 드롭다운 + 제목/내용/예산/희망일정 입력 |
| 견적 상세 | USER 시점 / EXPERT 시점 분기, 수락/거절/취소 |
| 채팅 | STOMP 실시간 메시지, 결제 요청/완료 시스템 메시지 |
| 결제 | Toss 결제창 (`requestCardPayment`) — 견적 결제 + 마켓 예약 결제 |
| 마켓 | Store / StoreDetail / StoreEditor, 예약 가능 시간 조회 + 예약 결제 |
| 거래내역 | USER 결제 내역, EXPERT 받은 거래 |
| 커뮤니티 | 생활 보드 / 고수센터(공지) 분리, 게시글 작성·상세·댓글 |
| 마이페이지 | 프로필·계정설정·찜한 고수·거래내역·관리자(ADMIN 전용) |
| 챗봇 | 백엔드 LLM 챗봇 연동 (홈 탭에서 진입) |
| 관리자 | role=ADMIN 계정 한정. 마이페이지 → 관리자 페이지(웹뷰) |

---

## 보안

| 항목 | 상태 | 위치 |
|---|---|---|
| 무결성 검증 | release만 실행 (debug는 우회) | `SplashActivity.kt` → `checkAppIntegrity()` |
| 루팅 탐지 (Native 5개) | 활성 (`detectSuBinary`/`detectMagiskFiles`/`detectWritableMount`/`detectSuspiciousRootPaths`/`detectRootShell`) | `cpp/native_security.cpp` + `security/RootDetectionManager.kt` |
| Frida 감지 (초기) | 활성 | `KungAndroidApplication.onCreate` → `SecurityCheckManager.collectInitialFridaCheck()` |
| 서명/Dex 해시 보고 | 활성 | `security/SignatureHashUtil.kt` + `security/DexHashUtil.kt` → `/api/app-integrity/report` |
| ProGuard / R8 | release `isMinifyEnabled=true` + `isShrinkResources=true` | `app/proguard-rules.pro` |
| KeyStore 암호화 | Android KeyStore + AES-GCM | `data/auth/KeystoreCipher.kt` (토큰 보호) |
| Cleartext 트래픽 | base-config `false`, 개발용/관리자 임시 도메인만 허용 | `res/xml/network_security_config.xml` |
| SSL Pinning | **OFF** (운영 핀 추출 후 활성화 예정) | `build.gradle.kts` (`SSL_PINNING_ENABLED=false`) |
| Token Authenticator | Refresh 토큰 자동 재발급 | `data/network/TokenAuthenticator.kt` |

> 관리자 도메인(`http://43.200.59.30/`)은 HTTPS 발급 전 한시 cleartext 허용. 운영 직전 HTTPS 교체 필요.

---

## 패키지 구조

```
app/src/main/java/kyung/kung_android/
├── KungAndroidApplication.kt        # Hilt Application + Frida 초기 감지
├── SplashActivity.kt                 # 토큰 prime + (release만) 무결성 검증
├── MainActivity.kt                   # Compose 진입
├── admin/                            # 관리자 웹뷰 (role=ADMIN 한정)
├── cpp/native_security.cpp           # JNI 루팅/Frida 검사
├── data/                             # 데이터 레이어 (Retrofit DTO/API/DI)
│   ├── auth, user, expert, request, chat, chatbot
│   ├── booking, store, checkout, payment, favorite
│   ├── network/                      # Interceptor, TokenAuthenticator, NetworkModule
│   └── serialization/                # BigDecimal/LocalDateTime kotlinx 직렬화 어댑터
├── domain/                           # 도메인 레이어 (Repository)
│   ├── auth, user, expert, request, chat, booking, store, checkout, payment, favorite
│   ├── category/model/Category.kt    # 5개 대분류 + 24개 세분류 트리 (하드코딩)
│   ├── location/model/Region.kt      # 17개 광역 (하드코딩)
│   └── ...
├── dto/                              # 무결성 리포트 DTO (RootSignals, AppIntegrityReportRequest/Response)
├── integrity/AppIntegrityReporter.kt # /api/app-integrity/report POST
├── network/ApiService.kt             # 무결성 리포트 API
├── security/                         # RootDetectionManager, SecurityCheckManager, Signature/DexHashUtil, NativeSecurityCheck
└── ui/                               # 30+ 화면 (Jetpack Compose)
    ├── auth/{login, signup}
    ├── home, expert_search, expert_detail, expert_register, favorite_experts
    ├── quote_request, quote_detail, received_quote
    ├── chat_list, chat_detail
    ├── store, store_detail, store_editor
    ├── booking (BookingCheckout)
    ├── checkout (CheckoutScreen, TossPaymentScreen, PaymentSuccess)
    ├── payment_history, transaction_detail, expert_transactions
    ├── community, post_detail, post_editor, notice_detail
    ├── mypage, account_settings, account_withdraw, profile_info, password_change
    ├── chatbot, webview
    ├── common, navigation, main, theme
    └── ...
```

---

## 빌드 / 실행

### 디버그
```bash
./gradlew :app:assembleDebug
# 또는
./gradlew :app:installDebug
```

→ debug 빌드는 무결성 검증 우회. 빠른 개발/테스트용.

### 릴리스
```bash
./gradlew :app:assembleRelease
# 또는
./gradlew :app:installRelease
```

→ release 빌드는 R8 난독화 + 리소스 축소 + 무결성 검증 활성. 서명 설정(`signingConfigs.release`) 필요 — `local.properties`에 keystore 경로/비밀번호 등록되어 있어야 함.

### local.properties 항목

```properties
sdk.dir=/Users/<you>/Library/Android/sdk

# (선택) debug 빌드 시 백엔드 BASE_URL 오버라이드. 미설정 시 prod URL 사용
BASE_URL=https://can-fly.shop/

# Release signing (필수: keystore 보유자만)
KEYSTORE_PASSWORD=<store_password>
KEY_ALIAS=<alias>
KEY_PASSWORD=<key_password>
```

`local.properties`는 `.gitignore` 대상 — 절대 커밋 금지.

### Keystore

- 위치: `Back-Kyung-Android-keystore.jks` (레포 루트, `.gitignore`)
- 분실 시 Play Store 앱 업데이트 영구 불가 — secret 매니저(1Password 등)에 백업 필수
- 같은 keystore + alias + password로 빌드하면 누가 빌드하든 동일 서명 해시 → 백엔드 무결성 화이트리스트 일치

---

## CI / CD — GitHub Actions

`.github/workflows/release-apk.yml` — 다음 트리거에서 release APK 자동 빌드 → Artifact 업로드:

| 트리거 | 시점 |
|---|---|
| `push: branches: [main]` | main push마다 자동 |
| `push: tags: [v*]` | 버전 태그 push 시 (`git tag v0.1.0 && git push origin v0.1.0`) |
| `workflow_dispatch` | Actions 탭에서 수동 |

빌드 후 **Actions 탭 → 해당 run → Artifacts → `matchingon-release-apk`** 에서 다운로드 (30일 보관).

### 필요한 Secrets (레포 Settings → Secrets and variables → Actions)

| Name | 내용 |
|---|---|
| `KEYSTORE_BASE64` | `base64 -i Back-Kyung-Android-keystore.jks` 결과 |
| `KEYSTORE_PASSWORD` | keystore 비밀번호 |
| `KEY_ALIAS` | alias |
| `KEY_PASSWORD` | key 비밀번호 |

---

## 결제 흐름 (Toss)

```
USER 결제 화면 (CheckoutScreen / BookingCheckoutScreen)
      ↓ prepareForServiceRequest / prepareForBooking
백엔드 PaymentService.preparePayment
      ↓ finalAmount + orderId + paymentKey(placeholder)
TossPaymentScreen
      ↓ TossPayments(BuildConfig.TOSS_CLIENT_KEY).requestCardPayment(...)
Toss 결제창 (카드/간편결제)
      ↓ paymentKey/orderId/amount 콜백
PaymentRepository.confirm
      ↓ POST /api/payments/confirm
백엔드 PaymentService.confirmPayment
      ↓ Tailscale VPN으로 Node 결제 서버 호출 → Toss 승인 API
PaymentSuccess
```

- 테스트 키: `test_ck_GePWvyJnrKmlw5N22DXR3gLzN97E`
- 운영 키 전환: `app/build.gradle.kts`의 `tossClientKey` 상수 또는 `local.properties` 분리 권장

---

## 5탭 + Nested NavController 패턴

- 외부 NavHost(`AppNavHost.kt`): 진입점 + 비전체화면 라우트 (로그인, 마이페이지, 상세 등)
- 내부 NavHost(`MainScaffold.kt`): 하단 5탭 — 홈/고수찾기/마켓/받은견적/채팅/커뮤니티
- 챗봇 FAB는 홈 탭에만
- `forceTab` 패턴: 외부 라우트에서 nested 탭으로 점프할 때 `savedStateHandle[ARG_FORCE_TAB]` 세팅 후 pop

---

## 백엔드 / 외부 연동

- **메인 백엔드**: [Back-Kyung](https://github.com/Mohaemohae-kyung/Back-Kyung) (Spring Boot, `https://can-fly.shop`)
- **웹 클라이언트**: [Back-Kyung-Web](https://github.com/Mohaemohae-kyung/Back-Kyung-Web)
- **결제 서버**: [payment-kyung](https://github.com/Mohaemohae-kyung/payment-kyung) (Node.js, Toss 승인 위임, Tailscale 내부망)
- **관리자 웹뷰**: 별도 운영 페이지 (현재 임시 `http://43.200.59.30/`)

---

## 기여 / 워크플로우

- 메인 브랜치: `main`
- 작업 단위: **이슈 + PR 1개** (`feat:` / `fix:` / `chore:` / `docs:` prefix)
- 머지: `gh pr merge --squash --delete-branch`
- 외부 PR 검토 시: main 최신화 + 통합 시뮬레이션 빌드 후 머지

---

## 라이선스

내부 프로젝트. SK Shieldus Rookies 30기 모해모해뀽팀.
