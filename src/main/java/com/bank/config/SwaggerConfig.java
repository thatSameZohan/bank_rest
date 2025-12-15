package com.bank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Конфигурация Swagger / OpenAPI для проекта.
 *
 * <p>Настраивает:
 * <ul>
 *     <li>Информацию о API (название, версия, описание)</li>
 *     <li>Схему аутентификации через Bearer JWT токен</li>
 *     <li>Глобальные требования безопасности для всех эндпоинтов</li>
 * </ul>
 * </p>
 */
@Configuration
public class SwaggerConfig {

    /**
     * Настраивает OpenAPI спецификацию с информацией о проекте
     * и глобальными требованиями безопасности.
     *
     * @return объект {@link OpenAPI} для SpringDoc
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank API")
                        .version("1.0.0")
                        .description("API for managing user, cards, transfers"))
                .security(Arrays.asList(new SecurityRequirement().addList("bearer-token")));
    }

    /**
     * Настраивает схему аутентификации Bearer JWT для Swagger UI.
     *
     * @return объект {@link SecurityScheme} для SpringDoc
     */
    @Bean
    public SecurityScheme bearerAuth() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
    }

}
