package com.example.wagemanager.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
        Components components = new Components()
                .addSecuritySchemes(jwt, new SecurityScheme()
                        .name(jwt)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"))
                .addSchemas("ApiErrorResponse", createErrorResponseSchema());

        return new OpenAPI()
                .info(new Info()
                        .title("WageManager API")
                        .description("근로자 급여 관리 시스템 API 문서")
                        .version("v1.0"))
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    /**
     * 에러 응답 스키마 정의
     * API_SPECIFICATION.md에 정의된 에러 응답 포맷과 일치
     */
    @SuppressWarnings("rawtypes")
    private Schema createErrorResponseSchema() {
        Schema errorDetailSchema = new Schema<>()
                .type("object")
                .addProperty("code", new Schema<>().type("string").example("NOT_FOUND"))
                .addProperty("message", new Schema<>().type("string").example("리소스를 찾을 수 없습니다."));

        return new Schema<>()
                .type("object")
                .addProperty("success", new Schema<>().type("boolean").example(false))
                .addProperty("data", new Schema<>().type("object").nullable(true).example(null))
                .addProperty("error", errorDetailSchema);
    }

    /**
     * 모든 API에 공통 에러 응답 추가
     * 기존 응답이 있더라도 에러 응답 스키마로 덮어씌움
     */
    @Bean
    public OperationCustomizer globalErrorResponseCustomizer() {
        return (operation, handlerMethod) -> {
            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }

            // 에러 응답용 Content 생성 (매번 새로 생성해야 함)
            // 400 Bad Request
            responses.addApiResponse("400", new ApiResponse()
                    .description("잘못된 요청")
                    .content(createErrorContent()));

            // 401 Unauthorized
            responses.addApiResponse("401", new ApiResponse()
                    .description("인증 실패")
                    .content(createErrorContent()));

            // 404 Not Found
            responses.addApiResponse("404", new ApiResponse()
                    .description("리소스를 찾을 수 없음")
                    .content(createErrorContent()));

            // 500 Internal Server Error
            responses.addApiResponse("500", new ApiResponse()
                    .description("서버 오류")
                    .content(createErrorContent()));

            return operation;
        };
    }

    /**
     * 에러 응답용 Content 생성
     */
    private Content createErrorContent() {
        return new Content()
                .addMediaType("application/json", new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ApiErrorResponse")));
    }
}
