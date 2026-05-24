package monochrome.libri.comment.service.impl;

import monochrome.libri.block.service.BlockService;
import monochrome.libri.comment.domain.NoteComment;
import monochrome.libri.comment.domain.NoteCommentReport;
import monochrome.libri.comment.dto.request.CommentCreateRequestDto;
import monochrome.libri.comment.dto.request.CommentReportCreateRequestDto;
import monochrome.libri.comment.dto.response.CommentResponseDto;
import monochrome.libri.comment.dto.response.CommentSliceResponseDto;
import monochrome.libri.comment.dto.response.MyCommentItemResponseDto;
import monochrome.libri.comment.dto.response.MyCommentListResponseDto;
import monochrome.libri.comment.repository.NoteCommentReportRepository;
import monochrome.libri.comment.repository.NoteCommentRepository;
import monochrome.libri.comment.service.NoteCommentService;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.note.domain.Note;
import monochrome.libri.note.repository.NoteRepository;
import monochrome.libri.notification.domain.NotificationType;
import monochrome.libri.notification.service.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NoteCommentServiceImpl implements NoteCommentService {

    private final NoteCommentRepository commentRepository;
    private final NoteCommentReportRepository commentReportRepository;
    private final NoteRepository noteRepository;
    private final MemberService memberService;
    private final BlockService blockService;
    private final NotificationService notificationService;

    public NoteCommentServiceImpl(
            NoteCommentRepository commentRepository,
            NoteCommentReportRepository commentReportRepository,
            NoteRepository noteRepository,
            MemberService memberService,
            BlockService blockService,
            NotificationService notificationService
    ) {
        this.commentRepository = commentRepository;
        this.commentReportRepository = commentReportRepository;
        this.noteRepository = noteRepository;
        this.memberService = memberService;
        this.blockService = blockService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void createComment(long noteId, long memberId, CommentCreateRequestDto request) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Note note = noteRepository.findWithMemberById(noteId)
                .orElseThrow(() -> new LibriException(ErrorCode.NOTE_NOT_FOUND));

        if (note.isSecret()) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }
        if (blockService.hasBlockRelation(memberId, note.getMember().getId())) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }

        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));

        NoteComment comment = NoteComment.builder()
                .note(note)
                .member(member)
                .content(request.content().trim())
                .build();

        commentRepository.save(comment);

        notificationService.createNotification(
                note.getMember().getId(),
                memberId,
                member.getProfilePath(),
                noteId,
                member.getNickname() + "님이 댓글을 남겼습니다.",
                NotificationType.COMMENT
        );
    }

    @Override
    @Transactional
    public void reportComment(long noteId, long commentId, long memberId, CommentReportCreateRequestDto request) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (request == null || request.reason() == null) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        NoteComment comment = commentRepository.findWithNoteAndMemberByIdAndNoteId(commentId, noteId)
                .orElseThrow(() -> new LibriException(ErrorCode.COMMENT_NOT_FOUND));
        if (blockService.hasBlockRelation(memberId, comment.getMember().getId())
                || blockService.hasBlockRelation(memberId, comment.getNote().getMember().getId())) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }
        Member reporter = memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));

        if (commentReportRepository.existsByNoteCommentAndReporter(comment, reporter)) {
            throw new LibriException(ErrorCode.COMMENT_REPORT_ALREADY_EXISTS);
        }

        NoteCommentReport report = NoteCommentReport.builder()
                .noteComment(comment)
                .reporter(reporter)
                .reason(request.reason())
                .detail(trimToNull(request.detail()))
                .build();

        commentReportRepository.save(report);
    }

    @Override
    public CommentSliceResponseDto getComments(long noteId, long memberId, Pageable pageable) {
        Note note = noteRepository.findWithMemberById(noteId)
                .orElseThrow(() -> new LibriException(ErrorCode.NOTE_NOT_FOUND));

        boolean isOwner = memberId > 0 && note.getMember().getId() == memberId;
        if (!isOwner && blockService.hasBlockRelation(memberId, note.getMember().getId())) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }
        if (note.isSecret() && !isOwner) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }

        Slice<NoteComment> slice = commentRepository.findByNoteIdOrderByCreatedDateDesc(noteId, pageable);
        List<CommentResponseDto> content = slice.getContent().stream()
                .map(comment -> new CommentResponseDto(
                        comment.getId(),
                        comment.getMember().getId(),
                        comment.getMember().getNickname(),
                        comment.getContent(),
                        comment.getCreatedDate(),
                        comment.getMember().getId() == memberId
                ))
                .toList();

        long totalCount = commentRepository.countByNoteId(noteId);

        return new CommentSliceResponseDto(
                totalCount,
                content,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    @Override
    @Transactional
    public void deleteComment(long noteId, long commentId, long memberId) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        NoteComment comment = commentRepository.findByIdAndNoteId(commentId, noteId)
                .orElseThrow(() -> new LibriException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getMember().getId() != memberId) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }

        commentRepository.delete(comment);
    }

    @Override
    public MyCommentListResponseDto getCommentsByMember(long memberId, Pageable pageable) {
        if (memberId <= 0) {
            return MyCommentListResponseDto.empty(pageable.getPageNumber(), pageable.getPageSize());
        }
        Slice<NoteComment> slice = commentRepository.findByMemberIdOrderByCreatedDateDesc(memberId, pageable);
        List<MyCommentItemResponseDto> content = slice.getContent().stream()
                .map(comment -> new MyCommentItemResponseDto(
                        comment.getId(),
                        comment.getNote().getId(),
                        comment.getNote().getShelf().getBook().getId(),
                        comment.getNote().getShelf().getBook().getTitle(),
                        comment.getNote().getShelf().getBook().getCoverImageUrl(),
                        comment.getNote().getContent(),
                        comment.getContent(),
                        comment.getCreatedDate()
                ))
                .toList();

        long totalCount = commentRepository.countByMemberId(memberId);

        return new MyCommentListResponseDto(
                totalCount,
                content,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
