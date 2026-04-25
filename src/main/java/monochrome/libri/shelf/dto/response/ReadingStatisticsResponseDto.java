package monochrome.libri.shelf.dto.response;

import java.util.List;

public record ReadingStatisticsResponseDto(
        int selectedYear,
        long totalStartedCount,
        long totalFinishedCount,
        List<YearlyCount> yearlyFinishedCounts,
        List<MonthlyCount> monthlyFinishedCounts,
        List<MonthlyCount> monthlyCumulativeFinishedCounts
) {
    public record YearlyCount(
            int year,
            long count
    ) {
    }

    public record MonthlyCount(
            int month,
            long count
    ) {
    }
}
