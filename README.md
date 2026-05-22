# Mohaemohae kyung — Android

[Back-Kyung](https://github.com/Mohaemohae-kyung/Back-Kyung) 백엔드(Spring Boot 3.2.3 + Oracle)와 연동하는 안드로이드 클라이언트.

---

## 환경

| 항목 | 버전 |
|---|---|
| Kotlin | 2.0.21 |
| Android Gradle Plugin | 8.7.3 |
| Gradle | 8.10.x |
| compileSdk / targetSdk | 36 (Android 16) |
| minSdk | 26 (Android 8.0+) |
| UI | Jetpack Compose + Material3 |
| DI | Hilt 2.52 |
| 네트워크 | Retrofit 2.11 + OkHttp 4.12 + Kotlinx Serialization |
| 이미지 | Coil 3 |
| NDK / CMake | 30.0.14904198 / 4.1.2 (현재 비활성) |

---

## 빌드 & 실행

```bash
./gradlew assembleDebug
./gradlew :app:installDebug
```

또는 Android Studio에서 `Open` → 본 디렉토리 선택 → Gradle sync.

---

## 백엔드 연결

- 기본 BASE_URL: `http://10.0.2.2:8080` (에뮬레이터 → 호스트 PC)
- 실기기 사용 시 호스트 PC IP 로 변경 필요
- 백엔드 실행 방법: [Back-Kyung repo](https://github.com/Mohaemohae-kyung/Back-Kyung) 참고

---

## 패키지 구조

```
app/src/main/java/kyung/kung_android/
├── KungAndroidApplication.kt   # @HiltAndroidApp
└── MainActivity.kt              # @AndroidEntryPoint, Compose 진입
```

향후 추가 예정 (기능 구현에 따라):
```
├── di/                # Hilt 모듈 (NetworkModule 등)
├── data/
│   ├── remote/{api, dto, interceptor}
│   ├── local/         # TokenStorage 등
│   └── repository/
├── domain/{model, repository, usecase}
└── presentation/{auth, expert, payment, chat, ...}
```

---

## 모듈

| 모듈 | 설명 |
|---|---|
| `app` | 메인 애플리케이션 모듈 (UI + 비즈니스 로직 + 데이터 레이어) |
