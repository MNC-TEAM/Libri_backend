package monochrome.libri.global.swagger;

import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import monochrome.libri.global.exception.ErrorCode;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.*;

@Configuration
public class OpenApiServiceErrorCustomizerConfig {

    private static final String JSON = "application/json";
    private static final String ERROR_SCHEMA_REF = "#/components/schemas/ApiErrorResponse";
    private static final String DEFAULT_DESCRIPTION = "Service error";

    @Bean
    public OperationCustomizer serviceErrorResponsesCustomizer() {
        return (operation, handlerMethod) -> {
            Set<ErrorCode> codes = collectErrorCodes(handlerMethod);
            if (codes.isEmpty()) return operation;

            Map<Integer, List<ErrorCode>> byStatus = groupByHttpStatus(codes);
            applyResponses(operation.getResponses(), byStatus);

            return operation;
        };
    }

    /**
     * 클래스(@ApiErrorCodes on class) + 메서드(@ApiErrorCodes on method)의 ErrorCode를 모두 모음
     * - LinkedHashSet: 중복 제거 + 선언 순서 유지
     */
    private Set<ErrorCode> collectErrorCodes(HandlerMethod hm) {
        Set<ErrorCode> set = new LinkedHashSet<>();
        addFromAnnotation(set, hm.getBeanType().getAnnotation(ApiErrorCodes.class));
        addFromAnnotation(set, hm.getMethodAnnotation(ApiErrorCodes.class));
        return set;
    }

    private void addFromAnnotation(Set<ErrorCode> target, ApiErrorCodes ann) {
        if (ann == null) return;
        target.addAll(Arrays.asList(ann.value()));
    }

    /**
     * ErrorCode들을 HTTP Status별로 그룹핑
     * 예) 400 -> [C002, F002], 404 -> [M001, F003]
     */
    private Map<Integer, List<ErrorCode>> groupByHttpStatus(Set<ErrorCode> codes) {
        Map<Integer, List<ErrorCode>> map = new LinkedHashMap<>();
        for (ErrorCode ec : codes) {
            map.computeIfAbsent(ec.getHttpStatus().value(), k -> new ArrayList<>()).add(ec);
        }
        return map;
    }

    /**
     * status별 응답을 OpenAPI responses에 반영
     * - 이미 status 응답이 있으면 examples만 merge
     * - 없으면 새 ApiResponse 생성
     */
    private void applyResponses(ApiResponses responses, Map<Integer, List<ErrorCode>> byStatus) {
        for (Map.Entry<Integer, List<ErrorCode>> entry : byStatus.entrySet()) {
            String status = String.valueOf(entry.getKey());
            List<ErrorCode> errorCodes = entry.getValue();

            ApiResponse existing = responses.get(status);
            if (existing != null) {
                mergeExamples(existing, errorCodes);
            } else {
                responses.addApiResponse(status, buildResponse(errorCodes));
            }
        }
    }

    /**
     * 특정 status(예: 400)의 ApiResponse 생성
     * - schema: ApiErrorResponse (문서용 실패 응답)
     * - examples: ErrorCode별 예시를 여러 개 달아줌
     */
    private ApiResponse buildResponse(List<ErrorCode> codes) {
        Schema<?> schema = new Schema<>().$ref(ERROR_SCHEMA_REF);

        MediaType mediaType = new MediaType()
                .schema(schema)
                .examples(toExamples(codes));

        Content content = new Content().addMediaType(JSON, mediaType);

        return new ApiResponse()
                .description(DEFAULT_DESCRIPTION)
                .content(content);
    }

    /**
     * existing ApiResponse에 ErrorCode 예시를 추가(merge)
     * - JSON content가 없으면 아무것도 하지 않음
     */
    private void mergeExamples(ApiResponse existing, List<ErrorCode> addCodes) {
        if (existing.getContent() == null) return;

        MediaType mt = existing.getContent().get(JSON);
        if (mt == null) return;

        Map<String, Example> merged = new LinkedHashMap<>();
        Map<String, Example> current = mt.getExamples();

        if (current != null) merged.putAll(current);
        merged.putAll(toExamples(addCodes)); // key 충돌 시 addCodes가 덮어씀

        mt.setExamples(merged);
    }

    /**
     * ErrorCode 리스트 -> Swagger examples(Map)
     * key: ErrorCode.code (예: M001)
     */
    private Map<String, Example> toExamples(List<ErrorCode> codes) {
        Map<String, Example> examples = new LinkedHashMap<>();
        for (ErrorCode ec : codes) {
            examples.put(ec.getCode(), new Example()
                    .summary(ec.name())
                    .value(exampleBody(ec)));
        }
        return examples;
    }

    /**
     * 네 GlobalExceptionHandler가 반환하는 실패 응답(ApiResponse.error()) 형태와 동일한 예시 JSON
     */
    private Map<String, Object> exampleBody(ErrorCode ec) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("code", ec.getCode());
        body.put("message", ec.getMessage());
        body.put("data", null);
        return body;
    }
}
