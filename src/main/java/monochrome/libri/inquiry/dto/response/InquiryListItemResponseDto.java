package monochrome.libri.inquiry.dto.response;

import monochrome.libri.inquiry.domain.InquiryStatus;

import java.time.LocalDateTime;

public record InquiryListItemResponseDto(
        long inquiryId,
        long memberId,
        String memberNickname,
        String title,
        InquiryStatus status,
        LocalDateTime answeredAt,
        LocalDateTime createdAt
) {
}
