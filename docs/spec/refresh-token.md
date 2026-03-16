# Refresh Token Guide

이 문서는 현재 백엔드에 추가된 `refresh token` 인증 흐름을 기준으로
프론트엔드 요청 방식, 응답 형식, 토큰 사용 순서, 서버 운영 체크리스트를 정리한다.

## 목적

- access token 만료 후 사용자가 다시 로그인하지 않고 토큰을 재발급할 수 있게 한다.
- 로그아웃 시 refresh token을 서버에서 제거해 세션을 종료할 수 있게 한다.
- access token과 refresh token의 역할을 분리한다.

## 토큰 정책

- `access token`
  - API 인증에 사용
  - 수명이 짧음
  - `Authorization: Bearer <accessToken>` 헤더에 담아 보냄
- `refresh token`
  - access token 재발급에만 사용
  - 수명이 김
  - 일반 API 인증 헤더에 넣지 않음
  - `/api/v1/auth/refresh`, `/api/v1/auth/logout` 요청 body로 보냄

## 백엔드 API

### 1. 이메일 로그인

- `POST /api/v1/auth/login/email`

요청:

```json
{
  "email": "user@test.com",
  "rawPassword": "password"
}
```

응답:

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
      "id": 1,
      "provider": "EMAIL",
      "email": "user@test.com",
      "nickname": "nick1",
      "profilePath": "/profile/1"
    }
  }
}
```

프론트 처리:

- `accessToken` 저장
- `refreshToken` 저장
- 이후 인증이 필요한 API는 `Authorization` 헤더에 access token만 사용

### 2. 토큰 재발급

- `POST /api/v1/auth/refresh`

요청:

```json
{
  "refreshToken": "REFRESH_TOKEN"
}
```

응답:

```json
{
  "success": true,
  "code": "OK",
  "message": null,
  "data": {
    "tokenType": "Bearer",
    "accessToken": "NEW_ACCESS_TOKEN",
    "refreshToken": "NEW_REFRESH_TOKEN"
  }
}
```

프론트 처리:

- 기존 refresh token 대신 새 refresh token으로 교체
- 기존 access token 대신 새 access token으로 교체
- 재발급 성공 후 실패했던 API를 1회 재시도

주의:

- 이 프로젝트는 refresh token 재발급 시 새 refresh token도 함께 발급하는 rotation 방식이다.
- 따라서 응답으로 받은 새 refresh token으로 반드시 교체해야 한다.

### 3. 로그아웃

- `POST /api/v1/auth/logout`

요청:

```json
{
  "refreshToken": "REFRESH_TOKEN"
}
```

응답:

```json
{
  "success": true,
  "code": "OK",
  "message": null,
  "data": null
}
```

프론트 처리:

- 로그아웃 성공 여부와 관계없이 로컬의 access token, refresh token 제거
- 로그인 화면 또는 비회원 상태로 전환

## 프론트엔드 사용 순서

### 기본 요청 흐름

1. 로그인 성공 시 `accessToken`, `refreshToken`을 저장한다.
2. 인증이 필요한 API 요청 시 access token만 헤더에 담아 보낸다.
3. access token 만료로 401이 오면 refresh API를 호출한다.
4. refresh 성공 시 새 토큰으로 교체하고 원래 요청을 다시 보낸다.
5. refresh 실패 시 로그아웃 처리하고 로그인 화면으로 보낸다.

### 권장 저장 위치

- access token: 메모리 또는 프론트 상태 저장소
- refresh token:
  - 웹: 우선 현재 구현 기준으로 localStorage/sessionStorage 또는 쿠키 중 하나를 선택
  - 앱: secure storage 권장

참고:

- 현재 백엔드는 refresh token을 body로 받는다.
- 아직 HttpOnly 쿠키 기반 구조는 아니다.

### 프론트 예시 흐름

```ts
async function authorizedFetch(input: RequestInfo, init: RequestInit = {}) {
  const accessToken = getAccessToken();

  const response = await fetch(input, {
    ...init,
    headers: {
      ...init.headers,
      Authorization: `Bearer ${accessToken}`,
      "Content-Type": "application/json"
    }
  });

  if (response.status !== 401) {
    return response;
  }

  const refreshToken = getRefreshToken();
  const refreshResponse = await fetch("/api/v1/auth/refresh", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ refreshToken })
  });

  if (!refreshResponse.ok) {
    clearTokens();
    throw new Error("RELOGIN_REQUIRED");
  }

  const refreshResult = await refreshResponse.json();
  saveAccessToken(refreshResult.data.accessToken);
  saveRefreshToken(refreshResult.data.refreshToken);

  return fetch(input, {
    ...init,
    headers: {
      ...init.headers,
      Authorization: `Bearer ${refreshResult.data.accessToken}`,
      "Content-Type": "application/json"
    }
  });
}
```

## 서버 동작 방식

### 로그인 시

- access token 발급
- refresh token 발급
- Redis에 `refresh:member:{memberId}` 키로 refresh token 저장
- TTL은 refresh token 만료 시간과 동일하게 설정

### 재발급 시

- refresh token 존재 여부 확인
- JWT 서명/만료 검증
- 토큰 타입이 `REFRESH`인지 확인
- token의 memberId 추출
- Redis 저장값과 비교
- 일치하면 새 access token, 새 refresh token 발급
- Redis 저장값을 새 refresh token으로 교체

### 로그아웃 시

- refresh token 유효성 확인
- token의 memberId 추출
- Redis의 해당 refresh token 삭제

## 서버 체크리스트

### 필수 환경 변수

- `JWT_SECRET`
- `JWT_ACCESS_EXPIRATION_SECONDS`
- `JWT_REFRESH_EXPIRATION_SECONDS`
- `JWT_ISSUER`
- `REDIS_HOST`
- `REDIS_PORT`

### 운영 시 확인할 것

- Redis 연결 가능 여부
- refresh token 만료 시간 정책
- access token 만료 시간 정책
- 서버 재배포 후 Redis 연결 설정 유지 여부

## 에러 코드

- `A003 INVALID_REFRESH_TOKEN`
  - refresh token이 비어 있거나
  - JWT 형식이 잘못됐거나
  - 만료됐거나
  - Redis 저장값과 일치하지 않을 때
- `A004 TOKEN_TYPE_MISMATCH`
  - access token을 refresh API에 넣는 등 토큰 타입이 맞지 않을 때

## 프론트 주의사항

- 일반 API 호출에는 refresh token을 보내지 않는다.
- access token 만료 시에만 refresh API를 호출한다.
- refresh 성공 시 새 refresh token으로 반드시 교체한다.
- logout 요청 후에는 로컬 토큰을 즉시 삭제한다.
- refresh 실패 시 무한 재시도하지 않는다.

## 현재 구현 범위

- 이메일 로그인 기반 access/refresh token 발급
- refresh token Redis 저장
- refresh API로 토큰 재발급
- logout API로 refresh token 삭제

## 아직 하지 않은 것

- 다중 기기 로그인 세션 분리
- refresh token 탈취 감지
- access token 블랙리스트
- HttpOnly 쿠키 기반 refresh token 운용
