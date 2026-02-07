package monochrome.libri.note.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import monochrome.libri.note.domain.NoteProgressType;

public record NoteCreateRequestDto(
        @NotBlank
        @Size(max = 2000)
        String content,
        Boolean secret,
        @NotNull
        NoteProgressType progressType,
        @NotNull
        Integer progressValue
){
}
