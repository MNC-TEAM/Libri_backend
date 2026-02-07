package monochrome.libri.review.dto.response;

public record MyReviewItemResponseDto(
        Long reviewId,
        Long bookId,
        String bookTitle,
        int rating,
        String content
) {
}
