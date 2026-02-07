package monochrome.libri.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequestDto(
        @NotNull
        Long bookId,
        @NotNull
        @Min(1)
        @Max(5)
        Integer rating,
        @NotBlank
        @Size(max = 300)
        String content
) {
}
