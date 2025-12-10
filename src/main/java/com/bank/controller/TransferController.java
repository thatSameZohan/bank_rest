package com.bank.controller;

import com.bank.dto.TransferRequestDto;
import com.bank.dto.TransferResponseDto;
import com.bank.entity.UserEntity;
import com.bank.service.impl.TransferService;
import com.bank.service.impl.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final UserService userService;

    @PostMapping
    public TransferResponseDto transfer(
            @RequestBody TransferRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserEntity user = userService.getByUsername(userDetails.getUsername());
        return transferService.transferBetweenOwnCards(user.getId(), dto);
    }
}
