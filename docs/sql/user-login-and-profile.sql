-- 001-create-users.sql을 적용한 로컬 DB에서 demo_user와 1을 테스트 계정 값으로 바꿉니다.

-- 로그인은 소문자 로그인 아이디로 조회하고, 앱에서 BCrypt로 비밀번호를 확인합니다.
SELECT id, password_hash, role
FROM users
WHERE login_id = 'demo_user';

-- 내 정보는 JWT에서 확인한 사용자 번호로 조회합니다.
SELECT id, login_id, email, nickname, role
FROM users
WHERE id = 1;
