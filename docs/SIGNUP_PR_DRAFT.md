# PR 제목 예시

feat: 회원 테이블과 회원가입 API 구현 (#8)

아래 본문은 제출 준비용 초안입니다. 검증 결과와 리뷰 상태를 실제 수행 내용으로 수정한 뒤 GitHub PR 양식에 붙여 넣습니다. base는 `dev`입니다. 이 브랜치는 미병합 Security PR #11을 기반으로 준비했으므로 #11 병합 후 변경 목록을 다시 확인하세요. 아직 GitHub에 업로드하지 않았습니다.

---

## 관련 이슈

Related to #8

선행: #3, Security #5 / PR #11

## 변경 요약

- 별도 로그인 아이디를 사용하는 User Entity와 CREATE TABLE SQL을 작성했습니다. 필드 길이·중복 정책은 `docs/SIGNUP.md`의 검토안으로 정리했습니다.
- `POST /api/users` 회원가입을 Controller → Service → Repository로 구현하고 요청·응답 DTO를 분리했습니다.
- 정상 가입 201, 입력 오류 400, 아이디·이메일 중복 409를 처리합니다. 서비스 사전 검사와 DB UNIQUE 제약을 함께 사용합니다.
- Security #5의 BCryptPasswordEncoder를 주입받아 해시를 저장하고 응답에서 원문·해시를 제외합니다. 가입 권한은 USER로 고정합니다.
- 기존 CSRF 보호를 유지하고 회원가입용 CSRF 토큰 보조 경로를 제공합니다.
- `ddl-auto: none`을 유지합니다. 로그인·내 정보·프로필 등의 기능은 추가하지 않았습니다.

## 확인 방법·결과

실행 위치: 프로젝트 루트 / JDK 21 / 개인 로컬 MySQL 설정은 `.env`.

```powershell
.\gradlew.bat --version
.\gradlew.bat classes
.\gradlew.bat test --tests 'com.redbeanz.backend.user.UserServiceTest' --tests 'com.redbeanz.backend.user.UserControllerTest' --tests 'com.redbeanz.backend.security.*'
# 아래 단계는 멘토 확인 후 개인 DB에 DDL을 적용한 뒤 실행합니다.
.\gradlew.bat test --tests com.redbeanz.backend.MySqlConnectionTest --rerun-tasks
.\gradlew.bat signupMySqlTest --rerun-tasks
.\gradlew.bat bootRun
```

- 코드 테스트 결과: 2026-09-29 사용자 PowerShell에서 `classes test` BUILD SUCCESSFUL. JUnit XML에서 총 27개 성공, 실패·오류·건너뜀 0 확인(UserService 5, UserController 17, 선행 Security 테스트 5). 상세 명령과 캐시 설정은 `docs/SIGNUP_VERIFICATION.md`에 기록했습니다. 이 테스트에는 실제 MySQL 회원 저장이 포함되지 않습니다.
- 실제 MySQL 검증: 2026-09-29 개인 MySQL에 SQL 적용 후 `signupMySqlTest --rerun-tasks --no-daemon --console=plain` BUILD SUCCESSFUL. JUnit XML에서 7개 성공, 실패·오류·건너뜀 0 확인. MockMvc를 통한 201/400/409, DB UNIQUE 제약, 저장 해시 `PasswordEncoder.matches`, USER 권한 고정을 검증했습니다.
- 실제 서버 수동 검증: 8080 포트 기동 확인 후 PowerShell에서 CSRF 토큰·쿠키를 이용해 정상 가입 201과 회원 정보 응답, 동일 요청 재전송 409, 잘못된 이메일 400을 확인했습니다. 최초 응답의 `$signupResult.StatusCode`도 201로 확인했습니다.
- 요청·응답 예시와 PowerShell 재현 절차: `docs/SIGNUP.md`
- DDL: `docs/sql/001-create-users.sql`

## 리뷰 요청·질문

- 아이디·이메일 소문자 정규화, 필수값·길이·중복 규칙과 SQL/Entity 대응을 확인 부탁드립니다. 개인 MySQL에는 SQL을 적용해 테스트했으며 멘토 리뷰 반영은 별도로 진행합니다.
- 박승민님의 BCryptPasswordEncoder를 함께 사용합니다. 회원가입 공개 규칙과 CSRF 토큰 제공 경로를 확인 부탁드립니다.
- 요청 DTO는 입력 검증을, 응답 DTO는 반환 정보 제한을 담당하도록 분리했습니다. 동시 가입 시 DB UNIQUE 위반을 409로 처리하는 부분도 확인 부탁드립니다.

## 체크리스트

- [x] 관련 이슈와 이번 PR의 작업 범위를 확인했습니다.
- [ ] 변경에 필요한 확인 방법과 결과를 작성했습니다.
- [ ] 코드·문서·로그·캡처에 실제 접속 정보와 비밀값이 없는지 확인했습니다.
- [ ] 멘토를 Reviewer로 지정했습니다.
