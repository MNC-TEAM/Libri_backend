package monochrome.libri.storage.service;

import monochrome.libri.storage.dto.request.PresignedUploadRequestDto;
import monochrome.libri.storage.dto.response.PresignedUploadResponseDto;

public interface PresignedUploadService {
    PresignedUploadResponseDto createPresignedUploadUrl(long memberId, PresignedUploadRequestDto request);
}
