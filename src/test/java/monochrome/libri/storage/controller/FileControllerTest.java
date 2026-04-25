package monochrome.libri.storage.controller;

import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.storage.dto.request.PresignedUploadRequestDto;
import monochrome.libri.storage.dto.response.PresignedDownloadResponseDto;
import monochrome.libri.storage.dto.response.PresignedUploadResponseDto;
import monochrome.libri.storage.service.PresignedUploadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private PresignedUploadService presignedUploadService;

    @InjectMocks
    private FileController controller;

    @Test
    void createPresignedUploadUrl_requiresAuth() {
        PresignedUploadRequestDto request = new PresignedUploadRequestDto("profiles", "me.png", "image/png");

        assertThatThrownBy(() -> controller.createPresignedUploadUrl(null, request))
                .isInstanceOf(LibriException.class);
        verifyNoInteractions(presignedUploadService);
    }

    @Test
    void createPresignedUploadUrl_returnsPresignedInfo() {
        UserPrincipal principal = new UserPrincipal(1L, null, List.of(), true);
        PresignedUploadRequestDto request = new PresignedUploadRequestDto("profiles", "me.png", "image/png");
        PresignedUploadResponseDto responseDto = new PresignedUploadResponseDto(
                "https://upload-url",
                "profiles/1/key.png",
                "https://file-url",
                300
        );
        when(presignedUploadService.createPresignedUploadUrl(1L, request)).thenReturn(responseDto);

        var response = controller.createPresignedUploadUrl(principal, request);

        verify(presignedUploadService).createPresignedUploadUrl(1L, request);
        assertThat(response.getBody().data().uploadUrl()).isEqualTo("https://upload-url");
        assertThat(response.getBody().data().key()).isEqualTo("profiles/1/key.png");
    }

    @Test
    void createPresignedDownloadUrl_requiresAuth() {
        assertThatThrownBy(() -> controller.createPresignedDownloadUrl(null, "https://file-url"))
                .isInstanceOf(LibriException.class);
        verifyNoInteractions(presignedUploadService);
    }

    @Test
    void createPresignedDownloadUrl_returnsPresignedInfo() {
        UserPrincipal principal = new UserPrincipal(1L, null, List.of(), true);
        PresignedDownloadResponseDto responseDto = new PresignedDownloadResponseDto(
                "https://download-url",
                "profiles/1/key.png",
                300
        );
        when(presignedUploadService.createPresignedDownloadUrl(1L, "https://file-url")).thenReturn(responseDto);

        var response = controller.createPresignedDownloadUrl(principal, "https://file-url");

        verify(presignedUploadService).createPresignedDownloadUrl(1L, "https://file-url");
        assertThat(response.getBody().data().downloadUrl()).isEqualTo("https://download-url");
        assertThat(response.getBody().data().key()).isEqualTo("profiles/1/key.png");
    }
}
