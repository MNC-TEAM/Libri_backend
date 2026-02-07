package monochrome.libri.note.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.note.domain.Note;
import monochrome.libri.note.repository.NoteBookmarkRepository;
import monochrome.libri.note.repository.NoteLikeRepository;
import monochrome.libri.note.repository.NoteRepository;
import monochrome.libri.shelf.domain.Shelf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteReactionServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteLikeRepository noteLikeRepository;

    @Mock
    private NoteBookmarkRepository noteBookmarkRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private monochrome.libri.note.service.impl.NoteReactionServiceImpl service;

    @Test
    void like_deniesSecretNoteForNonOwner() {
        Member owner = TestFixtures.member(1L);
        Shelf shelf = TestFixtures.shelf(2L, owner, TestFixtures.book(3L, 100));
        Note note = TestFixtures.note(10L, shelf, owner, true);

        when(noteRepository.findWithMemberById(10L)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> service.like(10L, 999L))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void like_noopWhenAlreadyLiked() {
        Member owner = TestFixtures.member(1L);
        Shelf shelf = TestFixtures.shelf(2L, owner, TestFixtures.book(3L, 100));
        Note note = TestFixtures.note(10L, shelf, owner, false);

        when(noteRepository.findWithMemberById(10L)).thenReturn(Optional.of(note));
        when(noteLikeRepository.existsByNoteIdAndMemberId(10L, 1L)).thenReturn(true);

        service.like(10L, 1L);

        verify(noteLikeRepository, never()).save(any());
    }

    @Test
    void like_incrementsCountWhenNew() {
        Member owner = TestFixtures.member(1L);
        Shelf shelf = TestFixtures.shelf(2L, owner, TestFixtures.book(3L, 100));
        Note note = TestFixtures.note(10L, shelf, owner, false);

        when(noteRepository.findWithMemberById(10L)).thenReturn(Optional.of(note));
        when(noteLikeRepository.existsByNoteIdAndMemberId(10L, 1L)).thenReturn(false);
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(owner));

        service.like(10L, 1L);

        verify(noteLikeRepository).save(any());
    }

    @Test
    void unlike_decrementsWhenExists() {
        Member owner = TestFixtures.member(1L);
        Shelf shelf = TestFixtures.shelf(2L, owner, TestFixtures.book(3L, 100));
        Note note = TestFixtures.note(10L, shelf, owner, false);
        note.increaseLikeCount();

        when(noteRepository.findWithMemberById(10L)).thenReturn(Optional.of(note));
        when(noteLikeRepository.existsByNoteIdAndMemberId(10L, 1L)).thenReturn(true);

        service.unlike(10L, 1L);

        verify(noteLikeRepository).deleteByNoteIdAndMemberId(10L, 1L);
    }

    @Test
    void bookmark_incrementsCountWhenNew() {
        Member owner = TestFixtures.member(1L);
        Shelf shelf = TestFixtures.shelf(2L, owner, TestFixtures.book(3L, 100));
        Note note = TestFixtures.note(10L, shelf, owner, false);

        when(noteRepository.findWithMemberById(10L)).thenReturn(Optional.of(note));
        when(noteBookmarkRepository.existsByNoteIdAndMemberId(10L, 1L)).thenReturn(false);
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(owner));

        service.bookmark(10L, 1L);

        verify(noteBookmarkRepository).save(any());
    }

    @Test
    void unbookmark_decrementsWhenExists() {
        Member owner = TestFixtures.member(1L);
        Shelf shelf = TestFixtures.shelf(2L, owner, TestFixtures.book(3L, 100));
        Note note = TestFixtures.note(10L, shelf, owner, false);
        note.increaseBookmarkCount();

        when(noteRepository.findWithMemberById(10L)).thenReturn(Optional.of(note));
        when(noteBookmarkRepository.existsByNoteIdAndMemberId(10L, 1L)).thenReturn(true);

        service.unbookmark(10L, 1L);

        verify(noteBookmarkRepository).deleteByNoteIdAndMemberId(10L, 1L);
    }
}
