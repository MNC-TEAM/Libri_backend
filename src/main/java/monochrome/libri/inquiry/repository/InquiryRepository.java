package monochrome.libri.inquiry.repository;

import monochrome.libri.inquiry.domain.Inquiry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Slice<Inquiry> findByMemberIdOrderByCreatedDateDesc(long memberId, Pageable pageable);
    long countByMemberId(long memberId);
}
