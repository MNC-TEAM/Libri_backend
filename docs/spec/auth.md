# Auth Spec (Thin)

## Policy
### SignUp (Email)
- Email 회원가입은 email 중복이면 실패한다.
- 회원가입 저장 중 유니크 충돌(DataIntegrityViolationException)은 EMAIL_ALREADY_EXISTS로 매핑한다.
- 가입할 때 비밀번호는 해시하여 저장한다.
- (추후) 이메일 인증을 강제한다: 가입 시 `emailVerified=false`로 저장하고, 인증 완료 시에만 ACTIVE 처리한다.

### Login (Email)
- Email 로그인은 회원이 없거나 비밀번호가 틀리면 INVALID_LOGIN으로 실패한다.
- 탈퇴 회원(memberStatus=DELETE)은 INVALID_LOGIN으로 실패한다.
- 소셜 계정(passwordHash == null)은 INVALID_LOGIN으로 실패한다.
- 로그인 시 비밀번호 해시를 검증한다.

### JWT (Access Token)
- 로그인 성공 시 Access Token을 발급한다.
- Access Token은 `sub=memberId`를 포함한다.
- Access Token에는 role/authorities 클레임을 포함하지 않는다.
- 응답은 `tokenType="Bearer"`와 `accessToken`을 포함한다.

### Security (Stateless)
- Security는 Stateless로 동작한다(SessionCreationPolicy.STATELESS).
- JWT 인증 필터는 다음 정책을 따른다.
  - 토큰이 없으면 인증 주입 없이 통과한다(익명).
  - 토큰이 있는데 유효하지 않으면 인증 주입 없이 통과한다.
    - 공개 API는 그대로 동작한다.
    - 보호 API는 authorization 단계에서 401로 처리된다.
  - 토큰이 유효하면 `sub(memberId)`를 추출하고 DB 조회(UserDetailsService)로 권한을 구성한다.
  - Authentication을 SecurityContextHolder에 저장한다.
- 인가 규칙은 requestMatchers 기반으로 적용한다(permitAll / authenticated / role).

### Error Handling
- 인증이 필요한데 인증이 없으면 401(EntryPoint)로 응답한다.
- 인증은 있으나 권한이 부족하면 403(DeniedHandler)로 응답한다.
- 401/403 응답은 공통 응답 포맷(ApiResponse)으로 내려준다.

### Logout
- Access Token만 사용하는 Stateless 구조에서 로그아웃은 서버 세션 제거 없이 처리한다.
- 기본 로그아웃은 클라이언트에서 토큰 삭제로 처리한다.
  - (추후) Refresh Token 도입 시 서버에서 refresh 무효화 처리한다.

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
- 이메일 인증(가입 시 강제)
  - verification token 발급/만료/재발송/rate limit
  - 인증 완료 시 emailVerified=true 및 상태 전환(PENDING -> ACTIVE)
- JWT Refresh Token(재발급, 탈취 대응, 로그아웃 정책)
- 소셜 로그인(구글/카카오/애플) 정책 및 중복/연동 정책