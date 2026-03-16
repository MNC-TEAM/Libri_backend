package monochrome.libri.search.service.impl;

import monochrome.libri.book.repository.BookRepository;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.search.domain.SearchKeyword;
import monochrome.libri.search.dto.response.RecentSearchItemResponseDto;
import monochrome.libri.search.dto.response.RecentSearchListResponseDto;
import monochrome.libri.search.dto.response.TrendingBookSummaryResponseDto;
import monochrome.libri.search.dto.response.TrendingKeywordListResponseDto;
import monochrome.libri.search.dto.response.TrendingKeywordResponseDto;
import monochrome.libri.search.repository.SearchKeywordRepository;
import monochrome.libri.search.service.SearchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private static final int MAX_KEYWORD_LENGTH = 200;

    private final SearchKeywordRepository searchKeywordRepository;
    private final MemberService memberService;
    private final BookRepository bookRepository;

    public SearchServiceImpl(
            SearchKeywordRepository searchKeywordRepository,
            MemberService memberService,
            BookRepository bookRepository
    ) {
        this.searchKeywordRepository = searchKeywordRepository;
        this.memberService = memberService;
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional
    public void recordSearch(long memberId, String keyword) {
        if (memberId <= 0) {
            return;
        }
        String normalized = normalizeKeyword(keyword);
        if (normalized.isBlank()) {
            return;
        }
        if (normalized.length() > MAX_KEYWORD_LENGTH) {
            normalized = normalized.substring(0, MAX_KEYWORD_LENGTH);
        }
        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));

        SearchKeyword record = SearchKeyword.builder()
                .member(member)
                .keyword(normalized)
                .build();
        searchKeywordRepository.save(record);
    }

    @Override
    public RecentSearchListResponseDto getRecentSearches(long memberId, Pageable pageable) {
        if (memberId <= 0) {
            return RecentSearchListResponseDto.empty(pageable.getPageNumber(), pageable.getPageSize());
        }

        Slice<SearchKeyword> slice = searchKeywordRepository.findByMemberIdOrderByCreatedDateDesc(memberId, pageable);
        List<RecentSearchItemResponseDto> content = slice.getContent().stream()
                .map(row -> new RecentSearchItemResponseDto(
                        row.getId(),
                        row.getKeyword(),
                        row.getCreatedDate()
                ))
                .toList();

        long totalCount = searchKeywordRepository.countByMemberId(memberId);

        return new RecentSearchListResponseDto(
                totalCount,
                content,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    @Override
    @Transactional
    public void deleteRecentSearch(long memberId, long searchId) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        SearchKeyword keyword = searchKeywordRepository.findByIdAndMemberId(searchId, memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.INVALID_INPUT_VALUE));
        searchKeywordRepository.delete(keyword);
    }

    @Override
    @Transactional
    public void deleteAllRecentSearches(long memberId) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        searchKeywordRepository.deleteByMemberId(memberId);
    }

    @Override
    public TrendingKeywordListResponseDto getTrendingKeywords(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        Pageable pageable = PageRequest.of(0, safeLimit);
        var rows = searchKeywordRepository.findTopKeywords(pageable);
        List<TrendingKeywordResponseDto> content = rows.stream()
                .map(row -> {
                    var books = bookRepository.searchByKeyword(row.getKeyword(), PageRequest.of(0, 1)).getContent();
                    if (books.isEmpty()) {
                        return null;
                    }
                    return new TrendingKeywordResponseDto(
                            TrendingBookSummaryResponseDto.from(books.get(0)),
                            row.getCount() == null ? 0L : row.getCount()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        return new TrendingKeywordListResponseDto(content);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim().replaceAll("\\s+", " ");
    }
}
