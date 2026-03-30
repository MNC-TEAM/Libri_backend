package monochrome.libri.notice.dto.request;

public record NoticeUpdateRequestDto(
        String title,
        String content,
        Boolean pinned,
        Boolean published
) {
}
