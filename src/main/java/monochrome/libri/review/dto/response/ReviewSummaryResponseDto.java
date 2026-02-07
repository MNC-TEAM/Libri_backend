package monochrome.libri.review.dto.response;

public record ReviewSummaryResponseDto(
        Long reviewId,
        Long memberId,
        String nickname,
        int rating,
        String content
) {
}
