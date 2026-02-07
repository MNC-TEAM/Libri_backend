package monochrome.libri.review.dto.response;

public record ReviewStatsResponseDto(
        double avgRating,
        long reviewCount,
        long rating1,
        long rating2,
        long rating3,
        long rating4,
        long rating5
) {
}
