# Mohaemohae kyung — Android

[Back-Kyung](https://github.com/Mohaemohae-kyung/Back-Kyung) 백엔드(Spring Boot 3.2.3 + Oracle)와 연동하는 안드로이드 클라이언트.

---

## 환경

| 항목 | 버전 |
|---|---|
| Kotlin | 2.0.21 |
| Android Gradle Plugin | 8.13.2 |
| Gradle | 8.14.5 |
| compileSdk / targetSdk | 36 (Android 16) |
| minSdk | 26 (Android 8.0+) |
| UI | Jetpack Compose + Material3 |
| DI | Hilt 2.56.2 |
| 네트워크 | Retrofit 2.11.0 + OkHttp 4.12.0 + Kotlinx Serialization |
| 이미지 | Coil 3 (3.0.4) |
| NDK / CMake | AGP 기본값 (활성, 16 KB 페이지 정렬) |

> 셋업·도구·트러블슈팅 상세는 **[SETUP.md](./SETUP.md)** 참고.

---

## 패키지 구조

```
app/src/main/java/kyung/kung_android/
├── KungAndroidApplication.kt   # Hilt Application
└── MainActivity.kt              # Compose 진입
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
