package monochrome.libri.shelf.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ReadingCalendarResponseDto(
        int year,
        int month,
        List<CalendarDay> days
) {
    public record CalendarDay(
            LocalDate date,
            List<BookEvent> startedBooks,
            List<BookEvent> finishedBooks
    ) {
    }

    public record BookEvent(
            Long shelfId,
            Long bookId,
            String title,
            String coverUrl
    ) {
    }
}
