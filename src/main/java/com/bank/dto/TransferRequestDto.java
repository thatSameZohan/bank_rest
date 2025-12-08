package com.bank.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TransferRequestDto(
    @NotNull Long fromCardId,
    @NotNull Long toCardId,
    @NotNull @DecimalMin("0.01") BigDecimal amount
) {}
