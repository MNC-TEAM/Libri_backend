package monochrome.libri.shelf.repository;

import monochrome.libri.shelf.domain.ShelfStatus;
import monochrome.libri.shelf.dto.MonthCountRow;
import monochrome.libri.shelf.dto.ShelfCalendarRow;
import monochrome.libri.shelf.dto.ShelfBookRow;
import monochrome.libri.shelf.dto.YearCountRow;

import java.time.LocalDate;
import java.util.List;

public interface ShelfRepositoryCustom {
    ShelfCountSummary findCountSummaryByMemberId(long memberId);

    List<ShelfBookRow> findShelfBooksByStatus(long memberId, ShelfStatus status, int limit);

    List<ShelfCalendarRow> findCalendarRowsByMemberIdAndDateRange(long memberId, LocalDate from, LocalDate to);

    List<YearCountRow> countFinishedBooksByYear(long memberId);

    List<MonthCountRow> countFinishedBooksByMonth(long memberId, int year);
}
