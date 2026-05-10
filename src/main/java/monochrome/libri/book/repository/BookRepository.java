package monochrome.libri.book.repository;

import monochrome.libri.book.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    Optional<Book> findByIsbn(String isbn);
    Optional<Book> findFirstByTitleAndAuthorAndPublisher(String title, String author, String publisher);
    Optional<Book> findFirstByTitleAndAuthor(String title, String author);
}
