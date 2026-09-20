# 코드를 확인한 다음 GitHub에 올리기

현재 작업은 로컬 파일 작성 단계입니다. 자동으로 commit·push·PR 생성·이슈 댓글 작성을 하지 않았습니다. 아래 명령은 코드를 이해하고 로컬 MySQL 검증을 끝낸 뒤 직접 실행할 절차입니다.

`commit`은 내 컴퓨터에 변경 기록을 저장하는 작업이고, `push`는 그 기록을 GitHub에 올리는 작업입니다. PR은 팀에 검토와 병합을 요청하는 화면입니다.

## 1. 업로드 전 실제 실행 확인

[LOCAL_DEVELOPMENT.md](LOCAL_DEVELOPMENT.md)를 따라 다음을 확인합니다.

- JDK 21, Gradle 9.7.1 확인.
- `classes` 성공.
- 개인 로컬 MySQL 준비와 `.env` 작성.
- OS·IDE의 같은 이름 DB 환경변수를 제거한 상태에서 `.env` 로딩 확인.
- `MySqlConnectionTest`를 `--rerun-tasks`로 실행해 `SELECT 1` 성공.
- `bootRun`으로 서버 기동 확인.
- 실제 결과와 MySQL 서버 버전을 `VERIFICATION.md` 및 PR 본문에 반영.

실행하지 않은 항목을 성공으로 표시하지 않습니다. IDE 설정을 하지 않았다면 IDE에서 검증했다고 적지 않습니다.

## 2. 원격과 브랜치 확인

프로젝트 루트에서 실행합니다.

```powershell
git remote -v
git branch --show-current
git status --short
git fetch origin
git branch -r
```

origin은 `https://github.com/DKU-RedBeanz/Web-2-Back.git`입니다. 현재 로컬 작업 브랜치는 `codex/initial-backend-setup`입니다. 2026-09-20 최초 확인 시 원격에는 `main`만 있고 `dev`가 없었습니다.

**dev가 없다면 멘토에게 팀의 dev 브랜치를 준비해 달라고 요청합니다.** 임의로 main에 병합하지 않습니다. 본 문서는 멘토에게 자동 연락하지 않습니다.

원격 dev가 준비되었으면 로컬 작업을 아래 절차대로 commit한 다음 `git merge origin/dev`로 최신 dev 내용을 반영할 수 있습니다. 충돌이 생기면 어느 내용을 유지할지 확인하고 해결한 뒤 다시 컴파일·연결을 검증합니다. 강제 push는 필요하지 않습니다. 팀에서 요구하는 브랜치 이름이 다르면 push 전에 로컬 브랜치를 변경합니다.

## 3. 비밀값 제외 확인과 로컬 commit

```powershell
git check-ignore -v .env
git ls-files -- .env
git check-ignore .env.example
```

- 첫 명령: `.gitignore`의 `/.env` 규칙이 나와야 합니다.
- 둘째 명령: 아무것도 나오지 않아야 합니다. `.env`가 나오면 이미 추적 중입니다.
- 셋째 명령: 아무것도 나오지 않아야 합니다. exit code 1은 여기서는 예시 파일이 무시되지 않는다는 뜻입니다.

이미 `.env`를 추적하고 있다면 `git rm --cached -- .env`로 Git 추적만 해제합니다. 파일은 로컬에 남습니다. 이전에 실제 비밀값을 원격에 올린 적이 있다면 ignore 추가만으로 기록에서 사라지지 않으므로 해당 비밀번호를 교체하고 멘토와 기록 정리를 상의합니다.

공유할 파일만 지정해서 준비합니다.

```powershell
git add README.md .gitignore .gitattributes .env.example build.gradle settings.gradle gradlew gradlew.bat gradle src docs
git update-index --chmod=+x gradlew
git diff --cached --stat
git diff --cached
git ls-files -- .env
```

`git update-index --chmod=+x gradlew`는 Windows에서 올린 Wrapper도 macOS·Linux에서 `./gradlew`로 실행할 수 있게 Git에 실행 권한을 기록합니다. `git diff --cached`로 공유할 코드를 직접 확인합니다. `.env`, 개인 접속값, build 폴더, 로그가 포함되면 commit하지 말고 먼저 제외합니다.

검토 후 로컬 기록을 만듭니다.

```powershell
git commit -m "config: 공통 Spring 프로젝트 및 MySQL 연결 검증 구성 (#3)"
```

Git 사용자 이름·메일 오류가 나면 본인의 GitHub 표시 이름과 GitHub에서 제공하는 비공개 noreply 메일 또는 공개해도 되는 본인 메일을 해당 저장소에 설정합니다. 다른 사람의 이름·메일을 대신 넣지 않습니다.

```powershell
git config user.name "본인의 이름"
git config user.email "본인의 GitHub 메일"
```

## 4. GitHub 업로드와 PR

여기부터 원격 저장소에 변경 사항이 올라갑니다. 본인 계정의 저장소 쓰기 권한과 GitHub 로그인이 필요합니다.

```powershell
git push -u origin codex/initial-backend-setup
```

1. GitHub 저장소에서 Compare & pull request를 선택합니다.
2. **base: dev**, compare: `codex/initial-backend-setup`을 선택합니다.
3. 제목 예시: `config: 공통 Spring 프로젝트 및 MySQL 연결 검증 구성 (#3)`.
4. PR 템플릿에 실제 수행 명령과 결과, Java·Gradle·SpringDoc·MySQL 버전을 적습니다.
5. 미확인 사항은 미확인으로 적고 멘토를 Reviewer로 지정합니다.
6. 멘토 리뷰에 따라 수정하고, 승인 뒤 팀 절차에 따라 dev로 병합합니다.

`dev`가 준비되지 않았으면 base를 main으로 바꿔 강행하지 않습니다. 리뷰 전에 자동 병합하거나 이슈를 완료 처리하지 않습니다.

## 5. 병합 후 팀원 연결 지원

팀원들은 병합된 최신 dev에서 각자의 `.env`와 개인 MySQL을 준비하고 동일한 테스트를 실행합니다. 결과 기록 양식은 LOCAL_DEVELOPMENT.md 끝에 있습니다. 김예진·박승민의 성공 여부는 각자가 확인한 결과로 기록합니다.

이슈 #4에는 사용 커밋, 명령, `SELECT 1` 테스트 결과, 남은 질문을 직접 남깁니다. 이 코드 작성만으로 세 사람의 연결 성공이나 멘토 리뷰·병합이 완료된 것은 아닙니다.
