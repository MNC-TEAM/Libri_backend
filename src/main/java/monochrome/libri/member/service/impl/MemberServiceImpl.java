package monochrome.libri.member.service.impl;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.PasswordHashService;
import monochrome.libri.fcm.domain.FcmNotificationToken;
import monochrome.libri.fcm.repository.FcmNotificationTokenRepository;
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
    private final FcmNotificationTokenRepository fcmNotificationTokenRepository;

    public MemberServiceImpl(
            MemberRepository memberRepository,
            PasswordHashService passwordHashService,
            FcmNotificationTokenRepository fcmNotificationTokenRepository
    ) {
        this.memberRepository = memberRepository;
        this.passwordHashService = passwordHashService;
        this.fcmNotificationTokenRepository = fcmNotificationTokenRepository;
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
                memberUpdateRequestDto.profilePath(),
                memberUpdateRequestDto.privateAccount()
        );
    }

    @Override
    public Optional<Member> getMemberById(long memberId) {
        return memberRepository.findById(memberId);
    }

    @Override
    public Optional<Member> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public void withdraw(Long memberId) {
        Member member = getMemberById(memberId)
                .orElseThrow(()->new LibriException(ErrorCode.MEMBER_NOT_FOUND));

        fcmNotificationTokenRepository.deleteByMember_Id(memberId);
        member.withdraw();
    }

    @Override
    @Transactional
    public void updateFcmRegistrationToken(long memberId, String token) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));
        if (token == null || token.isBlank()) {
            fcmNotificationTokenRepository.deleteByMember_Id(memberId);
            return;
        }
        String trimmed = token.trim();
        fcmNotificationTokenRepository.findByMember_IdAndToken(memberId, trimmed)
                .ifPresentOrElse(
                        FcmNotificationToken::touchLastUsed,
                        () -> fcmNotificationTokenRepository.save(FcmNotificationToken.create(member, trimmed))
                );
    }
}
