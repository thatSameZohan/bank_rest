package com.bank.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.math.BigDecimal;

public record CardCreateDto(
    @NotBlank String cardNumber,
    @NotBlank String ownerName,
    @NotNull LocalDate expiryDate,
    @NotNull @DecimalMin("0.0") BigDecimal initialBalance
) {}
