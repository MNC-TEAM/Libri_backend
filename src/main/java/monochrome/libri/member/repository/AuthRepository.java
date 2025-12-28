package monochrome.libri.member.repository;

import monochrome.libri.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Member, Long> {
    /**
     * 이메일 중복 체크
     */
    boolean existsByEmail(String email);

    /**
     * 이메일로 회원 조회
     */
    Optional<Member> findByEmail(String email);

}
