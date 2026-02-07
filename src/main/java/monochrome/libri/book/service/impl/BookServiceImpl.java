package monochrome.libri.book.service.impl;

import lombok.extern.slf4j.Slf4j;
import monochrome.libri.book.domain.Book;
import monochrome.libri.book.dto.request.BookDirectCreateRequestDto;
import monochrome.libri.book.dto.response.BookDetailResponseDto;
import monochrome.libri.book.dto.response.BookResponseDto;
import monochrome.libri.book.repository.BookRepository;
import monochrome.libri.book.service.BookService;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.review.domain.ReviewStatus;
import monochrome.libri.review.dto.response.ReviewSummaryResponseDto;
import monochrome.libri.review.repository.ReviewRepository;
import monochrome.libri.review.service.ReviewService;
import monochrome.libri.shelf.domain.Shelf;
import monochrome.libri.shelf.domain.ShelfProgressType;
import monochrome.libri.shelf.domain.ShelfStatus;
import monochrome.libri.shelf.repository.ShelfRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private static final int BOOK_DETAIL_REVIEW_PREVIEW_SIZE = 3;

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final ShelfRepository shelfRepository;
    private final MemberService memberService;

    public BookServiceImpl(
            BookRepository bookRepository,
            ReviewRepository reviewRepository,
            ReviewService reviewService,
            ShelfRepository shelfRepository,
            MemberService memberService
    ) {
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
        this.reviewService = reviewService;
        this.shelfRepository = shelfRepository;
        this.memberService = memberService;
    }

    // ISBN-10: 9자리 숫자 + (숫자 or X)
    private static final Pattern ISBN10 = Pattern.compile("^\\d{9}[\\dXx]$");
    // ISBN-13: 13자리 숫자
    private static final Pattern ISBN13 = Pattern.compile("^\\d{13}$");
    private static final int MIN_SEARCH_LENGTH = 2;

    /**
     * 검색 서비스
     * 책 제목, 저자, 출판사, ISBN 등 다양한 검색 가능
     * @param keyword 검색어
     * @param pageable 무한스크롤용 
     * @return Slice형태의 검색 결과
     */
    @Override
    public Slice<BookResponseDto> searchBooks(String keyword, Pageable pageable) {

        String textKeyword = normalizeText(keyword);
        String isbnKeyword = normalizeIsbn(keyword);

        if (textKeyword.isBlank() || textKeyword.length() < MIN_SEARCH_LENGTH) {
            return emptySlice(pageable);
        }

        // ISBN이면 검색 시도
        if (looksLikeIsbn(isbnKeyword)) {
            if (!isValidIsbn(isbnKeyword)) {
                return emptySlice(pageable);
            }
            Optional<Book> found = bookRepository.findByIsbn(isbnKeyword);

            return found.<Slice<BookResponseDto>>map(book -> new SliceImpl<>(
                    List.of(BookResponseDto.from(book)),
                    pageable,
                    false
            )).orElseGet(() -> emptySlice(pageable));
        }

        // 그 외 검색(책 제목, 저자, 출판사 등)
        return bookRepository.searchByKeyword(textKeyword, pageable)
                .map(BookResponseDto::from);
    }

    @Override
    public BookDetailResponseDto getBookDetail(long bookId, Long memberId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new LibriException(ErrorCode.BOOK_NOT_FOUND));

        BookDetailResponseDto.ShelfInfo shelfInfo = null;
        if (memberId != null && memberId > 0) {
            Shelf shelf = shelfRepository.findByMemberIdAndBookId(memberId, bookId).orElse(null);
            if (shelf != null) {
                shelfInfo = new BookDetailResponseDto.ShelfInfo(shelf.getId(), shelf.getStatus());
            }
        }

        var reviewSlice = reviewRepository.findByBookIdAndStatusOrderByCreatedDateDesc(
                bookId,
                ReviewStatus.ACTIVE,
                PageRequest.of(0, BOOK_DETAIL_REVIEW_PREVIEW_SIZE)
        );
        List<ReviewSummaryResponseDto> reviews = reviewSlice.getContent().stream()
                .map(review -> new ReviewSummaryResponseDto(
                        review.getId(),
                        review.getMember().getId(),
                        review.getMember().getNickname(),
                        review.getRating(),
                        review.getContent()
                ))
                .toList();

        return new BookDetailResponseDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getReleaseDate(),
                book.getCoverImageUrl(),
                book.getIntroduction(),
                shelfInfo,
                reviewService.getReviewStats(bookId),
                reviews
        );
    }

    @Override
    @Transactional
    public void createBookDirect(long memberId, BookDirectCreateRequestDto request) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (request == null) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));

        Book book = Book.builder()
                .title(request.title().trim())
                .author(request.author().trim())
                .publisher(request.publisher())
                .isbn(request.isbn())
                .totalPage(request.totalPage() == null ? 0 : request.totalPage())
                .coverImageUrl(request.coverUrl())
                .build();

        Book savedBook = bookRepository.save(book);

        if (shelfRepository.findByMemberIdAndBookId(memberId, savedBook.getId()).isPresent()) {
            return;
        }

        ShelfStatus status = request.status();
        int totalPage = savedBook.getTotalPage();
        int progressValue = 0;
        Integer currentPage = null;
        if (status == ShelfStatus.FINISHED && totalPage > 0) {
            progressValue = totalPage;
            currentPage = totalPage;
        }

        Shelf shelf = Shelf.builder()
                .member(member)
                .book(savedBook)
                .status(status)
                .progressType(ShelfProgressType.PAGE)
                .progressValue(progressValue)
                .currentPage(currentPage)
                .build();

        shelfRepository.save(shelf);
    }

    /**
     * 빈 SliceImpl 생성 함수(BookResponseDto 타입)
     * @param pageable pageable
     * @return 빈 Slice
     */
    private Slice<BookResponseDto> emptySlice(Pageable pageable) {
        return new SliceImpl<>(List.of(), pageable, false);
    }

    /**
     * 텍스트 검색(제목, 저자 등) 정규화
     * 앞 뒤 공백 제거, 중간 연속 공배(탭, 줄바꿈 포함)을 공백 1개로
     * ex) "  해리   포터   " -> "해리 포터"
     * @param s 검색 데이터
     * @return 정규화 후 검색 데이터
     */
    private String normalizeText(String s) {
        if(s == null) return "";

        return s.trim()
                .replaceAll("\\s+", " ");
    }

    /**
     * ISBN 검색 정규화
     * 하이픈, 공백을 제거
     * @param s 검색 데이터
     * @return 정규화 후 검색 데이터
     */
    private String normalizeIsbn(String s) {
        if(s == null) return "";
        return s.replaceAll("[\\s-]", "");
    }

    /**
     * ISBN 체크
     * TODO: 현재는 임시 코드(자리수, 형태만 보는 코드) 추후 검증 코드까지 추가 예정
     * @param s 검색 데이터
     * @return ISBN 유무
     */
    private boolean looksLikeIsbn(String s) {
        return ISBN13.matcher(s).matches() || ISBN10.matcher(s).matches();
    }

    private boolean isValidIsbn(String s) {
        if (ISBN13.matcher(s).matches()) {
            return isValidIsbn13(s);
        }
        if (ISBN10.matcher(s).matches()) {
            return isValidIsbn10(s);
        }
        return false;
    }

    private boolean isValidIsbn13(String s) {
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            int digit = s.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        return sum % 10 == 0;
    }

    private boolean isValidIsbn10(String s) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int digit = s.charAt(i) - '0';
            sum += (digit * (10 - i));
        }
        char last = s.charAt(9);
        int check = (last == 'X' || last == 'x') ? 10 : (last - '0');
        sum += check;
        return sum % 11 == 0;
    }
}
