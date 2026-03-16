package monochrome.libri.search.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.book.repository.BookRepository;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.search.domain.SearchKeyword;
import monochrome.libri.search.repository.SearchKeywordCountRow;
import monochrome.libri.search.repository.SearchKeywordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private SearchKeywordRepository searchKeywordRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private monochrome.libri.search.service.impl.SearchServiceImpl service;

    @Test
    void recordSearch_ignoresInvalidMemberId() {
        service.recordSearch(0L, "keyword");
        verifyNoInteractions(memberService, searchKeywordRepository);
    }

    @Test
    void recordSearch_savesNormalizedKeyword() {
        Member member = TestFixtures.member(1L);
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member));

        service.recordSearch(1L, "  hello   world ");

        ArgumentCaptor<SearchKeyword> captor = ArgumentCaptor.forClass(SearchKeyword.class);
        verify(searchKeywordRepository).save(captor.capture());
        assertThat(captor.getValue().getKeyword()).isEqualTo("hello world");
    }

    @Test
    void getRecentSearches_returnsEmptyWhenUnauthenticated() {
        var response = service.getRecentSearches(0L, PageRequest.of(0, 10));

        assertThat(response.totalCount()).isZero();
        assertThat(response.content()).isEmpty();
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    void getRecentSearches_mapsSlice() {
        SearchKeyword keyword = SearchKeyword.builder().id(1L).keyword("hello").build();
        when(searchKeywordRepository.findByMemberIdOrderByCreatedDateDesc(eq(1L), any()))
                .thenReturn(new SliceImpl<>(List.of(keyword), PageRequest.of(0, 10), false));
        when(searchKeywordRepository.countByMemberId(1L)).thenReturn(1L);

        var response = service.getRecentSearches(1L, PageRequest.of(0, 10));

        assertThat(response.totalCount()).isEqualTo(1L);
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void getTrendingKeywords_clampsLimitAndMaps() {
        SearchKeywordCountRow row = new SearchKeywordCountRow() {
            @Override
            public String getKeyword() {
                return "hello";
            }

            @Override
            public Long getCount() {
                return null;
            }
        };

        when(searchKeywordRepository.findTopKeywords(any())).thenReturn(List.of(row));
        when(bookRepository.searchByKeyword(eq("hello"), any()))
                .thenReturn(new SliceImpl<>(List.of(TestFixtures.book(1L, 100)), PageRequest.of(0, 1), false));

        var response = service.getTrendingKeywords(999);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).count()).isEqualTo(0L);
        assertThat(response.content().get(0).book().bookId()).isEqualTo(1L);
    }
}
