package monochrome.libri.inquiry.dto.response;

import monochrome.libri.inquiry.domain.Inquiry;

import java.time.LocalDateTime;

public record InquiryDetailResponseDto(
        long inquiryId,
        long memberId,
        String memberNickname,
        String title,
        String content,
        monochrome.libri.inquiry.domain.InquiryStatus status,
        String answerContent,
        LocalDateTime answeredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InquiryDetailResponseDto from(Inquiry inquiry) {
        return new InquiryDetailResponseDto(
                inquiry.getId(),
                inquiry.getMember().getId(),
                inquiry.getMember().getNickname(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getStatus(),
                inquiry.getAnswerContent(),
                inquiry.getAnsweredAt(),
                inquiry.getCreatedDate(),
                inquiry.getLastModifiedDate()
        );
    }
}
