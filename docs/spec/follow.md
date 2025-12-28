# Follow Spec (Thin)

## Policy
- follower → following 관계를 Follow로 관리한다.
- 상태는 FOLLOW / UNFOLLOW(soft delete)로 관리한다.
- 자기 자신을 팔로우할 수 없다.
- follower 또는 following 회원이 존재하지 않으면 MEMBER_NOT_FOUND로 실패한다.
- follow 요청
    - 관계가 없으면 생성 후 FOLLOW로 저장한다.
    - 관계가 UNFOLLOW면 FOLLOW로 재활성화한다.
    - 관계가 이미 FOLLOW면 EXIST_FOLLOW_RELATION로 실패한다.
- unfollow 요청
    - 관계가 존재하면 UNFOLLOW로 변경한다(soft delete).
    - 관계가 없으면 FOLLOW_NOT_FOUND로 실패한다.
- 팔로워/팔로잉 조회는 FOLLOW만 반환한다(UNFOLLOW 제외). *(Query 테스트/구현 시 반영)*

## Tests (Safety Belt)

### unfollow
- [x] unfollow_success_setsUNFOLLOW_whenRelationExists
    - 관계가 존재하면 언팔로우 시 followStatus=UNFOLLOW로 변경한다(soft delete).
- [x] unfollow_throws_whenRelationNotFound
    - 관계가 없으면 FOLLOW_RELATION_NOT_FOUND 예외를 던진다.

### follow
- [x] follow_success_createsFollow_whenNewRelation
    - 신규 관계면 팔로우 관계를 생성하고 followStatus=FOLLOW로 저장한다.
- [x] follow_success_reactivates_whenRelationUNFOLLOW
    - 기존 관계가 UNFOLLOW면 재팔로우 시 followStatus=FOLLOW로 재활성화한다.
- [x] follow_throws_whenAlreadyFOLLOWRelation
    - 이미 FOLLOW인 관계에 대해 팔로우 요청 시 FOLLOW_ALREADY_EXISTS 예외를 던진다.
- [x] follow_throws_whenSelfFollow
    - 자기 자신을 팔로우하려 하면 SELF_FOLLOW_NOT_ALLOWED 예외를 던진다.
- [x] follow_throws_whenFollowingNotFound
    - following 회원이 없으면 MEMBER_NOT_FOUND 예외를 던진다.
- [x] follow_throws_whenFollowerNotFound
    - follower 회원이 없으면 MEMBER_NOT_FOUND 예외를 던진다.

### Query
- [x] getFollowers_success_returnsOnlyFOLLOW
    - 팔로워 목록 조회 시 FOLLOW 상태의 팔로워만 반환한다(UNFOLLOW 제외).
- [x] getFollowings_success_returnsOnlyFOLLOW
    - 팔로잉 목록 조회 시 FOLLOW 상태의 팔로잉만 반환한다(UNFOLLOW 제외).

