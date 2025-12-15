package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO для регистрации нового пользователя.
 *
 * <p>Используется при создании нового аккаунта.
 * Поле {@code isAdmin} определяет, будет ли пользователь создан с ролью ADMIN.</p>
 *
 * @param username уникальное имя пользователя
 * @param password пароль пользователя
 * @param isAdmin  флаг создания администратора
 */
@Schema(description = "DTO for user registration request")
public record RegisterRequest(
        @NotBlank
        @Schema(
                description = "Unique username for the new user",
                example = "john",
                required = true
        )
        String username,

        @NotBlank
        @Schema(
                description = "Password for the new user",
                example = "P@ssw0rd!",
                required = true
        )
        String password,

        @Schema(
                description = "Flag indicating whether the user should be created with ADMIN role",
                example = "false"
        )
        boolean isAdmin) {}
