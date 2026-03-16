package monochrome.libri.comment.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.comment.domain.NoteComment;
import monochrome.libri.comment.dto.request.CommentCreateRequestDto;
import monochrome.libri.comment.repository.NoteCommentRepository;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.note.domain.Note;
import monochrome.libri.note.repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class NoteCommentServiceImplTest {

    @Mock
    private NoteCommentRepository commentRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private monochrome.libri.comment.service.impl.NoteCommentServiceImpl service;

    @Test
    void createComment_rejectsInvalidMemberId() {
        assertThatThrownBy(() -> service.createComment(1L, 0L, new CommentCreateRequestDto("hi")))
                .isInstanceOf(LibriException.class);
        verifyNoInteractions(noteRepository, memberService, commentRepository);
    }

    @Test
    void createComment_rejectsBlankContent() {
        assertThatThrownBy(() -> service.createComment(1L, 1L, new CommentCreateRequestDto(" ")))
                .isInstanceOf(LibriException.class);
        verifyNoInteractions(noteRepository, memberService, commentRepository);
    }

    @Test
    void createComment_savesTrimmedContent() {
        Member member = TestFixtures.member(1L);
        Note note = TestFixtures.note(2L, TestFixtures.shelf(3L, member, TestFixtures.book(4L, 100)), member, false);

        when(noteRepository.findWithMemberById(2L)).thenReturn(Optional.of(note));
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member));

        service.createComment(2L, 1L, new CommentCreateRequestDto("  hello "));

        ArgumentCaptor<NoteComment> captor = ArgumentCaptor.forClass(NoteComment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("hello");
    }

    @Test
    void getComments_deniesSecretNoteForNonOwner() {
        Member owner = TestFixtures.member(1L);
        Note note = TestFixtures.note(2L, TestFixtures.shelf(3L, owner, TestFixtures.book(4L, 100)), owner, true);
        when(noteRepository.findWithMemberById(2L)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> service.getComments(2L, 999L, PageRequest.of(0, 10)))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void getComments_mapsSlice() {
        Member member = TestFixtures.member(1L);
        Note note = TestFixtures.note(2L, TestFixtures.shelf(3L, member, TestFixtures.book(4L, 100)), member, false);
        NoteComment comment = NoteComment.builder()
                .id(10L)
                .note(note)
                .member(member)
                .content("hey")
                .build();

        when(noteRepository.findWithMemberById(2L)).thenReturn(Optional.of(note));
        when(commentRepository.findByNoteIdOrderByCreatedDateDesc(eq(2L), any()))
                .thenReturn(new SliceImpl<>(List.of(comment), PageRequest.of(0, 10), false));
        when(commentRepository.countByNoteId(2L)).thenReturn(1L);

        var response = service.getComments(2L, 1L, PageRequest.of(0, 10));

        assertThat(response.totalCount()).isEqualTo(1L);
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).isOwner()).isTrue();
    }

    @Test
    void getCommentsByMember_returnsEmptyWhenUnauthenticated() {
        var response = service.getCommentsByMember(0L, PageRequest.of(0, 10));

        assertThat(response.totalCount()).isZero();
        assertThat(response.content()).isEmpty();
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    void deleteComment_deniesNotOwner() {
        Member owner = TestFixtures.member(1L);
        Note note = TestFixtures.note(2L, TestFixtures.shelf(3L, owner, TestFixtures.book(4L, 100)), owner, false);
        NoteComment comment = NoteComment.builder()
                .id(10L)
                .note(note)
                .member(owner)
                .content("hey")
                .build();

        when(commentRepository.findByIdAndNoteId(10L, 2L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> service.deleteComment(2L, 10L, 999L))
                .isInstanceOf(LibriException.class);
        verify(commentRepository, never()).delete(any());
    }
}
