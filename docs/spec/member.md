# Member Spec (Thin)

## Policy

### Profile
- `GET /members/me` 는 로그인한 회원 자신의 정보를 반환한다.
- `GET /members/{memberId}` 는 누구나 조회 가능하다. 로그인 상태면 본인 여부와 팔로우 여부를 함께 반환한다.

### Nickname / Privacy / ProfileImage
- 닉네임, 비공개 여부, 프로필 이미지 경로는 각각 별도 PATCH API로 변경한다.
- 프로필 이미지는 S3 업로드 후 클라이언트가 경로를 직접 전달한다.

### Withdraw
- 탈퇴 처리 시 memberStatus를 DELETE로 변경한다(soft delete).
- 탈퇴 회원은 이메일 로그인 시 INVALID_LOGIN으로 실패한다.

### Follow
- 팔로우/언팔로우는 `POST/DELETE /members/{memberId}/follow`로 처리한다.
- 자기 자신 팔로우는 SELF_FOLLOW_NOT_ALLOWED(F002)로 실패한다.
- 이미 팔로우 중인 경우 EXIST_FOLLOW_RELATION(F001)으로 실패한다.
- 팔로우 관계는 FOLLOW/UNFOLLOW로 soft delete 관리한다.
- 팔로워/팔로잉 목록은 FOLLOW 상태만 반환한다.

### Block
- 차단은 `POST /members/{memberId}/block`으로 처리한다.
- 차단 해제는 `DELETE /members/{memberId}/block`으로 처리한다.
- 차단한 회원 목록은 `GET /members/me/blocks`로 조회한다.
- 자기 자신 차단은 SELF_BLOCK_NOT_ALLOWED(K002)로 실패한다.
- 이미 차단한 경우 BLOCK_ALREADY_EXISTS(K001)로 실패한다.

### Report
- 회원 신고는 `POST /members/{memberId}/reports`로 처리한다.
- 자기 자신 신고는 SELF_REPORT_NOT_ALLOWED(P001)로 실패한다.
- 같은 회원은 한 번만 신고 가능하며, 중복 시 MEMBER_REPORT_ALREADY_EXISTS(N004)로 실패한다.

## Tests (Safety Belt)

### Withdraw
- [ ] withdraw_success_setsStatusDELETE
    - 탈퇴 처리 시 memberStatus가 DELETE로 변경된다.

### Follow
- follow/unfollow 정책은 `follow.md` 참고.

### Block
- [ ] blockMember_success
    - 차단 관계를 생성한다.
- [ ] blockMember_throws_whenSelfBlock
    - 자기 자신을 차단하려 하면 SELF_BLOCK_NOT_ALLOWED 예외를 던진다.
- [ ] blockMember_throws_whenAlreadyBlocked
    - 이미 차단한 회원을 다시 차단하려 하면 BLOCK_ALREADY_EXISTS 예외를 던진다.
- [ ] unblockMember_success
    - 차단 해제 처리를 한다.
- [ ] unblockMember_throws_whenNotBlocked
    - 차단 관계가 없으면 BLOCK_NOT_FOUND 예외를 던진다.
