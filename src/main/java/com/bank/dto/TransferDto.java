package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO, представляющий собой операцию перевода
 *
 * <p>Используется для возврата информации о переводах в списках (формат для администратора)</p>
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Builder
public class TransferDto {

    @Schema(
            description = "Unique identifier of the transfer",
            example = "42"
    )
    private Long id;

    @Schema(
            description = "Identifier of the user who initiated the transfer",
            example = "10"
    )
    private Long userId;

    @Schema(
            description = "Identifier of the source card",
            example = "1001"
    )
    private Long fromCardId;

    @Schema(
            description = "Identifier of the destination card",
            example = "1002"
    )
    private Long toCardId;

    @Schema(
            description = "Transferred amount",
            example = "150.75"
    )
    private BigDecimal amount;

    @Schema(
            description = "Date and time when the transfer was created",
            example = "2025-01-15T13:45:00"
    )
    private LocalDateTime createdAt;
}
