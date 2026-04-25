package monochrome.libri.storage.dto.response;

public record PresignedDownloadResponseDto(
        String downloadUrl,
        String key,
        long expiresInSeconds
) {
}
