# 개발 환경 세팅 가이드

> Windows / macOS 공통. 팀 4명이 동일한 환경에서 빌드하기 위한 가이드.

---

## 0. 한눈에 보는 흐름

```
1. 도구 설치 (Android Studio + Git + NDK)
       ↓
2. Repository clone
       ↓
3. Android Studio에서 Open → Gradle Sync (자동)
       ↓
4. ./gradlew assembleDebug 로 빌드 검증
```

자동으로 맞춰지는 것: Gradle 8.14.5, AGP 8.13.2, Kotlin 2.0.21, Hilt 2.56.2 등 모든 라이브러리·플러그인 버전 (clone 후 sync 시 자동 다운로드).

---

## 1. 필수 도구 설치

### 1-1. Android Studio
- **다운로드**: https://developer.android.com/studio
- **권장 버전**: 2025.3 (Panda 4) 이상
- **설치 옵션**: 기본 설정 그대로 진행. 첫 실행 시 SDK Platform 35 자동 설치됨.

### 1-2. Git
| OS | 설치 |
|---|---|
| Windows | https://git-scm.com/download/win — installer 실행 |
| macOS | `brew install git` 또는 Xcode Command Line Tools |

설치 후 identity 설정:
```bash
git config --global user.name "본인 GitHub 이름"
git config --global user.email "본인 GitHub 이메일"
```

### 1-3. NDK + CMake (Android Studio 내장 SDK Manager)
Android Studio 실행 후:

1. `Settings` / `Preferences` → `Languages & Frameworks` → `Android SDK`
2. **SDK Tools** 탭 클릭
3. 체크:
   - ✅ **NDK (Side by side)** (1GB+ 다운로드)
   - ✅ **CMake**
4. `Apply` → 다운로드 대기

> 우리 프로젝트는 NDK 버전·CMake 버전을 코드에 명시하지 않고 **AGP 기본값**을 사용하므로, Studio 최신 버전 설치하면 자동으로 맞는 버전이 들어갑니다.

### 1-4. (선택) GitHub CLI
Issue·PR 생성을 명령줄로 하려면:
- Windows: https://cli.github.com/ installer
- macOS: `brew install gh`

설치 후:
```bash
gh auth login
```

### 1-5. JDK?
**별도 설치 불필요.** Android Studio가 내장 JBR(JetBrains Runtime, JDK 21 기반)을 사용합니다. `JAVA_HOME` 환경변수 건드릴 필요 없습니다.

---

## 2. Repository Clone

```bash
git clone https://github.com/Mohaemohae-kyung/Back-Kyung-Android.git
cd Back-Kyung-Android
```

---

## 3. Android Studio에서 프로젝트 열기

1. Android Studio 실행 → **Open** 클릭
2. 위에서 clone한 `Back-Kyung-Android` 폴더 선택 → **Open**
3. `Trust Project` 다이얼로그 나오면 **Trust**
4. 우측 하단 상태바에서 **Gradle Sync** 자동 시작 (첫 sync 5~15분 소요 — Gradle 8.14.5, 모든 의존성 다운로드)

### Gradle JDK 확인
sync 끝나면 `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Gradle`:
- **Gradle JDK**: `jbr-21` 또는 `Android Studio default JDK` (내장 JBR)
- **Use Gradle from**: `'gradle-wrapper.properties' file`

이 설정이면 시스템에 어떤 JDK가 있든 무관하게 동일한 빌드.

---

## 4. 빌드 검증

### CLI (모든 OS)
```bash
# macOS / Linux / Git Bash
./gradlew assembleDebug

# Windows CMD / PowerShell
gradlew.bat assembleDebug
```

성공 시 `BUILD SUCCESSFUL in ...초` + `app/build/outputs/apk/debug/app-debug.apk` 생성.

### Android Studio에서 실행
1. 상단 툴바 디바이스 셀렉터 → 에뮬레이터 또는 연결된 디바이스 선택
2. ▶️ **Run 'app'** 버튼 클릭 (또는 `Shift+F10` / `Ctrl+R`)
3. 앱 화면에 "환영합니다 — Mohaemohae kyung" 표시되면 성공

---

## 5. Windows 특이사항

### 5-1. Long Path 지원 활성화 (필수)
Windows의 260자 경로 제한 때문에 Gradle 빌드 실패할 수 있습니다.

**PowerShell 관리자 권한으로 실행:**
```powershell
New-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem" `
  -Name "LongPathsEnabled" -Value 1 -PropertyType DWORD -Force
```

**Git에도 적용:**
```bash
git config --system core.longpaths true
```

### 5-2. Line Ending (CRLF ↔ LF)
저장소 루트에 `.gitattributes` 파일이 있어 **자동으로 정규화**됩니다. 추가 설정 불필요.

확인용 명령:
```bash
git config --global core.autocrlf  # 결과 무관
```

### 5-3. 안티바이러스/실시간 보호 예외
Windows Defender 등이 `build/` `caches/` 폴더를 매번 스캔하면 빌드가 느려집니다. 다음 폴더를 예외 처리 권장:
- 프로젝트 폴더 (예: `C:\Users\<name>\Projects\Back-Kyung-Android\`)
- Gradle 캐시: `%USERPROFILE%\.gradle\`
- Android SDK: `%LOCALAPPDATA%\Android\Sdk\`

---

## 6. 백엔드 연결

| 시나리오 | BASE_URL |
|---|---|
| 에뮬레이터 → 본인 PC에서 백엔드 실행 | `http://10.0.2.2:8080` (모든 OS 동일) |
| 실기기 → 백엔드 PC | `http://<백엔드 PC의 IP>:8080` (같은 Wi-Fi 필요) |

백엔드 띄우는 방법은 [Back-Kyung repo](https://github.com/Mohaemohae-kyung/Back-Kyung) 참고.

---

## 7. 자주 막히는 곳

### `Gradle Sync Failed: Could not determine the dependencies`
- 인터넷 연결 / 회사 프록시 확인
- `~/.gradle/caches/` 삭제 후 재시도

### `SDK location not found`
- `local.properties` 파일이 자동 생성 안 됐다면 수동으로:
  ```
  sdk.dir=C:\\Users\\<본인>\\AppData\\Local\\Android\\Sdk   # Windows
  sdk.dir=/Users/<본인>/Library/Android/sdk                  # macOS
  ```

### CMake 또는 NDK 못 찾음
- Android Studio SDK Manager에서 `NDK (Side by side)`, `CMake` 재설치
- 우리 프로젝트는 AGP 기본값을 쓰므로, 설치만 되어 있으면 OK

### 빌드 너무 느림 (Windows)
- 위 §5-3 안티바이러스 예외 설정
- `gradle.properties`에 `org.gradle.parallel=true` (이미 설정됨)
- JVM heap 늘리기: `org.gradle.jvmargs=-Xmx6144m` (기본 4GB)

---

## 8. 버전 매트릭스 (참고용 — 자동 적용됨)

| 항목 | 버전 | 위치 |
|---|---|---|
| Android Studio | 2025.3.x (Panda 4)+ | 본인 설치 |
| AGP | 8.13.2 | `gradle/libs.versions.toml` |
| Gradle | 8.14.5 | `gradle/wrapper/gradle-wrapper.properties` |
| Kotlin | 2.0.21 | `gradle/libs.versions.toml` |
| KSP | 2.0.21-1.0.27 | `gradle/libs.versions.toml` |
| compileSdk / targetSdk | 36 (Android 16) | `app/build.gradle.kts` |
| minSdk | 26 (Android 8.0+) | `app/build.gradle.kts` |
| Compose BOM | 2024.12.01 | `gradle/libs.versions.toml` |
| Hilt | 2.56.2 | `gradle/libs.versions.toml` |
| Retrofit / OkHttp | 2.11.0 / 4.12.0 | `gradle/libs.versions.toml` |
| Coil 3 | 3.0.4 | `gradle/libs.versions.toml` |
| NDK / CMake | AGP 기본값 (자동) | 명시 안 함 |

→ 의존성 추가·버전 변경은 항상 **`gradle/libs.versions.toml`** 한 곳에서.
