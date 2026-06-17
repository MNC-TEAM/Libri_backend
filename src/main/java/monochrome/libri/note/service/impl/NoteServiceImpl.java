package monochrome.libri.note.service.impl;

import monochrome.libri.block.service.BlockService;
import monochrome.libri.book.repository.BookRepository;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.comment.repository.NoteCommentRepository;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.note.domain.Note;
import monochrome.libri.note.domain.NoteProgressType;
import monochrome.libri.note.dto.request.NoteCreateRequestDto;
import monochrome.libri.note.dto.request.NoteUpdateRequestDto;
import monochrome.libri.note.dto.response.BookNoteItemResponseDto;
import monochrome.libri.note.dto.response.BookNoteSliceResponseDto;
import monochrome.libri.note.dto.response.NoteBookmarkItemResponseDto;
import monochrome.libri.note.dto.response.NoteBookmarkListResponseDto;
import monochrome.libri.note.dto.response.NoteDetailResponseDto;
import monochrome.libri.note.dto.response.NoteListItemResponseDto;
import monochrome.libri.note.dto.response.NoteListResponseDto;
import monochrome.libri.note.dto.response.NoteSliceResponseDto;
import monochrome.libri.note.dto.response.NoteSummaryResponseDto;
import monochrome.libri.note.repository.NoteBookmarkRepository;
import monochrome.libri.note.repository.NoteLikeRepository;
import monochrome.libri.note.repository.NoteRepository;
import monochrome.libri.note.service.NoteService;
import monochrome.libri.shelf.domain.Shelf;
import monochrome.libri.shelf.repository.ShelfRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final BookRepository bookRepository;
    private final ShelfRepository shelfRepository;
    private final MemberService memberService;
    private final NoteLikeRepository noteLikeRepository;
    private final NoteBookmarkRepository noteBookmarkRepository;
    private final NoteCommentRepository noteCommentRepository;
    private final BlockService blockService;

    public NoteServiceImpl(
            NoteRepository noteRepository,
            BookRepository bookRepository,
            ShelfRepository shelfRepository,
            MemberService memberService,
            NoteLikeRepository noteLikeRepository,
            NoteBookmarkRepository noteBookmarkRepository,
            NoteCommentRepository noteCommentRepository,
            BlockService blockService
    ) {
        this.noteRepository = noteRepository;
        this.bookRepository = bookRepository;
        this.shelfRepository = shelfRepository;
        this.memberService = memberService;
        this.noteLikeRepository = noteLikeRepository;
        this.noteBookmarkRepository = noteBookmarkRepository;
        this.noteCommentRepository = noteCommentRepository;
        this.blockService = blockService;
    }

    @Override
    @Transactional
    public void createNote(long shelfId, long memberId, NoteCreateRequestDto request) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Shelf shelf = shelfRepository.findByIdAndMemberId(shelfId, memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.SHELF_NOT_FOUND));

        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));

        validateProgress(request.progressType(), request.progressValue(), shelf);

        Note note = Note.builder()
                .shelf(shelf)
                .member(member)
                .content(request.content())
                .secret(Boolean.TRUE.equals(request.secret()))
                .progressType(request.progressType())
                .progressValue(request.progressValue())
                .likeCount(0)
                .bookmarkCount(0)
                .build();

        noteRepository.save(note);
    }

    @Override
    public BookNoteSliceResponseDto getPublicNotesByBook(long bookId, long memberId, Pageable pageable) {
        if (!bookRepository.existsById(bookId)) {
            throw new LibriException(ErrorCode.BOOK_NOT_FOUND);
        }

        Slice<Note> slice = getPublicBookNoteSlice(bookId, memberId, pageable);
        List<Note> notes = slice.getContent();
        List<Long> noteIds = notes.stream().map(Note::getId).toList();
        var likedIds = resolveLikedIds(memberId, noteIds);
        var bookmarkedIds = resolveBookmarkedIds(memberId, noteIds);
        var commentCounts = resolveCommentCounts(noteIds);

        List<BookNoteItemResponseDto> content = notes.stream()
                .map(note -> new BookNoteItemResponseDto(
                        note.getId(),
                        note.getShelf().getId(),
                        note.getMember().getId(),
                        note.getMember().getNickname(),
                        note.getProgressType(),
                        note.getProgressValue(),
                        note.getCreatedDate(),
                        note.getContent(),
                        note.getLikeCount(),
                        (int) commentCounts.getOrDefault(note.getId(), 0L).longValue(),
                        likedIds.contains(note.getId()),
                        bookmarkedIds.contains(note.getId())
                ))
                .toList();

        return new BookNoteSliceResponseDto(
                content,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    @Override
    public List<NoteSummaryResponseDto> getNotesByShelf(long shelfId, long memberId) {
        Shelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new LibriException(ErrorCode.SHELF_NOT_FOUND));

        boolean isOwner = memberId > 0 && shelf.getMember().getId() == memberId;
        List<Note> notes = isOwner
                ? noteRepository.findByShelfIdOrderByCreatedDateDesc(shelfId)
                : noteRepository.findByShelfIdAndSecretFalseOrderByCreatedDateDesc(shelfId);

        List<Long> noteIds = notes.stream().map(Note::getId).toList();
        var likedIds = resolveLikedIds(memberId, noteIds);
        var bookmarkedIds = resolveBookmarkedIds(memberId, noteIds);
        var commentCounts = resolveCommentCounts(noteIds);

        return notes.stream()
                .map(note -> new NoteSummaryResponseDto(
                        note.getId(),
                        note.getContent(),
                        note.getCreatedDate(),
                        note.getProgressType(),
                        note.getProgressValue(),
                        note.getLikeCount(),
                        (int) commentCounts.getOrDefault(note.getId(), 0L).longValue(),
                        likedIds.contains(note.getId()),
                        bookmarkedIds.contains(note.getId()),
                        note.isSecret()
                ))
                .toList();
    }

    @Override
    public NoteSliceResponseDto getNotesSliceByShelf(long shelfId, long memberId, Pageable pageable) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        shelfRepository.findByIdAndMemberId(shelfId, memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.SHELF_NOT_FOUND));

        Slice<Note> slice = noteRepository.findByShelfIdOrderByCreatedDateDesc(shelfId, pageable);

        List<Note> notes = slice.getContent();
        List<Long> noteIds = notes.stream().map(Note::getId).toList();
        var likedIds = resolveLikedIds(memberId, noteIds);
        var bookmarkedIds = resolveBookmarkedIds(memberId, noteIds);
        var commentCounts = resolveCommentCounts(noteIds);

        List<NoteSummaryResponseDto> content = notes.stream()
                .map(note -> new NoteSummaryResponseDto(
                        note.getId(),
                        note.getContent(),
                        note.getCreatedDate(),
                        note.getProgressType(),
                        note.getProgressValue(),
                        note.getLikeCount(),
                        (int) commentCounts.getOrDefault(note.getId(), 0L).longValue(),
                        likedIds.contains(note.getId()),
                        bookmarkedIds.contains(note.getId()),
                        note.isSecret()
                ))
                .toList();

        return new NoteSliceResponseDto(
                content,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    @Override
    public NoteListResponseDto getNotesByMember(long memberId, Pageable pageable) {
        if (memberId <= 0) {
            return NoteListResponseDto.empty(pageable.getPageNumber(), pageable.getPageSize());
        }

        Slice<Note> slice = noteRepository.findByMemberId(memberId, pageable);

        List<Note> notes = slice.getContent();
        List<Long> noteIds = notes.stream().map(Note::getId).toList();
        var likedIds = resolveLikedIds(memberId, noteIds);
        var bookmarkedIds = resolveBookmarkedIds(memberId, noteIds);
        var commentCounts = resolveCommentCounts(noteIds);

        List<NoteListItemResponseDto> content = notes.stream()
                .map(note -> new NoteListItemResponseDto(
                        note.getId(),
                        note.getShelf().getBook().getId(),
                        note.getShelf().getBook().getTitle(),
                        note.getShelf().getBook().getCoverImageUrl(),
                        note.getProgressType(),
                        note.getProgressValue(),
                        note.getCreatedDate(),
                        note.getContent(),
                        note.isSecret(),
                        note.getLikeCount(),
                        (int) commentCounts.getOrDefault(note.getId(), 0L).longValue(),
                        likedIds.contains(note.getId()),
                        bookmarkedIds.contains(note.getId())
                ))
                .toList();

        long totalCount = noteRepository.countByMemberId(memberId);

        return new NoteListResponseDto(
                totalCount,
                content,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    @Override
    public NoteDetailResponseDto getNoteDetail(long noteId, long memberId) {
        Note note = noteRepository.findWithBookById(noteId)
                .orElseThrow(() -> new LibriException(ErrorCode.NOTE_NOT_FOUND));

        boolean isOwner = memberId > 0 && note.getMember().getId() == memberId;
        if (!isOwner && blockService.hasBlockRelation(memberId, note.getMember().getId())) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }
        if (note.isSecret() && !isOwner) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }

        NoteDetailResponseDto.BookInfo bookInfo = new NoteDetailResponseDto.BookInfo(
                note.getShelf().getBook().getId(),
                note.getShelf().getBook().getTitle(),
                note.getShelf().getBook().getAuthor(),
                note.getShelf().getBook().getCoverImageUrl()
        );

        boolean isLiked = memberId > 0 && noteLikeRepository.existsByNoteIdAndMemberId(noteId, memberId);
        boolean isBookmarked = memberId > 0 && noteBookmarkRepository.existsByNoteIdAndMemberId(noteId, memberId);

        int commentCount = (int) noteCommentRepository.countByNoteId(noteId);

        return new NoteDetailResponseDto(
                note.getId(),
                note.getShelf().getId(),
                note.getContent(),
                note.getCreatedDate(),
                note.getProgressType(),
                note.getProgressValue(),
                note.isSecret(),
                note.getLikeCount(),
                commentCount,
                isLiked,
                isBookmarked,
                isOwner,
                bookInfo
        );
    }

    @Override
    public NoteBookmarkListResponseDto getBookmarkedNotes(long memberId, Pageable pageable) {
        if (memberId <= 0) {
            return NoteBookmarkListResponseDto.empty(pageable.getPageNumber(), pageable.getPageSize());
        }
        var slice = noteBookmarkRepository.findByMemberIdOrderByCreatedDateDesc(memberId, pageable);
        List<Note> notes = slice.getContent().stream().map(bookmark -> bookmark.getNote()).toList();
        List<Long> noteIds = notes.stream().map(Note::getId).toList();
        var likedIds = resolveLikedIds(memberId, noteIds);
        var bookmarkedIds = resolveBookmarkedIds(memberId, noteIds);
        var commentCounts = resolveCommentCounts(noteIds);

        List<NoteBookmarkItemResponseDto> content = notes.stream()
                .map(note -> new NoteBookmarkItemResponseDto(
                        note.getId(),
                        note.getShelf().getBook().getId(),
                        note.getShelf().getBook().getTitle(),
                        note.getShelf().getBook().getCoverImageUrl(),
                        note.getMember().getNickname(),
                        note.getProgressType(),
                        note.getProgressValue(),
                        note.getCreatedDate(),
                        note.getContent(),
                        note.isSecret(),
                        note.getLikeCount(),
                        (int) commentCounts.getOrDefault(note.getId(), 0L).longValue(),
                        likedIds.contains(note.getId()),
                        bookmarkedIds.contains(note.getId())
                ))
                .toList();

        long totalCount = noteBookmarkRepository.countByMemberId(memberId);

        return new NoteBookmarkListResponseDto(
                totalCount,
                content,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    @Override
    public int countNotesByShelf(long shelfId) {
        return noteRepository.countByShelfId(shelfId);
    }

    @Override
    @Transactional
    public NoteSummaryResponseDto updateNote(
            long shelfId,
            long noteId,
            long memberId,
            NoteUpdateRequestDto request
    ) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (request == null) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
        boolean hasContent = request.content() != null && !request.content().isBlank();
        boolean hasSecret = request.secret() != null;
        boolean hasShelf = request.shelfId() != null;
        boolean hasProgressType = request.progressType() != null;
        boolean hasProgressValue = request.progressValue() != null;
        if (!hasContent && !hasSecret && !hasShelf && !hasProgressType && !hasProgressValue) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (hasProgressType != hasProgressValue) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Note note = noteRepository.findByIdAndShelfId(noteId, shelfId)
                .orElseThrow(() -> new LibriException(ErrorCode.NOTE_NOT_FOUND));

        if (note.getMember().getId() != memberId) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }

        note.update(
                request.content() == null ? null : request.content().trim(),
                request.secret()
        );
        if (request.shelfId() != null) {
            Shelf newShelf = shelfRepository.findByIdAndMemberId(request.shelfId(), memberId)
                    .orElseThrow(() -> new LibriException(ErrorCode.SHELF_NOT_FOUND));
            note.updateShelf(newShelf);
        }
        if (hasProgressType && hasProgressValue) {
            validateProgress(request.progressType(), request.progressValue(), note.getShelf());
            note.updateProgress(request.progressType(), request.progressValue());
        }

        boolean isLiked = memberId > 0 && noteLikeRepository.existsByNoteIdAndMemberId(noteId, memberId);
        boolean isBookmarked = memberId > 0 && noteBookmarkRepository.existsByNoteIdAndMemberId(noteId, memberId);

        int commentCount = (int) noteCommentRepository.countByNoteId(noteId);

        return new NoteSummaryResponseDto(
                note.getId(),
                note.getContent(),
                note.getCreatedDate(),
                note.getProgressType(),
                note.getProgressValue(),
                note.getLikeCount(),
                commentCount,
                isLiked,
                isBookmarked,
                note.isSecret()
        );
    }

    private void validateProgress(NoteProgressType progressType, Integer progressValue, Shelf shelf) {
        if (progressType == null || progressValue == null) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (progressType == NoteProgressType.PAGE) {
            if (progressValue < 1) {
                throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
            }
            int totalPage = shelf.getBook().getTotalPage();
            if (totalPage > 0 && progressValue > totalPage) {
                throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
            }
        } else if (progressType == NoteProgressType.PERCENT) {
            if (progressValue < 0 || progressValue > 100) {
                throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
            }
        } else {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private java.util.Set<Long> resolveLikedIds(long memberId, List<Long> noteIds) {
        if (memberId <= 0 || noteIds.isEmpty()) {
            return java.util.Set.of();
        }
        return new java.util.HashSet<>(noteLikeRepository.findNoteIdsByMemberIdAndNoteIdIn(memberId, noteIds));
    }

    private java.util.Set<Long> resolveBookmarkedIds(long memberId, List<Long> noteIds) {
        if (memberId <= 0 || noteIds.isEmpty()) {
            return java.util.Set.of();
        }
        return new java.util.HashSet<>(noteBookmarkRepository.findNoteIdsByMemberIdAndNoteIdIn(memberId, noteIds));
    }

    private java.util.Map<Long, Long> resolveCommentCounts(List<Long> noteIds) {
        if (noteIds.isEmpty()) {
            return java.util.Map.of();
        }
        var rows = noteCommentRepository.countByNoteIds(noteIds);
        java.util.Map<Long, Long> counts = new java.util.HashMap<>();
        for (var row : rows) {
            if (row.getNoteId() != null) {
                counts.put(row.getNoteId(), row.getCount() == null ? 0L : row.getCount());
            }
        }
        return counts;
    }

    private Slice<Note> getPublicBookNoteSlice(long bookId, long memberId, Pageable pageable) {
        java.util.Set<Long> blockedRelationIds = blockService.getBlockedRelationMemberIds(memberId);
        if (blockedRelationIds.isEmpty()) {
            return noteRepository.findByShelfBookIdAndSecretFalseOrderByCreatedDateDesc(bookId, pageable);
        }
        return noteRepository.findByShelfBookIdAndSecretFalseAndMemberIdNotInOrderByCreatedDateDesc(
                bookId,
                blockedRelationIds,
                pageable
        );
    }

    @Override
    @Transactional
    public void deleteNote(long shelfId, long noteId, long memberId) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Note note = noteRepository.findByIdAndShelfId(noteId, shelfId)
                .orElseThrow(() -> new LibriException(ErrorCode.NOTE_NOT_FOUND));

        if (note.getMember().getId() != memberId) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }

        noteRepository.delete(note);
    }
}
