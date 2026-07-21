# LogFit 집 PC 작업 인수인계

최종 갱신: 2026-07-21  
작업 브랜치: `feature/capacitor-app`  
패키지 ID: `com.logfit.app`  
현재 앱 버전: `0.5.0` / versionCode `5`

이 문서는 다른 PC의 Codex가 기존 작업을 다시 분석하지 않고 바로 이어서 진행할 수 있도록 현재 상태와 다음 작업을 정리한 문서입니다.

## 집 PC에서 시작하기

```bash
git clone https://github.com/rlatjd1f/LogFit.git
cd LogFit
git fetch origin
git switch feature/capacitor-app
git pull --ff-only origin feature/capacitor-app
npm ci
```

이미 저장소가 있다면 `git fetch`, `git switch`, `git pull --ff-only`부터 실행합니다. 프로젝트 루트의 `AGENTS.md`에 커밋 규칙과 커밋 후 즉시 푸시 규칙이 있으므로 새 Codex도 반드시 따라야 합니다.

필요 환경:

- Node.js 22 이상
- JDK 21
- Android Studio 및 Android SDK 36/36.1 이상
- Android Emulator 또는 USB 디버깅을 허용한 실제 기기

## 이번 작업에서 해결한 핵심 문제

목표는 세트 완료 후 시작되는 휴식 타이머를 앱 밖에서도 확인하는 것이었습니다.

- 홈 이동 시 자동 PiP 진입
- PiP에는 운동명, 완료 세트, 큰 남은 시간, 휴식 상태만 표시
- Android 알림 카드에 운동명과 남은 시간 표시
- Android 16.1 이상 Live Update 승격 요청과 짧은 남은 시간 값 제공
- 상태 표시줄용 단색 아이콘 적용
- 알림에서 `타이머 종료` 제공
- Foreground Service로 앱이 백그라운드에 있어도 타이머 유지

Android 상태 표시줄의 칩 배경색, 모양과 표시 우선순위는 System UI가 결정하므로 앱이 임의로 배경색을 강제할 수 없습니다. 앱에서는 알림 색상, 단색 아이콘, `setShortCriticalText`, promoted ongoing 요청까지만 설정합니다.

## 현재 기술 상태

- Capacitor 8.4.2
- `@capacitor/app` 8.1.1
- Android Gradle Plugin 8.13.0
- Gradle 8.14.3
- targetSdk 36
- compileSdk 36.1
- minSdk 24
- R8 및 리소스 축소 활성화
- 앱 데이터 OS 백업 차단
- Android 평문 통신 차단
- 광고, 분석 SDK, 계정, 원격 API 없음
- 운동 기록은 localStorage에만 저장

Pixel 10 AVD는 Android 17/API 37 이미지였으며 다음 항목을 검증했습니다.

- APK 설치와 콜드 실행
- targetSdk 36 확인
- 세트 완료 후 TimerService 실행
- Foreground Service `specialUse` 확인
- 알림 카드 및 PiP 진입 확인
- PiP 전용 타이머 UI 확인
- Android 계측 테스트 2개 통과

API 37에서 상위 호환성은 확인했지만, 출시 전 가능하면 Android 16.1/API 36.1 시스템 이미지와 실제 제조사 기기에서도 Live Update 표시를 다시 확인합니다.

## 자동 검증 명령

일반 개발 검사:

```bash
npm test
npm run android:test
```

실행 중인 에뮬레이터 또는 기기 계측 검사:

```bash
npm run android:test:device
```

전체 출시 검사:

```bash
npm run release:check
```

최근 최종 검증 결과:

- npm audit: 취약점 0개
- Node 회귀 테스트: 9개 통과
- Android 앱 계측 테스트: 2개 통과
- Android 단위 테스트: 통과
- Lint: 통과
- 디버그 APK: 생성 성공
- R8 릴리스 AAB: 생성 성공, 약 1.6MB

## 남은 작업 권장 순서

### 1. Play 업로드 키 생성

키는 Git에 올리지 말고 안전한 비밀번호 관리자와 별도 저장장치에 백업합니다.

```bash
cd android
keytool -genkeypair \
  -v \
  -keystore upload-keystore.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
cp keystore.properties.example keystore.properties
```

`android/keystore.properties`에 실제 값을 입력합니다.

```properties
storeFile=upload-keystore.jks
storePassword=실제_비밀번호
keyAlias=upload
keyPassword=실제_비밀번호
```

두 파일은 `.gitignore` 대상입니다. 채팅이나 커밋에 비밀번호를 남기지 않습니다.

```bash
cd ..
npm run release:bundle:signed
```

결과: `android/app/build/outputs/bundle/release/app-release.aab`

### 2. 개인정보처리방침 공개 배포

루트의 `privacy.html`을 로그인 없이 접근 가능한 HTTPS 주소로 배포합니다. GitHub Pages 사용 시 예상 주소는 다음과 같습니다.

`https://rlatjd1f.github.io/LogFit/privacy.html`

실제 URL이 시크릿 브라우저와 모바일에서 열리는지 확인한 뒤 Play Console에 입력합니다.

### 3. Play Console 등록

`docs/play-store/`의 문서를 복사·검토하여 입력합니다.

- `STORE_LISTING_KO.md`: 앱 이름과 설명, 출시 노트
- `DATA_SAFETY_KO.md`: 데이터 수집·공유 답변
- `FOREGROUND_SERVICE_KO.md`: specialUse 설명 및 촬영 절차
- `REVIEW_GUIDE_KO.md`: 심사자 안내
- `RELEASE_CHECKLIST_KO.md`: 전체 체크리스트
- `assets/`: 피처 그래픽과 스크린샷

Play Console에서 별도로 결정하거나 입력해야 하는 값:

- 개발자 연락처 이메일, 전화, 주소
- 무료/유료 여부와 출시 국가
- 타깃 연령 및 아동 대상 여부
- 콘텐츠 등급
- 건강 앱 선언
- Play App Signing 활성화

### 4. Foreground Service 시연 영상

운동 시작 → 첫 세트 완료 → 휴식 타이머 → 홈 이동 → PiP → 알림 패널 남은 시간 → 알림의 타이머 종료 순서로 한 번에 녹화합니다.

### 5. 내부 테스트

서명 AAB를 내부 테스트에 업로드하고 다음을 확인합니다.

- Play 사전 출시 보고서 오류
- 새 설치와 업데이트 설치
- 알림 허용 및 거부 흐름
- PiP 허용/차단 상태
- 타이머 완료와 수동 종료
- 프로세스 재생성 후 타이머 복원
- 라이트/다크 테마
- JSON 백업/복구
- Android 16.1, 17 및 가능하면 삼성 실제 기기

### 6. 정식 버전 결정

현재 `0.5.0`, versionCode `5`입니다. 내부 테스트를 유지할지, 정식 출시 전에 `1.0.0`과 더 높은 versionCode로 올릴지 소유자가 결정해야 합니다. Play에 업로드한 뒤에는 같은 versionCode를 재사용할 수 없습니다.

## 최근 주요 커밋

- `d84c985`: Android 16 빌드 환경과 Capacitor 8 전환
- `b6a79f5`: Android 타이머 보안과 종료 제어 강화
- `3b56604`: Android 출시 자동 검증 테스트 보강
- `38e5b70`: Play 릴리스 서명과 R8 구성 추가
- `3f1227f`: 개인정보처리방침과 Play 심사 문서 추가
- `fb8075b`: Play 스토어 문구와 이미지 자산 준비
- `8204df4`: Android 출시와 테스트 실행 방법 갱신

## 새 Codex에 전달할 시작 문구

다음 요청을 새 Codex에 그대로 전달하면 됩니다.

> 프로젝트 루트의 AGENTS.md와 docs/HANDOFF_HOME_PC_KO.md를 먼저 읽고 현재 브랜치와 작업 트리를 확인해줘. 인수인계 문서의 남은 작업 순서대로 진행하되, 비밀번호와 keystore는 절대 커밋하지 말고 각 변경을 검증한 뒤 프로젝트 커밋 규칙에 따라 커밋 및 즉시 푸시해줘.

Play Console 로그인, 결제, 법적 선언, 연락처 입력 및 비밀 키 생성처럼 사용자 확인이 필요한 작업은 임의로 확정하지 말고 현재 상태와 필요한 입력을 먼저 알려야 합니다.
