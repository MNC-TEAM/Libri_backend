package monochrome.libri.shelf.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import monochrome.libri.book.domain.QBook;
import monochrome.libri.shelf.domain.QShelf;
import monochrome.libri.shelf.domain.ShelfStatus;
import monochrome.libri.shelf.dto.MonthCountRow;
import monochrome.libri.shelf.dto.ShelfCalendarRow;
import monochrome.libri.shelf.dto.ShelfBookRow;
import monochrome.libri.shelf.dto.YearCountRow;

import java.time.LocalDate;
import java.util.List;

public class ShelfRepositoryCustomImpl implements ShelfRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ShelfRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public ShelfCountSummary findCountSummaryByMemberId(long memberId) {
        QShelf s = QShelf.shelf;

        // 대상 회원의 행을 상태별로 1/0으로 바꿔 합산해서 카운트를 만든다.
        NumberExpression<Integer> wantToReadExpr = new CaseBuilder()
                .when(s.status.eq(ShelfStatus.WANT_TO_READ)).then(1)
                .otherwise(0)
                .sum();
        NumberExpression<Integer> readingExpr = new CaseBuilder()
                .when(s.status.eq(ShelfStatus.READING)).then(1)
                .otherwise(0)
                .sum();
        NumberExpression<Integer> finishedExpr = new CaseBuilder()
                .when(s.status.eq(ShelfStatus.FINISHED)).then(1)
                .otherwise(0)
                .sum();

        // 한 번의 조회로 WANT/READING/FINISHED 카운트를 동시에 가져온다.
        Tuple result = queryFactory
                .select(wantToReadExpr, readingExpr, finishedExpr)
                .from(s)
                .where(s.member.id.eq(memberId))
                .fetchOne();

        if (result == null) {
            return ShelfCountSummary.empty();
        }

        Integer wantToReadCount = result.get(wantToReadExpr);
        Integer readingCount = result.get(readingExpr);
        Integer finishedCount = result.get(finishedExpr);

        return new ShelfCountSummary(
                wantToReadCount == null ? 0 : wantToReadCount,
                readingCount == null ? 0 : readingCount,
                finishedCount == null ? 0 : finishedCount
        );
    }

    @Override
    public java.util.List<ShelfBookRow> findShelfBooksByStatus(long memberId, ShelfStatus status, int limit) {
        QShelf s = QShelf.shelf;
        QBook b = QBook.book;

        return queryFactory
                .select(Projections.constructor(
                        ShelfBookRow.class,
                        s.id,
                        b.id,
                        b.title,
                        b.coverImageUrl,
                        b.totalPage,
                        s.currentPage,
                        s.progressType,
                        s.progressValue,
                        s.startDate
                ))
                .from(s)
                .join(s.book, b)
                .where(
                        s.member.id.eq(memberId),
                        s.status.eq(status)
                )
                .orderBy(s.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<ShelfCalendarRow> findCalendarRowsByMemberIdAndDateRange(long memberId, LocalDate from, LocalDate to) {
        QShelf s = QShelf.shelf;
        QBook b = QBook.book;

        return queryFactory
                .select(Projections.constructor(
                        ShelfCalendarRow.class,
                        s.id,
                        b.id,
                        b.title,
                        b.coverImageUrl,
                        s.startDate,
                        s.endDate,
                        s.status
                ))
                .from(s)
                .join(s.book, b)
                .where(
                        s.member.id.eq(memberId),
                        s.startDate.between(from, to)
                                .or(s.status.eq(ShelfStatus.FINISHED).and(s.endDate.between(from, to)))
                )
                .fetch();
    }

    @Override
    public List<YearCountRow> countFinishedBooksByYear(long memberId) {
        QShelf s = QShelf.shelf;
        NumberTemplate<Integer> yearExpr = Expressions.numberTemplate(Integer.class, "year({0})", s.endDate);

        return queryFactory
                .select(Projections.constructor(
                        YearCountRow.class,
                        yearExpr,
                        s.id.count()
                ))
                .from(s)
                .where(
                        s.member.id.eq(memberId),
                        s.status.eq(ShelfStatus.FINISHED),
                        s.endDate.isNotNull()
                )
                .groupBy(yearExpr)
                .orderBy(yearExpr.asc())
                .fetch();
    }

    @Override
    public List<MonthCountRow> countFinishedBooksByMonth(long memberId, int year) {
        QShelf s = QShelf.shelf;
        NumberTemplate<Integer> yearExpr = Expressions.numberTemplate(Integer.class, "year({0})", s.endDate);
        NumberTemplate<Integer> monthExpr = Expressions.numberTemplate(Integer.class, "month({0})", s.endDate);

        return queryFactory
                .select(Projections.constructor(
                        MonthCountRow.class,
                        monthExpr,
                        s.id.count()
                ))
                .from(s)
                .where(
                        s.member.id.eq(memberId),
                        s.status.eq(ShelfStatus.FINISHED),
                        s.endDate.isNotNull(),
                        yearExpr.eq(year)
                )
                .groupBy(monthExpr)
                .orderBy(monthExpr.asc())
                .fetch();
    }
}
