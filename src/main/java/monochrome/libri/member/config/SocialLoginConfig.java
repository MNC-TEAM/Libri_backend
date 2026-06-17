package monochrome.libri.member.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SocialLoginProperties.class)
public class SocialLoginConfig {

    @Bean
    public RestClient kakaoSocialRestClient(SocialLoginProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.kakao().baseUrl())
                .build();
    }

    @Bean
    public RestClient appleSocialRestClient(SocialLoginProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.apple().baseUrl())
                .build();
    }
}
