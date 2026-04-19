package monochrome.libri.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import monochrome.libri.firebase.FcmPushSender;
import monochrome.libri.firebase.FirebaseFcmPushSender;
import monochrome.libri.firebase.FirebaseProperties;
import monochrome.libri.member.repository.FcmNotificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@EnableConfigurationProperties(FirebaseProperties.class)
public class FirebaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfiguration.class);

    @Bean
    @ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
    public FirebaseApp firebaseApp(FirebaseProperties properties, ResourceLoader resourceLoader) {
        String path = properties.getCredentialsPath();
        if (path == null || path.isBlank()) {
            throw new IllegalStateException(
                    "firebase.enabled=true 인데 firebase.credentials-path 가 비어 있습니다. "
                            + "FIREBASE_CREDENTIALS_PATH 등으로 서비스 계정 JSON 경로를 설정하세요."
            );
        }
        Resource resource = resourceLoader.getResource(path.trim());
        try (InputStream in = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(in))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("FirebaseApp 초기화 완료 (credentials: {})", path.trim());
                return app;
            }
            return FirebaseApp.getInstance();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Firebase credentials 를 읽을 수 없습니다. 경로를 확인하세요: " + path.trim()
                            + " (classpath: 는 src/main/resources 기준 파일명)",
                    e
            );
        }
    }

    @Bean
    public FcmPushSender fcmPushSender(
            FirebaseProperties properties,
            ObjectProvider<FirebaseApp> firebaseAppProvider,
            FcmNotificationTokenRepository fcmNotificationTokenRepository
    ) {
        if (properties.isEnabled()) {
            FirebaseApp app = firebaseAppProvider.getIfAvailable();
            if (app != null) {
                return new FirebaseFcmPushSender(app, fcmNotificationTokenRepository);
            }
            log.warn("firebase.enabled=true 이지만 FirebaseApp 빈이 없어 FCM 전송을 건너뜁니다.");
        }
        return (recipientMemberId, title, body, data) -> {
            // firebase 비활성 또는 FirebaseApp 미초기화 시 no-op
        };
    }
}
