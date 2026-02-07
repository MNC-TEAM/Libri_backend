# Service Layer Test Coverage

이 문서는 service 계층 단위 테스트(모킹 기반)에서 검증한 항목을 요약합니다.

## 공통
- 모든 테스트는 Mockito 기반 단위 테스트로, Repository/외부 의존성을 모킹함
- 도메인 객체 생성은 `src/test/java/monochrome/libri/TestFixtures.java` 사용

## Comment
`NoteCommentServiceImplTest`
- createComment: 인증 실패(memberId<=0) 차단
- createComment: 빈/공백 content 차단
- createComment: content trim 저장
- getComments: 비공개 노트 비소유자 접근 차단
- getComments: slice 매핑 및 소유자 표시
- deleteComment: 비소유자 삭제 차단

## Member/Auth
`MemberServiceImplTest`
- updateMember: 비밀번호 변경 시 해시 적용
- updateMember: 미존재 회원 예외
- withdraw: 회원 상태 DELETE 전환

`AuthServiceImplTest`
- signupByEmail: 중복 이메일 차단
- signupByEmail: 이메일 정규화 + 비밀번호 해시 저장
- loginByEmail: 비밀번호 불일치 차단
- loginByEmail: 탈퇴 회원 차단
- loginByEmail: 정상 로그인 시 응답 매핑

## Home
`HomeServiceImplTest`
- getHome: 비회원(null) 접근 시 요약 정보 기본값
- getHome: 진행중 도서 progressPercent/readingDays 계산

## Search
`SearchServiceImplTest`
- recordSearch: 비로그인(memberId<=0) 무시
- recordSearch: 키워드 정규화 후 저장
- getRecentSearches: 인증 필요
- getRecentSearches: slice 매핑
- getTrendingKeywords: limit 상한 처리 및 count null -> 0 매핑

## Follow
`FollowServiceImplTest`
- follow: 자기 자신 팔로우 차단
- follow: 기존 UNFOLLOW 관계 재활성화
- follow: 이미 FOLLOW 상태면 예외
- unfollow: 정상 UNFOLLOW 전환
- unfollow: 이미 UNFOLLOW 상태면 예외

## Review
`ReviewBookmarkServiceImplTest`
- bookmark: 인증 필요
- bookmark: 이미 북마크면 no-op
- bookmark: 신규 북마크 저장
- unbookmark: 미북마크면 no-op
- unbookmark: 북마크 존재 시 삭제

`ReviewServiceImplTest`
- createReview: 인증 필요
- createReview: content trim 저장, ACTIVE 상태
- updateReview: 유효하지 않은 요청 차단
- updateReview: 비소유자 수정 차단
- deleteReview: 비소유자 삭제 차단
- getReviewStats: 별점 집계 매핑
- getReviewsByBook: 책 미존재 차단
- getReviewsByMember: slice 매핑

## Shelf
`ShelfServiceImplTest`
- getShelfDetail: 인증 필요
- createShelf: 이미 존재하면 no-op
- createShelf: 진행도 검증(페이지 초과 등) 차단
- updateShelf: 상태/진행도 업데이트 반영

## Note
`NoteServiceImplTest`
- createNote: 인증 필요
- createNote: 진행도 검증(페이지 초과 등) 차단
- getNoteDetail: 비공개 노트 비소유자 접근 차단
- getNotesByMember: slice/카운트 매핑
- updateNote: 비소유자 수정 차단
- deleteNote: 비소유자 삭제 차단

`NoteReactionServiceImplTest`
- like: 비공개 노트 비소유자 접근 차단
- like: 이미 좋아요면 no-op
- like: 신규 좋아요 저장
- unlike: 좋아요 존재 시 삭제
- bookmark: 신규 북마크 저장
- unbookmark: 북마크 존재 시 삭제

## Book
`BookServiceImplTest`
- searchBooks: 짧은 키워드 빈 결과
- searchBooks: 잘못된 ISBN 빈 결과
- searchBooks: 유효 ISBN 조회 시 결과 매핑
- getBookDetail: 책장 정보/리뷰 프리뷰 포함
- createBookDirect: 인증 필요
- createBookDirect: 도서/책장 저장

## Security
`PasswordHashServiceTest`
- hashPassword: PasswordEncoder 위임
- matches: PasswordEncoder 위임

`CustomUserDetailsServiceTest`
- 잘못된 memberId 문자열 차단
- 미존재 회원 차단
- 비활성 회원 차단
- 정상 로드 시 권한 매핑

`JwtTokenServiceTest`
- access token 발급 위임 결과 반환
