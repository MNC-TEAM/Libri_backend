package monochrome.libri.home.service;

import monochrome.libri.home.dto.response.HomeResponseDto;

public interface HomeService {
    HomeResponseDto getHome(Long memberId);
}
