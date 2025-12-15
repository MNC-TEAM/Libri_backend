package monochrome.libri.book.service.impl;

import monochrome.libri.book.domain.Book;
import monochrome.libri.book.dto.response.BookResponseDto;
import monochrome.libri.book.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    @DisplayName("keyword가 null/blank이면 빈 Slice를 반환하고 repository 호출을 하지 않는다")
    void searchBooks_blankKeyword_returnsEmptySlice_andNoRepositoryCalls() {
        Pageable pageable = PageRequest.of(0, 20);

        Slice<BookResponseDto> result1 = bookService.searchBooks(null, pageable);
        assertNotNull(result1);
        assertTrue(result1.getContent().isEmpty());
        assertFalse(result1.hasNext());

        Slice<BookResponseDto> result2 = bookService.searchBooks("   \n\t  ", pageable);
        assertNotNull(result2);
        assertTrue(result2.getContent().isEmpty());
        assertFalse(result2.hasNext());

        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("ISBN 형태(하이픈/공백 포함)면 findByIsbn()으로 단건 조회를 시도하고 성공 시 1개 Slice를 반환한다")
    void searchBooks_isbnFound_returnsSingleSlice() {
        Pageable pageable = PageRequest.of(0, 20);

        // 하이픈 포함 ISBN-13 입력 -> normalizeIsbn() 후 13자리 숫자로 변환되어야 함
        String raw = "978-893-247-3901";
        String normalizedIsbn = "9788932473901";

        Book book = mock(Book.class);
        when(bookRepository.findByIsbn(normalizedIsbn)).thenReturn(Optional.of(book));

        Slice<BookResponseDto> result = bookService.searchBooks(raw, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertFalse(result.hasNext()); // ISBN 단건 반환 로직에서 false로 고정

        // ISBN이면 searchByKeyword는 호출되면 안 됨
        verify(bookRepository, times(1)).findByIsbn(normalizedIsbn);
        verify(bookRepository, never()).searchByKeyword(anyString(), any());
    }

    @Test
    @DisplayName("ISBN 형태지만 DB에 없으면 빈 Slice를 반환한다")
    void searchBooks_isbnNotFound_returnsEmptySlice() {
        Pageable pageable = PageRequest.of(0, 20);

        String raw = "978-893-247-3901";
        String normalizedIsbn = "9788932473901";

        when(bookRepository.findByIsbn(normalizedIsbn)).thenReturn(Optional.empty());

        Slice<BookResponseDto> result = bookService.searchBooks(raw, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertFalse(result.hasNext());

        verify(bookRepository, times(1)).findByIsbn(normalizedIsbn);
        verify(bookRepository, never()).searchByKeyword(anyString(), any());
    }

    @Test
    @DisplayName("ISBN이 아니면 searchByKeyword()로 통합 검색하고, 공백 정규화된 keyword로 호출한다")
    void searchBooks_notIsbn_callsSearchByKeyword_withNormalizedTextKeyword() {
        Pageable pageable = PageRequest.of(0, 2);

        // 공백이 여러 개 섞인 입력
        String raw = "  해리   포터   ";
        String normalizedText = "해리 포터";

        Book b1 = mock(Book.class);
        Book b2 = mock(Book.class);

        // repository가 hasNext=true인 Slice를 준다고 가정 (무한스크롤 다음 페이지 존재)
        Slice<Book> repoSlice = new SliceImpl<>(List.of(b1, b2), pageable, true);
        when(bookRepository.searchByKeyword(anyString(), any())).thenReturn(repoSlice);

        Slice<BookResponseDto> result = bookService.searchBooks(raw, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertTrue(result.hasNext()); // Slice.map()은 hasNext를 유지해야 함

        // searchByKeyword 호출 시 keyword가 "해리 포터"로 정규화되어 전달되는지 캡처로 검증
        ArgumentCaptor<String> keywordCaptor = ArgumentCaptor.forClass(String.class);
        verify(bookRepository, times(1)).searchByKeyword(keywordCaptor.capture(), eq(pageable));
        assertEquals(normalizedText, keywordCaptor.getValue());

        // ISBN 분기 로직이 아니라서 findByIsbn은 호출되면 안 됨
        verify(bookRepository, never()).findByIsbn(anyString());
    }
}