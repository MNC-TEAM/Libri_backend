# Member 기능 미니 스펙 (Policy + Tests)

## 1) 용어
- Member: 회원 도메인 엔티티
- UpdateMember: 회원 정보를 부분 수정하는 유스케이스
- Withdraw: 회원 탈퇴 처리(도메인 메서드 member.withdraw() 호출로 위임)
- PasswordHash: 비밀번호 원문(raw)을 해시로 변환한 값

---

## 2) 데이터 모델/상태
- Member는 회원의 기본 프로필/권한/상태 정보를 가진다.
    - 예: primaryEmail, username, nickname, passwordHash, profilePath, memberStatus, role 등
- MemberStatus
    - 예: ACTIVE 등(구체 값은 도메인 정의에 따름)
- Role
    - 예: USER 등(구체 값은 도메인 정의에 따름)

---

## 3) 정책/규칙 (최소)
- 회원 조회(findById)가 실패하면 유스케이스는 실패한다.
- updateMember는 부분 업데이트로 동작한다.
    - DTO에서 null로 전달된 필드는 기존 값을 유지한다.
- 비밀번호 변경 규칙
    - rawPassword가 없으면(null) 기존 passwordHash를 유지한다.
    - rawPassword가 있으면 PasswordHashService.hashPassword(rawPassword)로 해시 생성 후 반영한다.
    - rawPassword가 없을 때 PasswordHashService는 호출되지 않아야 한다.
- withdraw는 회원이 존재하면 member.withdraw()를 호출한다.

---

## 4) API 동작 요약 (간단)
> 서비스 레이어 메서드 기준

- UpdateMember 요청: memberId, MemberUpdateRequestDto로 회원 정보를 수정한다.
    - 회원 없음 -> 실패
    - rawPassword 없음 -> 기존 passwordHash 유지(해시 서비스 호출 X)
    - rawPassword 있음 -> 해시 후 업데이트 반영
    - DTO null 필드 -> 기존 값 유지
- GetMemberById: repository 결과(Optional)를 그대로 반환한다.
- GetMemberByEmail: repository 결과(Optional)를 그대로 반환한다.
- Withdraw 요청: memberId로 회원 탈퇴 처리 수행(도메인 withdraw 호출)

---

## 5) 예외 규격 (프로젝트 표준)
- 예외는 LibriException + ErrorCode로 처리한다.
- ErrorCode에 정의된 httpStatus/code/message를 응답으로 사용한다.

### Member 도메인 ErrorCode 매핑(테스트 기준/권장)
- MEMBER_NOT_FOUND -> 404
    - updateMember에서 회원 조회 실패
    - withdraw에서 회원 조회 실패

참고: 현재 테스트는 주로 LibriException 발생 여부를 검증하며, ErrorCode까지 강제하지는 않는다(확장 가능).

---

## 6) 최소 테스트 케이스 (Safety Belt)
> 서비스 레이어 기준(Given-When-Then). 필요 시 @Nested로 기능별 그룹화.

### UpdateMember (회원 수정)
- [x] 비밀번호가_없으면_기존_hash_유지
    - rawPassword가 없으면 기존 passwordHash를 유지한다.
    - DTO에서 null로 들어온 필드는 기존 값을 유지한다.
    - PasswordHashService는 호출되지 않는다.
- [x] updateMember_withRawPassword_updatesPasswordHash
    - rawPassword가 있으면 해시 후 member.updateMember(..., new_hash, ...) 호출로 반영한다.
- [x] updateMember_withoutRawPassword_keepsOldPasswordHash
    - rawPassword가 null이면 기존 해시(old_hash)를 사용하여 member.updateMember(..., old_hash, ...)로 호출한다.
    - 해시 서비스는 호출되지 않는다.
- [x] updateMember_memberNotFound_throwsException
    - 회원이 존재하지 않으면 LibriException을 던진다.
    - repository는 findById만 호출되고 추가 상호작용이 없어야 한다.
    - 해시 서비스는 호출되지 않아야 한다.

### Query (조회)
- [x] getMemberById_returnsRepositoryResult
    - getMemberById는 repository 결과(Optional)를 그대로 반환한다.
- [x] getMemberByEmail_returnsRepositoryResult
    - getMemberByEmail은 repository 결과(Optional)를 그대로 반환한다.

### Withdraw (탈퇴)
- [x] withdraw_existingMember_callsWithdraw
    - 회원이 존재하면 member.withdraw()를 호출한다.
- [x] withdraw_memberNotFound_throwsException
    - 회원이 없으면 LibriException을 던진다.

---

## 7) 추후 확장(요구사항 생기면 추가)
- updateMember의 공백/빈 문자열 정책(blank도 비밀번호 없음으로 볼지)
- updateMember 시 이메일/유저네임 중복 검증
- 권한(Role) 변경 정책(관리자만 가능 등)
- Withdraw 시 후처리(토큰 무효화, 연관 데이터 처리 등)
- ErrorCode까지 단정하는 테스트 강화(ex.getErrorCode())
