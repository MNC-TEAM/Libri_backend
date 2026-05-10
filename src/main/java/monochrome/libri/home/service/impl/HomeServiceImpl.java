package monochrome.libri.home.service.impl;

import monochrome.libri.book.domain.Book;
import monochrome.libri.book.repository.BookRepository;
import monochrome.libri.book.service.AladinBookCatalogService;
import monochrome.libri.follow.domain.FollowStatus;
import monochrome.libri.follow.repository.FollowRepository;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.home.dto.response.HomeResponseDto;
import monochrome.libri.home.service.HomeService;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.shelf.domain.ShelfStatus;
import monochrome.libri.shelf.dto.ShelfBookRow;
import monochrome.libri.shelf.repository.ShelfCountSummary;
import monochrome.libri.shelf.repository.ShelfRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class HomeServiceImpl implements HomeService {

    private static final int HOME_BOOK_PREVIEW_LIMIT = 3;
    private static final int HOME_RECOMMENDATION_LIMIT = 3;

    private final MemberService memberService;
    private final FollowRepository followRepository;
    private final ShelfRepository shelfRepository;
    private final BookRepository bookRepository;
    private final AladinBookCatalogService aladinBookCatalogService;
    private final Clock clock;

    public HomeServiceImpl(
            MemberService memberService,
            FollowRepository followRepository,
            ShelfRepository shelfRepository,
            BookRepository bookRepository,
            AladinBookCatalogService aladinBookCatalogService,
            Clock clock
    ) {
        this.memberService = memberService;
        this.followRepository = followRepository;
        this.shelfRepository = shelfRepository;
        this.bookRepository = bookRepository;
        this.aladinBookCatalogService = aladinBookCatalogService;
        this.clock = clock;
    }

    @Override
    public HomeResponseDto getHome(Long memberId) {
        HomeResponseDto.ShelfSummary shelfSummary = buildShelfSummary(memberId);
        HomeResponseDto.MeSummary me = buildMeSummary(memberId, shelfSummary);

        List<HomeResponseDto.ReadingBook> readingBooks = buildReadingBooks(memberId);
        List<HomeResponseDto.BookSummary> wantToReadBooks = buildBookSummaries(memberId, ShelfStatus.WANT_TO_READ);
        List<HomeResponseDto.BookSummary> finishedBooks = buildBookSummaries(memberId, ShelfStatus.FINISHED);

        List<HomeResponseDto.BookRecommendation> recommendations = buildRecommendations(memberId);

        return new HomeResponseDto(
                me,
                shelfSummary,
                readingBooks,
                wantToReadBooks,
                finishedBooks,
                recommendations
        );
    }

    private HomeResponseDto.MeSummary buildMeSummary(Long memberId, HomeResponseDto.ShelfSummary shelfSummary) {
        if (memberId == null) {
            return new HomeResponseDto.MeSummary(null, null, null, 0, 0, 0);
        }

        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));
        long followerCount = followRepository.countByFollowingAndFollowStatus(member, FollowStatus.FOLLOW);
        long followingCount = followRepository.countByFollowerAndFollowStatus(member, FollowStatus.FOLLOW);

        int totalBookCount = shelfSummary.wantToReadCount()
                + shelfSummary.finishedCount()
                + shelfSummary.readingCount();

        return new HomeResponseDto.MeSummary(
                member.getId(),
                member.getNickname(),
                member.getProfilePath(),
                followerCount,
                followingCount,
                totalBookCount
        );
    }

    private HomeResponseDto.ShelfSummary buildShelfSummary(Long memberId) {
        if (memberId == null) {
        return new HomeResponseDto.ShelfSummary(0, 0, 0);
        }

        ShelfCountSummary summary = shelfRepository.findCountSummaryByMemberId(memberId);
        return new HomeResponseDto.ShelfSummary(
                summary.wantToReadCount(),
                summary.finishedCount(),
                summary.readingCount()
        );
    }

    private List<HomeResponseDto.ReadingBook> buildReadingBooks(Long memberId) {
        if (memberId == null) {
            return List.of();
        }

        List<ShelfBookRow> rows = shelfRepository.findShelfBooksByStatus(
                memberId,
                ShelfStatus.READING,
                HOME_BOOK_PREVIEW_LIMIT
        );

        LocalDate today = LocalDate.now(clock);
        return rows.stream()
                .map(row -> {
                    int totalPage = safeInt(row.totalPage());
                    int currentPage = resolveCurrentPage(row.progressType(), row.currentPage(), row.progressValue());
                    int progressPercent = calculateProgressPercent(
                            row.progressType(),
                            safeInt(row.progressValue()),
                            currentPage,
                            totalPage
                    );
                    int readingDays = calculateReadingDays(row.startDate(), today);

                    return new HomeResponseDto.ReadingBook(
                            row.shelfId(),
                            row.bookId(),
                            row.title(),
                            row.coverUrl(),
                            progressPercent,
                            readingDays
                    );
                })
                .toList();
    }

    private List<HomeResponseDto.BookSummary> buildBookSummaries(Long memberId, ShelfStatus status) {
        if (memberId == null) {
            return List.of();
        }

        List<ShelfBookRow> rows = shelfRepository.findShelfBooksByStatus(
                memberId,
                status,
                HOME_BOOK_PREVIEW_LIMIT
        );

        return rows.stream()
                .map(row -> new HomeResponseDto.BookSummary(
                        row.shelfId(),
                        row.bookId(),
                        row.title(),
                        row.coverUrl()
                ))
                .toList();
    }

    private List<HomeResponseDto.BookRecommendation> buildRecommendations(Long memberId) {
        List<HomeResponseDto.BookRecommendation> recommendations = bookRepository.findRecommendedBooks(memberId, HOME_RECOMMENDATION_LIMIT).stream()
                .map(book -> toRecommendation(book, "리뷰 인기"))
                .toList();

        if (recommendations.size() >= HOME_RECOMMENDATION_LIMIT || !aladinBookCatalogService.isConfigured()) {
            return recommendations;
        }

        int remaining = HOME_RECOMMENDATION_LIMIT - recommendations.size();
        List<Long> existingBookIds = recommendations.stream()
                .map(HomeResponseDto.BookRecommendation::bookId)
                .toList();

        List<HomeResponseDto.BookRecommendation> bestsellerRecommendations = aladinBookCatalogService.fetchAndUpsertBestsellers(HOME_RECOMMENDATION_LIMIT + recommendations.size()).stream()
                .filter(book -> !existingBookIds.contains(book.getId()))
                .limit(remaining)
                .map(book -> toRecommendation(book, "알라딘 베스트셀러"))
                .toList();

        return java.util.stream.Stream.concat(recommendations.stream(), bestsellerRecommendations.stream())
                .toList();
    }

    private HomeResponseDto.BookRecommendation toRecommendation(Book book, String badgeText) {
        return new HomeResponseDto.BookRecommendation(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCoverImageUrl(),
                book.getPublisher() == null ? List.of() : List.of(book.getPublisher()),
                badgeText
        );
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int resolveCurrentPage(
            monochrome.libri.shelf.domain.ShelfProgressType progressType,
            Integer currentPage,
            Integer progressValue
    ) {
        if (progressType == monochrome.libri.shelf.domain.ShelfProgressType.PAGE && currentPage == null) {
            return safeInt(progressValue);
        }
        return safeInt(currentPage);
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
}
