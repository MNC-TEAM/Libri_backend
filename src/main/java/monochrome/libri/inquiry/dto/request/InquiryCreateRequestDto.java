package monochrome.libri.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;

public record InquiryCreateRequestDto(
        @NotBlank(message = "문의 제목은 필수입니다.")
        String title,
        @NotBlank(message = "문의 내용은 필수입니다.")
        String content
) {
}
