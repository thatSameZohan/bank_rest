package com.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponseDto(

    Long id,

    Long userId,

    String maskedNumber,

    String ownerName,

    LocalDate expiryDate,

    String status,

    BigDecimal balance
) {}
