package monochrome.libri.note.service.impl;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.note.domain.Note;
import monochrome.libri.note.domain.NoteBookmark;
import monochrome.libri.note.domain.NoteLike;
import monochrome.libri.note.repository.NoteBookmarkRepository;
import monochrome.libri.note.repository.NoteLikeRepository;
import monochrome.libri.note.repository.NoteRepository;
import monochrome.libri.note.service.NoteReactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NoteReactionServiceImpl implements NoteReactionService {

    private final NoteRepository noteRepository;
    private final NoteLikeRepository noteLikeRepository;
    private final NoteBookmarkRepository noteBookmarkRepository;
    private final MemberService memberService;

    public NoteReactionServiceImpl(
            NoteRepository noteRepository,
            NoteLikeRepository noteLikeRepository,
            NoteBookmarkRepository noteBookmarkRepository,
            MemberService memberService
    ) {
        this.noteRepository = noteRepository;
        this.noteLikeRepository = noteLikeRepository;
        this.noteBookmarkRepository = noteBookmarkRepository;
        this.memberService = memberService;
    }

    @Override
    @Transactional
    public void like(long noteId, long memberId) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Note note = getAccessibleNote(noteId, memberId);
        if (noteLikeRepository.existsByNoteIdAndMemberId(noteId, memberId)) {
            return;
        }

        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));

        NoteLike like = NoteLike.builder()
                .note(note)
                .member(member)
                .build();

        noteLikeRepository.save(like);
        note.increaseLikeCount();
    }

    @Override
    @Transactional
    public void unlike(long noteId, long memberId) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Note note = getAccessibleNote(noteId, memberId);
        if (!noteLikeRepository.existsByNoteIdAndMemberId(noteId, memberId)) {
            return;
        }

        noteLikeRepository.deleteByNoteIdAndMemberId(noteId, memberId);
        note.decreaseLikeCount();
    }

    @Override
    @Transactional
    public void bookmark(long noteId, long memberId) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Note note = getAccessibleNote(noteId, memberId);
        if (noteBookmarkRepository.existsByNoteIdAndMemberId(noteId, memberId)) {
            return;
        }

        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));

        NoteBookmark bookmark = NoteBookmark.builder()
                .note(note)
                .member(member)
                .build();

        noteBookmarkRepository.save(bookmark);
        note.increaseBookmarkCount();
    }

    @Override
    @Transactional
    public void unbookmark(long noteId, long memberId) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Note note = getAccessibleNote(noteId, memberId);
        if (!noteBookmarkRepository.existsByNoteIdAndMemberId(noteId, memberId)) {
            return;
        }

        noteBookmarkRepository.deleteByNoteIdAndMemberId(noteId, memberId);
        note.decreaseBookmarkCount();
    }

    private Note getAccessibleNote(long noteId, long memberId) {
        Note note = noteRepository.findWithMemberById(noteId)
                .orElseThrow(() -> new LibriException(ErrorCode.NOTE_NOT_FOUND));
        boolean isOwner = note.getMember().getId() == memberId;
        if (note.isSecret() && !isOwner) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }
        return note;
    }
}
