package com.bank.controller;

import com.bank.dto.*;
import com.bank.service.impl.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/cards")
public class CardController {

    private final CardService cardService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> createCard(@RequestBody @Valid CardCreateDto dto, Authentication auth) {
        Long adminUserId = getUserIdFromAuth(auth);
        var res = cardService.createCard(adminUserId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Page<CardResponseDto>> listUserCards(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Authentication auth
    ) {
        Long userId = getUserIdFromAuth(auth);
        Page<CardResponseDto> pageRes = cardService.getUserCards(userId, PageRequest.of(page, size), null);
        return ResponseEntity.ok(pageRes);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> transfer(@RequestBody @Valid TransferRequestDto dto, Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        cardService.transferBetweenOwnCards(userId, dto.fromCardId(), dto.toCardId(), dto.amount());
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> block(@PathVariable Long id) {
        cardService.blockCard(id);
        return ResponseEntity.ok("blocked");
    }

    private Long getUserIdFromAuth(Authentication auth) {
        // В JwtAuthenticationFilter мы устанавливаем principal = userId (String), поэтому auth.getName() возвращает id
        return Long.parseLong(auth.getName());
    }
}
