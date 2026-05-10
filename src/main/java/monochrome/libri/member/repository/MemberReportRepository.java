package monochrome.libri.member.repository;

import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberReportRepository extends JpaRepository<MemberReport, Long> {
    boolean existsByReportedMemberAndReporter(Member reportedMember, Member reporter);
}
