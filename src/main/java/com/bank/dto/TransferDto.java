package com.bank.dto;

import com.bank.entity.CardEntity;
import com.bank.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class TransferDto {

    private Long id;

    private UserEntity user;

    private CardEntity fromCard;

    private CardEntity toCard;

    private BigDecimal amount;

    private LocalDateTime createdAt;
}
