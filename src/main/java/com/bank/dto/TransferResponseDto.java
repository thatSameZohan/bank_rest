package com.bank.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponseDto {

    private String fromCardMasked;

    private String toCardMasked;

    private BigDecimal amount;

    private BigDecimal fromCardBalanceAfter;

    private BigDecimal toCardBalanceAfter;

    private LocalDateTime timestamp;

    private String status;  // SUCCESS / FAILED
}
