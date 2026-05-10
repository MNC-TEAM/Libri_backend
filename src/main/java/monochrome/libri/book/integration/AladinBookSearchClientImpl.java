package monochrome.libri.book.integration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import monochrome.libri.book.config.AladinProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Component
public class AladinBookSearchClientImpl implements AladinBookSearchClient {

    private final RestClient restClient;
    private final AladinProperties properties;

    public AladinBookSearchClientImpl(RestClient aladinRestClient, AladinProperties properties) {
        this.restClient = aladinRestClient;
        this.properties = properties;
    }

    @Override
    public List<AladinBookItem> searchByKeyword(String keyword, int start, int maxResults) {
        if (!properties.isConfigured()) {
            return List.of();
        }

        JsonNode body = get("/ttb/api/ItemSearch.aspx", uriBuilder -> uriBuilder
                .queryParam("Query", keyword)
                .queryParam("QueryType", "Keyword")
                .queryParam("SearchTarget", "Book")
                .queryParam("Cover", "Big")
                .queryParam("start", start)
                .queryParam("MaxResults", maxResults)
        );

        return parseItems(body);
    }

    @Override
    public Optional<AladinBookItem> lookupByIsbn(String isbn) {
        if (!properties.isConfigured()) {
            return Optional.empty();
        }
        String itemIdType = isbn != null && isbn.length() == 13 ? "ISBN13" : "ISBN";

        JsonNode body = get("/ttb/api/ItemLookUp.aspx", uriBuilder -> uriBuilder
                .queryParam("ItemIdType", itemIdType)
                .queryParam("ItemId", isbn)
                .queryParam("SearchTarget", "Book")
                .queryParam("Cover", "Big")
                .queryParam("MaxResults", 1)
        );

        return parseItems(body).stream().findFirst();
    }

    @Override
    public List<AladinBookItem> fetchBestsellers(int maxResults) {
        if (!properties.isConfigured()) {
            return List.of();
        }

        JsonNode body = get("/ttb/api/ItemList.aspx", uriBuilder -> uriBuilder
                .queryParam("QueryType", "Bestseller")
                .queryParam("SearchTarget", "Book")
                .queryParam("Cover", "Big")
                .queryParam("start", 1)
                .queryParam("MaxResults", maxResults)
        );

        return parseItems(body);
    }

    private JsonNode get(String path, Function<UriBuilder, UriBuilder> customizer) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> buildUri(customizer.apply(uriBuilder.path(path))))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            log.warn("aladin.request.failed path={} message={}", path, e.getMessage());
            return null;
        }
    }

    private URI buildUri(UriBuilder uriBuilder) {
        return uriBuilder
                .queryParam("ttbkey", properties.ttbKey())
                .queryParam("output", "js")
                .queryParam("Version", properties.version())
                .build();
    }

    private List<AladinBookItem> parseItems(JsonNode body) {
        if (body == null || !body.has("item") || !body.get("item").isArray()) {
            return List.of();
        }

        List<AladinBookItem> items = new ArrayList<>();
        for (JsonNode item : body.get("item")) {
            items.add(new AladinBookItem(
                    text(item, "title"),
                    text(item, "author"),
                    text(item, "publisher"),
                    text(item, "isbn"),
                    text(item, "isbn13"),
                    text(item, "cover"),
                    text(item, "description"),
                    parseDate(text(item, "pubDate")),
                    item.path("subInfo").path("itemPage").asInt(0),
                    text(item, "link")
            ));
        }
        return items;
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        String value = field.asText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
