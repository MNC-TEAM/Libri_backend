package monochrome.libri.book.repository;

import monochrome.libri.book.domain.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface BookRepositoryCustom {
    Slice<Book> searchByKeyword(String textKeyword, Pageable pageable);
}
