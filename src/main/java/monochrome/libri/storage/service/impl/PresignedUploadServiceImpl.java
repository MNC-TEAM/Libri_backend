package monochrome.libri.storage.service.impl;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.storage.config.S3Properties;
import monochrome.libri.storage.dto.request.PresignedUploadRequestDto;
import monochrome.libri.storage.dto.response.PresignedDownloadResponseDto;
import monochrome.libri.storage.dto.response.PresignedUploadResponseDto;
import monochrome.libri.storage.domain.UploadDirectory;
import monochrome.libri.storage.service.PresignedUploadService;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Service
public class PresignedUploadServiceImpl implements PresignedUploadService {

    private static final java.util.Set<String> SUPPORTED_CONTENT_TYPES = java.util.Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    public PresignedUploadServiceImpl(S3Presigner s3Presigner, S3Properties s3Properties) {
        this.s3Presigner = s3Presigner;
        this.s3Properties = s3Properties;
    }

    @Override
    public PresignedUploadResponseDto createPresignedUploadUrl(long memberId, PresignedUploadRequestDto request) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (request == null || request.directory() == null || request.fileName() == null || request.contentType() == null) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (s3Properties.bucket() == null || s3Properties.bucket().isBlank()) {
            throw new LibriException(ErrorCode.S3_CONFIGURATION_MISSING);
        }

        String directory = request.directory().trim();
        String fileName = request.fileName().trim();
        String contentType = request.contentType().trim();

        if (!UploadDirectory.supports(directory) || !SUPPORTED_CONTENT_TYPES.contains(contentType)) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String key = buildObjectKey(directory, memberId, fileName);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(s3Properties.uploadExpirationSeconds()))
                .putObjectRequest(putObjectRequest)
                .build();
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUploadResponseDto(
                presignedRequest.url().toString(),
                key,
                buildFileUrl(key),
                s3Properties.uploadExpirationSeconds()
        );
    }

    @Override
    public PresignedDownloadResponseDto createPresignedDownloadUrl(long memberId, String fileUrl) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (s3Properties.bucket() == null || s3Properties.bucket().isBlank()) {
            throw new LibriException(ErrorCode.S3_CONFIGURATION_MISSING);
        }

        String key = extractObjectKey(fileUrl.trim());
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(s3Properties.uploadExpirationSeconds()))
                .getObjectRequest(getObjectRequest)
                .build();
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return new PresignedDownloadResponseDto(
                presignedRequest.url().toString(),
                key,
                s3Properties.uploadExpirationSeconds()
        );
    }

    private String buildObjectKey(String directory, long memberId, String fileName) {
        String extension = extractExtension(fileName);
        return directory + "/" + memberId + "/" + UUID.randomUUID() + extension;
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex).toLowerCase();
    }

    private String buildFileUrl(String key) {
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8).replace("+", "%20");
        if (s3Properties.publicBaseUrl() != null && !s3Properties.publicBaseUrl().isBlank()) {
            return trimTrailingSlash(s3Properties.publicBaseUrl()) + "/" + encodedKey;
        }
        return "https://" + s3Properties.bucket() + ".s3." + s3Properties.region() + ".amazonaws.com/" + encodedKey;
    }

    private String extractObjectKey(String fileUrl) {
        try {
            URI uri = URI.create(fileUrl);
            String path = uri.getPath();
            if (path == null || path.isBlank() || "/".equals(path)) {
                throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
            }
            String key = path.startsWith("/") ? path.substring(1) : path;
            return URLDecoder.decode(key, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
