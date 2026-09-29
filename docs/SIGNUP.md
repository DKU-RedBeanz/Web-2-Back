# 회원가입 구현 · 김형빈 · 이슈 #8

## 1. 확정한 내용과 검토할 내용

로그인에는 **별도 아이디 `loginId`를 사용**합니다(김형빈 확인). 로그인 자체는 이번 구현에 포함하지 않습니다.

아래 길이·중복·정규화 규칙은 구현을 위한 **검토안**입니다. 확인 결과에 따라 SQL, Entity, DTO, 테스트를 함께 수정합니다. 사용자 요청으로 개인 DB에 SQL을 적용해 통합 테스트 7개를 통과했으며, 실제 멘토 리뷰 기록은 별도 확인이 필요합니다. 수행 결과는 [검증 기록](SIGNUP_VERIFICATION.md)에 구분합니다.

| 항목 | API 입력 | DB 컬럼·형식 | 필수 | 검토안 |
|---|---|---|---|---|
| 내부 회원 번호 | 입력하지 않음 | `id BIGINT` | 서버 생성 | 자동 증가 PK, 로그인 아이디와 다름 |
| 로그인 아이디 | `loginId` | `login_id VARCHAR(30)` | 예 | 영문·숫자·밑줄 3~30자. 소문자 저장. 중복 불가 |
| 이메일 | `email` | `email VARCHAR(254)` | 예 | ASCII 이메일, 최대 254자. 전체 소문자 저장. 중복 불가 |
| 원문 비밀번호 | `password` | 원문 컬럼 없음 | 예 | 8자 이상, UTF-8 72바이트 이하, 공백만인 값·NUL 불가. 자르거나 trim하지 않음 |
| 비밀번호 해시 | 입력하지 않음 | `password_hash VARCHAR(60)` | 서버 생성 | BCrypt, 원문 복원용 암호화가 아닌 단방향 해시 |
| 닉네임 | `nickname` | `nickname VARCHAR(30)` | 예 | 2~30자, 양끝 공백·줄바꿈 불가. 중복 허용 |
| 권한 | 입력하지 않음 | `role VARCHAR(16)` | 서버 생성 | 일반 가입은 항상 `USER`. `USER`/`ADMIN` CHECK 제약 |

Java 길이 검사는 UTF-16 기준입니다. 한글 비밀번호처럼 여러 바이트인 문자는 별도로 UTF-8 바이트 길이를 확인합니다. 생일·전화번호·주소·프로필 이미지 등은 추가하지 않았습니다.

아이디와 이메일은 `Locale.ROOT`로 소문자화합니다. 로그인 구현에서도 같은 아이디 정규화 규칙을 사용해야 합니다. 이메일 전체를 대소문자 구분 없이 취급한다는 정책도 리뷰 대상입니다. MySQL 테이블은 `utf8mb4_0900_as_cs`로 지정해 악센트 등이 임의로 같은 값으로 취급되는 것을 피합니다.

## 2. Security #5와 연결

- [Security PR #11](https://github.com/DKU-RedBeanz/Web-2-Back/pull/11)의 `efaa09e`를 바탕으로 작업했습니다. 해당 PR은 작업 시작 시 아직 미병합이었습니다.
- 기존 `SecurityConfig`의 `new BCryptPasswordEncoder()` Bean을 주입받습니다. 별도의 인코더를 만들거나 `{bcrypt}` 접두사를 붙이지 않습니다.
- HTTP 필터 Bean에만 Servlet 조건을 적용해, HTTP 서버를 띄우지 않는 기존 MySQL 연결 테스트에서도 PasswordEncoder를 사용할 수 있게 했습니다.
- `POST /api/users`, `GET /api/users/csrf`만 메서드와 경로를 지정해 공개했습니다. 기존 나머지 인가 규칙을 유지합니다.
- 기존 CSRF 보호를 유지합니다. 회원가입은 로그인은 필요 없지만 CSRF 토큰과 같은 세션의 쿠키는 필요합니다. CSRF 누락·불일치는 Security 필터에서 **403**입니다. 이는 DTO 검증 오류 **400**과 다릅니다.
- `GET /api/users/csrf`는 Spring Security가 생성한 토큰을 받는 보조 경로입니다. DB 정보를 노출하지 않으며 응답에 `Cache-Control: no-store`를 설정합니다. 이를 로그인 세션 방식의 최종 결정으로 간주하지 않습니다. CORS·JWT·로그인·자동 로그인은 구현하지 않았습니다.

토큰 경로와 정책은 박승민·멘토의 리뷰 대상입니다. CSRF 공식 문서의 [토큰 제공 방식](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)을 참고했습니다. Spring Boot 4의 [WebMvcTest](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/webmvc/test/autoconfigure/WebMvcTest.html) 패키지를 사용합니다.

## 3. 요청과 응답

먼저 `GET /api/users/csrf`를 호출하고 응답의 `headerName`, `token` 및 응답 쿠키를 보관합니다. 같은 쿠키와 토큰 헤더를 다음 요청에 사용합니다. 토큰을 로그인 비밀번호나 JWT로 사용하지 않습니다.

```http
POST /api/users
Content-Type: application/json
X-CSRF-TOKEN: <앞에서 받은 token>
Cookie: <앞에서 받은 세션 쿠키>
```

다음 값은 문서용 가상 예시입니다.

```json
{
  "loginId": "beanuser",
  "email": "bean@example.com",
  "password": "ExamplePass123!",
  "nickname": "레드빈"
}
```

성공은 **201 Created**입니다. `id`는 실제 생성된 번호입니다.

```json
{
  "id": 1,
  "loginId": "beanuser",
  "email": "bean@example.com",
  "nickname": "레드빈",
  "role": "USER"
}
```

요청 DTO에는 권한·해시·내부 번호가 없습니다. 알 수 없는 필드는 무시하며, `role: "ADMIN"`을 보내도 서버가 생성하는 권한은 `USER`입니다. 응답은 Entity 대신 응답 DTO를 사용합니다.

잘못된 입력은 **400 Bad Request**입니다. 필수값, 형식, 길이 및 JSON 파싱 오류를 처리합니다. 아래는 예시이며 실제 오류 배열은 입력에 따라 달라집니다.

```json
{"message":"입력값을 확인해 주세요.","errors":["이메일 형식을 확인해 주세요."]}
```

중복 아이디 또는 이메일은 **409 Conflict**입니다.

```json
{"message":"이미 사용 중인 아이디 또는 이메일입니다.","errors":[]}
```

서비스가 먼저 중복을 조회하고, 동시에 들어온 요청은 DB의 UNIQUE 제약으로 막습니다. `saveAndFlush`에서 두 가입 UNIQUE 제약 위반을 구분해 409로 변환합니다. 다른 DB 오류를 중복으로 처리하지 않습니다. 오류 응답에 입력값·SQL·예외 원문을 넣지 않습니다. 이 오류 처리는 회원가입 Controller에만 적용합니다.

## 4. 멘토 확인 후 개인 MySQL에 테이블 만들기

1. 위 필드 표와 [CREATE TABLE SQL](sql/001-create-users.sql), `user/User.java`를 함께 리뷰받습니다.
2. 프로젝트 루트에서 기존 `.env`와 개인 MySQL을 준비합니다. 공통 `application.yml`의 `ddl-auto: none`을 유지합니다.
3. MySQL 클라이언트에 본인의 계정으로 접속합니다. 비밀번호는 `-p` 프롬프트에서 입력하며 명령줄·문서에 적지 않습니다.
4. 아래 두 줄을 먼저 실행합니다.

```sql
USE redbeanz;
SHOW TABLES LIKE 'users';
```

5. `users`가 없고 멘토 확인을 받았다면 SQL을 **한 번** 실행합니다. MySQL 클라이언트를 프로젝트 루트에서 시작했다면 다음 상대 경로를 사용할 수 있습니다.

```sql
SOURCE docs/sql/001-create-users.sql;
SHOW CREATE TABLE users;
```

이미 테이블이 있으면 DROP·재생성하지 않고 기존 구조와 검토한 SQL을 비교합니다. 스크립트는 기존 구조 차이를 숨기지 않도록 `IF NOT EXISTS`를 쓰지 않습니다. 애플리케이션 시작이나 테스트가 테이블을 자동 생성하지 않습니다.

## 5. 단계별 실행·검증

모든 명령은 **프로젝트 루트 PowerShell**, **JDK 21**에서 실행합니다. Java 선택 방법은 [로컬 실행 안내](LOCAL_DEVELOPMENT.md)를 참고하세요.

### 5-1. DB 변경 없이 코드·HTTP 규칙 확인

```powershell
.\gradlew.bat --version
.\gradlew.bat classes
.\gradlew.bat test --tests 'com.redbeanz.backend.user.UserServiceTest' --tests 'com.redbeanz.backend.user.UserControllerTest' --tests 'com.redbeanz.backend.security.*'
```

Service 테스트는 저장소 대역을, HTTP 테스트는 Service 대역을 사용합니다. HTTP 테스트에는 실제 Security 필터가 적용됩니다. BCrypt 해시 검증·201/400/409·CSRF·응답의 비밀번호 제외·관리자 권한 방지·동시 중복의 예외 처리 분기를 검사합니다. **이 단계 성공은 실제 MySQL 저장 성공을 뜻하지 않습니다.**

### 5-2. 검토된 테이블을 만든 뒤 MySQL 검증

```powershell
.\gradlew.bat test --tests com.redbeanz.backend.MySqlConnectionTest --rerun-tasks
.\gradlew.bat signupMySqlTest --rerun-tasks
```

`signupMySqlTest`는 `.env`의 실제 DataSource로 Controller → Service → Repository → MySQL을 거칩니다. DB를 H2로 바꾸지 않습니다. 저장 후 영속성 캐시를 비우고 다시 읽어 BCrypt `matches`를 확인합니다. 테스트 데이터는 임의의 식별자를 사용하고 트랜잭션을 롤백합니다. 자동 증가 번호에 빈 번호가 남을 수 있으나 기존 회원은 삭제하지 않습니다. 이 테스트는 테이블이 준비되지 않은 공통 작업을 방해하지 않도록 일반 `test`와 분리했습니다.

### 5-3. 서버 켜기

```powershell
.\gradlew.bat bootRun
```

`Started RedbeanzBackendApplication`이 나오면 실행 중입니다. Gradle 진행률은 서버가 계속 동작하므로 100%가 되지 않아도 정상입니다. 새 PowerShell 창에서 아래 API 검증을 진행합니다.

### 5-4. 직접 HTTP 요청 보내기

아래는 개인 개발 DB에 가상의 예시 회원 **한 명을 실제로 저장**합니다. 이미 같은 예시 회원이 있다면 첫 요청부터 409가 나올 수 있습니다. 실제 사용하는 비밀번호를 예제에 입력하지 마세요.

```powershell
$signupCsrf = Invoke-RestMethod -Uri 'http://localhost:8080/api/users/csrf' -SessionVariable signupSession
$signupHeaders = @{}
$signupHeaders[$signupCsrf.headerName] = $signupCsrf.token
$signupBody = @{
    loginId = 'beanuser'
    email = 'bean@example.com'
    password = 'ExamplePass123!'
    nickname = 'redbean'
} | ConvertTo-Json

try {
    $signupResult = Invoke-WebRequest -UseBasicParsing -Method Post `
        -Uri 'http://localhost:8080/api/users' -WebSession $signupSession `
        -Headers $signupHeaders -ContentType 'application/json; charset=utf-8' `
        -Body ([System.Text.Encoding]::UTF8.GetBytes($signupBody))
    [int]$signupResult.StatusCode
    $signupResult.Content
} catch {
    if ($null -ne $_.Exception.Response) {
        [int]$_.Exception.Response.StatusCode
    } else {
        '서버 기동과 주소를 확인하세요.'
    }
}
```

- 정상: `201`과 비밀번호 없는 응답.
- 같은 요청을 다시 보내면 중복 `409`.
- `$signupBody`의 이메일을 잘못된 형식으로 바꿔 다시 보내면 입력 오류 `400`.
- 토큰이나 세션 쿠키가 없으면 `403`. 토큰부터 다시 받으며 CSRF를 끄지 않습니다.

`GET /api/users`와 `/api/users/{id}`는 구현하지 않았습니다. 가입 성공 응답의 번호로 조회 API가 생기는 것은 아닙니다. 서버를 종료할 때는 서버 창에서 Ctrl+C를 한 번 누릅니다.

## 6. 제출 전 확인

- `ddl-auto: none` 유지, `.env` 미추적·Git 제외 확인.
- 원문 비밀번호와 해시를 로그·캡처·PR에 넣지 않기. 보고서 전체 대신 테스트 개수·성공 요약을 기록하기.
- 실제 결과는 [회원가입 검증 기록](SIGNUP_VERIFICATION.md)에 기록하기.
- [코드 읽는 순서](SIGNUP_CODE_GUIDE.md)로 각 계층의 역할을 설명할 수 있는지 확인하기.
- [PR 본문 초안](SIGNUP_PR_DRAFT.md)을 실제 결과로 고친 뒤 본인이 제출하기. Security PR #11을 기반으로 했으므로 #11 병합 후 최신 dev와 차이를 확인하기. 팀원의 Security 변경을 본인 작업으로 기록하지 않기.
- 아직 리뷰받지 않은 SQL·필드 규칙·보안 경로를 완료 처리하지 않기.
