package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * DTO для создания новой банковской карты.
 *
 * <p>Используется администратором для создания карты для конкретного пользователя.</p>
 *
 * @param cardNumber номер карты (строка, не пустая)
 * @param ownerName имя владельца карты (строка, не пустая)
 * @param userId идентификатор пользователя, которому принадлежит карта
 * @param expiryDate срок действия карты
 * @param initialBalance начальный баланс карты (>= 0)
 */
@Schema(description = "DTO for creating a new bank card")
public record CardCreateDto(

    @NotBlank
    @Schema(
            description = "Card number in plain format. Will be masked in responses",
            example = "1234567890123456",
            required = true
    )
    String cardNumber,

    @NotBlank
    @Schema(
            description = "Owner's full name",
            example = "John Doe",
            required = true
    )
    String ownerName,

    @NotNull
    @Schema(
            description = "ID of the user to whom this card belongs",
            example = "1",
            required = true
    )
    Long userId,

    @Schema(
            description = "Expiry date of the card",
            example = "2027-12-31",
            required = true
    )
    @NotNull
    LocalDate expiryDate,

    @NotNull @DecimalMin("0.0")
    @Schema(
            description = "Initial balance of the card. Must be zero or positive",
            example = "1000.50",
            required = true
    )
    BigDecimal initialBalance
) {}
