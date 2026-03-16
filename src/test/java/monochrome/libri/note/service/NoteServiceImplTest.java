package monochrome.libri.note.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.comment.repository.NoteCommentRepository;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.note.domain.Note;
import monochrome.libri.note.domain.NoteProgressType;
import monochrome.libri.note.dto.request.NoteCreateRequestDto;
import monochrome.libri.note.dto.request.NoteUpdateRequestDto;
import monochrome.libri.note.repository.NoteBookmarkRepository;
import monochrome.libri.note.repository.NoteLikeRepository;
import monochrome.libri.note.repository.NoteRepository;
import monochrome.libri.shelf.domain.Shelf;
import monochrome.libri.shelf.repository.ShelfRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private ShelfRepository shelfRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private NoteLikeRepository noteLikeRepository;

    @Mock
    private NoteBookmarkRepository noteBookmarkRepository;

    @Mock
    private NoteCommentRepository noteCommentRepository;

    @InjectMocks
    private monochrome.libri.note.service.impl.NoteServiceImpl service;

    @Test
    void createNote_requiresAuth() {
        NoteCreateRequestDto dto = new NoteCreateRequestDto("content", false, NoteProgressType.PAGE, 1);
        assertThatThrownBy(() -> service.createNote(1L, 0L, dto))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void createNote_validatesProgressAgainstTotalPage() {
        Member member = TestFixtures.member(1L);
        Shelf shelf = TestFixtures.shelf(2L, member, TestFixtures.book(3L, 10));

        when(shelfRepository.findByIdAndMemberId(2L, 1L)).thenReturn(Optional.of(shelf));
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member));

        NoteCreateRequestDto dto = new NoteCreateRequestDto("content", false, NoteProgressType.PAGE, 50);

        assertThatThrownBy(() -> service.createNote(2L, 1L, dto))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void getNoteDetail_deniesSecretNoteForNonOwner() {
        Member owner = TestFixtures.member(1L);
        Shelf shelf = TestFixtures.shelf(2L, owner, TestFixtures.book(3L, 100));
        Note note = TestFixtures.note(10L, shelf, owner, true);

        when(noteRepository.findWithBookById(10L)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> service.getNoteDetail(10L, 999L))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void getNotesByMember_mapsSlice() {
        Member member = TestFixtures.member(1L);
        Shelf shelf = TestFixtures.shelf(2L, member, TestFixtures.book(3L, 100));
        Note note = TestFixtures.note(10L, shelf, member, false);

        when(noteRepository.findByMemberId(eq(1L), any()))
                .thenReturn(new SliceImpl<>(List.of(note), PageRequest.of(0, 10), false));
        when(noteRepository.countByMemberId(1L)).thenReturn(1L);
        when(noteCommentRepository.countByNoteIds(List.of(10L))).thenReturn(List.of());
        when(noteLikeRepository.findNoteIdsByMemberIdAndNoteIdIn(1L, List.of(10L))).thenReturn(List.of());
        when(noteBookmarkRepository.findNoteIdsByMemberIdAndNoteIdIn(1L, List.of(10L))).thenReturn(List.of());

        var response = service.getNotesByMember(1L, PageRequest.of(0, 10));

        assertThat(response.totalCount()).isEqualTo(1L);
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void getNotesByMember_returnsEmptyWhenUnauthenticated() {
        var response = service.getNotesByMember(0L, PageRequest.of(0, 10));

        assertThat(response.totalCount()).isZero();
        assertThat(response.content()).isEmpty();
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    void updateNote_requiresOwner() {
        Member owner = TestFixtures.member(1L);
        Shelf shelf = TestFixtures.shelf(2L, owner, TestFixtures.book(3L, 100));
        Note note = TestFixtures.note(10L, shelf, owner, false);

        when(noteRepository.findByIdAndShelfId(10L, 2L)).thenReturn(Optional.of(note));

        NoteUpdateRequestDto dto = new NoteUpdateRequestDto("new", null, null, null, null);

        assertThatThrownBy(() -> service.updateNote(2L, 10L, 999L, dto))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void deleteNote_requiresOwner() {
        Member owner = TestFixtures.member(1L);
        Shelf shelf = TestFixtures.shelf(2L, owner, TestFixtures.book(3L, 100));
        Note note = TestFixtures.note(10L, shelf, owner, false);

        when(noteRepository.findByIdAndShelfId(10L, 2L)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> service.deleteNote(2L, 10L, 999L))
                .isInstanceOf(LibriException.class);
        verify(noteRepository, never()).delete(any());
    }
}
