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

## 실행 방법

Spring 기본 프로젝트 업로드 후 실제 실행 명령을 추가할 예정입니다. 개인 로컬 MySQL 준비와 환경변수 설정은 [로컬 개발 환경 및 MySQL 연결](https://github.com/DKU-RedBeanz/Web-2-Back/wiki/Local-Development)을 참고하세요.

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
