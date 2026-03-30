package monochrome.libri.notice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NoticeCreateRequestDto(
        @NotBlank(message = "공지사항 제목은 필수입니다.")
        String title,
        @NotBlank(message = "공지사항 내용은 필수입니다.")
        String content,
        Boolean pinned,
        Boolean published
) {
}
