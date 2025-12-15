package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO, представляющий результат перевода между картами пользователя.
 *
 * <p>Возвращается после успешного перевода (формат для пользователя)</p>
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponseDto {

    @Schema(
            description = "Masked number of the source card",
            example = "**** **** **** 1234"
    )
    private String fromCardMasked;

    @Schema(
            description = "Masked number of the destination card",
            example = "**** **** **** 5678"
    )
    private String toCardMasked;

    @Schema(
            description = "Amount of money transferred",
            example = "250.00"
    )
    private BigDecimal amount;

    @Schema(
            description = "Source card balance after the transfer",
            example = "750.00"
    )
    private BigDecimal fromCardBalanceAfter;

    @Schema(
            description = "Destination card balance after the transfer",
            example = "1250.00"
    )
    private BigDecimal toCardBalanceAfter;

    @Schema(
            description = "Date and time when the transfer was completed",
            example = "2025-01-15T14:32:10"
    )
    private LocalDateTime timestamp;
}
