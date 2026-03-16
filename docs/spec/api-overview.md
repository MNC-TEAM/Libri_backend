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

### 회원 탈퇴

- `DELETE /members/withdraw`
- Auth: 필요

출력:

- `data: null`

대표 에러:

- `A001 AUTHENTICATION_FAILED`
- `M001 MEMBER_NOT_FOUND`

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

비고:

- 현재는 설정 저장까지 적용
- 실제 공개 범위 제어는 후속 정책 반영 필요

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

## 대표 에러 코드

- `C002`
  - 잘못된 요청
  - validation 실패 포함
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
  - 로그인 실패
- `B001`
  - 도서 없음
- `S001`
  - 서재 없음
- `N001`
  - 노트 없음
- `N002`
  - 댓글 없음
- `R001`
  - 리뷰 없음

## 프론트 체크리스트

- 로그인 후 access/refresh token 둘 다 저장
- refresh 성공 시 access/refresh token 둘 다 교체
- 비로그인 빈 배열 정책 API는 `401`이 아니라 `200 + empty content`로 처리
- validation 실패는 `C002`와 `message`를 그대로 사용자에게 노출 가능
- 직접 등록 도서 수정은 등록자만 가능하므로 `ACCESS_DENIED` 처리 필요
- 비공개 계정 설정은 현재 저장까지 반영되며, 실제 접근 제한 정책은 후속 반영 가능성 있음
