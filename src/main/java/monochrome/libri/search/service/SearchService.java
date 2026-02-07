package monochrome.libri.search.service;

import monochrome.libri.search.dto.response.RecentSearchListResponseDto;
import monochrome.libri.search.dto.response.TrendingKeywordListResponseDto;
import org.springframework.data.domain.Pageable;

public interface SearchService {
    void recordSearch(long memberId, String keyword);
    RecentSearchListResponseDto getRecentSearches(long memberId, Pageable pageable);
    void deleteRecentSearch(long memberId, long searchId);
    void deleteAllRecentSearches(long memberId);
    TrendingKeywordListResponseDto getTrendingKeywords(int limit);
}
