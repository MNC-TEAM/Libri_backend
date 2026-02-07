package monochrome.libri.note.dto.request;

public record NoteUpdateRequestDto(
        String content,
        Boolean secret,
        Long shelfId,
        monochrome.libri.note.domain.NoteProgressType progressType,
        Integer progressValue
) {
}
