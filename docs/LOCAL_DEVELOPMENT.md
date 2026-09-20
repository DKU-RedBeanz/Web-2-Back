# 처음부터 실행하기

이 문서의 명령은 `build.gradle`과 `gradlew.bat`이 있는 **프로젝트 루트 폴더**에서 실행합니다. Windows는 PowerShell 기준입니다. 실제 접속 정보는 각자의 `.env`에만 넣습니다.

## 1. 프로젝트와 Java 21 준비

JDK는 Java 코드를 컴파일하고 실행하는 도구입니다. Gradle은 필요한 라이브러리를 내려받고 컴파일·테스트를 수행합니다. 저장소에 Wrapper가 있으므로 Gradle을 따로 설치할 필요가 없습니다.

1. [Eclipse Temurin](https://adoptium.net/temurin/releases/?version=21)에서 자신의 OS에 맞는 **JDK 21**을 설치합니다. JRE만 설치하지 않습니다.
2. IDE에서 저장소 폴더를 엽니다. `src` 폴더만 열지 않습니다.
3. 새 터미널에서 `java -version`과 `javac -version`을 실행해 둘 다 21인지 확인합니다.

Java가 인식되지 않거나 다른 버전이면 PowerShell의 현재 창에 다음을 설정합니다. 경로는 자신의 설치 폴더로 바꿉니다.

```powershell
$env:JAVA_HOME = 'C:\실제\JDK21설치폴더'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
javac -version
.\gradlew.bat --version
```

`JAVA_HOME`은 `bin`의 상위 JDK 폴더입니다. 위 설정은 현재 터미널에서만 유효합니다. 매번 설정하기 싫다면 Windows의 사용자 환경변수에서 JAVA_HOME과 Path를 설정하고 IDE·터미널을 다시 엽니다. DB 접속 정보는 Windows 환경변수에 등록할 필요가 없습니다.

`gradlew --version`의 Gradle 버전은 **9.7.1**, Launcher JVM과 Daemon JVM은 **21**이어야 합니다. Gradle toolchain 21 설정만으로 IDE의 Gradle JVM까지 자동 변경되지는 않습니다.

macOS에서는 설치한 JDK를 선택한 뒤 `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`을 사용할 수 있습니다. Linux도 JAVA_HOME을 설치한 JDK 21 폴더로 설정합니다. 이 `export`는 셸의 Java 설정 예시이며, `.env` 안에 적는 문법이 아닙니다.

## 2. IDE 설정

처음 쓰는 IDE가 정해지지 않았다면 터미널만으로도 모든 명령을 실행할 수 있습니다.

### IntelliJ IDEA

1. Open으로 `build.gradle`이 있는 프로젝트 폴더를 엽니다. Gradle 프로젝트로 가져옵니다.
2. File → Project Structure → Project에서 Project SDK를 JDK 21, Language level을 21로 선택합니다.
3. Settings → Build, Execution, Deployment → Build Tools → Gradle에서 Gradle distribution은 Wrapper, Gradle JVM은 JDK 21로 선택합니다.
4. Build and run using / Run tests using을 Gradle로 맞추면 저장소의 실행 설정을 공유할 수 있습니다.
5. Run → Edit Configurations에서 애플리케이션과 테스트의 Working directory를 프로젝트 루트로 지정합니다. `$PROJECT_DIR$`가 이 폴더를 가리키는지 확인합니다.
6. 같은 실행 구성의 Environment variables에 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`가 중복 등록되어 있지 않은지 확인합니다.
7. Lombok 사용 시 IDE의 annotation processing을 활성화합니다. `.env`용 플러그인은 필요 없습니다.

### VS Code / Eclipse

Java 프로젝트와 Gradle 실행에 쓰는 JDK를 각각 21로 맞추고, 애플리케이션·테스트의 실행 작업 디렉터리를 루트로 선택합니다. IDE 버전에 따라 메뉴 이름이 다르므로 처음에는 루트 터미널에서 Wrapper로 실행하면 동일한 조건을 재현하기 쉽습니다.

Gradle의 `bootRun`과 `test`에는 이미 프로젝트 루트 작업 디렉터리를 지정했습니다. IDE에서 Gradle을 거치지 않고 Java를 직접 실행하면 IDE의 작업 디렉터리 설정도 필요합니다.

## 3. 개인 MySQL 준비

MySQL Workbench는 DB 관리 도구이고 MySQL Server가 실제 서버입니다. Workbench만 설치해서는 서버가 실행되지 않습니다.

1. [공식 MySQL Community Server 다운로드](https://dev.mysql.com/downloads/mysql/)에서 OS에 맞는 서버를 설치합니다. 팀에서는 LTS 계열을 맞춰 쓰고 실제 설치 버전을 결과에 적습니다.
2. 설치 과정에서 정한 자신의 계정·비밀번호를 안전하게 보관합니다. 이 문서에 비밀번호를 적지 않습니다.
3. MySQL 서버를 실행하고 개인 포트를 확인합니다. 기본값은 3306입니다.
4. Workbench나 MySQL 클라이언트로 접속해 아래 SQL을 실행합니다.

```sql
CREATE DATABASE IF NOT EXISTS redbeanz CHARACTER SET utf8mb4;
SELECT VERSION();
```

애플리케이션에서 사용할 개인 계정에 `redbeanz` 접속 권한이 있어야 합니다. 계정·권한 설정은 자신의 서버에서 수행합니다. 이 단계에서는 도메인 테이블을 만들지 않습니다. 기존 DB를 삭제하거나 비우는 명령도 필요하지 않습니다. 이미 사용 중인 같은 이름의 DB가 있으면 내용부터 확인합니다.

여기서 DB 클라이언트로 연결에 성공한 것은 서버 준비 확인일 뿐입니다. 최종 검증은 아래 Java 테스트로 수행합니다.

## 4. 나만의 .env 작성

최초 한 번만 실행합니다. 기존 파일이 있으면 복사하지 않고 그 파일을 편집합니다.

```powershell
Copy-Item .env.example .env
```

`.env`를 편집기로 열어 세 값을 자신의 값으로 바꿉니다. 아래 계정·비밀번호는 실제 값이 아닌 자리표시자입니다.

```properties
DB_URL=jdbc:mysql://localhost:3306/redbeanz
DB_USERNAME=your_local_username
DB_PASSWORD=your_local_password
```

- `.env.txt`가 아니라 정확히 `.env`여야 합니다. Windows 탐색기의 파일 확장명 표시를 켜면 확인하기 쉽습니다.
- 포트가 3306이 아니면 URL의 포트도 수정합니다.
- 이 파일은 Spring Boot가 읽는 **Java properties 설정**입니다. 셸 스크립트가 아닙니다.
- `export DB_USERNAME=...`처럼 쓰지 않고 값을 따옴표로 감싸지 않습니다. 따옴표를 쓰면 실제 값의 일부가 됩니다.
- 값에 실제 역슬래시가 하나 있으면 파일에는 `\\` 두 개로 적습니다. `\t`나 `\n`은 properties에서 특수 문자로 해석될 수 있습니다.
- 값 뒤에 같은 줄 주석을 덧붙이지 않습니다. 주석은 별도 줄에 `#`로 시작합니다.
- 비 ASCII 문자가 들어가는 properties 값은 필요하면 `\uXXXX` 이스케이프를 사용합니다.
- 공통 `application.yml`을 개인 접속 정보로 수정하지 않습니다.

Spring Boot는 다음 설정으로 파일을 직접 읽습니다.

```yaml
spring:
  config:
    import: "optional:file:./.env[.properties]"
```

`./`는 실행 작업 디렉터리입니다. `[.properties]`는 확장자가 없는 `.env`를 properties로 해석하라는 힌트입니다. `optional:`은 파일이 없어도 가져오기 단계에서 즉시 실패하지 않게 할 뿐입니다. 파일과 환경변수 어디에도 필수 DB 값이 없으면 DB 연결은 실패합니다.

### 중복 환경변수 확인

OS·IDE 환경변수는 `.env`보다 우선합니다. 값은 출력하지 않고 변수 이름만 확인할 수 있습니다.

```powershell
Get-ChildItem Env:DB_* | Select-Object -ExpandProperty Name
```

`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`가 나오면 어디서 설정했는지 확인합니다. `.env`로만 검증하려면 현재 터미널의 해당 변수들을 지우고 IDE 실행 구성에도 중복값이 없는지 확인합니다.

```powershell
Remove-Item Env:DB_URL,Env:DB_USERNAME,Env:DB_PASSWORD -ErrorAction SilentlyContinue
```

이 명령은 현재 PowerShell의 변수만 지웁니다. 사용자·시스템에 저장한 환경변수는 새 터미널에 다시 나타날 수 있으므로 Windows 환경변수 설정도 확인합니다. 별도로 `SPRING_DATASOURCE_*`, JVM 옵션이나 실행 인자에 접속 정보를 지정했다면 그것도 제거한 상태에서 검증합니다.

## 5. 컴파일과 실제 DB 연결 검증

프로젝트 루트에서 순서대로 실행합니다.

```powershell
.\gradlew.bat --version
.\gradlew.bat classes
.\gradlew.bat test --tests com.redbeanz.backend.MySqlConnectionTest --rerun-tasks
```

macOS / Linux에서는 `.\gradlew.bat` 대신 `./gradlew`를 사용합니다. 처음에는 라이브러리를 내려받느라 시간이 걸립니다.

- `--version`: Gradle과 Gradle 실행 Java 확인.
- `classes`: 메인 코드 컴파일. 이 명령만 성공했다고 DB 연결 성공은 아닙니다.
- `test`: 애플리케이션 설정으로 만든 DataSource를 사용해 `SELECT 1` 실행.
- `--rerun-tasks`: 이전 테스트 결과를 재사용하지 않고 다시 연결하게 함.

성공 기준은 `MySqlConnectionTest > selectOneReturnsOne() PASSED`와 `BUILD SUCCESSFUL`입니다. 테스트는 결과가 정수 1인지 검증합니다. 가짜 DB나 H2로 교체하지 않으며 DB 자동 설정도 끄지 않습니다. 테스트는 HTTP 서버 없이 수행하고 별도의 검증용 공개 API를 만들지 않습니다.

테스트 보고서는 로컬 `build/reports/tests/test/index.html`에 생성됩니다. 실패 로그에는 사용자명·주소 등이 포함될 수 있으므로 원문 보고서·로그·화면을 그대로 공유하지 말고, 실제 값이 없는 성공 요약 또는 가린 오류만 공유합니다. `--debug`로 상세 접속 정보를 수집할 필요가 없습니다.

## 6. 서버 실행과 종료

```powershell
.\gradlew.bat bootRun
```

`Started RedbeanzBackendApplication`이 보이고 프로세스가 계속 실행 중이면 기동된 것입니다. 기본 포트는 8080입니다. 종료할 때는 해당 터미널에서 Ctrl+C를 누릅니다.

Spring Security 기본 설정으로 로그인 화면 또는 401 응답이 나올 수 있습니다. 기본 사용자 암호가 실행 로그에 생성될 수 있으므로 로그 전체를 공유하지 않습니다. 기본 보안을 끄거나 모든 요청을 허용하는 설정을 임의로 추가하지 않았습니다. 로그인 기능 구현은 이번 작업 범위 밖입니다.

SpringDoc 기본 경로는 `/swagger-ui.html`, `/v3/api-docs`이며 현재는 기본 보안 적용 대상입니다. 아직 도메인 컨트롤러가 없으므로 문서화할 업무 API도 없습니다.

Jar 생성은 `./gradlew bootJar`(Windows ` .\gradlew.bat bootJar`)로 합니다. 생성된 Jar를 실행할 때도 프로젝트 루트에서 `java -jar build/libs/redbeanz-backend-0.0.1-SNAPSHOT.jar`를 사용해야 루트 `.env`를 읽습니다.

## 7. 자주 막히는 부분

| 현상 | 확인할 것 |
|---|---|
| java를 찾을 수 없음 | JDK 설치, JAVA_HOME, Path, 새 터미널 |
| Gradle JVM이 21이 아님 | 터미널 JAVA_HOME과 IDE Gradle JVM을 각각 확인 |
| Wrapper 첫 실행 실패 | 인터넷·프록시, Gradle ZIP 다운로드 가능 여부. Wrapper 파일을 임의 버전으로 교체하지 않음 |
| Windows 캐시 파일 AccessDeniedException | 다른 Gradle 실행이 파일을 점유하는지 확인하고, 캐시 경로가 지나치게 길면 개인 GRADLE_USER_HOME을 짧은 쓰기 가능한 폴더로 설정해 재시도 |
| DB_URL을 찾지 못함 | `.env` 이름·위치, 실행 작업 디렉터리, YAML import |
| Access denied | 개인 계정·비밀번호·접속 권한, 불필요한 따옴표, 역슬래시 이스케이프 |
| Communications link failure / Connection refused | MySQL 서버 상태, URL의 호스트와 포트 |
| Unknown database | 빈 `redbeanz` DB 생성 여부 |
| Public Key Retrieval is not allowed | MySQL 인증 방식·TLS 설정을 확인. 보안 관련 URL 옵션을 원인 확인 없이 공통 설정에 추가하지 않음 |
| 값을 바꿔도 이전 설정 사용 | 중복 OS·IDE 환경변수, 기존 실행 프로세스, 테스트 재실행 여부 |
| 8080 포트 사용 중 | 먼저 실행한 서버 종료 또는 개인 실행 인자에서 포트 변경 |
| 테스트가 UP-TO-DATE | `--rerun-tasks`로 실제 DB 연결 재검증 |

## 8. 팀원 결과 기록 양식

다음 내용을 본인이 검증한 뒤 이슈 #4에 직접 남깁니다. 김예진·박승민의 결과는 각자의 컴퓨터에서 확인해야 하며 대신 성공했다고 기록하지 않습니다.

```text
이름:
사용한 커밋: (git rev-parse HEAD)
OS / JDK / Gradle / MySQL 버전:
실행 위치: 프로젝트 루트
설정 방식: .env, DB_* OS·IDE 중복 없음
실행 명령: gradlew test --tests com.redbeanz.backend.MySqlConnectionTest --rerun-tasks
결과: selectOneReturnsOne PASSED / 실패(실제 값을 가린 요약)
서버 기동 확인:
남은 질문:
```

멘토 리뷰·dev 병합 후 팀원들은 같은 dev 커밋을 받아 이 절차를 재현합니다. 코드 변경이 없으면 빈 PR은 만들지 않습니다. 세 사람의 실제 결과를 모두 확인하기 전에는 이슈 #4 완료로 처리하지 않습니다.
