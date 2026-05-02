package monochrome.libri.storage.dto.response;

public record PresignedUploadResponseDto(
        String uploadUrl,
        String key,
        String fileUrl,
        long expiresInSeconds
) {
}
