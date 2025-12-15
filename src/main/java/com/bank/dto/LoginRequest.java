package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO для запроса аутентификации пользователя.
 *
 * <p>Используется при логине для передачи имени пользователя и пароля.</p>
 *
 * @param username имя пользователя (обязательное поле)
 * @param password пароль пользователя (обязательное поле)
 */
@Schema(description = "DTO for user login request")
public record LoginRequest(

        @NotBlank
        @Schema(description = "Username of the user", example = "johndoe", required = true)
        String username,

        @NotBlank
        @Schema(description = "Password of the user", example = "P@ssw0rd!", required = true)
        String password
) {}
