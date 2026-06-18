# LogFit

LogFit은 N분할 순환 루틴을 기반으로 운동을 기록하는 초경량 모바일 PWA 운동 일지입니다. 정적 웹 앱으로 동작하며, 모든 데이터는 브라우저 `localStorage`에 저장됩니다. 첫 로드 이후에는 서비스 워커를 통해 오프라인 환경에서도 사용할 수 있습니다.

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

## 프로젝트 구조

```text
.
├── index.html          # 앱 UI, 스타일, JavaScript가 포함된 메인 파일
├── manifest.json       # PWA 메타데이터
├── sw.js               # 오프라인 캐싱용 서비스 워커
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
- Chart.js CDN
- Web Audio API
- Service Worker API

## 현재 상태

모바일 운동 기록과 오프라인 우선 로컬 데이터 관리를 목표로 만든 초기 단일 파일 PWA 프로토타입입니다.
