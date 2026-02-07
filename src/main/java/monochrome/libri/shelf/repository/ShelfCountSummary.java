package monochrome.libri.shelf.repository;

public record ShelfCountSummary(
        int wantToReadCount,
        int readingCount,
        int finishedCount
) {
    public static ShelfCountSummary empty() {
        return new ShelfCountSummary(0, 0, 0);
    }
}
