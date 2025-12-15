package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для ответа аутентификации.
 *
 * <p>Содержит JWT токен доступа, который выдается после успешного логина
 * или обновления токенов.</p>
 *
 * @param accessToken JWT токен доступа для аутентификации пользователя
 */
@Schema(description = "Response DTO containing JWT access token")
public record AuthResponse(

        @Schema(
                description = "JWT access token for authenticating the user",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String accessToken
) {}