# 회원가입 코드 읽는 순서

이번 작업은 회원가입만입니다. 아래 경로는 `src/main/java/com/redbeanz/backend/` 기준입니다. 파일 이름을 IDE에서 찾아 전체 코드를 읽을 수 있습니다.

## 1. 요청 DTO — 사용자가 보내는 입력 양식

`user/dto/SignupRequest.java`는 `loginId`, `email`, `password`, `nickname` 네 가지를 받습니다. `@NotBlank`, `@Email`, `@Size`, `@Pattern`은 필수값·형식·길이를 검사합니다. 비밀번호는 UTF-8 바이트 제한도 검사합니다. `role`, `id`, `passwordHash`를 입력으로 받지 않습니다.

DTO는 Data Transfer Object, 즉 데이터를 전달하는 객체입니다. 요청 DTO에 원문 비밀번호가 일시적으로 들어갈 수는 있지만 DB에 그대로 저장하거나 응답으로 돌려주면 안 됩니다. `toString()`도 입력 전체를 가리도록 작성했습니다.

## 2. Controller — HTTP 요청을 받는 입구

`user/UserController.java`의 핵심입니다.

```java
@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
@ResponseStatus(HttpStatus.CREATED)
public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
    return userService.signup(request);
}
```

- `@RequestBody`: JSON을 요청 DTO로 읽습니다.
- `@Valid`: DTO 규칙에 맞는지 확인합니다. 틀리면 Service로 넘어가기 전에 400입니다.
- `userService.signup`: 실제 가입 처리를 맡깁니다.
- `CREATED`: 성공했을 때 HTTP 상태 201을 보냅니다.

Controller는 SQL을 직접 실행하지 않습니다.

## 3. Service — 회원가입 순서를 정하는 곳

`user/UserService.java`는 다음 순서로 일합니다.

1. 아이디·이메일을 소문자로 통일합니다.
2. Repository에 중복 여부를 물어봅니다.
3. 주입받은 PasswordEncoder로 원문 비밀번호를 해시로 만듭니다.
4. `User.register()`로 일반 회원을 만듭니다.
5. Repository로 저장하고 응답 DTO를 반환합니다.

```java
User user = User.register(loginId, email,
    passwordEncoder.encode(request.password()), request.nickname());
return SignupResponse.from(userRepository.saveAndFlush(user));
```

`@Transactional`은 가입 처리가 실패하면 DB 변경을 되돌릴 수 있도록 작업 범위를 묶습니다. `saveAndFlush`는 이 시점에 SQL을 실행하게 합니다. 먼저 중복을 검사했어도 두 요청이 동시에 들어올 수 있어, DB UNIQUE 위반도 가입 중복으로 처리합니다.

## 4. Repository — DB 작업을 요청하는 곳

`user/UserRepository.java`입니다.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
}
```

Spring Data JPA가 구현체를 만들어 줍니다. `User`를 다루고 기본 키는 `Long`입니다. 메서드 이름을 바탕으로 아이디와 이메일 존재 여부를 조회합니다.

## 5. Entity — DB의 한 회원을 Java로 표현

`user/User.java`의 `@Entity`와 `@Table(name = "users")`는 Java 객체와 MySQL `users` 테이블을 연결합니다. `@Column`은 컬럼 이름·길이 등을 맞춥니다. 테이블 생성 SQL은 `docs/sql/001-create-users.sql`입니다.

```java
user.passwordHash = passwordHash;
user.role = UserRole.USER;
```

Entity에는 해시만 저장합니다. 일반 회원을 생성하는 메서드는 권한을 인자로 받지 않아 요청자가 관리자 권한을 고를 수 없습니다. `ddl-auto: none`이므로 Entity를 작성했다고 MySQL 테이블이 자동으로 생기지 않습니다.

## 6. 응답 DTO — 밖으로 내보낼 정보만 선택

`user/dto/SignupResponse.java`는 다음 필드만 가지고 있습니다.

```java
public record SignupResponse(
    Long id, String loginId, String email, String nickname, UserRole role
) { /* Entity에서 허용된 필드만 옮깁니다. */ }
```

비밀번호와 해시가 없습니다. Entity를 그대로 응답하면 해시까지 노출할 수 있으므로 응답 DTO를 따로 만듭니다.

## 7. 오류와 보안 연결

- `user/SignupExceptionHandler.java`: 이 Controller에서 생긴 입력 오류 400, 중복 409, DB 오류의 안전한 응답을 처리합니다. 입력값과 SQL·예외 원문은 반환하지 않습니다.
- `user/DuplicateUserException.java`: 서비스가 중복임을 알리는 예외입니다.
- `config/SecurityConfig.java`: 박승민 코드에 회원가입과 CSRF 토큰 경로의 공개 규칙을 연결했습니다.
- `user/SignupCsrfController.java`: 로그인 전에도 요청 위조 방지 토큰을 받을 수 있게 합니다. 로그인 기능은 아닙니다.

본인 말로는 이렇게 설명할 수 있습니다: “Controller가 회원가입 JSON을 받아 DTO를 검증하고, Service가 중복 확인과 비밀번호 해시 처리를 합니다. Repository가 Entity를 MySQL에 저장하고, Controller는 비밀번호가 없는 응답 DTO를 201로 돌려줍니다.”
