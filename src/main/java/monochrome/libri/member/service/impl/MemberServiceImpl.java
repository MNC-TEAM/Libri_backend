package monochrome.libri.member.service.impl;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.PasswordHashService;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.dto.request.MemberUpdateRequestDto;
import monochrome.libri.member.repository.MemberRepository;
import monochrome.libri.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordHashService passwordHashService;

    public MemberServiceImpl(MemberRepository memberRepository, PasswordHashService passwordHashService) {
        this.memberRepository = memberRepository;
        this.passwordHashService = passwordHashService;
    }

    @Override
    @Transactional
    public Member updateMember(long memberId, MemberUpdateRequestDto memberUpdateRequestDto) {

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new LibriException(ErrorCode.MEMBER_NOT_FOUND)
        );

        String passwordHash = member.getPasswordHash();

        // dto에 비밀번호가 들어온 경우 업데이트
        if(memberUpdateRequestDto.rawPassword() != null && !memberUpdateRequestDto.rawPassword().isBlank()) {
            passwordHash = passwordHashService.hashPassword(memberUpdateRequestDto.rawPassword());
        }

        return member.updateMember(
                memberUpdateRequestDto.username(),
                memberUpdateRequestDto.nickname(),
                passwordHash,
                memberUpdateRequestDto.profilePath()
        );
    }

    @Override
    public Optional<Member> getMemberById(long memberId) {
        return memberRepository.findById(memberId);
    }

    @Override
    public Optional<Member> getMemberByEmail(String email) {
        return memberRepository.findByPrimaryEmail(email);
    }

    @Override
    public void withdraw(Long memberId) {
        Member member = getMemberById(memberId)
                .orElseThrow(()->new LibriException(ErrorCode.MEMBER_NOT_FOUND));

        member.withdraw();
    }
}
