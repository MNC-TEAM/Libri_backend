# Libri 도메인 ERD

JPA `@Entity` 기준으로 `src/main/java` 하위 도메인을 정리했습니다. 공통 상위 타입 `AuditableEntity`(`@MappedSuperclass`)는 테이블이 없으며, 모든 엔티티에 `created_date`, `last_modified_date`, `created_by`, `last_modified_by`가 포함됩니다.

## 엔티티 목록

| 엔티티 | 테이블명(명시/기본) | 패키지 |
|--------|---------------------|--------|
| Member | `member` | `member.domain` |
| MemberReport | `member_report` | `member.domain` |
| MemberBlock | `member_block` | `block.domain` |
| FcmNotificationToken | `fcm_notification_token` | `fcm.domain` |
| Book | `book` (기본) | `book.domain` |
| Shelf | `shelf` | `shelf.domain` |
| Note | `note` | `note.domain` |
| NoteLike | `note_like` | `note.domain` |
| NoteBookmark | `note_bookmark` | `note.domain` |
| NoteComment | `note_comment` | `comment.domain` |
| NoteCommentReport | `note_comment_report` | `comment.domain` |
| Follow | `follow` (기본) | `follow.domain` |
| Review | `review` | `review.domain` |
| ReviewBookmark | `review_bookmark` | `review.domain` |
| SearchKeyword | `search_keyword` | `search.domain` |
| Notification | `notification` | `notification.domain` |
| Notice | `notice` (기본) | `notice.domain` |
| Inquiry | `inquiry` (기본) | `inquiry.domain` |

## ERD (Mermaid)

GitHub·GitLab·VS Code(Mermaid 확장) 등에서 렌더링할 수 있습니다.

```mermaid
erDiagram
    member ||--o{ fcm_notification_token : "member_id"
    member ||--o{ shelf : "member_id"
    book ||--o{ shelf : "book_id"
    member ||--o{ book : "registered_by_member_id (nullable)"

    shelf ||--o{ note : "shelf_id"
    member ||--o{ note : "member_id (author)"

    note ||--o{ note_like : "note_id"
    member ||--o{ note_like : "member_id"

    note ||--o{ note_bookmark : "note_id"
    member ||--o{ note_bookmark : "member_id"

    note ||--o{ note_comment : "note_id"
    member ||--o{ note_comment : "member_id"

    note_comment ||--o{ note_comment_report : "note_comment_id"
    member ||--o{ note_comment_report : "reporter_member_id"

    member ||--o{ follow : "follow_id (follower)"
    member ||--o{ follow : "following_id (following)"

    member ||--o{ member_report : "reported_member_id"
    member ||--o{ member_report : "reporter_member_id"

    member ||--o{ member_block : "blocker_member_id"
    member ||--o{ member_block : "blocked_member_id"

    book ||--o{ review : "book_id"
    member ||--o{ review : "member_id"

    review ||--o{ review_bookmark : "review_id"
    member ||--o{ review_bookmark : "member_id"

    member ||--o{ search_keyword : "member_id"
    member ||--o{ inquiry : "member_id"

    member {
        bigint member_id PK
        string provider
        string provider_user_id
        string email
        boolean email_verified
        string password_hash
        string username
        string nickname
        string profile_path
        boolean private_account
        string member_status
        string role
    }

    fcm_notification_token {
        bigint fcm_notification_token_id PK
        bigint member_id FK
        string token
        datetime last_used_date
    }

    book {
        bigint book_id PK
        string title
        string author
        string introduction
        string publisher
        date release_date
        int total_page
        string isbn
        string sale_page_url
        string cover_image_url
        bigint registered_by_member_id FK "nullable"
    }

    shelf {
        bigint shelf_id PK
        bigint member_id FK
        bigint book_id FK
        string status
        string progress_type
        int progress_value
        date start_date
        date end_date
        int current_page
    }

    note {
        bigint note_id PK
        bigint shelf_id FK
        bigint member_id FK
        string content
        boolean secret
        string progress_type
        int progress_value
        int like_count
        int bookmark_count
    }

    note_like {
        bigint note_like_id PK
        bigint note_id FK
        bigint member_id FK
    }

    note_bookmark {
        bigint note_bookmark_id PK
        bigint note_id FK
        bigint member_id FK
    }

    note_comment {
        bigint note_comment_id PK
        bigint note_id FK
        bigint member_id FK
        string content
    }

    note_comment_report {
        bigint note_comment_report_id PK
        bigint note_comment_id FK
        bigint reporter_member_id FK
        string reason
        string detail
    }

    member_report {
        bigint member_report_id PK
        bigint reported_member_id FK
        bigint reporter_member_id FK
        string reason
        string detail
    }

    member_block {
        bigint member_block_id PK
        bigint blocker_member_id FK
        bigint blocked_member_id FK
    }

    follow {
        bigint member_follow PK
        bigint follow_id FK
        bigint following_id FK
        string follow_status
    }

    review {
        bigint review_id PK
        bigint book_id FK
        bigint member_id FK
        int rating
        string content
        string status
    }

    review_bookmark {
        bigint review_bookmark_id PK
        bigint review_id FK
        bigint member_id FK
    }

    search_keyword {
        bigint search_keyword_id PK
        bigint member_id FK
        string keyword
    }

    inquiry {
        bigint inquiry_id PK
        bigint member_id FK
        string title
        string content
        string status
        string answer_content
        datetime answered_at
    }

    notice {
        bigint notice_id PK
        string title
        string content
        boolean pinned
        boolean published
    }

    notification {
        bigint notification_id PK
        bigint recipient_member_id "FK 아님, Long"
        bigint actor_member_id "FK 아님, Long"
        string actor_profile_path
        bigint note_id "FK 아님, Long"
        string content
        string notification_type
        boolean is_read
        boolean is_deleted
    }
```

## Notification (논리적 연결)

`Notification`은 `recipient_member_id`, `actor_member_id`, `note_id`를 **Long 컬럼**으로만 들고 있으며, JPA `@ManyToOne`은 없습니다. DB FK 제약이 없을 수 있으나, 의미상은 다음과 같습니다.

- `recipient_member_id` → 수신자 `member.member_id`
- `actor_member_id` → 행위자 `member.member_id`
- `note_id` → `note.note_id`

## 유니크 제약 (요약)

- `member`: `email`, `(provider, provider_user_id)`
- `shelf`: `(member_id, book_id)`
- `note_like`: `(note_id, member_id)`
- `note_bookmark`: `(note_id, member_id)`
- `note_comment_report`: `(note_comment_id, reporter_member_id)`
- `member_report`: `(reported_member_id, reporter_member_id)`
- `member_block`: `(blocker_member_id, blocked_member_id)`
- `review_bookmark`: `(review_id, member_id)`
- `fcm_notification_token`: `(member_id, token)`

## Follow 엔티티 참고

- PK 컬럼명은 `member_follow`로 매핑되어 있습니다(`@Column(name = "member_follow")`).
- `follow_id` / `following_id` 컬럼명은 도메인상 **팔로우하는 사람 / 팔로우 당하는 사람**에 각각 대응합니다. 코드 필드명은 `follower` / `following`입니다.

---

*생성 기준: 저장소 내 JPA 엔티티 소스. 운영 DB 스키마는 `ddl-auto`·마이그레이션에 따라 일부 타입·제약이 다를 수 있습니다.*
