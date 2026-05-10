package monochrome.libri.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum ErrorCode {

    // 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 에러가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C002", "잘못된 요청입니다."),
    S3_CONFIGURATION_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "S3 설정이 누락되었습니다."),

    // 인증/인가 관련
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A001", "인증에 실패했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 리프레시 토큰입니다."),
    TOKEN_TYPE_MISMATCH(HttpStatus.UNAUTHORIZED, "A004", "토큰 타입이 올바르지 않습니다."),

    // 회원 관련
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 회원입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "M002", "이미 가입된 이메일입니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "M003", "이메일 또는 비밀번호가 올바르지 않습니다."),
    MEMBER_WITHDRAWN(HttpStatus.BAD_REQUEST, "M004", "탈퇴한 회원입니다."),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "존재하지 않는 도서입니다."),
    SHELF_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "책장 정보를 찾을 수 없습니다."),
    NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "노트를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "N002", "댓글을 찾을 수 없습니다."),
    COMMENT_REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "N003", "이미 신고한 댓글입니다."),
    MEMBER_REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "N004", "이미 신고한 회원입니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "리뷰를 찾을 수 없습니다."),
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "공지사항을 찾을 수 없습니다."),
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "Q001", "문의 내역을 찾을 수 없습니다."),

    // 팔로우
    EXIST_FOLLOW_RELATION(HttpStatus.CONFLICT, "F001", "이미 팔로우한 회원입니다."),
    SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "F002", "자기 자신은 팔로우할 수 없습니다."),
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "F003", "팔로우 관계를 찾을 수 없습니다."),
    ALREADY_UNFOLLOWED(HttpStatus.BAD_REQUEST, "F004", "이미 언팔로우한 상태입니다."),

    // 차단
    BLOCK_ALREADY_EXISTS(HttpStatus.CONFLICT, "K001", "이미 차단한 회원입니다."),
    SELF_BLOCK_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "K002", "자기 자신은 차단할 수 없습니다."),
    BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "K003", "차단 관계를 찾을 수 없습니다."),

    // 신고
    SELF_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "P001", "자기 자신은 신고할 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
