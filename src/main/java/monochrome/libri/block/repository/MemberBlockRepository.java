package monochrome.libri.block.repository;

import monochrome.libri.block.domain.MemberBlock;
import monochrome.libri.member.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberBlockRepository extends JpaRepository<MemberBlock, Long> {
    boolean existsByBlockerAndBlocked(Member blocker, Member blocked);

    boolean existsByBlockerIdAndBlockedId(long blockerId, long blockedId);

    Optional<MemberBlock> findByBlockerAndBlocked(Member blocker, Member blocked);

    @EntityGraph(attributePaths = {"blocked"})
    Slice<MemberBlock> findByBlockerIdOrderByCreatedDateDesc(long blockerId, Pageable pageable);

    long countByBlockerId(long blockerId);

    @Query("select case when mb.blocker.id = :memberId then mb.blocked.id else mb.blocker.id end " +
            "from MemberBlock mb where mb.blocker.id = :memberId or mb.blocked.id = :memberId")
    List<Long> findBlockedRelationMemberIds(@Param("memberId") long memberId);
}
