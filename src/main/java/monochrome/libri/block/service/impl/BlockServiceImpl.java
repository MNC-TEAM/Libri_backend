package monochrome.libri.block.service.impl;

import monochrome.libri.block.domain.MemberBlock;
import monochrome.libri.block.dto.response.BlockedMemberItemResponseDto;
import monochrome.libri.block.dto.response.BlockedMemberListResponseDto;
import monochrome.libri.block.repository.MemberBlockRepository;
import monochrome.libri.block.service.BlockService;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.repository.MemberRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BlockServiceImpl implements BlockService {

    private final MemberBlockRepository memberBlockRepository;
    private final MemberRepository memberRepository;

    public BlockServiceImpl(MemberBlockRepository memberBlockRepository, MemberRepository memberRepository) {
        this.memberBlockRepository = memberBlockRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public void blockMember(long blockerMemberId, long blockedMemberId) {
        validateNotSelf(blockerMemberId, blockedMemberId);

        Member blocker = getMember(blockerMemberId);
        Member blocked = getMember(blockedMemberId);

        if (memberBlockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            throw new LibriException(ErrorCode.BLOCK_ALREADY_EXISTS);
        }

        memberBlockRepository.save(MemberBlock.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build());
    }

    @Override
    @Transactional
    public void unblockMember(long blockerMemberId, long blockedMemberId) {
        validateNotSelf(blockerMemberId, blockedMemberId);

        Member blocker = getMember(blockerMemberId);
        Member blocked = getMember(blockedMemberId);
        MemberBlock memberBlock = memberBlockRepository.findByBlockerAndBlocked(blocker, blocked)
                .orElseThrow(() -> new LibriException(ErrorCode.BLOCK_NOT_FOUND));

        memberBlockRepository.delete(memberBlock);
    }

    @Override
    public BlockedMemberListResponseDto getBlockedMembers(long blockerMemberId, Pageable pageable) {
        if (blockerMemberId <= 0) {
            return BlockedMemberListResponseDto.empty(pageable.getPageNumber(), pageable.getPageSize());
        }

        getMember(blockerMemberId);
        Slice<MemberBlock> slice = memberBlockRepository.findByBlockerIdOrderByCreatedDateDesc(blockerMemberId, pageable);
        List<BlockedMemberItemResponseDto> content = slice.getContent().stream()
                .map(BlockedMemberItemResponseDto::from)
                .toList();

        return new BlockedMemberListResponseDto(
                memberBlockRepository.countByBlockerId(blockerMemberId),
                content,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    @Override
    public boolean hasBlockRelation(long memberId, long otherMemberId) {
        if (memberId <= 0 || otherMemberId <= 0) {
            return false;
        }

        return memberBlockRepository.existsByBlockerIdAndBlockedId(memberId, otherMemberId)
                || memberBlockRepository.existsByBlockerIdAndBlockedId(otherMemberId, memberId);
    }

    private Member getMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateNotSelf(long blockerMemberId, long blockedMemberId) {
        if (blockerMemberId == blockedMemberId) {
            throw new LibriException(ErrorCode.SELF_BLOCK_NOT_ALLOWED);
        }
    }
}
