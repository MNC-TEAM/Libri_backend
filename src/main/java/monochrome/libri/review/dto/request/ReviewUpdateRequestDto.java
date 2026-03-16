package monochrome.libri.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ReviewUpdateRequestDto(
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
        Integer rating,
        @Size(max = 300, message = "리뷰는 300자 이하여야 합니다.")
        String content
) {
}
