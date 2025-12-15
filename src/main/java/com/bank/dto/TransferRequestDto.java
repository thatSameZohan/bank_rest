package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO, представляющий собой запрос на перевод денег между двумя картами.
 *
 * <p>Используется для переводов между картами, принадлежащими одному и тому же пользователю.</p>
 */
public record TransferRequestDto(

    @NotNull
    @Schema(
            description = "Identifier of the source card (money will be withdrawn from this card)",
            example = "101",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long fromCardId,

    @NotNull
    @Schema (description = "Identifier of the destination card (money will be credited to this card)",
    example = "202",
    requiredMode = Schema.RequiredMode.REQUIRED
)
    Long toCardId,

    @NotNull @DecimalMin("0.01")
    @Schema(
            description = "Transfer amount. Must be greater than zero",
            example = "150.50",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    BigDecimal amount
) {}
