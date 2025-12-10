package com.bank.entity;

import com.bank.enums.CardStatus;
import com.bank.util.CardNumberAttributeConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "cards")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CardEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = CardNumberAttributeConverter.class)
    @Column(name = "card_number_encrypted", nullable = false)
    private String cardNumberEncrypted; // Хранится в БД в зашифрованном виде. Шифрование обеспечит JPA AttributeConverter.

    @Column(name = "masked_number", nullable = false)
    private String maskedNumber; // Маскированный номер карты (**** **** **** 1234). Хранится открыто

    @Column(nullable = false)
    private String ownerName;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal balance;
}
