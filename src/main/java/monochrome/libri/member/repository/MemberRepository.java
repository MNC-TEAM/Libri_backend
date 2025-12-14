package monochrome.libri.member.repository;

import monochrome.libri.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByPrimaryEmail(String email);
    Optional<Member> findByPrimaryEmail(String email);
}
