package monochrome.libri.search.repository;

import monochrome.libri.search.domain.SearchKeyword;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {
    Slice<SearchKeyword> findByMemberIdOrderByCreatedDateDesc(long memberId, Pageable pageable);
    long countByMemberId(long memberId);
    Optional<SearchKeyword> findByIdAndMemberId(long id, long memberId);
    void deleteByMemberId(long memberId);

    @Query("select sk.keyword as keyword, count(sk) as count " +
            "from SearchKeyword sk group by sk.keyword order by count(sk) desc")
    List<SearchKeywordCountRow> findTopKeywords(Pageable pageable);
}
