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

현재 저장소는 프로젝트 안내와 협업 템플릿을 준비하는 단계입니다. 아래 기술 설정과 의존성은 초기 Spring 프로젝트에 적용할 예정이며, 아직 구현된 기능을 의미하지 않습니다.

## 기술 및 프로젝트 설정

| 항목 | 설정 |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 |
| 빌드 | Gradle · Groovy DSL |
| Group | `com.redbeanz` |
| Artifact | `redbeanz-backend` |
| 기본 패키지 | `com.redbeanz.backend` |
| 패키징 | Jar |
| 설정 형식 | YAML |
| 로컬·테스트용 DB | **각자의 개인 로컬 MySQL** |
| 추후 팀 공유 DB | AWS RDS MySQL |

Gradle·MySQL의 세부 버전은 초기 프로젝트 구성 시 확정합니다.

### 구성 예정 의존성

- Spring Web
- Lombok
- Validation
- Spring Data JPA
- MySQL Driver
- Spring Boot DevTools
- SpringDoc OpenAPI
- Spring Security

의존성 추가와 기능 구현은 구분합니다. 특히 인증·인가 구현은 이번 주 과제에 포함하지 않습니다.

## 이번 주 과제 — 기본 프로젝트 및 로컬 MySQL 연결

로컬 개발과 테스트 모두 각자의 개인 로컬 MySQL을 사용합니다. **이번 주에는 DB 연결 확인까지만 진행합니다.**

| 담당 | 작업 |
|---|---|
| 학생 A — 초기 프로젝트 담당 | Spring 기본 프로젝트와 MySQL 연결 설정 구현, 설정 방법 문서화 후 PR 제출 |
| 학생 B·C | A의 코드 병합 후 Pull → 개인 로컬 MySQL 준비 → 환경변수 등록 → 연결 확인 |
| 멘토 | 이슈 배정, PR 리뷰·병합, 세 사람의 연결 결과 확인 |

학생 B·C는 A의 PR 병합 전에도 개인 MySQL과 빈 프로젝트 DB를 준비할 수 있습니다. 연결 확인은 병합된 동일한 코드로 진행합니다.

### 공통 연결 설정

학생 A는 다음 환경변수를 읽도록 설정하고, 다른 팀원은 개인 환경에 맞는 값만 등록합니다.

| 환경변수 | 의미 | 예시 |
|---|---|---|
| `DB_URL` | 개인 로컬 MySQL JDBC URL | `jdbc:mysql://localhost:3306/redbeanz` |
| `DB_USERNAME` | 개인 로컬 DB 계정 | `<개인 DB 계정>` |
| `DB_PASSWORD` | 개인 로컬 DB 비밀번호 | `<개인 DB 비밀번호>` |

설정 예시이며 실제 설정 파일은 초기 프로젝트 PR에서 추가합니다.

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: none
```

- 실제 값은 각자의 IntelliJ 실행 설정에 있는 Environment variables에 등록합니다.
- 저장소에는 환경변수 참조와 예시만 작성합니다.
- 개인 접속 정보를 코드·문서·로그·PR 캡처에 포함하지 않습니다.
- 공통 설정 파일을 개인 접속 정보로 수정하지 않습니다.
- 자동 테이블 생성은 `spring.jpa.hibernate.ddl-auto=none`으로 끕니다.

### 완료 기준

- [ ] 학생 A가 공통 프로젝트와 환경변수 기반 MySQL 설정을 PR로 제출했습니다.
- [ ] 멘토 리뷰 후 코드가 병합되었습니다.
- [ ] 세 사람 모두 동일한 코드로 각자의 개인 로컬 MySQL에서 `SELECT 1` 실행에 성공했습니다.
- [ ] 다른 팀원이 재현할 수 있는 설정·연결 확인 방법을 문서화했습니다.
- [ ] 실제 접속 정보가 커밋이나 제출 자료에 포함되지 않았습니다.

서버 실행 로그만으로 DB 연결 성공을 판단하지 않습니다. 애플리케이션의 DB 연결을 통해 `SELECT 1`을 실행한 결과로 확인합니다.

### 이번 주 제외 범위

ERD 설계, Entity 작성, 테이블 생성, CRUD, 인증 구현, AWS RDS 연동은 이번 주 과제에 포함하지 않습니다.

### 다음 주 회의

ERD 설계 및 AWS RDS MySQL 연동을 진행합니다. 로컬·테스트용 개인 MySQL과 추후 팀 공유 RDS의 용도를 구분하여 운영합니다.

## 실행 방법

Spring 기본 프로젝트 업로드 후 추가 예정입니다. 학생 A의 초기 코드 PR에 MySQL 준비, 빈 DB 생성, 환경변수 등록, 서버 실행, 연결 확인 방법을 함께 작성합니다.

## 협업 방법

**이슈 생성 → 멘토 Assignee 지정 → 작업 브랜치 생성 → PR 제출 → 멘토 리뷰 → 수정 반영 → 병합**

1. 이슈에 작업 범위와 완료 기준을 작성하고 멘토가 담당자를 지정합니다.
2. 담당자는 선행 작업의 병합을 확인한 뒤 최신 개발 코드에서 작업 브랜치를 생성합니다.
3. 구현 및 확인 결과를 PR에 작성하고 멘토를 Reviewer로 지정합니다.
4. 같은 브랜치에서 리뷰를 반영하고 재리뷰를 요청합니다.
5. 멘토가 완료 기준과 수정 내용을 확인한 뒤 병합합니다.

### 브랜치 및 병합

| 브랜치 | 용도 | PR 및 병합 |
|---|---|---|
| `main` | 검증된 코드, 기본 브랜치 | `dev → main`: Create a merge commit |
| `dev` | 개발 코드 통합 — 초기 문서 병합 후 생성 예정 | 작업 브랜치 → `dev`: Squash and merge |
| 작업 브랜치 | 이슈별 작업 | `type/이슈번호-짧은-영문설명` |

초기 문서 PR은 `docs/initial-project-docs → main`으로 제출하고 Squash Merge합니다. 이후 학생들의 작업은 `dev` 생성 후 진행하며, `main`·`dev`에 직접 Push하지 않습니다.

### 제목 규칙

- 이슈: `[type] 작업 제목`
- 커밋: `type: 작업 내용`
- PR: `type: 작업 내용 (#이슈번호)`
- 타입: `feat`, `fix`, `docs`, `config`, `refactor`, `test`, `style`, `chore`

실제 이슈가 없는 초기 문서 PR에는 이슈 번호를 임의로 넣지 않습니다. 일반 개발 PR은 `Related to #이슈번호`로 참조하고, 멘토가 병합 후 완료 조건을 확인하여 이슈를 종료합니다.

[Issue 템플릿](.github/ISSUE_TEMPLATE/task.md) · [PR 템플릿](.github/pull_request_template.md)
