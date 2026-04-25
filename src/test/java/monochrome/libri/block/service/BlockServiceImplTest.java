package monochrome.libri.block.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.block.domain.MemberBlock;
import monochrome.libri.block.repository.MemberBlockRepository;
import monochrome.libri.block.service.impl.BlockServiceImpl;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BlockServiceImplTest {

    @Test
    void blockMember_savesNewBlock() {
        MemberBlockRepository blockRepository = mock(MemberBlockRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        BlockServiceImpl service = new BlockServiceImpl(blockRepository, memberRepository);
        Member blocker = TestFixtures.member(1L);
        Member blocked = TestFixtures.member(2L);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(blocker));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(blocked));
        when(blockRepository.existsByBlockerAndBlocked(blocker, blocked)).thenReturn(false);

        service.blockMember(1L, 2L);

        ArgumentCaptor<MemberBlock> captor = ArgumentCaptor.forClass(MemberBlock.class);
        verify(blockRepository).save(captor.capture());
        assertThat(captor.getValue().getBlocker()).isEqualTo(blocker);
        assertThat(captor.getValue().getBlocked()).isEqualTo(blocked);
    }

    @Test
    void blockMember_rejectsDuplicate() {
        MemberBlockRepository blockRepository = mock(MemberBlockRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        BlockServiceImpl service = new BlockServiceImpl(blockRepository, memberRepository);
        Member blocker = TestFixtures.member(1L);
        Member blocked = TestFixtures.member(2L);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(blocker));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(blocked));
        when(blockRepository.existsByBlockerAndBlocked(blocker, blocked)).thenReturn(true);

        assertThatThrownBy(() -> service.blockMember(1L, 2L))
                .isInstanceOf(LibriException.class);
        verify(blockRepository, never()).save(any());
    }

    @Test
    void getBlockedMembers_returnsSlice() {
        MemberBlockRepository blockRepository = mock(MemberBlockRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        BlockServiceImpl service = new BlockServiceImpl(blockRepository, memberRepository);
        Member blocker = TestFixtures.member(1L);
        Member blocked = TestFixtures.member(2L);
        MemberBlock memberBlock = MemberBlock.builder()
                .id(10L)
                .blocker(blocker)
                .blocked(blocked)
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(blocker));
        when(blockRepository.findByBlockerIdOrderByCreatedDateDesc(1L, PageRequest.of(0, 20)))
                .thenReturn(new SliceImpl<>(List.of(memberBlock), PageRequest.of(0, 20), false));
        when(blockRepository.countByBlockerId(1L)).thenReturn(1L);

        var response = service.getBlockedMembers(1L, PageRequest.of(0, 20));

        assertThat(response.totalCount()).isEqualTo(1L);
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).memberId()).isEqualTo(2L);
    }
}
