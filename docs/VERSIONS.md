# 버전과 구성

확인일: 2026-09-20. 다른 Boot 버전의 참고 프로젝트를 복사하지 않고 Spring Initializr에서 Boot 4.1.1 프로젝트를 생성한 뒤 공통 설정을 추가했습니다.

| 항목 | 고정값 / 관리 방식 | 근거 |
|---|---|---|
| Java | 21 | Gradle Java toolchain에 21 지정. Gradle 실행 JVM은 JAVA_HOME 또는 IDE에서 별도로 21 선택 |
| Spring Boot | 4.1.1 | 요구 버전 |
| Gradle Wrapper | 9.7.1 | Boot 4.1.1용 Spring Initializr가 생성한 버전. 배포 ZIP SHA-256도 Wrapper 설정에 고정 |
| Dependency management plugin | 1.1.7 | 해당 Initializr의 생성 결과 |
| SpringDoc | 3.1.1 | 공식 3.x는 Boot 4 지원. 3.1.1 POM의 Boot parent는 4.1.0 |
| Web / Validation / JPA / Security / DevTools | Boot 4.1.1 BOM 관리 | 개별 버전을 임의로 덮어쓰지 않음 |
| Lombok / MySQL Connector/J / 테스트 라이브러리 | Boot 4.1.1 BOM 관리 | 버전 충돌 방지를 위해 BOM 사용 |
| 개인 MySQL | MySQL 8.4 설치 및 연결 검증. 클라이언트 8.4.11 확인 | 서버 SQL 버전은 별도 미수집. JDBC 드라이버 버전과 구분 |

Boot 4.1.1 공식 요구사항은 Java 17 이상, Gradle 8.14 이상인 8.x 또는 9.x입니다. 선택한 Java 21과 Gradle 9.7.1은 이 범위에 들어갑니다. SpringDoc의 선언상 호환성과 실제 DB·HTTP 실행 검증은 구분하며, 실행 결과는 VERIFICATION.md에 기록합니다.

## 의존성이 하는 일

| 의존성 | 역할 |
|---|---|
| `spring-boot-starter-webmvc` | Spring MVC와 내장 Tomcat으로 HTTP 서버 구성. Boot 4 생성기의 Spring Web 선택 결과 |
| `spring-boot-starter-validation` | 향후 요청 값의 필수값·길이 등 검증 |
| `spring-boot-starter-data-jpa` | JPA, Hibernate, JDBC 및 DB 연결 풀 구성 |
| `spring-boot-starter-security` | 기본 보안 구성. 로그인·인가 기능 개발은 별도 작업 |
| `springdoc-openapi-starter-webmvc-ui:3.1.1` | OpenAPI 명세·Swagger UI 지원. 현재 기본 보안의 적용 대상 |
| `lombok` | 반복 Java 코드 생성. compileOnly와 annotationProcessor 모두 등록 |
| `spring-boot-devtools` | 로컬 개발 편의 기능. developmentOnly로 등록 |
| `mysql-connector-j` | Java에서 실제 MySQL로 접속하는 JDBC 드라이버 |
| `spring-boot-starter-test` | JUnit, AssertJ, Spring Boot 통합 테스트 지원 |
| `junit-platform-launcher` | Gradle에서 JUnit 테스트 실행 |

## 공식 근거

- [Spring Initializr](https://start.spring.io/)
- [Spring Boot 4.1 시스템 요구사항](https://docs.spring.io/spring-boot/4.1/system-requirements.html)
- [SpringDoc 공식 문서와 호환성 안내](https://springdoc.org/)
- [SpringDoc v3.1.1 POM](https://github.com/springdoc/springdoc-openapi/blob/v3.1.1/pom.xml)
- [Spring Boot 외부 설정·우선순위·확장자 힌트](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html)

팀원이 실제로 해석된 의존성 버전을 확인하려면 프로젝트 루트에서 다음을 실행합니다.

```powershell
.\gradlew.bat dependencies --configuration runtimeClasspath
.\gradlew.bat dependencyInsight --dependency springdoc-openapi --configuration runtimeClasspath
.\gradlew.bat dependencyInsight --dependency mysql-connector-j --configuration runtimeClasspath
```
