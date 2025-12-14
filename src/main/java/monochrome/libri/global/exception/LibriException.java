package monochrome.libri.global.exception;

public class LibriException extends RuntimeException{

    private final ErrorCode errorCode;

    public LibriException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public LibriException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public LibriException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public LibriException(Throwable cause, ErrorCode errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
