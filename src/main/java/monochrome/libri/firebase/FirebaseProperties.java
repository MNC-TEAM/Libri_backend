package monochrome.libri.firebase;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {

    /**
     * true일 때 서비스 계정으로 FirebaseApp을 초기화하고 FCM 전송을 시도합니다.
     */
    private boolean enabled = true;

    /**
     * 서비스 계정 JSON 리소스 위치. 기본은 {@code classpath:libri-firebase.json}({@code src/main/resources/libri-firebase.json}).
     * 운영 등에서는 {@code file:/절대경로/키.json} 또는 {@code FIREBASE_CREDENTIALS_PATH}로 덮어씁니다.
     */
    private String credentialsPath = "";
}
