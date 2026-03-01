package monochrome.libri.note.dto.request;

import jakarta.validation.constraints.Size;

public record NoteUpdateRequestDto(
        @Size(max = 2000)
        String content,
        Boolean secret,
        Long shelfId,
        monochrome.libri.note.domain.NoteProgressType progressType,
        Integer progressValue
) {
}
