package monochrome.libri.inquiry.dto.request;

import monochrome.libri.inquiry.domain.InquiryStatus;

public record InquiryStatusUpdateRequestDto(
        InquiryStatus status
) {
}
