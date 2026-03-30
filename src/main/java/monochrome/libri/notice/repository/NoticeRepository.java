package monochrome.libri.notice.repository;

import monochrome.libri.notice.domain.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Slice<Notice> findByPublishedTrueOrderByPinnedDescCreatedDateDesc(Pageable pageable);
    Optional<Notice> findByIdAndPublishedTrue(Long id);
}
