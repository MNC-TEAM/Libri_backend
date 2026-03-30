package monochrome.libri.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;

public record InquiryAnswerRequestDto(
        @NotBlank(message = "답변 내용은 필수입니다.")
        String answerContent
) {
}
