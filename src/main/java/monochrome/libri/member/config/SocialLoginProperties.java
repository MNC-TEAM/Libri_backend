package monochrome.libri.member.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.social")
public record SocialLoginProperties(
        Kakao kakao,
        Apple apple
) {
    public record Kakao(
            String baseUrl
    ) {
    }

    public record Apple(
            String baseUrl,
            String issuer,
            List<String> clientIds
    ) {
    }
}
