package com.bank.service.impl;

import com.bank.dto.TransferDto;
import com.bank.dto.TransferRequestDto;
import com.bank.dto.TransferResponseDto;
import com.bank.entity.CardEntity;
import com.bank.entity.TransferEntity;
import com.bank.entity.UserEntity;
import com.bank.enums.CardStatus;
import com.bank.exception.CommonException;
import com.bank.repository.CardRepository;
import com.bank.repository.TransferRepository;
import com.bank.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final CardRepository cardRepository;
    private final UserServiceImpl userService;
    private final TransferRepository transferRepository;

    @Override
    @Transactional
    public TransferResponseDto transferBetweenOwnCards(Long userId, TransferRequestDto dto) {

        if (dto.fromCardId().equals(dto.toCardId())) {
            throw new CommonException (400, "From and To card must differ");
        }

        CardEntity from = cardRepository.findById(dto.fromCardId()).orElseThrow(() -> new CommonException (404, "From card not found"));
        CardEntity to = cardRepository.findById(dto.toCardId()).orElseThrow(() -> new CommonException (404, "To card not found"));

        if (!from.getUserId().equals(userId) || !to.getUserId().equals(userId)) {
            throw new CommonException (400, "Cards must belong to the same user");
        }

        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
            throw new CommonException (400, "Both cards must be ACTIVE");
        }

        if (from.getBalance().compareTo(dto.amount()) < 0) {
            throw new CommonException(400, "Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(dto.amount()));
        to.setBalance(to.getBalance().add(dto.amount()));

        UserEntity user= userService.getById(userId);

        TransferEntity  transfer = TransferEntity.builder()
                .user(user)
                .fromCard(from)
                .toCard(to)
                .amount(dto.amount())
                .createdAt(LocalDateTime.now())
                .build();

        cardRepository.save(from);
        cardRepository.save(to);
        TransferEntity transferEntity=transferRepository.save(transfer);

        return buildResponseDto(transferEntity);
    }

    @Override
    @NullMarked
    public Page<TransferDto> getAll(Pageable pageable) {

        Page<TransferEntity> page = transferRepository.findAll(pageable);

        return page.map(this::mapToDto);
    }

    private TransferDto mapToDto(TransferEntity e) {
        return TransferDto.builder()
                .id(e.getId())
                .userId(e.getUser().getId())
                .fromCardId(e.getFromCard().getId())
                .toCardId(e.getToCard().getId())
                .amount(e.getAmount())
                .createdAt(e.getCreatedAt())
                .build();
    }
    private TransferResponseDto buildResponseDto(TransferEntity e) {
        return TransferResponseDto.builder()
                .fromCardMasked(e.getFromCard().getMaskedNumber())
                .toCardMasked( e.getToCard().getMaskedNumber())
                .amount(e.getAmount())
                .fromCardBalanceAfter(e.getFromCard().getBalance())
                .toCardBalanceAfter(e.getToCard().getBalance())
                .timestamp(e.getCreatedAt())
                .build();
    }
}