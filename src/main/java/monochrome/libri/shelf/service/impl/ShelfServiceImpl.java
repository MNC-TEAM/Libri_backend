package monochrome.libri.shelf.service.impl;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.book.domain.Book;
import monochrome.libri.book.repository.BookRepository;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.review.service.ReviewService;
import monochrome.libri.shelf.domain.Shelf;
import monochrome.libri.shelf.dto.response.ShelfDetailResponseDto;
import monochrome.libri.shelf.dto.response.ShelfListItemResponseDto;
import monochrome.libri.shelf.dto.response.ShelfListResponseDto;
import monochrome.libri.shelf.dto.request.ShelfUpdateRequestDto;
import monochrome.libri.shelf.dto.request.ShelfCreateRequestDto;
import monochrome.libri.shelf.repository.ShelfRepository;
import monochrome.libri.shelf.service.ShelfService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Service
@Transactional(readOnly = true)
public class ShelfServiceImpl implements ShelfService {

    private final ShelfRepository shelfRepository;
    private final ReviewService reviewService;
    private final MemberService memberService;
    private final BookRepository bookRepository;
    private final Clock clock;

    public ShelfServiceImpl(
            ShelfRepository shelfRepository,
            ReviewService reviewService,
            MemberService memberService,
            BookRepository bookRepository,
            Clock clock
    ) {
        this.shelfRepository = shelfRepository;
        this.reviewService = reviewService;
        this.memberService = memberService;
        this.bookRepository = bookRepository;
        this.clock = clock;
    }

    @Override
    public ShelfDetailResponseDto getShelfDetail(long shelfId, Long memberId) {
        if (memberId == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Shelf shelf = shelfRepository.findByIdAndMemberId(shelfId, memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.SHELF_NOT_FOUND));

        return buildShelfDetail(shelf, shelfId, memberId);
    }

    @Override
    public ShelfListResponseDto getShelvesByStatus(Long memberId, monochrome.libri.shelf.domain.ShelfStatus status, Pageable pageable) {
        if (memberId == null || memberId <= 0) {
            return ShelfListResponseDto.empty(pageable.getPageNumber(), pageable.getPageSize());
        }

        Slice<Shelf> slice = shelfRepository.findByMemberIdAndStatusOrderByCreatedDateDesc(memberId, status, pageable);
        List<ShelfListItemResponseDto> content = slice.getContent().stream()
                .map(this::toShelfListItem)
                .toList();

        long totalCount = shelfRepository.countByMemberIdAndStatus(memberId, status);

        return new ShelfListResponseDto(
                totalCount,
                content,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    @Override
    @Transactional
    public ShelfDetailResponseDto updateShelf(long shelfId, Long memberId, ShelfUpdateRequestDto request) {
        if (memberId == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (request == null || isEmptyUpdate(request)) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Shelf shelf = shelfRepository.findByIdAndMemberId(shelfId, memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.SHELF_NOT_FOUND));

        validateUpdateRequest(request, shelf);

        shelf.updateReadingInfo(
                request.status(),
                request.startDate(),
                request.endDate(),
                null
        );
        if (request.progressType() != null || request.progressValue() != null) {
            validateProgress(request.progressType(), request.progressValue(), shelf.getBook());
            Integer currentPage = toCurrentPage(request.progressType(), request.progressValue());
            shelf.updateProgress(request.progressType(), request.progressValue(), currentPage);
        }

        return buildShelfDetail(shelf, shelfId, memberId);
    }

    @Override
    @Transactional
    public void createShelf(long memberId, ShelfCreateRequestDto request) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (request == null || request.bookId() == null || request.status() == null) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 이미 책장에 있으면 no-op
        if (shelfRepository.findByMemberIdAndBookId(memberId, request.bookId()).isPresent()) {
            return;
        }

        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new LibriException(ErrorCode.BOOK_NOT_FOUND));

        validateCreateRequest(request, book);

        Shelf shelf = Shelf.builder()
                .member(member)
                .book(book)
                .status(request.status())
                .progressType(request.progressType())
                .progressValue(request.progressValue())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .currentPage(toCurrentPage(request.progressType(), request.progressValue()))
                .build();

        shelfRepository.save(shelf);
    }

    private void validateCreateRequest(ShelfCreateRequestDto request, Book book) {
        if (request.startDate() != null && request.endDate() != null
                && request.startDate().isAfter(request.endDate())) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
        validateProgress(request.progressType(), request.progressValue(), book);
    }

    private boolean isEmptyUpdate(ShelfUpdateRequestDto request) {
        return request.status() == null
                && request.startDate() == null
                && request.endDate() == null
                && request.progressType() == null
                && request.progressValue() == null;
    }

    private void validateUpdateRequest(ShelfUpdateRequestDto request, Shelf shelf) {
        if (request.startDate() != null && request.endDate() != null
                && request.startDate().isAfter(request.endDate())) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if ((request.progressType() != null && request.progressValue() == null)
                || (request.progressType() == null && request.progressValue() != null)) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private ShelfDetailResponseDto buildShelfDetail(Shelf shelf, long shelfId, Long memberId) {
        int totalPage = shelf.getBook().getTotalPage();
        int currentPage = resolveCurrentPage(shelf);
        int progressPercent = calculateProgressPercent(
                shelf.getProgressType(),
                shelf.getProgressValue(),
                currentPage,
                totalPage
        );

        ShelfDetailResponseDto.BookInfo bookInfo = new ShelfDetailResponseDto.BookInfo(
                shelf.getBook().getId(),
                shelf.getBook().getTitle(),
                shelf.getBook().getAuthor(),
                shelf.getBook().getPublisher(),
                shelf.getBook().getCoverImageUrl(),
                shelf.getBook().getReleaseDate()
        );

        int reviewCount = (int) reviewService.countReviewsByBook(shelf.getBook().getId());

        int readingDays = calculateReadingDays(shelf.getStartDate(), LocalDate.now(clock));
        ShelfDetailResponseDto.ReadingInfo readingInfo = new ShelfDetailResponseDto.ReadingInfo(
                shelf.getStatus(),
                shelf.getStartDate(),
                shelf.getEndDate(),
                progressPercent,
                reviewCount,
                readingDays
        );

        return new ShelfDetailResponseDto(bookInfo, readingInfo);
    }

    private void validateProgress(monochrome.libri.shelf.domain.ShelfProgressType progressType, Integer progressValue, Book book) {
        if (progressType == null || progressValue == null) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (progressType == monochrome.libri.shelf.domain.ShelfProgressType.PAGE) {
            if (progressValue < 0) {
                throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
            }
            int totalPage = book.getTotalPage();
            if (totalPage > 0 && progressValue > totalPage) {
                throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
            }
        } else if (progressType == monochrome.libri.shelf.domain.ShelfProgressType.PERCENT) {
            if (progressValue < 0 || progressValue > 100) {
                throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
            }
        } else {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private Integer toCurrentPage(monochrome.libri.shelf.domain.ShelfProgressType progressType, Integer progressValue) {
        if (progressType == monochrome.libri.shelf.domain.ShelfProgressType.PAGE) {
            return progressValue;
        }
        return null;
    }

    private int calculateProgressPercent(int currentPage, int totalPage) {
        return calculateProgressPercent(
                monochrome.libri.shelf.domain.ShelfProgressType.PAGE,
                currentPage,
                currentPage,
                totalPage
        );
    }

    private int calculateProgressPercent(
            monochrome.libri.shelf.domain.ShelfProgressType progressType,
            int progressValue,
            int currentPage,
            int totalPage
    ) {
        if (progressType == monochrome.libri.shelf.domain.ShelfProgressType.PERCENT) {
            return Math.min(100, Math.max(0, progressValue));
        }
        if (totalPage <= 0) {
            return 0;
        }
        int percent = (int) Math.round((currentPage * 100.0) / totalPage);
        return Math.min(100, Math.max(0, percent));
    }

    private int calculateReadingDays(LocalDate startDate, LocalDate today) {
        if (startDate == null || today == null) {
            return 0;
        }
        long diff = ChronoUnit.DAYS.between(startDate, today);
        if (diff < 0) {
            return 0;
        }
        return (int) diff + 1;
    }

    private ShelfListItemResponseDto toShelfListItem(Shelf shelf) {
        int currentPage = resolveCurrentPage(shelf);
        int progressPercent = calculateProgressPercent(
                shelf.getProgressType(),
                shelf.getProgressValue(),
                currentPage,
                shelf.getBook().getTotalPage()
        );

        return new ShelfListItemResponseDto(
                shelf.getId(),
                shelf.getBook().getId(),
                shelf.getBook().getTitle(),
                shelf.getBook().getAuthor(),
                shelf.getBook().getPublisher(),
                shelf.getBook().getCoverImageUrl(),
                shelf.getStatus(),
                progressPercent,
                shelf.getStartDate(),
                shelf.getEndDate()
        );
    }

    private int resolveCurrentPage(Shelf shelf) {
        if (shelf.getProgressType() == monochrome.libri.shelf.domain.ShelfProgressType.PAGE
                && shelf.getCurrentPage() == null) {
            return shelf.getProgressValue();
        }
        return shelf.getCurrentPage() == null ? 0 : shelf.getCurrentPage();
    }
}
