# LogFit

LogFit은 N분할 순환 루틴을 기반으로 운동을 기록하는 초경량 PWA 및 Android 운동 일지입니다. 웹에서는 정적 앱으로, Android에서는 Capacitor WebView 앱으로 동작합니다. 모든 데이터는 `localStorage`에 저장되며 첫 로드 이후에는 오프라인에서도 사용할 수 있습니다.

## 주요 기능

- N분할 순환 루틴 생성 및 관리
- 50종 이상 내장 운동 마스터 DB 제공
- 커스텀 운동 직접 등록
- 세트별 중량과 반복수 기록
- 전체 화면 휴식 카운트다운 타이머
- 휴식 종료 10초 전 경고 배경 및 Web Audio 비프음
- 완료한 운동 카드 상태 표시 및 자동 정렬
- Chart.js 기반 점진적 과부하 리포트
- 총 볼륨과 추정 1RM 추이 차트
- 라이트/다크 테마 전환
- JSON 데이터 백업 및 복구
- `manifest.json`과 서비스 워커 기반 PWA 지원
- Android 포그라운드 휴식 타이머와 알림
- 타이머 실행 중 자동 PIP(Picture-in-Picture) 진입

## 프로젝트 구조

```text
.
├── index.html          # 앱 UI, 스타일, JavaScript가 포함된 메인 파일
├── logfit-core.js      # 렌더링 보안 및 공통 타이머 로직
├── tests/              # Node.js 공통 로직 회귀 테스트
├── manifest.json       # PWA 메타데이터
├── sw.js               # 오프라인 캐싱용 서비스 워커
├── capacitor.config.json
├── android/            # Capacitor Android 프로젝트와 타이머 서비스
├── icon.svg            # 앱 아이콘
├── AGENTS.md           # 프로젝트 작업 및 커밋 규칙
├── .gitignore
└── README.md
```

## 실행 방법

프로젝트 루트에서 정적 서버를 실행합니다.

```bash
python3 -m http.server 4173
```

브라우저에서 아래 주소를 엽니다.

```text
http://localhost:4173/
```

GitHub Pages, Netlify, Vercel, Cloudflare Pages 같은 정적 호스팅 서비스에도 배포할 수 있습니다.

## 테스트 및 Android 실행

Node.js 18 이상과 Android SDK/JDK가 필요합니다.

```bash
npm install
npm test
npm run android:sync
```

Android Studio에서 `android/` 폴더를 열어 실제 기기 또는 에뮬레이터로 실행합니다. 명령행 단위 테스트는 다음과 같이 실행합니다.

```bash
npm run android:test
```

PIP, 알림 권한, 홈 버튼 진입, 앱 프로세스 재생성은 Android 버전과 제조사 정책의 영향을 받으므로 릴리스 전 실제 기기에서 별도로 확인해야 합니다.

## 데이터 저장 방식

LogFit은 서버나 계정 없이 `localStorage`만 사용합니다.

저장되는 데이터는 다음과 같습니다.

- 테마 설정
- 운동 마스터 데이터
- 커스텀 운동
- 루틴 설정
- 운동 기록

앱의 백업 화면에서 전체 데이터를 JSON 파일로 내보내거나 다시 가져올 수 있습니다.

## PWA 안내

앱에는 다음 PWA 구성 요소가 포함되어 있습니다.

- `manifest.json`
- `sw.js`
- SVG 앱 아이콘
- 오프라인 앱 셸 캐싱

설치 프롬프트와 서비스 워커 기능은 `http://localhost`, `https`, 또는 정적 호스팅 환경에서 정상 동작합니다. HTML 파일을 디스크에서 직접 열면 일부 PWA 기능이 제한될 수 있습니다.

## 기술 스택

- HTML
- CSS
- Vanilla JavaScript
- 로컬 Chart.js
- Capacitor 6
- Android Java 포그라운드 서비스
- Web Audio API
- Service Worker API

## 현재 상태

현재 버전은 웹 PWA와 Android 앱을 함께 지원합니다. 서버 동기화나 계정 기능은 없으므로 기기 교체 전 JSON 백업이 필요합니다.
