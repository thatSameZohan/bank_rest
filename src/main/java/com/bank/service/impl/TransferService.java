package com.bank.service.impl;

import com.bank.dto.TransferRequestDto;
import com.bank.dto.TransferResponseDto;
import com.bank.entity.CardEntity;
import com.bank.enums.CardStatus;
import com.bank.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final CardRepository cardRepository;

    @Transactional
    public TransferResponseDto transferBetweenOwnCards(Long userId, TransferRequestDto dto) {

        if (dto.fromCardId().equals(dto.toCardId())) {
            throw new IllegalArgumentException("From and To card must differ");
        }

        CardEntity from = cardRepository.findById(dto.fromCardId()).orElseThrow(() -> new IllegalArgumentException("From card not found"));
        CardEntity to = cardRepository.findById(dto.toCardId()).orElseThrow(() -> new IllegalArgumentException("To card not found"));

        if (!from.getUserId().equals(userId) || !to.getUserId().equals(userId)) {
            throw new SecurityException("Cards must belong to the same user");
        }

        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Both cards must be ACTIVE");
        }

        if (from.getBalance().compareTo(dto.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(dto.amount()));
        to.setBalance(to.getBalance().add(dto.amount()));

        cardRepository.save(from);
        cardRepository.save(to);

        return buildResponse(from, to, dto.amount());
    }

    private TransferResponseDto buildResponse(
            CardEntity from, CardEntity to, BigDecimal amount
    ) {
        return TransferResponseDto.builder()
                .fromCardMasked(from.getMaskedNumber())
                .toCardMasked(to.getMaskedNumber())
                .amount(amount)
                .fromCardBalanceAfter(from.getBalance())
                .toCardBalanceAfter(to.getBalance())
                .timestamp(LocalDateTime.now())
                .status("SUCCESS")
                .build();
    }
}