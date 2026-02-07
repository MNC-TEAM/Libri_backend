package monochrome.libri.home.dto.response;

import java.util.List;

public record HomeResponseDto(
        MeSummary me,
        ShelfSummary shelfSummary,
        List<ReadingBook> readingBooks,
        List<BookSummary> wantToReadBooks,
        List<BookSummary> finishedBooks,
        List<BookRecommendation> recommendations
) {
    public record MeSummary(
            Long memberId,
            String nickname,
            String profileImageUrl,
            int unreadNotiCount
    ) {
    }

    public record ShelfSummary(
            int wantToReadCount,
            int finishedCount,
            int readingCount
    ) {
    }

    public record ReadingBook(
            Long shelfId,
            Long bookId,
            String title,
            String coverUrl,
            int progressPercent,
            int readingDays
    ) {
    }

    public record BookSummary(
            Long shelfId,
            Long bookId,
            String title,
            String coverUrl
    ) {
    }

    public record BookRecommendation(
            Long bookId,
            String title,
            String author,
            String coverUrl,
            List<String> tags,
            String badgeText
    ) {
    }
}
