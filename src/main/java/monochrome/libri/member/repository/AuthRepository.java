package monochrome.libri.member.repository;

import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.SignType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByProviderAndProviderUserId(SignType provider, String providerUserId);
}
