package com.bank.service.impl;

import com.bank.dto.TransferRequestDto;
import com.bank.entity.CardEntity;
import com.bank.entity.TransferEntity;
import com.bank.entity.UserEntity;
import com.bank.enums.CardStatus;
import com.bank.exception.CommonException;
import com.bank.repository.CardRepository;
import com.bank.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferServiceImplTest {

    @InjectMocks
    private TransferServiceImpl transferService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private TransferRepository transferRepository;

    private UserEntity user;
    private CardEntity fromCard;
    private CardEntity toCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new UserEntity();
        user.setId(1L);

        fromCard = new CardEntity();
        fromCard.setId(10L);
        fromCard.setUserId(1L);
        fromCard.setBalance(new BigDecimal("1000"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setMaskedNumber("**** **** **** 1111");

        toCard = new CardEntity();
        toCard.setId(20L);
        toCard.setUserId(1L);
        toCard.setBalance(new BigDecimal("500"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setMaskedNumber("**** **** **** 2222");
    }

    @Test
    void testSuccessfulTransfer() {
        TransferRequestDto dto = new TransferRequestDto(fromCard.getId(), toCard.getId(), new BigDecimal("200"));

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));
        when(userService.getById(user.getId())).thenReturn(user);
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transferRepository.save(any(TransferEntity.class))).thenAnswer(i -> i.getArgument(0));

        transferService.transferBetweenOwnCards(user.getId(), dto);

        assertEquals(new BigDecimal("800"), fromCard.getBalance());
        assertEquals(new BigDecimal("700"), toCard.getBalance());
        verify(cardRepository, times(2)).save(any());
        verify(transferRepository, times(1)).save(any());
    }

    @Test
    void testTransferInsufficientFunds() {
        fromCard.setBalance(new BigDecimal("100"));
        TransferRequestDto dto = new TransferRequestDto(fromCard.getId(), toCard.getId(), new BigDecimal("200"));

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        CommonException ex = assertThrows(CommonException.class, () ->
                transferService.transferBetweenOwnCards(user.getId(), dto)
        );

        assertEquals("Insufficient funds", ex.getMessage());
    }

    @Test
    void testTransferBlockedCard() {
        fromCard.setStatus(CardStatus.BLOCKED);
        TransferRequestDto dto = new TransferRequestDto(fromCard.getId(), toCard.getId(), new BigDecimal("50"));

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        CommonException ex = assertThrows(CommonException.class, () ->
                transferService.transferBetweenOwnCards(user.getId(), dto)
        );

        assertEquals("Both cards must be ACTIVE", ex.getMessage());
    }

    @Test
    void testTransferDifferentUser() {
        toCard.setUserId(2L); // другая карта
        TransferRequestDto dto = new TransferRequestDto(fromCard.getId(), toCard.getId(), new BigDecimal("50"));

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        CommonException ex = assertThrows(CommonException.class, () ->
                transferService.transferBetweenOwnCards(user.getId(), dto)
        );

        assertEquals("Cards must belong to the same user", ex.getMessage());
    }

    @Test
    void testTransferSameCard() {
        TransferRequestDto dto = new TransferRequestDto(fromCard.getId(), fromCard.getId(), new BigDecimal("50"));

        CommonException ex = assertThrows(CommonException.class, () ->
                transferService.transferBetweenOwnCards(user.getId(), dto)
        );

        assertEquals("From and To card must differ", ex.getMessage());
    }

    @Test
    void testGetAllTransfers() {
        TransferEntity transferEntity = TransferEntity.builder()
                .id(1L)
                .fromCard(fromCard)
                .toCard(toCard)
                .user(user)
                .amount(new BigDecimal("100"))
                .createdAt(LocalDateTime.now())
                .build();

        Page<TransferEntity> page = new PageImpl<>(List.of(transferEntity));
        when(transferRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<?> result = transferService.getAll(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }
}
