# API Overview

이 문서는 현재 `Libri_backend` 기준 전체 API 개요 문서다.

포함 범위:

- API 이름
- Method / Path
- 인증 필요 여부
- 입력
- 출력
- 비로그인 정책
- 대표 에러 코드
- 프론트 처리 포인트

세부 스키마는 Swagger를 함께 참고하고, 이 문서는 프론트/백엔드 협업용 운영 문서로 사용한다.

## 공통 규칙

### Base URL

- `/api/v1`

### 공통 성공 응답

```json
{
  "success": true,
  "code": "OK",
  "message": null,
  "data": {}
}
```

### 공통 실패 응답

```json
{
  "success": false,
  "code": "C002",
  "message": "비밀번호는 8자 이상 100자 이하여야 합니다.",
  "data": null
}
```

### 인증 방식

- 인증 API는 `Authorization: Bearer {accessToken}` 사용
- refresh token은 일반 API 헤더에 넣지 않음
- refresh/logout API는 body에 `refreshToken` 전달

### 검증 실패

- 검증 실패는 `400 Bad Request`
- 공통 코드: `C002`
- `message`에는 프론트가 바로 보여줄 수 있는 필드 메시지가 들어감

예:

- `비밀번호는 8자 이상 100자 이하여야 합니다.`
- `닉네임은 1자 이상 30자 이하여야 합니다.`

### 비로그인 빈 배열 정책

일부 목록 조회 API는 비로그인 시 에러 대신 빈 배열을 반환한다.

대상 예:

- 최근 검색어 조회
- 상태별 서재 목록 조회
- 내 노트 목록 조회
- 내 리뷰 목록 조회
- 북마크 목록 조회
- 내 댓글 목록 조회

## Auth

### 이메일 회원가입

- `POST /auth/signup/email`
- Auth: 없음

입력:

- `email`
- `rawPassword`
- `nickname`
- `profilePath`

출력:

- `201 Created`
- `data: null`

대표 에러:

- `M002 EMAIL_ALREADY_EXISTS`
- `C002 INVALID_INPUT_VALUE`

프론트 포인트:

- validation 메시지는 그대로 노출 가능
- 성공 후 자동 로그인은 현재 없음

### 이메일 로그인

- `POST /auth/login/email`
- Auth: 없음

입력:

- `email`
- `rawPassword`

출력:

- `tokenType`
- `accessToken`
- `refreshToken`
- `memberResponseDto`

프론트 포인트:

- 로그인 성공 시 access/refresh token 둘 다 저장
- 이후 인증 API는 access token만 헤더에 사용

### 소셜 로그인

- `POST /auth/login/social`
- Auth: 없음

지원 provider:

- `KAKAO`
- `APPLE`

요청 규칙:

- `provider=KAKAO` 이면 `accessToken` 필수, `idToken` 생략
- `provider=APPLE` 이면 `idToken` 필수, `accessToken` 생략
- 토큰은 프론트가 각 SDK 또는 네이티브 로그인 결과에서 받아서 백엔드로 전달

동작 방식:

- 이미 연동된 소셜 계정이면 기존 회원으로 로그인
- 같은 이메일의 기존 회원이 있으면 해당 회원에 자동 연동 후 로그인
- 일치 회원이 없으면 새 회원 생성 후 로그인

입력:

- `provider`
- `accessToken`
- `idToken`

카카오 요청 예시:

```json
{
  "provider": "KAKAO",
  "accessToken": "kakao-access-token"
}
```

애플 요청 예시:

```json
{
  "provider": "APPLE",
  "idToken": "eyJraWQiOiJ...apple-id-token"
}
```

출력:

- `tokenType`
- `accessToken`
- `refreshToken`
- `memberResponseDto`

성공 응답 예시:

```json
{
  "success": true,
  "code": "OK",
  "message": null,
  "data": {
    "tokenType": "Bearer",
    "accessToken": "ACCESS_TOKEN",
    "refreshToken": "REFRESH_TOKEN",
    "memberResponseDto": {
      "id": 12,
      "provider": "EMAIL",
      "email": "user@test.com",
      "nickname": "nick12",
      "profilePath": "/profile/12",
      "privateAccount": false,
      "followerCount": 0,
      "followingCount": 0
    }
  }
}
```

대표 에러:

- `A005 INVALID_SOCIAL_TOKEN`
- `C002 INVALID_INPUT_VALUE`
- `A001 AUTHENTICATION_FAILED`

프론트 포인트:

- 성공 시 이메일 로그인과 동일하게 `accessToken`, `refreshToken` 둘 다 저장
- `tokenType`은 현재 `Bearer`로 고정이지만 그대로 사용
- 이후 인증 API는 `Authorization: Bearer {accessToken}` 헤더 사용
- `memberResponseDto.provider`는 회원의 기본 가입 타입이며, 자동 연동된 경우 `EMAIL`일 수 있음

프론트 구현 순서:

1. 카카오/애플 SDK로 사용자 로그인 진행
2. SDK 결과에서 provider별 토큰 획득
3. 백엔드 `/api/v1/auth/login/social` 호출
4. 응답의 `accessToken`, `refreshToken` 저장
5. 이후 API부터 access token만 Authorization 헤더에 첨부

카카오 프론트 예시:

```ts
const kakaoAccessToken = await getKakaoAccessTokenFromSdk();

const response = await fetch("/api/v1/auth/login/social", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    provider: "KAKAO",
    accessToken: kakaoAccessToken,
  }),
});

const result = await response.json();

if (!response.ok || !result.success) {
  throw new Error(result.message ?? "카카오 로그인에 실패했습니다.");
}

const { tokenType, accessToken, refreshToken, memberResponseDto } = result.data;
localStorage.setItem("accessToken", accessToken);
localStorage.setItem("refreshToken", refreshToken);
```

애플 프론트 예시:

```ts
const appleIdToken = await getAppleIdTokenFromSdk();

const response = await fetch("/api/v1/auth/login/social", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    provider: "APPLE",
    idToken: appleIdToken,
  }),
});

const result = await response.json();

if (!response.ok || !result.success) {
  throw new Error(result.message ?? "애플 로그인에 실패했습니다.");
}

const { accessToken, refreshToken } = result.data;
localStorage.setItem("accessToken", accessToken);
localStorage.setItem("refreshToken", refreshToken);
```

에러 처리 가이드:

- `A005`: 소셜 토큰 만료, 위조, 잘못된 provider 토큰. 프론트는 SDK 로그인부터 다시 시도
- `C002`: body 누락 또는 잘못된 요청. 프론트 버그 또는 잘못된 파라미터
- `A001`: 이미 다른 회원에 연동된 소셜 계정 등 인증 불일치 상황

소셜 로그인 예외 케이스 표:

| 상황 | HTTP | code | 프론트 처리 |
| --- | --- | --- | --- |
| `provider` 누락 | 400 | `C002` | 요청 body 구성 오류 수정 |
| `provider=KAKAO`인데 `accessToken` 없음 | 400 | `C002` | 카카오 SDK 결과 확인 후 재요청 |
| `provider=APPLE`인데 `idToken` 없음 | 400 | `C002` | 애플 SDK 결과 확인 후 재요청 |
| 지원하지 않는 provider 전달 | 400 | `C002` | 프론트 상수값 확인 |
| 카카오 access token 만료/위조 | 401 | `A005` | 카카오 로그인부터 다시 시작 |
| 애플 id token 만료/서명 불일치 | 401 | `A005` | 애플 로그인부터 다시 시작 |
| 애플 audience(client id) 불일치 | 401 | `A005` | 앱 번들 ID, 서비스 ID, 환경변수 설정 확인 |
| 이미 다른 회원에 연결된 소셜 계정으로 충돌 | 401 | `A001` | 자동 재시도하지 말고 문의/안내 처리 |
| 탈퇴 회원에 연결된 소셜 계정 로그인 | 401 | `M003` | 재가입 정책 확인 필요. 현재는 로그인 실패 처리 |
| 소셜 provider에서 이메일 미제공 | 200 | `OK` | 정상 로그인 가능. 이메일 없이 가입/로그인될 수 있음 |
| 같은 이메일의 기존 계정 발견 | 200 | `OK` | 정상 로그인. 백엔드가 자동 연동 처리 |

프론트 에러 메시지 처리 권장:

- `C002`는 개발 중이면 필드 매핑 오류를 우선 의심
- `A005`는 사용자에게 "로그인이 만료되었어요. 다시 시도해주세요." 수준으로 안내
- `A001`은 자동 연동 충돌 가능성이 있으므로 일반 재시도보다 고객센터 문의 문구가 안전
- `M003`은 현재 메시지가 이메일 로그인과 공용일 수 있으므로, 필요하면 추후 소셜 전용 코드 분리 검토

주의:

- 애플은 이메일이 항상 내려오지 않을 수 있다
- 카카오는 이메일 동의가 없으면 이메일이 비어 있을 수 있다
- 백엔드는 이메일이 없더라도 provider 식별자로 회원 생성/로그인 가능
- 자동 연동은 이메일이 정확히 일치할 때만 수행한다

### 액세스 토큰 재발급

- `POST /auth/refresh`
- Auth: 없음

입력:

- `refreshToken`

출력:

- `tokenType`
- `accessToken`
- `refreshToken`

대표 에러:

- `A003 INVALID_REFRESH_TOKEN`
- `A004 TOKEN_TYPE_MISMATCH`

프론트 포인트:

- refresh 성공 시 access/refresh token 둘 다 교체
- 실패 시 로그아웃 처리

### 로그아웃

- `POST /auth/logout`
- Auth: 없음

입력:

- `refreshToken`

출력:

- `data: null`

프론트 포인트:

- 성공 여부와 관계없이 로컬 토큰 제거 권장

## Member

### 내 정보 조회

- `GET /members/me`
- Auth: 필요

출력:

- `MemberResponseDto`

대표 에러:

- `A001`
- `M001`

### 내 비공개 여부 조회

- `GET /members/me/privacy`
- Auth: 필요

출력:

- `MemberPrivacyResponseDto`

대표 에러:

- `A001`
- `M001`

### 차단한 회원 목록 조회

- `GET /members/me/blocks`
- Auth: 필요

Query:

- `page`
- `size`

출력:

- `BlockedMemberListResponseDto`

대표 에러:

- `A001`
- `M001`
- `C002`

### 닉네임 변경

- `PATCH /members/me/nickname`
- Auth: 필요

입력:

- `nickname`

출력:

- `MemberResponseDto`

대표 에러:

- `A001`
- `M001`
- `C002`

### 계정 비공개 설정 변경

- `PATCH /members/me/privacy`
- Auth: 필요

입력:

- `privateAccount: true | false`

출력:

- `MemberResponseDto`

### 프로필 이미지 경로 저장

- `PATCH /members/me/profile-image`
- Auth: 필요

입력:

- `profilePath`

출력:

- `MemberResponseDto`

비고:

- S3 업로드 완료 후 해당 경로를 저장하는 API

대표 에러:

- `A001`
- `M001`
- `C002`

### 회원 탈퇴

- `DELETE /members/withdraw`
- Auth: 필요

출력:

- `data: null`

대표 에러:

- `A001`
- `M001`

### 회원 프로필 조회

- `GET /members/{memberId}`
- Auth: 선택

출력:

- `MemberProfileResponseDto`
- 로그인 상태면 본인 여부, 팔로우 여부 포함

대표 에러:

- `M001`

### 팔로워 목록 조회

- `GET /members/{memberId}/followers`
- Auth: 없음

Query:

- `page`
- `size`

출력:

- `FollowMemberSliceResponseDto`

대표 에러:

- `M001`
- `C002`

### 팔로잉 목록 조회

- `GET /members/{memberId}/followings`
- Auth: 없음

Query:

- `page`
- `size`

출력:

- `FollowMemberSliceResponseDto`

대표 에러:

- `M001`
- `C002`

### 회원 팔로우

- `POST /members/{memberId}/follow`
- Auth: 필요

출력:

- `201 Created`
- `data: null`

대표 에러:

- `A001`
- `M001`
- `F001 EXIST_FOLLOW_RELATION`
- `F002 SELF_FOLLOW_NOT_ALLOWED`
- `A002`

### 회원 언팔로우

- `DELETE /members/{memberId}/follow`
- Auth: 필요

출력:

- `data: null`

대표 에러:

- `A001`
- `M001`
- `F002`
- `F003 FOLLOW_NOT_FOUND`
- `F004 ALREADY_UNFOLLOWED`

### 회원 신고

- `POST /members/{memberId}/reports`
- Auth: 필요

입력:

- `reason`
- `detail` (선택)

출력:

- `201 Created`
- `data: null`

대표 에러:

- `A001`
- `M001`
- `N004 MEMBER_REPORT_ALREADY_EXISTS`
- `P001 SELF_REPORT_NOT_ALLOWED`
- `C002`

### 회원 차단

- `POST /members/{memberId}/block`
- Auth: 필요

출력:

- `data: null`

대표 에러:

- `A001`
- `M001`
- `K001 BLOCK_ALREADY_EXISTS`
- `K002 SELF_BLOCK_NOT_ALLOWED`

### 회원 차단 해제

- `DELETE /members/{memberId}/block`
- Auth: 필요

출력:

- `data: null`

대표 에러:

- `A001`
- `M001`
- `K002`
- `K003 BLOCK_NOT_FOUND`

## Home

### 홈 화면 데이터 조회

- `GET /home`
- Auth: 선택

출력:

- `me`
- `shelfSummary`
- `readingBooks`
- `wantToReadBooks`
- `finishedBooks`
- `recommendations`

비로그인 정책:

- 비회원이면 기본값 반환

## Book

### 도서 검색

- `GET /books`
- Auth: 선택

Query:

- `keyword`
- `page`
- `size`

출력:

- `BookSliceResponseDto`

비고:

- 로그인 상태면 검색어가 최근 검색어에 저장됨
- 짧은 키워드는 빈 결과 가능

### 도서 상세 조회

- `GET /books/{bookId}`
- Auth: 선택

출력:

- 도서 상세
- 내 서재 정보
- 리뷰 통계
- 리뷰 미리보기

### 도서 직접 등록

- `POST /books/direct`
- Auth: 필요

입력:

- `title`
- `author`
- `publisher`
- `isbn`
- `totalPage`
- `status`
- `coverUrl`

출력:

- `201 Created`
- `data: null`

프론트 포인트:

- validation 실패 시 필드 메시지 반환

### 직접 등록한 도서 수정

- `PATCH /books/{bookId}`
- Auth: 필요

입력:

- `title`
- `author`
- `publisher`
- `isbn`
- `totalPage`
- `coverUrl`
- `introduction`
- `releaseDate`
- `salePageUrl`

출력:

- 수정된 `BookDetailResponseDto`

대표 에러:

- `A001`
- `B001 BOOK_NOT_FOUND`
- `A002 ACCESS_DENIED`
- `C002`

비고:

- 직접 등록한 도서만 수정 가능
- 등록자가 아니면 수정 불가

## Shelf

### 상태별 서재 목록 조회

- `GET /shelves`
- Auth: 선택

Query:

- `status`: `WANT_TO_READ | READING | FINISHED`
- `page`
- `size`

출력:

- `ShelfListResponseDto`
- 각 항목:
  - `shelfId`
  - `bookId`
  - `title`
  - `author`
  - `coverUrl`
  - `status`
  - `progressPercent`
  - `startDate`
  - `endDate`

비로그인 정책:

- `200 OK`
- `content: []`

### 서재 상세 조회

- `GET /shelves/{shelfId}`
- Auth: 필요

출력:

- `ShelfDetailResponseDto`

### 서재 생성

- `POST /shelves`
- Auth: 필요

입력:

- `bookId`
- `status`
- `progressType`
- `progressValue`
- `startDate`
- `endDate`

### 서재 수정

- `PATCH /shelves/{shelfId}`
- Auth: 필요

입력:

- `status`
- `startDate`
- `endDate`
- `progressType`
- `progressValue`

## Shelf Notes

### 특정 서재 노트 조회

- `GET /shelves/{shelfId}/notes`
- Auth: 필요

출력:

- `NoteSliceResponseDto`

비고:

- 서재 소유자 기준 조회

### 서재 노트 작성

- `POST /shelves/{shelfId}/notes`
- Auth: 필요

입력:

- `content`
- `secret`
- `progressType`
- `progressValue`

### 서재 노트 수정

- `PATCH /shelves/{shelfId}/notes/{noteId}`
- Auth: 필요

입력:

- `content`
- `secret`
- `shelfId`
- `progressType`
- `progressValue`

### 서재 노트 삭제

- `DELETE /shelves/{shelfId}/notes/{noteId}`
- Auth: 필요

## Note

### 내 노트 목록 조회

- `GET /notes`
- Auth: 선택

Query:

- `page`
- `size`
- `sort: latest | oldest`

출력:

- `NoteListResponseDto`

비로그인 정책:

- `200 OK`
- `content: []`

### 노트 상세 조회

- `GET /notes/{noteId}`
- Auth: 선택

출력:

- `NoteDetailResponseDto`

비고:

- 비공개 노트는 소유자만 조회 가능

## Note Reaction

### 노트 좋아요

- `POST /notes/{noteId}/likes`
- Auth: 필요

### 노트 좋아요 취소

- `DELETE /notes/{noteId}/likes`
- Auth: 필요

### 노트 북마크

- `POST /notes/{noteId}/bookmarks`
- Auth: 필요

### 노트 북마크 취소

- `DELETE /notes/{noteId}/bookmarks`
- Auth: 필요

## Note Comment

### 노트 댓글 목록 조회

- `GET /notes/{noteId}/comments`
- Auth: 선택

출력:

- `CommentSliceResponseDto`

비고:

- 비공개 노트면 소유자만 조회 가능

### 노트 댓글 작성

- `POST /notes/{noteId}/comments`
- Auth: 필요

입력:

- `content`

### 노트 댓글 삭제

- `DELETE /notes/{noteId}/comments/{commentId}`
- Auth: 필요

### 노트 댓글 신고

- `POST /notes/{noteId}/comments/{commentId}/reports`
- Auth: 필요

입력:

- `reason`
- `detail` (선택)

출력:

- `201 Created`
- `data: null`

대표 에러:

- `A001`
- `M001`
- `N001`
- `N002`
- `N003 COMMENT_REPORT_ALREADY_EXISTS`
- `C002`

## Review

### 도서 리뷰 목록 조회

- `GET /books/{bookId}/reviews`
- Auth: 선택

출력:

- `ReviewSliceWithStatsResponseDto`

### 리뷰 작성

- `POST /reviews`
- Auth: 필요

입력:

- `bookId`
- `rating`
- `content`

### 리뷰 수정

- `PATCH /reviews/{reviewId}`
- Auth: 필요

입력:

- `rating`
- `content`

### 리뷰 삭제

- `DELETE /reviews/{reviewId}`
- Auth: 필요

### 내 리뷰 목록 조회

- `GET /reviews/me`
- Auth: 선택

출력:

- `ReviewSliceResponseDto`

비로그인 정책:

- `200 OK`
- `content: []`

## Review Bookmark

### 리뷰 북마크

- `POST /reviews/{reviewId}/bookmarks`
- Auth: 필요

### 리뷰 북마크 취소

- `DELETE /reviews/{reviewId}/bookmarks`
- Auth: 필요

## Notice

### 공지사항 목록 조회

- `GET /notices`
- Auth: 없음

Query:

- `page`
- `size`

출력:

- `NoticeListResponseDto`

비고:

- 공개된 공지사항만 조회
- 고정 공지가 우선 노출되고 최신순 정렬

### 공지사항 상세 조회

- `GET /notices/{noticeId}`
- Auth: 없음

출력:

- `NoticeDetailResponseDto`

대표 에러:

- `O001 NOTICE_NOT_FOUND`

### 공지사항 등록

- `POST /admin/notices`
- Auth: 필요

입력:

- `title`
- `content`
- `pinned`
- `published`

출력:

- `201 Created`
- `NoticeDetailResponseDto`

대표 에러:

- `A001`
- `A002`
- `M001`
- `C002`

비고:

- 관리자만 등록 가능

### 공지사항 수정

- `PATCH /admin/notices/{noticeId}`
- Auth: 필요

입력:

- `title`
- `content`
- `pinned`
- `published`

출력:

- `NoticeDetailResponseDto`

대표 에러:

- `A001`
- `A002`
- `M001`
- `O001`
- `C002`

### 공지사항 삭제

- `DELETE /admin/notices/{noticeId}`
- Auth: 필요

대표 에러:

- `A001`
- `A002`
- `M001`
- `O001`

## Inquiry

### 문의 작성

- `POST /inquiries`
- Auth: 필요

입력:

- `title`
- `content`

출력:

- `201 Created`
- `InquiryDetailResponseDto`

대표 에러:

- `A001`
- `M001`
- `C002`

### 내 문의 목록 조회

- `GET /inquiries/me`
- Auth: 필요

Query:

- `page`
- `size`

출력:

- `InquiryListResponseDto`

대표 에러:

- `A001`
- `C002`

### 문의 상세 조회

- `GET /inquiries/{inquiryId}`
- Auth: 필요

출력:

- `InquiryDetailResponseDto`

대표 에러:

- `A001`
- `A002`
- `M001`
- `Q001 INQUIRY_NOT_FOUND`

비고:

- 본인 문의 또는 관리자만 조회 가능

### 문의 전체 조회

- `GET /admin/inquiries`
- Auth: 필요

Query:

- `page`
- `size`

출력:

- `InquiryListResponseDto`

대표 에러:

- `A001`
- `A002`
- `M001`
- `C002`

비고:

- 관리자만 조회 가능

### 문의 답변 등록

- `PATCH /admin/inquiries/{inquiryId}/answer`
- Auth: 필요

입력:

- `answerContent`

출력:

- `InquiryDetailResponseDto`

대표 에러:

- `A001`
- `A002`
- `M001`
- `Q001`
- `C002`

비고:

- 답변 등록 시 상태는 `ANSWERED`로 변경

### 문의 상태 변경

- `PATCH /admin/inquiries/{inquiryId}/status`
- Auth: 필요

입력:

- `status: PENDING | ANSWERED | CLOSED`

출력:

- `InquiryDetailResponseDto`

대표 에러:

- `A001`
- `A002`
- `M001`
- `Q001`
- `C002`

비고:

- 관리자만 상태 변경 가능

## Search

### 최근 검색어 조회

- `GET /searches/recent`
- Auth: 선택

Query:

- `page`
- `size`

출력:

- `RecentSearchListResponseDto`

비로그인 정책:

- `200 OK`
- `content: []`

### 최근 검색어 전체 삭제

- `DELETE /searches/recent`
- Auth: 필요

### 최근 검색어 단건 삭제

- `DELETE /searches/recent/{searchId}`
- Auth: 필요

### 인기 검색어 조회

- `GET /searches/trending`
- Auth: 없음

Query:

- `limit`

출력:

- `TrendingKeywordListResponseDto`
- 각 항목:
  - `book.bookId`
  - `book.title`
  - `book.coverUrl`
  - `count`

비고:

- 응답은 문자열 키워드가 아니라 대표 도서 정보 기준

## My Content

### 북마크한 노트 조회

- `GET /me/bookmarks/notes`
- Auth: 선택

비로그인 정책:

- `200 OK`
- `content: []`

### 북마크한 리뷰 조회

- `GET /me/bookmarks/reviews`
- Auth: 선택

비로그인 정책:

- `200 OK`
- `content: []`

### 내 리뷰 조회

- `GET /me/reviews`
- Auth: 선택

비로그인 정책:

- `200 OK`
- `content: []`

### 내 댓글 조회

- `GET /me/comments`
- Auth: 선택

비로그인 정책:

- `200 OK`
- `content: []`

## Notification

### 알림 목록 조회

- `GET /notifications`
- Auth: 필요

Query:

- `page`
- `size`

출력:

- `NotificationSliceResponseDto`
  - `totalCount`
  - `content[]`: id, noteId, content, actorProfilePath, actorMemberId, read, createdAt, notificationType
  - `hasNext`, `page`, `size`

비고:

- 소프트 삭제된 알림은 제외
- notificationType: `LIKED` | `COMMENT` | `FOLLOW`
- `FOLLOW` 타입은 `noteId = null`

대표 에러:

- `A001`
- `C002`

### 알림 읽음 처리

- `PATCH /notifications/{notificationId}/read`
- Auth: 필요

출력:

- `data: null`

대표 에러:

- `A001`
- `A002`
- `N003 NOTIFICATION_NOT_FOUND`

### 알림 전체 읽음 처리

- `PATCH /notifications/read-all`
- Auth: 필요

출력:

- `data: null`

대표 에러:

- `A001`

### 알림 삭제

- `DELETE /notifications/{notificationId}`
- Auth: 필요

출력:

- `data: null`

대표 에러:

- `A001`
- `A002`
- `N003 NOTIFICATION_NOT_FOUND`

### 알림 전체 삭제

- `DELETE /notifications/all`
- Auth: 필요

출력:

- `data: null`

대표 에러:

- `A001`

## FCM Token

### FCM 토큰 등록

- `POST /fcm-tokens`
- Auth: 필요

입력:

- `fcmToken` (필수, 최대 512자)

출력:

- `201 Created`
- `data: null`

비고:

- 동일 회원+토큰 조합이 이미 존재하면 `last_used_date`만 갱신

대표 에러:

- `A001`
- `M001`
- `C002`

### FCM 토큰 전체 삭제

- `DELETE /fcm-tokens`
- Auth: 필요

출력:

- `data: null`

비고:

- 로그아웃 또는 푸시 알림 수신 거부 시 호출

대표 에러:

- `A001`
- `M001`

## 대표 에러 코드

- `C001`
  - 서버 에러
- `C002`
  - 잘못된 요청 / validation 실패
- `C003`
  - S3 설정 누락
- `A001`
  - 인증 실패
- `A002`
  - 접근 권한 없음
- `A003`
  - 유효하지 않은 refresh token
- `A004`
  - 잘못된 token type
- `M001`
  - 회원 없음
- `M002`
  - 중복 이메일
- `M003`
  - 로그인 실패 (이메일/비밀번호 불일치)
- `M004`
  - 탈퇴한 회원
- `B001`
  - 도서 없음
- `S001`
  - 서재 없음
- `N001`
  - 노트 없음
- `N002`
  - 댓글 없음
- `N003`
  - 댓글 신고 중복 또는 알림 없음 (코드 충돌 존재 — 코드 참고)
- `N004`
  - 회원 신고 중복
- `R001`
  - 리뷰 없음
- `O001`
  - 공지사항 없음
- `Q001`
  - 문의 없음
- `F001`
  - 이미 팔로우한 회원
- `F002`
  - 자기 자신 팔로우 불가
- `F003`
  - 팔로우 관계 없음
- `F004`
  - 이미 언팔로우 상태
- `K001`
  - 이미 차단한 회원
- `K002`
  - 자기 자신 차단 불가
- `K003`
  - 차단 관계 없음
- `P001`
  - 자기 자신 신고 불가

## 프론트 체크리스트

- 로그인 후 access/refresh token 둘 다 저장
- refresh 성공 시 access/refresh token 둘 다 교체 (Rotation 방식)
- 비로그인 빈 배열 정책 API는 `401`이 아니라 `200 + empty content`로 처리
- validation 실패는 `C002`와 `message`를 그대로 사용자에게 노출 가능
- 직접 등록 도서 수정은 등록자만 가능하므로 `A002 ACCESS_DENIED` 처리 필요
- 관리자 전용 공지/문의 API는 토큰이 있어도 일반 사용자면 `A002` 처리
- 팔로우/차단/신고는 본인 대상 시도 시 각각 `F002`, `K002`, `P001` 처리 필요
- 알림 기능 이용 시 FCM 토큰을 로그인 후 등록, 로그아웃 시 전체 삭제
- `N003`은 댓글 신고 중복(`COMMENT_REPORT_ALREADY_EXISTS`)과 알림 없음(`NOTIFICATION_NOT_FOUND`)에 동시 사용됨 (코드 충돌)
