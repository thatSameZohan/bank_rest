package com.bank.service;

import com.bank.dto.TransferDto;
import com.bank.dto.TransferRequestDto;
import com.bank.dto.TransferResponseDto;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransferService {

    TransferResponseDto transferBetweenOwnCards(Long userId, TransferRequestDto dto);

    @NullMarked
    Page<TransferDto> getAll(Pageable pageable);
}
