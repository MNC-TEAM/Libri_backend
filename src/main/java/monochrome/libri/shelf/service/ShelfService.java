package monochrome.libri.shelf.service;

import monochrome.libri.shelf.dto.response.ShelfDetailResponseDto;
import monochrome.libri.shelf.dto.response.ShelfListResponseDto;
import monochrome.libri.shelf.dto.request.ShelfUpdateRequestDto;
import monochrome.libri.shelf.dto.request.ShelfCreateRequestDto;
import monochrome.libri.shelf.domain.ShelfStatus;
import org.springframework.data.domain.Pageable;

public interface ShelfService {
    ShelfDetailResponseDto getShelfDetail(long shelfId, Long memberId);
    ShelfListResponseDto getShelvesByStatus(Long memberId, ShelfStatus status, Pageable pageable);
    ShelfDetailResponseDto updateShelf(long shelfId, Long memberId, ShelfUpdateRequestDto request);
    void createShelf(long memberId, ShelfCreateRequestDto request);
}
