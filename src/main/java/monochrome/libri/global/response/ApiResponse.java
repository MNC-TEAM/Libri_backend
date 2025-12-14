package monochrome.libri.global.response;

public record ApiResponse<T>(
        boolean success,        // 성공 여부
        String code,            // 성공/실패 공통 코드
        String message,         // 메시지
        T data                  // 성공 시 데이터, 실패 시 null
) {
    // 성공 응답용
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", null, data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, "OK", message, data);
    }

    // 실패 응답용 (ErrorCode 기반)
    public static <T> ApiResponse<T> error(ErrorResponse error) {
        return new ApiResponse<>(false, error.code(), error.message(), null);
    }

    // ✅ 데이터 없이 성공만 알리고 싶을 때 (logout, delete 등)
    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, "OK", null, null);
    }
}