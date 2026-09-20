# 학생 A 로컬 검증 기록

최종 확인일: 2026-09-21. 학생 A가 본인 Windows PowerShell에서 실행했습니다. GitHub 업로드와 팀원 연락은 하지 않았습니다.

## 실제 결과

| 항목 | 결과와 근거 |
|---|---|
| Java / Gradle | Temurin 21.0.12.1+1 / Wrapper 9.7.1. --version 확인 |
| Spring Boot / SpringDoc | 4.1.1 / 3.1.1 |
| classes | 사용자 PowerShell에서 성공했다고 사용자 확인 |
| MySQL | 8.4.11 클라이언트와 MySQL84 서비스 Running 직접 확인. 서버 SQL 버전은 별도 미수집 |
| redbeanz DB | 사용자가 생성 및 조회 성공 확인 |
| 개인 .env | 사용자가 프로젝트 루트에 작성·저장. 실제 값은 문서에 기록하지 않음 |
| MySqlConnectionTest | JUnit XML 직접 확인: 테스트 1개, failures=0, errors=0 |
| 테스트 메서드 | selectOneReturnsOne(): 애플리케이션 DataSource에서 SELECT 1 결과가 1인지 확인 |
| 테스트 시각 | 최초 2026-09-20 16:06:16 KST, 재검증 2026-09-21 00:34:08 KST |
| 서버 기동 | 사용자 화면에서 Tomcat 8080 및 Started RedbeanzBackendApplication in 6.887 seconds 확인 |
| 서버 종료 | 사용자 화면에서 Ctrl+C 후 PowerShell 입력줄 복귀 확인 |
| .env Git 제외 | ignore 규칙 적용, ls-files 출력 없음 |
| 기존 LICENSE / 협업 템플릿 | 보존 |

테스트 실행에 테스트 코드 컴파일도 포함되어 성공했습니다. bootJar는 아직 별도로 실행하지 않았습니다. IDE는 아직 미설정이며 현재 검증은 터미널 기준입니다.

## 재현 명령

JDK 21을 선택하고 프로젝트 루트에서 실행합니다. 개인 DB 정보는 .env에만 입력합니다.

```powershell
.\gradlew.bat --version
.\gradlew.bat classes
.\gradlew.bat test --tests com.redbeanz.backend.MySqlConnectionTest --rerun-tasks
.\gradlew.bat bootRun
```

bootRun의 80% EXECUTING은 서버 실행 작업이 계속된다는 뜻입니다. 기동 성공은 Started 문구로 판단하고 Ctrl+C로 종료합니다. 종료해도 코드·설정·DB는 삭제되지 않습니다.

## 남은 확인과 제출

- 사용자가 현재 PowerShell의 DB_URL, DB_USERNAME, DB_PASSWORD 환경변수를 제거한 후 루트 .env를 사용해 테스트를 재실행했습니다. 성공 보고를 받았고 갱신된 JUnit XML에서 테스트 1개, 실패 0개를 확인했습니다. IDE 환경변수 등록이나 dotenv 플러그인은 사용하지 않았습니다.
- 사용 IDE를 정하면 Project JDK와 Gradle JVM을 각각 21로 설정합니다.
- 원본 로그·보고서·캡처에는 접속 정보나 개발용 보안 암호가 포함될 수 있으므로 PR에는 실제 값이 없는 성공 요약만 공유합니다.
- 새 commit은 아직 없습니다. 시작 커밋은 0c79797f2e2882be4cb06d9daa21ce74e0510c08입니다. 최종 commit ID는 commit 후 PR에 기록합니다.
- 최초 원격 확인 시 main만 있고 dev는 없었습니다. 제출 직전에 원격 상태를 다시 확인합니다.
- GitHub push·PR·멘토 리뷰·dev 병합은 미수행입니다.
- 학생 B·C의 개인 연결 검증은 각자 수행할 작업입니다.

## 초기 자동 실행 환경의 오류

초기 자동 실행에서는 Java의 Gradle 캐시 JAR 경로 확인 중 AccessDeniedException이 발생했습니다. 짧은 캐시에서도 재현됐으나 이후 사용자의 PowerShell에서 공통 코드를 바꾸지 않고 컴파일·통합 테스트·서버 기동에 성공했습니다. 자동 실행 환경의 접근 오류 원인 자체가 해결됐다는 뜻은 아닙니다.