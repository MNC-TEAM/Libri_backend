package monochrome.libri.note.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.block.service.BlockService;
import monochrome.libri.book.repository.BookRepository;
import monochrome.libri.comment.repository.NoteCommentRepository;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.note.domain.Note;
import monochrome.libri.note.domain.NoteProgressType;
import monochrome.libri.note.dto.request.NoteCreateRequestDto;
import monochrome.libri.note.dto.request.NoteUpdateRequestDto;
import monochrome.libri.note.dto.response.NoteDetailResponseDto;
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
    private BookRepository bookRepository;

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

    @Mock
    private BlockService blockService;

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
        when(blockService.hasBlockRelation(999L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> service.getNoteDetail(10L, 999L))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void getNoteDetail_deniesBlockedMember() {
        Member owner = TestFixtures.member(1L);
        Shelf shelf = TestFixtures.shelf(2L, owner, TestFixtures.book(3L, 100));
        Note note = TestFixtures.note(10L, shelf, owner, false);

        when(noteRepository.findWithBookById(10L)).thenReturn(Optional.of(note));
        when(blockService.hasBlockRelation(999L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.getNoteDetail(10L, 999L))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void getNoteDetail_includesShelfIdForOwner() {
        Member owner = TestFixtures.member(1L);
        Shelf shelf = TestFixtures.shelf(2L, owner, TestFixtures.book(3L, 100));
        Note note = TestFixtures.note(10L, shelf, owner, false);

        when(noteRepository.findWithBookById(10L)).thenReturn(Optional.of(note));
        when(noteLikeRepository.existsByNoteIdAndMemberId(10L, 1L)).thenReturn(false);
        when(noteBookmarkRepository.existsByNoteIdAndMemberId(10L, 1L)).thenReturn(false);
        when(noteCommentRepository.countByNoteId(10L)).thenReturn(0L);

        NoteDetailResponseDto response = service.getNoteDetail(10L, 1L);

        assertThat(response.shelfId()).isEqualTo(2L);
        assertThat(response.isOwner()).isTrue();
    }

    @Test
    void getPublicNotesByBook_filtersBlockedMembers() {
        Member viewer = TestFixtures.member(1L);
        Member blockedAuthor = TestFixtures.member(2L);
        Shelf shelf = TestFixtures.shelf(20L, blockedAuthor, TestFixtures.book(3L, 100));
        Note note = TestFixtures.note(10L, shelf, blockedAuthor, false);

        when(bookRepository.existsById(3L)).thenReturn(true);
        when(blockService.getBlockedRelationMemberIds(1L)).thenReturn(java.util.Set.of(2L));
        when(noteRepository.findByShelfBookIdAndSecretFalseAndMemberIdNotInOrderByCreatedDateDesc(
                eq(3L), eq(java.util.Set.of(2L)), any()
        ))
                .thenReturn(new SliceImpl<>(List.of(note), PageRequest.of(0, 10), false));
        when(noteCommentRepository.countByNoteIds(List.of(10L))).thenReturn(List.of());
        when(noteLikeRepository.findNoteIdsByMemberIdAndNoteIdIn(1L, List.of(10L))).thenReturn(List.of());
        when(noteBookmarkRepository.findNoteIdsByMemberIdAndNoteIdIn(1L, List.of(10L))).thenReturn(List.of());

        var response = service.getPublicNotesByBook(3L, 1L, PageRequest.of(0, 10));

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).memberId()).isEqualTo(2L);
        verify(noteRepository).findByShelfBookIdAndSecretFalseAndMemberIdNotInOrderByCreatedDateDesc(
                eq(3L), eq(java.util.Set.of(2L)), any()
        );
        verify(noteRepository, never()).findByShelfBookIdAndSecretFalseOrderByCreatedDateDesc(anyLong(), any());
    }

    @Test
    void getPublicNotesByBook_usesDefaultQueryWhenNoBlockedMembers() {
        Member author = TestFixtures.member(2L);
        Shelf shelf = TestFixtures.shelf(20L, author, TestFixtures.book(3L, 100));
        Note note = TestFixtures.note(10L, shelf, author, false);

        when(bookRepository.existsById(3L)).thenReturn(true);
        when(blockService.getBlockedRelationMemberIds(1L)).thenReturn(java.util.Set.of());
        when(noteRepository.findByShelfBookIdAndSecretFalseOrderByCreatedDateDesc(eq(3L), any()))
                .thenReturn(new SliceImpl<>(List.of(note), PageRequest.of(0, 10), false));
        when(noteCommentRepository.countByNoteIds(List.of(10L))).thenReturn(List.of());
        when(noteLikeRepository.findNoteIdsByMemberIdAndNoteIdIn(1L, List.of(10L))).thenReturn(List.of());
        when(noteBookmarkRepository.findNoteIdsByMemberIdAndNoteIdIn(1L, List.of(10L))).thenReturn(List.of());

        var response = service.getPublicNotesByBook(3L, 1L, PageRequest.of(0, 10));

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).shelfId()).isEqualTo(20L);
        verify(noteRepository).findByShelfBookIdAndSecretFalseOrderByCreatedDateDesc(eq(3L), any());
    }

    @Test
    void getPublicNotesByBook_throwsWhenBookMissing() {
        when(bookRepository.existsById(3L)).thenReturn(false);

        assertThatThrownBy(() -> service.getPublicNotesByBook(3L, 1L, PageRequest.of(0, 10)))
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
