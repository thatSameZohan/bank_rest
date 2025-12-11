package com.bank.service;

import com.bank.dto.CardCreateDto;
import com.bank.dto.CardResponseDto;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {

    CardResponseDto createCard(CardCreateDto dto);

    @NullMarked
    Page<CardResponseDto> getUserCards(Long userId, Pageable pageable);

    CardResponseDto getCardForUser(Long cardId, Long userId);

    void blockCard(Long id);

    void activateCard(Long id);

    void deleteCard(Long id);

    @NullMarked
    Page<CardResponseDto> getAllCards(Pageable pageable);

}
