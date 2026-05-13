# 알림(Notification) API 명세

## 개요

이 문서는 Libri 백엔드의 알림 기능 관련 API를 정리합니다.  
알림 기능은 두 가지 영역으로 구성됩니다.

- **Notification API** — 앱 내 알림 목록 조회·읽음 처리·삭제
- **FCM Token API** — 기기 FCM 토큰 등록·삭제 (푸시 알림 수신용)

### 공통 사항

- 모든 API는 Bearer 토큰 인증이 필요합니다.
- 응답 본문은 아래 공통 포맷을 따릅니다.

```json
{
  "success": true,
  "code": "OK",
  "message": null,
  "data": { ... }
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `success` | boolean | 요청 성공 여부 |
| `code` | string | 성공(`OK`) 또는 오류 코드 |
| `message` | string | 오류 메시지 (성공 시 `null`) |
| `data` | any | 성공 시 반환 데이터 |

---

## 1. 알림 목록 조회

```
GET /api/v1/notifications
```

본인이 받은 알림을 최신순으로 페이지네이션 조회합니다.  
소프트 삭제(`is_deleted = true`)된 알림은 제외됩니다.

### Query Parameters

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `page` | int | `0` | 페이지 번호 (0부터 시작) |
| `size` | int | `20` | 페이지 크기 |

### 성공 응답 `200 OK`

```json
{
  "success": true,
  "code": "OK",
  "message": null,
  "data": {
    "totalCount": 42,
    "content": [
      {
        "id": 10,
        "noteId": 5,
        "content": "홍길동님이 내 노트에 댓글을 달았습니다.",
        "actorProfilePath": "https://example.com/profiles/1.jpg",
        "actorMemberId": 7,
        "read": false,
        "createdAt": "2026-05-09T10:30:00",
        "notificationType": "COMMENT"
      }
    ],
    "hasNext": true,
    "page": 0,
    "size": 20
  }
}
```

**`data` 필드 설명**

| 필드 | 타입 | 설명 |
|---|---|---|
| `totalCount` | long | 삭제되지 않은 전체 알림 수 |
| `content` | array | 현재 페이지 알림 목록 |
| `hasNext` | boolean | 다음 페이지 존재 여부 |
| `page` | int | 현재 페이지 번호 |
| `size` | int | 요청 페이지 크기 |

**`content` 항목 필드 설명**

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | long | 알림 ID |
| `noteId` | long | 알림이 발생한 노트 ID |
| `content` | string | 알림 본문 |
| `actorProfilePath` | string | 행위자 프로필 이미지 경로 |
| `actorMemberId` | long | 알림을 발생시킨 회원 ID |
| `read` | boolean | 읽음 여부 |
| `createdAt` | datetime | 알림 생성 시각 |
| `notificationType` | enum | `LIKED` (좋아요) / `COMMENT` (댓글) |

### 오류 응답

| HTTP | 코드 | 메시지 |
|---|---|---|
| 401 | `A001` | 인증에 실패했습니다. |
| 400 | `C002` | 잘못된 요청입니다. (`page < 0` 또는 `size ≤ 0`) |

---

## 2. 알림 단건 읽음 처리

```
PATCH /api/v1/notifications/{notificationId}/read
```

단일 알림을 읽음(`is_read = true`) 처리합니다.

### Path Parameters

| 파라미터 | 타입 | 설명 |
|---|---|---|
| `notificationId` | long | 읽음 처리할 알림 ID |

### 성공 응답 `200 OK`

```json
{
  "success": true,
  "code": "OK",
  "message": null,
  "data": null
}
```

### 오류 응답

| HTTP | 코드 | 메시지 |
|---|---|---|
| 401 | `A001` | 인증에 실패했습니다. |
| 403 | `A002` | 접근 권한이 없습니다. (본인 알림이 아닌 경우) |
| 404 | `N003` | 알림을 찾을 수 없습니다. |

---

## 3. 알림 전체 읽음 처리

```
PATCH /api/v1/notifications/read-all
```

본인의 모든 미읽음 알림을 일괄 읽음 처리합니다.

### 성공 응답 `200 OK`

```json
{
  "success": true,
  "code": "OK",
  "message": null,
  "data": null
}
```

### 오류 응답

| HTTP | 코드 | 메시지 |
|---|---|---|
| 401 | `A001` | 인증에 실패했습니다. |

---

## 4. 알림 단건 삭제

```
DELETE /api/v1/notifications/{notificationId}
```

본인이 받은 알림을 소프트 삭제합니다. 삭제된 알림은 목록 조회에서 제외됩니다.

### Path Parameters

| 파라미터 | 타입 | 설명 |
|---|---|---|
| `notificationId` | long | 삭제할 알림 ID |

### 성공 응답 `200 OK`

```json
{
  "success": true,
  "code": "OK",
  "message": null,
  "data": null
}
```

### 오류 응답

| HTTP | 코드 | 메시지 |
|---|---|---|
| 401 | `A001` | 인증에 실패했습니다. |
| 403 | `A002` | 접근 권한이 없습니다. (본인 알림이 아닌 경우) |
| 404 | `N003` | 알림을 찾을 수 없습니다. |

---

## 5. 알림 전체 삭제

```
DELETE /api/v1/notifications/all
```

본인이 받은 모든 알림을 소프트 삭제합니다.

### 성공 응답 `200 OK`

```json
{
  "success": true,
  "code": "OK",
  "message": null,
  "data": null
}
```

### 오류 응답

| HTTP | 코드 | 메시지 |
|---|---|---|
| 401 | `A001` | 인증에 실패했습니다. |

---

## 6. FCM 토큰 등록

```
POST /api/v1/fcm-tokens
```

현재 로그인된 회원에 기기 FCM 토큰을 연결합니다.  
동일한 회원+토큰 조합이 이미 존재하면 `last_used_date`만 갱신합니다.  
등록된 토큰은 알림 생성 시 FCM 푸시 발송에 사용됩니다.

### Request Body

```json
{
  "fcmToken": "exxxxxxx..."
}
```

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `fcmToken` | string | 필수, 최대 512자 | Firebase Cloud Messaging 등록 토큰 |

### 성공 응답 `201 Created`

```json
{
  "success": true,
  "code": "OK",
  "message": null,
  "data": null
}
```

### 오류 응답

| HTTP | 코드 | 메시지 |
|---|---|---|
| 401 | `A001` | 인증에 실패했습니다. |
| 404 | `M001` | 존재하지 않는 회원입니다. |
| 400 | `C002` | 잘못된 요청입니다. (`fcmToken` 누락 또는 512자 초과) |

---

## 7. FCM 토큰 전체 삭제

```
DELETE /api/v1/fcm-tokens
```

현재 로그인된 회원에 연결된 모든 FCM 토큰을 삭제합니다.  
로그아웃 또는 푸시 알림 수신 거부 시 호출합니다.

### 성공 응답 `200 OK`

```json
{
  "success": true,
  "code": "OK",
  "message": null,
  "data": null
}
```

### 오류 응답

| HTTP | 코드 | 메시지 |
|---|---|---|
| 401 | `A001` | 인증에 실패했습니다. |
| 404 | `M001` | 존재하지 않는 회원입니다. |

---

## 알림 타입 (`NotificationType`)

| 값 | 설명 |
|---|---|
| `LIKED` | 내 노트에 좋아요가 달린 경우 |
| `COMMENT` | 내 노트에 댓글이 달린 경우 |

---

## 알림 생성 흐름

알림은 클라이언트가 직접 생성하지 않으며, 서버 내부 이벤트(좋아요·댓글)에 의해 자동 생성됩니다.

```
사용자 A가 노트에 좋아요/댓글 발생
        │
        ▼
NotificationService.createNotification()
        │
        ├── DB에 Notification 레코드 저장
        │
        └── FcmPushSender.sendToMember()
                │
                └── 수신자의 FCM 토큰으로 Firebase 푸시 발송
                    (실패해도 DB 저장 트랜잭션에 영향 없음)
```

- 행위자(actorMemberId)와 수신자(recipientMemberId)가 동일하면 알림을 생성하지 않습니다.
- FCM 푸시 전송 실패는 경고 로그만 남기며 알림 저장에는 영향을 주지 않습니다.
- FCM 메시지 `data` 페이로드에는 `notificationId`, `noteId`, `type` 필드가 포함됩니다.
