package monochrome.libri.global.swagger;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    /**
     * ✅ 커스터마이저에서 $ref로 참조하는 스키마(ApiErrorResponse)를
     * components.schemas에 "강제 등록"한다.

     * - ApiErrorResponse가 컨트롤러 메서드 시그니처에 직접 등장하지 않으면
     *   springdoc이 components에 스키마를 안 만들 수 있음
     * - 이 Bean이 있으면 항상 schemas에 들어가서 $ref가 해결됨
     */
    @Bean
    public OpenApiCustomizer registerApiErrorResponseSchema() {
        return openApi -> {
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.setComponents(components);
            }

            Map<String, Schema> schemas =
                    ModelConverters.getInstance().read(ApiErrorResponse.class);

            schemas.forEach(components::addSchemas);
        };
    }

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme bearer = new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("Libri API Documentation")
                        .version("v1.0.0"))
                .schemaRequirement(SECURITY_SCHEME_NAME, bearer)
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
