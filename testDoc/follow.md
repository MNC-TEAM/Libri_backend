# Follow 기능 미니 스펙 (Policy + Tests)

## 1) 용어
- follower: 팔로우를 "거는" 사용자
- following: 팔로우 "당하는" 사용자
- 관계(Follow): follower -> following

## 2) 데이터 모델/상태
- Follow는 (follower, following) 관계를 가진다.
- 상태(Status)
    - FOLLOW: 팔로우 중
    - UNFOLLOW: 언팔로우(soft delete)

## 3) 정책/규칙
- 자기 자신을 팔로우할 수 없다.
- follower 또는 following 회원이 존재하지 않으면 실패한다.
- 신규 팔로우 요청 시 관계가 없으면 생성하고 followStatus=FOLLOW로 저장한다.
- 이미 FOLLOW인 관계에 대해 팔로우 요청 시 실패한다.
- UNFOLLOW 관계에 대해 재팔로우 요청 시 followStatus=FOLLOW로 재활성화한다.
- 언팔로우는 hard delete가 아니라 soft delete로 처리하며 followStatus=UNFOLLOW로 변경한다.
- 팔로워/팔로잉 조회는 FOLLOW만 반환한다(UNFOLLOW 제외).

## 4) API 동작 요약 (간단)
- Follow 요청: followerId, followingId로 관계를 FOLLOW로 만든다.
- Unfollow 요청: followerId, followingId 관계를 UNFOLLOW로 만든다.
- GetFollowers: targetMemberId의 팔로워 목록 반환 (FOLLOW만)
- GetFollowings: targetMemberId의 팔로잉 목록 반환 (FOLLOW만)

## 5) 예외 규격 (프로젝트 표준)
- 예외는 LibriException + ErrorCode로 처리한다.
- ErrorCode에 정의된 httpStatus/code/message를 응답으로 사용한다.

### Follow 도메인 ErrorCode 매핑
- SELF_FOLLOW_NOT_ALLOWED -> 400
- MEMBER_NOT_FOUND -> 404
- EXIST_FOLLOW_RELATION -> 409
- FOLLOW_NOT_FOUND -> 404

## 6) 최소 테스트 케이스 (Safety Belt)
> 서비스 레이어 기준(Given-When-Then). 필요 시 @Nested로 기능별 그룹화.

### Follow (팔로우)
- [X] follow_success_createsFollow_whenNewRelation
    - 신규 관계면 팔로우 관계를 생성하고 followStatus=FOLLOW로 저장한다.
- [X] follow_success_reactivates_whenRelationUNFOLLOW
    - 기존 관계가 UNFOLLOW면 재팔로우 시 followStatus=FOLLOW로 재활성화한다.
- [X] follow_throws_whenAlreadyFOLLOWRelation
    - 이미 FOLLOW인 관계에 대해 팔로우 요청 시 FOLLOW_ALREADY_EXISTS 예외를 던진다.
- [X] follow_throws_whenSelfFollow
    - 자기 자신을 팔로우하려 하면 SELF_FOLLOW_NOT_ALLOWED 예외를 던진다.
- [x] follow_throws_whenFollowingNotFound
    - following 회원이 없으면 MEMBER_NOT_FOUND 예외를 던진다.
    - [x] followerNotFound도 동일 패턴이므로 필요하면 추가

### Unfollow (언팔로우)
- [x] unfollow_success_setsUNFOLLOW_whenRelationExists
    - 관계가 존재하면 언팔로우 시 followStatus=UNFOLLOW로 변경한다(soft delete).
- [x] unfollow_throws_whenRelationNotFound
    - 관계가 없으면 FOLLOW_RELATION_NOT_FOUND 예외를 던진다.

### Query (조회) - Filtering 필요한 경우 DataJpaTest 진행
- [x] getFollowers_success_returnsOnlyFOLLOW
    - 팔로워 목록 조회 시 FOLLOW 상태의 팔로워만 반환한다(UNFOLLOW 제외).
- [x] getFollowings_success_returnsOnlyFOLLOW
    - 팔로잉 목록 조회 시 FOLLOW 상태의 팔로잉만 반환한다(UNFOLLOW 제외).

## 7) 추후 확장(요구사항 생기면 추가)
- 차단(Block) 정책
- 비공개 계정/권한 정책
- 정지/탈퇴 회원 처리
- 페이징/정렬/검색
