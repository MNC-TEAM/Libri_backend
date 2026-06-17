package monochrome.libri.block.service;

import monochrome.libri.block.dto.response.BlockedMemberListResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface BlockService {
    void blockMember(long blockerMemberId, long blockedMemberId);
    void unblockMember(long blockerMemberId, long blockedMemberId);
    BlockedMemberListResponseDto getBlockedMembers(long blockerMemberId, Pageable pageable);
    boolean hasBlockRelation(long memberId, long otherMemberId);
    Set<Long> getBlockedRelationMemberIds(long memberId);
}
