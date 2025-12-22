package com.bank.service.impl;

import com.bank.dto.CardCreateDto;
import com.bank.dto.CardResponseDto;
import com.bank.entity.CardEntity;
import com.bank.enums.CardStatus;
import com.bank.exception.CommonException;
import com.bank.repository.CardRepository;
import com.bank.util.CardUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    // -----------------------------------
    // CREATE CARD
    // -----------------------------------

    @Test
    void createCard_success() {

        CardCreateDto dto = new CardCreateDto(
                "1234567890123456",
                "John Doe",
                5L,
                LocalDate.now().plusYears(1),
                new BigDecimal(500)
        );

        CardEntity saved = CardEntity.builder()
                .id(1L)
                .cardNumberEncrypted(dto.cardNumber())
                .maskedNumber(CardUtils.maskNumber(dto.cardNumber()))
                .ownerName(dto.ownerName())
                .expiryDate(dto.expiryDate())
                .status(CardStatus.ACTIVE)
                .userId(dto.userId())
                .balance(dto.initialBalance())
                .build();

        when(cardRepository.existsByCardNumberEncrypted(dto.cardNumber())).thenReturn(false);
        when(cardRepository.save(any(CardEntity.class))).thenReturn(saved);

        CardResponseDto result = cardService.createCard(dto);

        assertEquals(1L, result.id());
        assertEquals(dto.userId(), result.userId());
        assertEquals(CardUtils.maskNumber(dto.cardNumber()), result.maskedNumber());
        assertEquals("ACTIVE", result.status());
        assertEquals(new BigDecimal(500), result.balance());
    }

    @Test
    void createCard_expiredDate_throw() {

        CardCreateDto dto = new CardCreateDto(
                "1234567890123456",
                "John",
                5L,
                LocalDate.now().minusDays(1),
                new BigDecimal(100)
        );

        CommonException ex = assertThrows(CommonException.class,
                () -> cardService.createCard(dto));

        assertEquals(400, ex.getCode());
    }

    @Test
    void createCard_alreadyExists_throw() {

        CardCreateDto dto = new CardCreateDto(
                "1234567890123456",
                "John",
                5L,
                LocalDate.now().plusYears(1),
                new BigDecimal(100)
        );

        when(cardRepository.existsByCardNumberEncrypted(dto.cardNumber()))
                .thenReturn(true);

        CommonException ex = assertThrows(CommonException.class,
                () -> cardService.createCard(dto));

        assertEquals(409, ex.getCode());
    }

    // -----------------------------------
    // GET USER CARDS
    // -----------------------------------

    @Test
    void getUserCards_success() {

        Pageable pageable = PageRequest.of(0, 10);

        CardEntity e = CardEntity.builder()
                .id(1L)
                .userId(5L)
                .maskedNumber("**** 1234")
                .ownerName("John")
                .expiryDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal(100))
                .build();

        Page<CardEntity> page = new PageImpl<>(List.of(e));

        when(cardRepository.findAllByUserId(5L, pageable)).thenReturn(page);

        Page<CardResponseDto> result = cardService.getUserCards(5L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(new BigDecimal(100), result.getContent().getFirst().balance());
        assertEquals("ACTIVE", result.getContent().getFirst().status());
    }

    // -----------------------------------
    // GET CARD FOR USER
    // -----------------------------------

    @Test
    void getCardForUser_success() {

        CardEntity e = CardEntity.builder()
                .id(10L)
                .userId(5L)
                .maskedNumber("**** 1111")
                .ownerName("John")
                .expiryDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal(200))
                .build();

        when(cardRepository.findByIdAndUserId(10L, 5L)).thenReturn(Optional.of(e));

        CardResponseDto dto = cardService.getCardForUser(10L, 5L);

        assertEquals(10L, dto.id());
        assertEquals(new BigDecimal(200), dto.balance());
    }

    @Test
    void getCardForUser_wrongUser_throw() {

        when(cardRepository.findByIdAndUserId(10L, 5L))
                .thenReturn(Optional.empty());

        CommonException ex = assertThrows(CommonException.class,
                () -> cardService.getCardForUser(10L, 5L));

        assertEquals(400, ex.getCode());
    }

    // -----------------------------------
    // BLOCK CARD
    // -----------------------------------

    @Test
    void blockCard_success() {

        CardEntity e = new CardEntity();
        e.setId(1L);
        e.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(e));

        cardService.blockCard(1L);

        assertEquals(CardStatus.BLOCKED, e.getStatus());
        verify(cardRepository).save(e);
    }

    @Test
    void blockCard_notFound_throw() {

        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        CommonException ex = assertThrows(CommonException.class,
                () -> cardService.blockCard(1L));

        assertEquals(404, ex.getCode());
    }

    // -----------------------------------
    // ACTIVATE CARD
    // -----------------------------------

    @Test
    void activateCard_success() {

        CardEntity e = new CardEntity();
        e.setId(1L);
        e.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(e));

        cardService.activateCard(1L);

        assertEquals(CardStatus.ACTIVE, e.getStatus());
        verify(cardRepository).save(e);
    }

    @Test
    void activateCard_notFound_throw() {

        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        CommonException ex = assertThrows(CommonException.class,
                () -> cardService.activateCard(1L));

        assertEquals(404, ex.getCode());
    }

    // -----------------------------------
    // DELETE CARD
    // -----------------------------------

    @Test
    void deleteCard_success() {

        when(cardRepository.existsById(10L)).thenReturn(true);

        cardService.deleteCard(10L);

        verify(cardRepository).deleteById(10L);
    }

    @Test
    void deleteCard_notFound_throw() {

        when(cardRepository.existsById(10L)).thenReturn(false);

        CommonException ex = assertThrows(CommonException.class,
                () -> cardService.deleteCard(10L));

        assertEquals(404, ex.getCode());
    }

    // -----------------------------------
    // GET ALL CARDS
    // -----------------------------------

    @Test
    void getAllCards_success() {

        Pageable pageable = PageRequest.of(0, 5);

        CardEntity e = CardEntity.builder()
                .id(1L)
                .userId(1L)
                .maskedNumber("**** 2222")
                .ownerName("John")
                .expiryDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal(300))
                .build();

        Page<CardEntity> page = new PageImpl<>(List.of(e));
        when(cardRepository.findAll(pageable)).thenReturn(page);

        Page<CardResponseDto> result = cardService.getAllCards(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(new BigDecimal(300), result.getContent().getFirst().balance());
        assertEquals("ACTIVE", result.getContent().getFirst().status());
    }
}
