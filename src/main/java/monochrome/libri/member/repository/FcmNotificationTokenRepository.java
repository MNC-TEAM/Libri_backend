package monochrome.libri.member.repository;

import monochrome.libri.member.domain.FcmNotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmNotificationTokenRepository extends JpaRepository<FcmNotificationToken, Long> {

    List<FcmNotificationToken> findByMember_IdOrderByLastUsedDateDesc(long memberId);

    Optional<FcmNotificationToken> findByMember_IdAndToken(long memberId, String token);

    void deleteByMember_Id(long memberId);
}
