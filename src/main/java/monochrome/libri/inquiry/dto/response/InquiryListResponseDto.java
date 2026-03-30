package monochrome.libri.inquiry.dto.response;

import java.util.List;

public record InquiryListResponseDto(
        long totalCount,
        List<InquiryListItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
    public static InquiryListResponseDto empty(int page, int size) {
        return new InquiryListResponseDto(0, List.of(), false, page, size);
    }
}
