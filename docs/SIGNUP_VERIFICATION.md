# 회원가입 검증 기록

작업일: 2026-09-29. 기존 공통 프로젝트의 SELECT 1 성공 기록은 이번 회원가입 검증 결과와 구분합니다.

| 항목 | 상태 |
|---|---|
| 로그인 식별자 | 김형빈 확인: 별도 아이디 |
| 인코더 | Security PR #11의 BCryptPasswordEncoder 사용 |
| 필드·길이·중복 규칙 / DDL 멘토 확인 | 사용자 요청으로 로컬 검증 진행. 실제 멘토 리뷰 기록은 미확인 |
| Java / Gradle | Wrapper `--version`: Launcher JVM 21.0.12.1, Gradle 9.7.1 확인 |
| 메인·테스트 컴파일 | 사용자 PowerShell에서 `classes test` 실행 후 BUILD SUCCESSFUL 확인 |
| Service·HTTP·기존 Security 테스트 | 27개 성공, 실패 0, 오류 0, 건너뜀 0. 로컬 JUnit XML 보고서 확인(2026-09-29 15:10 KST) |
| 정적 확인 | `git diff --check` 통과. `.env` 미추적·ignore 적용, `ddl-auto: none` 유지 확인 |
| 개인 MySQL에 DDL 적용 | 사용자 실행: 기존 users 테이블 없음 확인 후 SQL 적용, DESCRIBE users 성공 보고 |
| 실제 MySQL 가입 201 / 오류 400 / 중복 409 / 저장 해시 검증 | SignupMySqlTest 7개 성공, 실패·오류·건너뜀 0. JUnit XML 확인(2026-09-29 15:22 KST) |
| 실제 서버 HTTP 요청 | 서버 8080 기동 화면 확인. 사용자 PowerShell에서 정상 가입 201 및 회원 정보 응답, 중복 409·입력 오류 400 확인 보고 |
| GitHub 업로드·PR 제출·멘토 리뷰 반영 | 사용자가 직접 진행할 예정 |

## 확인한 코드 테스트

| 테스트 | 성공 개수 |
|---|---:|
| UserServiceTest | 5 |
| UserControllerTest | 17 |
| PasswordEncoderTest (Security 선행 코드) | 1 |
| SecurityConfigTest (Security 선행 코드) | 4 |

기존 캐시의 접근 거부 문제로 사용자 PowerShell에서 별도 캐시를 지정해 실행했습니다.

```powershell
$env:GRADLE_USER_HOME = "$env:LOCALAPPDATA\Redbeanz\gradle-user-cache"
.\gradlew.bat classes test --tests 'com.redbeanz.backend.user.UserServiceTest' --tests 'com.redbeanz.backend.user.UserControllerTest' --tests 'com.redbeanz.backend.security.*' --no-daemon --console=plain
```

위 27개는 저장소·서비스 대역을 이용한 코드 테스트입니다. 실제 MySQL 저장은 아래 별도 테스트로 확인했습니다.

## 확인한 개인 MySQL 통합 테스트

```powershell
.\gradlew.bat signupMySqlTest --rerun-tasks --no-daemon --console=plain
```

사용자가 BUILD SUCCESSFUL을 보고했으며, `build/test-results/signupMySqlTest/TEST-com.redbeanz.backend.user.SignupMySqlTest.xml`에서 7개 성공을 확인했습니다.

- 실제 가입 201 및 DB에서 다시 읽은 비밀번호 해시의 PasswordEncoder.matches 성공
- 입력 오류 400 및 회원 미저장
- 아이디 중복 409, 이메일 중복 409
- DB UNIQUE 제약의 아이디 중복 차단, 이메일 중복 차단
- ADMIN을 요청해도 DB에는 USER 저장

애플리케이션 DataSource와 개인 MySQL을 사용했고, 테스트 데이터는 트랜잭션 롤백 방식으로 정리합니다. HTTP 계층은 MockMvc를 사용하므로 실제 8080 포트로 보낸 요청 결과와 구분합니다.

## 실제 서버 수동 확인

2026-09-29 `bootRun --no-daemon --console=plain` 실행 후 8080 포트 기동과 `Started RedbeanzBackendApplication`을 화면에서 확인했습니다. 사용자가 별도 PowerShell에서 CSRF 토큰과 세션 쿠키를 받은 뒤 회원가입 요청을 보냈습니다.

- 정상 요청: 사용자가 회원 정보 응답 수신 및 `$signupResult.StatusCode`의 201을 확인해 보고했습니다.
- 동일 요청 재전송: 사용자 보고 409.
- 잘못된 이메일 요청: 사용자 보고 400.

수동 요청으로 생성한 예시 회원은 자동 롤백되지 않습니다. 동일 예시를 다시 사용하면 409가 나올 수 있습니다. 재현 명령은 `SIGNUP.md`에 있습니다.

DB 주소·계정·비밀번호·회원 비밀번호·해시가 포함된 로그 원문은 공유하지 않습니다.
