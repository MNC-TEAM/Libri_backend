package monochrome.libri.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import monochrome.libri.global.domain.AuditableEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "member_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_report_reported_reporter",
                        columnNames = {"reported_member_id", "reporter_member_id"}
                )
        }
)
public class MemberReport extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_report_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_member_id", nullable = false)
    private Member reportedMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_member_id", nullable = false)
    private Member reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MemberReportReason reason;

    @Column(length = 300)
    private String detail;
}
