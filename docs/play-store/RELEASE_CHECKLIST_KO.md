# Google Play 출시 체크리스트

## 저장소에서 완료된 항목

- [x] targetSdk 36 / compileSdk 36.1
- [x] Capacitor 8 및 최신 AndroidX 전환
- [x] Android 17(API 37) Pixel 10 설치·실행·PiP·Foreground Service 검증
- [x] 웹 테스트, Android 단위 테스트, 기기 계측 테스트
- [x] Lint, R8, 리소스 축소, AAB 빌드
- [x] 업로드 키 외부 주입 구성 및 비밀 파일 Git 제외
- [x] 개인정보처리방침과 앱 내 링크
- [x] Data safety, Foreground Service, 심사자 안내 초안
- [x] 한국어 스토어 문구
- [x] 1024×500 피처 그래픽 및 세로 스크린샷 3장

## 소유자가 Play Console에서 해야 할 항목

- [ ] Google Play 개발자 계정과 결제 프로필 확인
- [ ] 고유한 개발자 연락처 이메일·전화·주소 입력
- [ ] Play App Signing 활성화 및 업로드 인증서 등록
- [ ] `android/keystore.properties`와 업로드 키 생성 후 서명 AAB 빌드
- [ ] 개인정보처리방침을 공개 HTTPS URL로 배포하고 URL 입력
- [ ] 앱 액세스: 모든 기능 제한 없음/로그인 불필요로 제출
- [ ] 광고: 광고 없음으로 제출
- [ ] Data safety 답변을 코드 현황과 대조해 제출
- [ ] Foreground Service `specialUse` 설명과 실제 사용 영상 제출
- [ ] 콘텐츠 등급 설문 작성
- [ ] 타깃층과 아동 대상 여부 결정
- [ ] 건강 앱 선언 및 건강 기능/데이터 접근 여부 답변
- [ ] 국가/지역, 무료·유료 여부, 배포 트랙 선택
- [ ] 내부 테스트 업로드 후 사전 출시 보고서 확인
- [ ] versionCode 증가 후 비공개/공개 테스트 또는 프로덕션 출시

## 로컬 최종 명령

```bash
npm ci
npm run release:check
npm run android:test:device
npm run release:bundle:signed
```

서명된 AAB 경로:

`android/app/build/outputs/bundle/release/app-release.aab`
