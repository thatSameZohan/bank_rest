package com.bank.service.impl;

import com.bank.dto.*;
import com.bank.entity.*;
import com.bank.repository.CardRepository;
import com.bank.entity.CardNumberAttributeConverter;
import com.bank.util.CardUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
@RequiredArgsConstructor
@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardNumberAttributeConverter converter;

    public CardResponseDto createCard(Long userId, CardCreateDto dto) {
        if (dto.expiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expiry date must be in the future");
        }

        String encrypted = converter.convertToDatabaseColumn(dto.cardNumber());
        if (cardRepository.existsByCardNumberEncrypted(encrypted)) {
            throw new IllegalArgumentException("Card already exists");
        }

        CardEntity card = CardEntity.builder()
            .cardNumberEncrypted(dto.cardNumber())
            .maskedNumber(CardUtils.maskNumber(dto.cardNumber()))
            .ownerName(dto.ownerName())
            .expiryDate(dto.expiryDate())
            .status(CardStatus.ACTIVE)
            .userId(userId)
            .balance(dto.initialBalance())
            .build();

        CardEntity saved = cardRepository.save(card);
        return mapToDto(saved);
    }

    public Page<CardResponseDto> getUserCards(Long userId, Pageable pageable, String q) {
        Page<CardEntity> page = cardRepository.findAllByUserId(userId, pageable);
        return page.map(this::mapToDto);
    }

    @Transactional
    public void blockCard(Long id) {
        CardEntity c = cardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Card not found"));
        c.setStatus(CardStatus.BLOCKED);
        cardRepository.save(c);
    }

    @Transactional
    public void activateCard(Long id) {
        CardEntity c = cardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Card not found"));
        c.setStatus(CardStatus.ACTIVE);
        cardRepository.save(c);
    }

    @Transactional
    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }

    @Transactional
    public void transferBetweenOwnCards(Long userId, Long fromCardId, Long toCardId, BigDecimal amount) {
        if (fromCardId.equals(toCardId)) throw new IllegalArgumentException("From and To card must differ");

        CardEntity from = cardRepository.findById(fromCardId).orElseThrow(() -> new IllegalArgumentException("From card not found"));
        CardEntity to = cardRepository.findById(toCardId).orElseThrow(() -> new IllegalArgumentException("To card not found"));

        if (!from.getUserId().equals(userId) || !to.getUserId().equals(userId)) {
            throw new SecurityException("Cards must belong to the same user");
        }
        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Both cards must be ACTIVE");
        }
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardRepository.save(from);
        cardRepository.save(to);
    }

    private CardResponseDto mapToDto(CardEntity e) {
        return new CardResponseDto(
            e.getId(),
            e.getMaskedNumber(),
            e.getOwnerName(),
            e.getExpiryDate(),
            e.getStatus().name(),
            e.getBalance()
        );
    }
}
