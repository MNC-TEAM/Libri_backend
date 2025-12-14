package monochrome.libri.book.service;

import monochrome.libri.book.dto.response.BookResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface BookService {
    /**
     * 검색 API
     * 책 제목, 저자, 출판사, isbn 등 다양한 검색 지원
     */

    Slice<BookResponseDto> searchBooks(String keyword, Pageable pageable);
}
