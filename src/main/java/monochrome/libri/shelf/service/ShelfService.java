package monochrome.libri.shelf.service;

import monochrome.libri.shelf.dto.response.ShelfDetailResponseDto;
import monochrome.libri.shelf.dto.request.ShelfUpdateRequestDto;
import monochrome.libri.shelf.dto.request.ShelfCreateRequestDto;

public interface ShelfService {
    ShelfDetailResponseDto getShelfDetail(long shelfId, Long memberId);
    ShelfDetailResponseDto updateShelf(long shelfId, Long memberId, ShelfUpdateRequestDto request);
    void createShelf(long memberId, ShelfCreateRequestDto request);
}
