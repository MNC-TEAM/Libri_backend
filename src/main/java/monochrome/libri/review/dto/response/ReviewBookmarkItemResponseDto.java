package monochrome.libri.review.dto.response;

public record ReviewBookmarkItemResponseDto(
        Long reviewId,
        Long bookId,
        String bookTitle,
        String reviewerNickname,
        int rating,
        String content
) {
}
