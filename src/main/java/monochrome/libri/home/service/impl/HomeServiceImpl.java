package monochrome.libri.home.service.impl;

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

    private final MemberService memberService;
    private final ShelfRepository shelfRepository;
    private final Clock clock;

    public HomeServiceImpl(MemberService memberService, ShelfRepository shelfRepository, Clock clock) {
        this.memberService = memberService;
        this.shelfRepository = shelfRepository;
        this.clock = clock;
    }

    @Override
    public HomeResponseDto getHome(Long memberId) {
        // 사용자 요약
        HomeResponseDto.MeSummary me = buildMeSummary(memberId);

        HomeResponseDto.ShelfSummary shelfSummary = buildShelfSummary(memberId);

        List<HomeResponseDto.ReadingBook> readingBooks = buildReadingBooks(memberId);
        List<HomeResponseDto.BookSummary> wantToReadBooks = buildBookSummaries(memberId, ShelfStatus.WANT_TO_READ);
        List<HomeResponseDto.BookSummary> finishedBooks = buildBookSummaries(memberId, ShelfStatus.FINISHED);

        // TODO: 도서 추천 로직 구현 예정
        List<HomeResponseDto.BookRecommendation> recommendations = List.of();

        return new HomeResponseDto(
                me,
                shelfSummary,
                readingBooks,
                wantToReadBooks,
                finishedBooks,
                recommendations
        );
    }

    private HomeResponseDto.MeSummary buildMeSummary(Long memberId) {
        if (memberId == null) {
            return new HomeResponseDto.MeSummary(null, null, null, 0);
        }

        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));

        // TODO: 현재 알림 기능이 없으므로 미확인 알림 수는 0으로 설정
        return new HomeResponseDto.MeSummary(
                member.getId(),
                member.getNickname(),
                member.getProfilePath(),
                0
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
                    int currentPage = safeInt(row.currentPage());
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

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
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
