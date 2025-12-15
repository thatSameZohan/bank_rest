package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO для ответа с информацией о банковской карте.
 *
 * <p>Используется для отображения информации о карте пользователю или администратору.</p>
 *
 * @param id уникальный идентификатор карты
 * @param userId идентификатор владельца карты
 * @param maskedNumber маскированный номер карты (например, **** **** **** 1234)
 * @param ownerName имя владельца карты
 * @param expiryDate срок действия карты
 * @param status статус карты ("ACTIVE", "BLOCKED", "EXPIRED")
 * @param balance текущий баланс карты
 */
@Schema(description = "DTO representing a bank card information")
public record CardResponseDto(

    @Schema(description = "Card ID", example = "1")
    Long id,

    @Schema(description = "ID of the user owning the card", example = "1")
    Long userId,

    @Schema(description = "Masked card number", example = "**** **** **** 1234")
    String maskedNumber,

    @Schema(description = "Card owner's full name", example = "John Doe")
    String ownerName,

    @Schema(description = "Expiry date of the card", example = "2027-12-31")
    LocalDate expiryDate,

    @Schema(description = "Status of the card", example = "ACTIVE")
    String status,

    @Schema(description = "Current balance of the card", example = "1500.75")
    BigDecimal balance
) {}
