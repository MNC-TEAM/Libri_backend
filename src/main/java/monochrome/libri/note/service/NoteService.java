package monochrome.libri.note.service;

import monochrome.libri.note.dto.request.NoteCreateRequestDto;
import monochrome.libri.note.dto.request.NoteUpdateRequestDto;
import monochrome.libri.note.dto.response.NoteBookmarkListResponseDto;
import monochrome.libri.note.dto.response.NoteDetailResponseDto;
import monochrome.libri.note.dto.response.NoteListResponseDto;
import monochrome.libri.note.dto.response.NoteSliceResponseDto;
import monochrome.libri.note.dto.response.NoteSummaryResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoteService {
    void createNote(long shelfId, long memberId, NoteCreateRequestDto request);
    List<NoteSummaryResponseDto> getNotesByShelf(long shelfId, long memberId);
    NoteSliceResponseDto getNotesSliceByShelf(long shelfId, long memberId, Pageable pageable);
    NoteListResponseDto getNotesByMember(long memberId, Pageable pageable);
    NoteDetailResponseDto getNoteDetail(long noteId, long memberId);
    NoteBookmarkListResponseDto getBookmarkedNotes(long memberId, Pageable pageable);
    int countNotesByShelf(long shelfId);
    NoteSummaryResponseDto updateNote(long shelfId, long noteId, long memberId, NoteUpdateRequestDto request);
    void deleteNote(long shelfId, long noteId, long memberId);
}
