# 레드빈즈 웹개발 프로젝트 2팀

학교 주변 맛집/식당 리뷰 및 평점 사이트를 만드는 백엔드 프로젝트입니다.

## 프로젝트 정보

| 항목 | 내용 |
|---|---|
| 프로젝트명 | 레드빈즈 웹개발 프로젝트 2팀 |
| 핵심 영역 | 학교, 식당, 리뷰·평점, 사용자 |
| 개발 상태 | 초기 구성 중 |
| 서비스 버전 | 미릴리스 |
| 저장소 | [DKU-RedBeanz/Web-2-Back](https://github.com/DKU-RedBeanz/Web-2-Back) |
| 라이선스 | [MIT](LICENSE) |

공통 Spring 시작 코드와 개인 로컬 MySQL 연결 검증 테스트를 제공합니다. 도메인 기능과 로그인 기능은 아직 구현하지 않았습니다.

## 기술 및 프로젝트 설정

| 항목 | 설정 |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 |
| Gradle Wrapper | 9.7.1 |
| SpringDoc OpenAPI | 3.1.1 |
| 빌드 | Gradle · Groovy DSL |
| Group | `com.redbeanz` |
| Artifact | `redbeanz-backend` |
| 기본 패키지 | `com.redbeanz.backend` |
| 패키징 | Jar |
| 설정 형식 | YAML |
| 로컬·테스트용 DB | **각자의 개인 로컬 MySQL** |
| 추후 팀 공유 DB | AWS RDS MySQL |

의존성 선택 근거는 [버전과 구성](docs/VERSIONS.md)에 기록합니다. 학생 A의 개인 MySQL 연결 테스트와 서버 기동을 확인했습니다. 확인 근거와 남은 항목은 [검증 기록](docs/VERIFICATION.md)에 구분합니다.

### 구성한 의존성

- Spring Web
- Lombok
- Validation
- Spring Data JPA
- MySQL Driver
- Spring Boot DevTools
- SpringDoc OpenAPI
- Spring Security

의존성 추가와 기능 구현은 구분합니다. 특히 인증·인가 구현은 이번 주 과제에 포함하지 않습니다.

## 실행 방법

처음 실행한다면 [초보자용 로컬 실행 안내](docs/LOCAL_DEVELOPMENT.md)를 순서대로 따라 하세요. 기존 [로컬 개발 환경 및 MySQL 연결 위키](https://github.com/DKU-RedBeanz/Web-2-Back/wiki/Local-Development)도 함께 참고할 수 있습니다.

1. JDK 21을 준비하고 IDE의 Project JDK와 Gradle JVM을 모두 21로 선택합니다.
2. 이 저장소의 루트 폴더(`build.gradle`이 있는 위치)를 IDE에서 엽니다.
3. 개인 MySQL 서버를 켜고 빈 데이터베이스 `redbeanz`를 만듭니다.
4. `.env.example`을 `.env`로 복사하고 개인 DB 값을 입력합니다. `.env`는 공유하지 않습니다.
5. 아래 명령을 프로젝트 루트에서 실행합니다. IDE의 애플리케이션·테스트 작업 디렉터리도 루트로 맞춥니다.

Windows PowerShell:

```powershell
Copy-Item .env.example .env  # 최초 한 번만: 기존 .env를 덮어쓰지 마세요.
# .env를 편집한 뒤 실행
.\gradlew.bat --version
.\gradlew.bat classes
.\gradlew.bat test --tests com.redbeanz.backend.MySqlConnectionTest --rerun-tasks
.\gradlew.bat bootRun
```

macOS / Linux:

```bash
cp -n .env.example .env
./gradlew --version
./gradlew classes
./gradlew test --tests com.redbeanz.backend.MySqlConnectionTest --rerun-tasks
./gradlew bootRun
```

`classes`는 컴파일만 확인하므로 DB 없이도 실행할 수 있습니다. `test`와 `bootRun`은 개인 MySQL과 실제 설정이 필요합니다. 테스트의 `selectOneReturnsOne() PASSED`가 애플리케이션 DataSource로 `SELECT 1`을 실행하여 `1`을 받았다는 뜻입니다. 서버 기동 성공만으로 이 테스트를 대체하지 않습니다.

Spring Boot가 `.env`를 Java properties 형식으로 직접 읽습니다. IDE용 dotenv 플러그인은 필요 없습니다. `export`나 값을 감싸는 따옴표를 쓰지 않고, 역슬래시는 `\\`로 적습니다. 같은 이름의 OS·IDE 환경변수가 있으면 파일보다 우선합니다. `optional:`은 파일 누락만 허용하며 DB 값 자체를 선택 사항으로 만들지 않습니다.

Spring Security의 기본 보안 동작을 유지합니다. 브라우저에서 로그인 화면이나 401 응답이 보일 수 있습니다. 이는 DB 연결 실패를 의미하지 않습니다. 인증·인가 기능이나 DB 확인용 공개 API는 추가하지 않았습니다.

### 파일 구조

```text
Web-2-Back/
├── build.gradle                 # Java·의존성·실행/테스트 설정
├── settings.gradle              # 프로젝트 이름
├── gradlew / gradlew.bat         # 공통 Gradle 실행기
├── gradle/wrapper/               # Wrapper JAR 및 버전·체크섬
├── .env.example                 # 공유하는 빈 설정 양식(예시 값)
├── .env                         # 각자의 실제 값: Git 제외
├── src/main/java/com/redbeanz/backend/RedbeanzBackendApplication.java
├── src/main/resources/application.yml
├── src/test/java/com/redbeanz/backend/MySqlConnectionTest.java
└── docs/                        # 실행 안내·버전·검증 기록
```

현재 실제 수행한 확인과 미실행 항목은 [검증 기록](docs/VERIFICATION.md)에 구분합니다. GitHub 업로드와 PR 절차는 [업로드 안내](docs/GITHUB_UPLOAD.md)를 참고하세요.

## 현재 과제 — Week 2

**기본 Spring 프로젝트 구성과 각자의 개인 로컬 MySQL 연결 확인까지만 진행합니다.** 학생 A가 공통 코드를 작성하고, 학생 B·C는 병합된 코드를 받아 같은 설정 방식으로 연결을 재현합니다.

완료 기준은 세 사람 모두 애플리케이션을 통해 `SELECT 1` 실행에 성공하는 것입니다. 역할별 작업·제출물·제외 범위는 [Week 2 과제](https://github.com/DKU-RedBeanz/Web-2-Back/wiki/Week-2)에서 확인하세요. 날짜와 제출 기한은 미정입니다.

## 팀 위키

과제와 협업 규칙의 상세 내용은 [위키](https://github.com/DKU-RedBeanz/Web-2-Back/wiki)에서 관리합니다.

| 분류 | 문서 |
|---|---|
| [협업 가이드](https://github.com/DKU-RedBeanz/Web-2-Back/wiki#협업-가이드) | [Git 컨벤션](https://github.com/DKU-RedBeanz/Web-2-Back/wiki/Git-Convention) · [작업 및 코드 리뷰 절차](https://github.com/DKU-RedBeanz/Web-2-Back/wiki/Work-and-Review) |
| [과제](https://github.com/DKU-RedBeanz/Web-2-Back/wiki/Assignments) | [주차별 과제 목록](https://github.com/DKU-RedBeanz/Web-2-Back/wiki/Assignments) · [Week 2](https://github.com/DKU-RedBeanz/Web-2-Back/wiki/Week-2) |
| [기타](https://github.com/DKU-RedBeanz/Web-2-Back/wiki#기타) | [TO-DO](https://github.com/DKU-RedBeanz/Web-2-Back/wiki/TODO) · [로컬 환경](https://github.com/DKU-RedBeanz/Web-2-Back/wiki/Local-Development) · [서버 아키텍처](https://github.com/DKU-RedBeanz/Web-2-Back/wiki/Architecture) · [ERD·RDS 설계](https://github.com/DKU-RedBeanz/Web-2-Back/wiki/ERD-and-RDS) |

[Issue 템플릿](.github/ISSUE_TEMPLATE/task.md) · [PR 템플릿](.github/pull_request_template.md)
