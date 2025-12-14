package monochrome.libri.book.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import monochrome.libri.book.domain.Book;
import monochrome.libri.book.domain.QBook;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

public class BookRepositoryCustomImpl implements BookRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public BookRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Slice<Book> searchByKeyword(String textKeyword, Pageable pageable) {
        QBook b = QBook.book;

        String keyword = (textKeyword == null) ? "" : textKeyword.trim();
        if(keyword.isBlank()) {
            return new SliceImpl<>(List.of(), pageable, false);
        }

        // title, author, publisher 포함 검색(대소문자 무시)
        BooleanExpression condition =
                b.title.containsIgnoreCase(keyword)
                        .or(b.author.containsIgnoreCase(keyword))
                        .or(b.publisher.containsIgnoreCase(keyword));

        // 정확도 정렬(숫자가 낮을수록 상위)
        // 완전일치 > 접두(prefix) > 키워드 포함(contains)
        NumberExpression<Integer> rank =
                new CaseBuilder()
                        .when(b.title.equalsIgnoreCase(keyword)).then(0)
                        .when(b.author.equalsIgnoreCase(keyword)).then(1)
                        .when(b.publisher.equalsIgnoreCase(keyword)).then(2)

                        .when(b.title.startsWithIgnoreCase(keyword)).then(3)
                        .when(b.author.startsWithIgnoreCase(keyword)).then(4)
                        .when(b.publisher.startsWithIgnoreCase(keyword)).then(5)

                        // condition에 이미 contains가 들어있지만, 순서 보장용으로 명시
                        .when(b.title.containsIgnoreCase(keyword)).then(6)
                        .when(b.author.containsIgnoreCase(keyword)).then(7)
                        .when(b.publisher.containsIgnoreCase(keyword)).then(8)
                        .otherwise(9);

        OrderSpecifier<?> orderRankAsc = rank.asc();
        OrderSpecifier<?> orderIdDesc = b.id.desc(); // tie-breaker

        int pageSize = pageable.getPageSize();
        List<Book> results = queryFactory
                .selectFrom(b)
                .where(condition)
                .orderBy(orderRankAsc, orderIdDesc)
                .offset(pageable.getOffset())
                .limit(pageSize + 1L)
                .fetch();

        boolean hasNext = results.size() > pageSize;
        if(hasNext) {
            results = results.subList(0, pageSize);
        }

        return new SliceImpl<>(results, pageable, hasNext);
    }
}
