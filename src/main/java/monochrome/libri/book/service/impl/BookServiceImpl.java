package monochrome.libri.book.service.impl;

import lombok.extern.slf4j.Slf4j;
import monochrome.libri.book.domain.Book;
import monochrome.libri.book.dto.response.BookResponseDto;
import monochrome.libri.book.repository.BookRepository;
import monochrome.libri.book.repository.BookRepositoryCustom;
import monochrome.libri.book.service.BookService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // ISBN-10: 9자리 숫자 + (숫자 or X)
    private static final Pattern ISBN10 = Pattern.compile("^\\d{9}[\\dXx]$");
    // ISBN-13: 13자리 숫자
    private static final Pattern ISBN13 = Pattern.compile("^\\d{13}$");

    /**
     * 검색 서비스
     * 책 제목, 저자, 출판사, ISBN 등 다양한 검색 가능
     * @param keyword 검색어
     * @param pageable 무한스크롤용 
     * @return Slice형태의 검색 결과
     */
    @Override
    public Slice<BookResponseDto> searchBooks(String keyword, Pageable pageable) {

        String textKeyword = normalizeText(keyword);
        String isbnKeyword = normalizeIsbn(keyword);

        if(textKeyword.isBlank()) {
            return emptySlice(pageable);
        }

        // ISBN이면 검색 시도
        if(looksLikeIsbn(isbnKeyword)) {
            Optional<Book> found = bookRepository.findByIsbn(isbnKeyword);

            return found.<Slice<BookResponseDto>>map(book -> new SliceImpl<>(
                    List.of(BookResponseDto.from(book)),
                    pageable,
                    false
            )).orElseGet(() -> emptySlice(pageable));
        }

        // 그 외 검색(책 제목, 저자, 출판사 등)
        return bookRepository.searchByKeyword(textKeyword, pageable)
                .map(BookResponseDto::from);
    }

    /**
     * 빈 SliceImpl 생성 함수(BookResponseDto 타입)
     * @param pageable pageable
     * @return 빈 Slice
     */
    private Slice<BookResponseDto> emptySlice(Pageable pageable) {
        return new SliceImpl<>(List.of(), pageable, false);
    }

    /**
     * 텍스트 검색(제목, 저자 등) 정규화
     * 앞 뒤 공백 제거, 중간 연속 공배(탭, 줄바꿈 포함)을 공백 1개로
     * ex) "  해리   포터   " -> "해리 포터"
     * @param s 검색 데이터
     * @return 정규화 후 검색 데이터
     */
    private String normalizeText(String s) {
        if(s == null) return "";

        return s.trim()
                .replaceAll("\\s+", " ");
    }

    /**
     * ISBN 검색 정규화
     * 하이픈, 공백을 제거
     * @param s 검색 데이터
     * @return 정규화 후 검색 데이터
     */
    private String normalizeIsbn(String s) {
        if(s == null) return "";
        return s.replaceAll("[\\s-]", "");
    }

    /**
     * ISBN 체크
     * TODO: 현재는 임시 코드(자리수, 형태만 보는 코드) 추후 검증 코드까지 추가 예정
     * @param s 검색 데이터
     * @return ISBN 유무
     */
    private boolean looksLikeIsbn(String s) {
        return ISBN13.matcher(s).matches() || ISBN10.matcher(s).matches();
    }
}
