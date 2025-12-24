package monochrome.libri.follow.repository;

import monochrome.libri.follow.domain.Follow;
import monochrome.libri.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom {
    Optional<Follow> findByFollowerAndFollowing(Member follower, Member following);
}
