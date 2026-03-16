package monochrome.libri.note.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import monochrome.libri.note.domain.NoteProgressType;

public record NoteCreateRequestDto(
        @NotBlank(message = "노트 내용은 필수입니다.")
        @Size(max = 2000, message = "노트는 2000자 이하여야 합니다.")
        String content,
        Boolean secret,
        @NotNull(message = "진행도 타입은 필수입니다.")
        NoteProgressType progressType,
        @NotNull(message = "진행도 값은 필수입니다.")
        Integer progressValue
){
}
