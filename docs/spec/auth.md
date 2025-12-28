# Auth Spec (Thin)

## Policy
- Auth는 인증(Authentication)과 인가(Authorization)를 다룬다.
- 로그인 식별자는 email 하나로 한다.
- email은 trim + lowercase로 정규화하여 저장/조회/로그인에 사용한다.
- Email 회원가입은 email 중복이면 실패한다.
- 회원가입 저장 중 유니크 충돌(DataIntegrityViolationException)은 EMAIL_ALREADY_EXISTS로 매핑한다.
- Email 로그인은 회원이 없거나 비밀번호가 틀리면 INVALID_LOGIN으로 실패한다.
- 탈퇴 회원(memberStatus=DELETE)은 INVALID_LOGIN으로 실패한다.
- 가입할 때 비밀번호는 해시하여 저장하고, 로그인 시 해시 검증한다.

## Tests (Safety Belt)

### SignUpByEmail
- [x] signupByEmail_throws_whenEmailAlreadyExists
  - 이메일이 이미 존재하면 회원가입에 실패하고 EMAIL_ALREADY_EXISTS 예외를 던진다.
- [x] signupByEmail_success_savesExpectedMember
  - 이메일을 정규화(trim/lowercase)하여 저장하고 비밀번호는 해시 후 저장한다.
- [x] signupByEmail_mapsUniqueViolation_toEmailAlreadyExists
  - 저장 중 유니크 충돌이 발생하면 EMAIL_ALREADY_EXISTS로 매핑한다.

### LoginByEmail
- [ ] loginByEmail_throws_whenMemberNotFound
  - 회원이 없으면 INVALID_LOGIN 예외를 던진다.
- [ ] loginByEmail_throws_whenMemberWithdrawn
  - 탈퇴 회원이면 MEMBER_WITHDRAWN 예외를 던진다.
- [ ] loginByEmail_throws_whenPasswordMismatch
  - 비밀번호가 틀리면 INVALID_LOGIN 예외를 던진다.
- [ ] loginByEmail_success_returnsResponse
  - 이메일을 정규화하여 조회하고 비밀번호 검증 성공 시 응답 DTO를 반환한다.

## TODO
- JWT 발급/검증(Access/Refresh, 재발급, 로그아웃 정책)
- 소셜 로그인(구글/카카오/애플) 정책 및 중복/연동 정책