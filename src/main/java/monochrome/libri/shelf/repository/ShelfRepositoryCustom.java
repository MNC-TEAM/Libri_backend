package monochrome.libri.shelf.repository;

import monochrome.libri.shelf.domain.ShelfStatus;
import monochrome.libri.shelf.dto.ShelfBookRow;

import java.util.List;

public interface ShelfRepositoryCustom {
    ShelfCountSummary findCountSummaryByMemberId(long memberId);

    List<ShelfBookRow> findShelfBooksByStatus(long memberId, ShelfStatus status, int limit);
}
