package monochrome.libri.member.repository;

import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.domain.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndProviderUserId(SignType provider, String providerUserId);

    Optional<SocialAccount> findByMemberIdAndProvider(Long memberId, SignType provider);
}
