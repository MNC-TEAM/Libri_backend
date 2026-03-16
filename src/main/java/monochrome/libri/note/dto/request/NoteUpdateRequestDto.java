package monochrome.libri.note.dto.request;

import jakarta.validation.constraints.Size;

public record NoteUpdateRequestDto(
        @Size(max = 2000, message = "노트는 2000자 이하여야 합니다.")
        String content,
        Boolean secret,
        Long shelfId,
        monochrome.libri.note.domain.NoteProgressType progressType,
        Integer progressValue
) {
}
