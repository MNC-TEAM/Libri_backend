package monochrome.libri.notification.repository;

import monochrome.libri.notification.domain.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Slice<Notification> findByRecipientMemberIdAndDeletedFalseOrderByCreatedDateDesc(
            Long recipientMemberId,
            Pageable pageable
    );

    long countByRecipientMemberIdAndDeletedFalse(Long recipientMemberId);

    List<Notification> findByRecipientMemberIdAndReadFalseAndDeletedFalse(Long recipientMemberId);

    List<Notification> findByRecipientMemberIdAndDeletedFalse(Long recipientMemberId);
}
