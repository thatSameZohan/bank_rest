package com.bank.service.impl;

import com.bank.dto.*;
import com.bank.entity.*;
import com.bank.enums.CardStatus;
import com.bank.exception.CommonException;
import com.bank.repository.CardRepository;
import com.bank.service.CardService;
import com.bank.util.CardUtils;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    @Override
    @Transactional
    public CardResponseDto createCard(CardCreateDto dto) {

        if (dto.expiryDate().isBefore(LocalDate.now())) {
            throw new CommonException (400, "Expiry date must be in the future");
        }

        if (cardRepository.existsByCardNumberEncrypted(dto.cardNumber())) {
            throw new CommonException (409, "Card already exists");
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

    @Override
    @NullMarked
    public Page<CardResponseDto> getUserCards(Long userId, Pageable pageable) {
        Page<CardEntity> page = cardRepository.findAllByUserId(userId, pageable);
        return page.map(this::mapToDto);
    }

    @Override
    public CardResponseDto getCardForUser(Long cardId, Long userId) {

        CardEntity card=cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new CommonException(400, "Card does not belong to user"));

        return mapToDto(card);
    }

    @Override
    @Transactional
    public void blockCard(Long id) {
        CardEntity card = cardRepository.findById(id).orElseThrow(() -> new CommonException (404, "Card not found"));
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void activateCard(Long id) {
        CardEntity card = cardRepository.findById(id).orElseThrow(() -> new CommonException (404, "Card not found"));
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new CommonException(404, "Card not found");
        }
        cardRepository.deleteById(id);
    }

    @Override
    @NullMarked
    public Page<CardResponseDto> getAllCards(Pageable pageable) {
        Page<CardEntity> page = cardRepository.findAll(pageable);
        return page.map(this::mapToDto);
    }

    private CardResponseDto mapToDto(CardEntity e) {
        return new CardResponseDto(
            e.getId(),
            e.getUserId(),
            e.getMaskedNumber(),
            e.getOwnerName(),
            e.getExpiryDate(),
            e.getStatus().name(),
            e.getBalance()
        );
    }
}
