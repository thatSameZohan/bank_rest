package com.bank.service.impl;

import com.bank.dto.*;
import com.bank.entity.*;
import com.bank.enums.CardStatus;
import com.bank.repository.CardRepository;
import com.bank.util.CardUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class CardService {

    private final CardRepository cardRepository;

    @Transactional
    public CardResponseDto createCard(CardCreateDto dto) {

        if (dto.expiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expiry date must be in the future");
        }

        if (cardRepository.existsByCardNumberEncrypted(dto.cardNumber())) {
            throw new IllegalArgumentException("Card already exists");
        }

        CardEntity card = CardEntity.builder()
            .cardNumberEncrypted(dto.cardNumber())
            .maskedNumber(CardUtils.maskNumber(dto.cardNumber()))
            .ownerName(dto.ownerName())
            .expiryDate(dto.expiryDate())
            .status(CardStatus.ACTIVE)
            .userId(dto.userId())
            .balance(dto.initialBalance())
            .build();

        CardEntity saved = cardRepository.save(card);
        return mapToDto(saved);
    }

    public Page<CardResponseDto> getUserCards(Long userId, Pageable pageable) {
        Page<CardEntity> page = cardRepository.findAllByUserId(userId, pageable);
        return page.map(this::mapToDto);
    }

    public CardResponseDto getCardForUser(Long cardId, Long userId) {

        CardEntity card=cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new RuntimeException("Card does not belong to user"));

        return mapToDto(card);
    }

    @Transactional
    public void blockCard(Long id) {
        CardEntity card = cardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Card not found"));
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Transactional
    public void activateCard(Long id) {
        CardEntity card = cardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Card not found"));
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    @Transactional
    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new RuntimeException("Card not found");
        }
        cardRepository.deleteById(id);
    }

    public CardEntity getById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
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
