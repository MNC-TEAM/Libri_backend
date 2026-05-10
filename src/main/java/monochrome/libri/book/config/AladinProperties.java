package monochrome.libri.book.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aladin")
public record AladinProperties(
        String baseUrl,
        String ttbKey,
        String version,
        int maxResults
) {
    public boolean isConfigured() {
        return ttbKey != null && !ttbKey.isBlank();
    }
}
