package monochrome.libri.member.service;

import monochrome.libri.member.domain.Member;
import monochrome.libri.member.dto.request.MemberReportCreateRequestDto;
import monochrome.libri.member.dto.request.MemberUpdateRequestDto;
import monochrome.libri.member.dto.response.MemberPrivacyResponseDto;
import monochrome.libri.member.dto.response.MemberProfileResponseDto;
import monochrome.libri.member.dto.response.MemberResponseDto;

import java.util.Optional;

public interface MemberService {
    /**
     * 회원 정보 수정
     * @param memberId 수정하려는 회원 ID
     * @param memberUpdateRequestDto 수정하려는 정보
     * @return 수정된 Member 엔티티
     */
    Member updateMember(long memberId, MemberUpdateRequestDto memberUpdateRequestDto);

    MemberResponseDto getMyProfile(long memberId);

    MemberPrivacyResponseDto getMyPrivacy(long memberId);

    MemberProfileResponseDto getMemberProfile(Long actorMemberId, long targetMemberId);

    void reportMember(long reporterMemberId, long reportedMemberId, MemberReportCreateRequestDto request);

    /**
     * ID로 회원 조회
     * @param memberId 조회하려는 회원 ID
     * @return 존재하면 Member 엔티티, 없으면 Optional.empty()
     */
    Optional<Member> getMemberById(long memberId);

    /**
     * 이메일로 회원 조회
     * @param email 조회하려는 회원 email
     * @return 존재하면 Member 엔티티, 없으면 Optional.empty()
     */
    Optional<Member> getMemberByEmail(String email);

    /**
     * 회원 탈퇴
     */
    void withdraw(Long memberId);
}
